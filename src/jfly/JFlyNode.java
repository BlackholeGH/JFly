/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfly;

import java.util.concurrent.*;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.io.*;
import java.net.ServerSocket;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;
import java.net.InetSocketAddress;
import java.util.Hashtable;
import java.util.regex.Pattern;
/**
 *
 * @author Blackhole
 * TO DO:
 * Let socket connections time out and dispose the thread if so as unsuccessful
 */
public class JFlyNode {
    public static long time()
    {
        Date date = new Date();
        return date.getTime();
    }
    private String recordedHostAddress = "";
    public void resetAddress(String addr, BlockchainNodeManager myManager)
    {
        if(myManager == blockManager) { recordedHostAddress = addr; }
    }
    public void setDefaultAddress(String addr)
    {
        if(recordedHostAddress.isEmpty())
        {
            recordedHostAddress = addr;
        }
    }
    public String hostAddr()
    {
        try
        {
            if(recordedHostAddress.isEmpty()) { return java.net.InetAddress.getLocalHost().getHostAddress(); }
            else { return recordedHostAddress; }
        }
        catch(Exception e) { return "Retrieval failure"; }
    }
    public String getUserID()
    {
        return myNCS.getIDFromIP(hostAddr());
    }
    private Boolean pShutdown = false;
    public Boolean shuttingDown()
    {
        return pShutdown;
    }
    public void shutdownNode()
    {
        shutdownNode(false);
    }
    public void shutdownNode(Boolean relaunch)
    {
        Runnable finalShutdownThread = () -> 
        {
            Thread.currentThread().setName("Shutdown cleanup thread");
            if(pingerThread != null) { pingerThread.interrupt(); }
            if(coordinatorThread != null) { coordinatorThread.interrupt(); }
            leaveCluster();
            pShutdown = true;
            if(receivePool != null)
            {
                receivePool.shutdownNow();
            }
            if(myGUI != null)
            {
                myGUI.closeMainframe();
                myGUI.dispose();
            }
            if(launcher != null) { launcher.dispose(); }
            if(!relaunch) { System.exit(0); }
            else
            {
                ConnectionThreadDirectory = new ArrayList();
                blockManager = new BlockchainNodeManager(this);
                launcher = new FlyLauncher(this);
            }
        };
        new Thread(finalShutdownThread).start();
    }
    public void pingThreads()
    {
        ArrayList ctdCopy = null;
        threadListLock.lock();
        try
        {
            if(!ConnectionThreadDirectory.isEmpty()) 
            {
                ctdCopy = (ArrayList)ConnectionThreadDirectory.clone();
            }
        }
        finally { threadListLock.unlock(); }
        if(ctdCopy != null)
        {
            for(Object o : ctdCopy)
            {
                OneLinkThread olt = (OneLinkThread)o;
                olt.queryReplies();
            }
        }
    }
    private void startPinger()
    {
        pingerThread = new Thread(new pingerRunnable(this));
        pingerThread.start();
    }
    Thread pingerThread = null;
    private class pingerRunnable implements Runnable
    {
        JFlyNode myNode = null;
        public pingerRunnable(JFlyNode node)
        {
            myNode = node;
        }
        @Override
        public void run()
        {
            Thread.currentThread().setName("Connection autoping thread");
            while(!myNode.shuttingDown())
            {
                try
                {
                    myNode.pingThreads();
                    crossNetworkSeekNode("");
                    Thread.sleep(5000);
                }
                catch(InterruptedException e) { }
            } 
        }
    }
    Thread coordinatorThread = null;
    private void startCoordinator()
    {
        coordinatorThread = new Thread(new coordinatorRunnable(this));
        coordinatorThread.start();
    }
    private class coordinatorRunnable implements Runnable
    {
        JFlyNode myNode = null;
        public coordinatorRunnable(JFlyNode node)
        {
            myNode = node;
        }
        @Override
        public void run()
        {
            Thread.currentThread().setName("Coordination layer thread");
            while(!myNode.shuttingDown())
            {
                try
                {
                    ArrayList<String> usrIDs = myNode.getNCS().getUserIDs();
                    if(usrIDs.size() > 0)
                    {
                        usrIDs.sort(null);
                        if(myNode.getUserID().equals(usrIDs.get(0)))
                        {
                            for(int i = 1; i < usrIDs.size(); i++)
                            {
                                myNode.crossNetworkSeekNode("USERHASHID|" + usrIDs.get(i));
                            }
                        }
                        Thread.sleep(10000);
                    }
                }
                catch(InterruptedException e) { }
            }
        }
    }
    public void closeAll()
    {
        try
        {
            threadListLock.lock();
            for(int i = 0; i < ConnectionThreadDirectory.size(); i++)
            {    
                if(ConnectionThreadDirectory.size() == 0) { break; }
                OneLinkThread olt = (OneLinkThread)ConnectionThreadDirectory.get(0);
                olt.stop(true);
            }
        }
        catch(IOException e) { System.out.println(e.getMessage()); }
        finally { threadListLock.unlock(); }
    }
    public static final int defaultPort = 44665;
    private String myID = "";
    private BlockchainNodeManager blockManager;
    private ArrayList ConnectionThreadDirectory;
    private ReentrantLock threadListLock = new ReentrantLock();
    private NetworkConfigurationState myNCS = new NetworkConfigurationState();
    private ArrayList<String> transientMessages = new ArrayList<String>();
    public synchronized Boolean allowTransientForwarding(String transientPackage)
    {
        String tpHash = BlockchainNodeManager.SharedStateBlock.getHash(transientPackage);
        String foundStr = null;
        ArrayList<String> expiredTransients = new ArrayList();
        for(String s : transientMessages)
        {
            String[] strs = s.split(Pattern.quote(":+:"), -1);
            long timeSent = Long.decode(strs[0]);
            if(time() - timeSent > 30000) { expiredTransients.add(s); }
            if(strs[1].equals(tpHash))
            {
                foundStr = s;
            }
        }
        for(String eT : expiredTransients)
        {
            if(eT == foundStr) { continue; }
            transientMessages.remove(foundStr);
        }
        if(foundStr != null)
        {
            transientMessages.remove(foundStr);
            return false;
        }
        else
        {
            transientMessages.add(time() + ":~:" + tpHash);
            return true;
        }
    }
    public NetworkConfigurationState getNCS()
    {
        return myNCS;
    }
    public BlockchainNodeManager getBNM()
    {
        return blockManager;
    }
    public OneLinkThread[] getThreadsForUser(String hashIdentifier)
    {
        String tryGetIP = myNCS.getIPFromID(hashIdentifier);
        if(tryGetIP.equals("UNKNOWN_USER")) { return null; }
        ArrayList<OneLinkThread> foundThreads = new ArrayList();
        threadListLock.lock();
        try
        {
            for(Object thread : ConnectionThreadDirectory)
            {
                OneLinkThread trueThread = (OneLinkThread)thread;
                String remoteIP = trueThread.getConnectionAddr().split(":")[0];
                if(remoteIP.equals(tryGetIP)) { foundThreads.add(trueThread); }
            }
        }
        finally { threadListLock.unlock(); }
        if(foundThreads.isEmpty()) { return null; }
        else
        {
            return foundThreads.toArray(new OneLinkThread[0]);
        }
    }
    public void attemptContact(String hashIdentifier)
    {
        OneLinkThread[] openConnects = getThreadsForUser(hashIdentifier);
        if(openConnects != null)
        {
            OutputJobInfo missedJob = new OutputJobInfo(OutputJobInfo.JobType.SINGLE_DISPATCH, "Query-ack", "QUERYACK");
            for(OneLinkThread olt : openConnects)
            {
                olt.oneDispatch(missedJob);
            }
        }
        else
        {
            String iP = myNCS.getIPFromID(hashIdentifier);
            if(!iP.equals("UNKNOWN_USER"))
            {
                ClientStyleThread questThread = new ClientStyleThread(new Object[] { iP, defaultPort }, this, true);
                new Thread(questThread).start();
            }
        }
    }
    private String questNode = "";
    public Boolean queryAcceptQuester(String questerIP)
    {
        Boolean accept = false;
        if(!questNode.isEmpty())
        {
            try
            {
                long pastAcceptTime = Long.decode(questNode.split(Pattern.quote(":"), -1)[0]);
                if(JFlyNode.time() - pastAcceptTime > 30000) { accept = true; }
            }
            finally {};
        }
        else
        {
            accept = true;
        }
        if(accept)
        {
            questNode = JFlyNode.time() + ":" + questerIP;
            return true;
        }
        else { return false; }
    }
    public void leaveCluster()
    {
        blockManager.authorBlock(BlockchainNodeManager.SharedStateBlock.ContentType.USER_LEFT, myNCS.getUserDataFromID(getUserID()));
        closeAll();
    }
    public String[] getLastMessages(int num)
    {
        String[] out = blockManager.getLast(num);
        for(int i = 0; i < out.length; i++)
        {
            out[i] = TextUtility.desanitizeText(out[i]);
        }
        return out;
    }
    public void sendMessage(String message)
    {
        message = TextUtility.sanitizeText(message);
        blockManager.authorBlock(BlockchainNodeManager.SharedStateBlock.ContentType.MESSAGE, message);
    }
    public synchronized String pullOneBlockByHash(String hash)
    {
        String pulledBlock = blockManager.getByHash(hash);
        if(pulledBlock == null) { return "BLOCK_HASH_NOT_FOUND"; }
        else{ return pulledBlock; }
    }
    public synchronized String tryOneBlock(String data)
    {
        Object[] trueAddReturn = blockManager.addExtantBlockToChain(data);
        int attemptAdd = (int)trueAddReturn[0];
        if(attemptAdd == 2)
        {
            return "FAILED_REQUEST_PREVIOUS";
        }
        else if(attemptAdd == 0 || attemptAdd == 1)
        {

            return "SUCCESSFULLY_INTEGRATED:" + (String)trueAddReturn[1];
        }
        else if(attemptAdd == 4) { return "BLOCK_ALREADY_ADDED"; }
        else { return "FAILED_OTHER_UNSPECIFIED"; }
    }
    public void sendJobToThreads(OutputJobInfo job, OneLinkThread[] blacklist)
    {
        threadListLock.lock();
        try
        {
            for(Object thread : ConnectionThreadDirectory)
            {
                OneLinkThread trueThread = (OneLinkThread)thread;
                if(blacklist != null)
                {
                    Boolean skip = false;
                    for(OneLinkThread blackThread : blacklist)
                    {
                        if(blackThread == trueThread)
                        { 
                            skip = true;
                            break;
                        }
                    }
                    if(skip) { continue; }
                }
                trueThread.oneDispatch(job);
            }
        }
        finally { threadListLock.unlock(); }
    }
    public void registerThread(OneLinkThread thread)
    {
        threadListLock.lock();
        try
        {
            if(!ConnectionThreadDirectory.contains(thread)) { ConnectionThreadDirectory.add(thread); }
        }
        finally { threadListLock.unlock(); }
    }
    public void unregisterThread(OneLinkThread thread, Boolean skipBlocking)
    {
        if(!skipBlocking) { threadListLock.lock(); }
        try
        {
            if(ConnectionThreadDirectory.contains(thread)) { ConnectionThreadDirectory.remove(thread); }
        }
        finally { if(!skipBlocking) { threadListLock.unlock(); } }
    }
    String usr = null;
    public String getLocalUsername()
    {
        return usr;
    }
    public void setLocalUsername(String username)
    {
        usr = username;
    }
    public static void main(String[] args)
    {
        new JFlyNode();
    }
    private FlyLauncher launcher = null;
    public JFlyNode()
    {
        ConnectionThreadDirectory = new ArrayList();
        blockManager = new BlockchainNodeManager(this);
        launcher = new FlyLauncher(this);
        /*try
        {
            openReceiveAndWait(-1);
        }
        catch(Exception e) { }*/
    }
    public void wipeLauncher(FlyLauncher fl)
    {
        if(fl == launcher)
        {
            launcher = null;
        }
    }
    //Insecure without verification hashing with 2-key encryption. Idea for later?
    public void issueExistenceTransient()
    {
        String transientBody = "responseping+-+" + getUserID();
        OutputJobInfo existTransient = new OutputJobInfo(OutputJobInfo.JobType.MULTIPLE_DISPATCH, transientBody, "JFLYTRANSIENT");
        sendJobToThreads(existTransient, null);
    }
    private Hashtable seekers = new Hashtable();
    private final int seekTolerance = 20000;
    private final int contactTolerance = 20000;
    public synchronized void crossNetworkSeekNode(String userIDorResponseTransient)
    {
        if(userIDorResponseTransient.startsWith("JFLYTRANSIENT"))
        {
            String findUsrID = (userIDorResponseTransient.split(":~:", -1)[1]).split("+-+", -1)[1];
            if(seekers.containsKey(findUsrID))
            {
                seekers.put(findUsrID, "RESPONSE_RECEIVED|RESPONSE_RECEIVED");
            }
        }
        else if(userIDorResponseTransient.startsWith("USERHASHID"))
        {
            String userHashID = userIDorResponseTransient.replaceAll("USERHASHID|", "");
            if(!seekers.containsKey(usr) || (seekers.containsKey(usr) && seekers.get(usr).equals("RESPONSE_RECEIVED|RESPONSE_RECEIVED")))
            {
                seekers.put("SEEK|" + usr, time());
                String transientBody = "seeking+-+" + getUserID();
                OutputJobInfo seekingTransient = new OutputJobInfo(OutputJobInfo.JobType.MULTIPLE_DISPATCH, transientBody, "JFLYTRANSIENT");
                sendJobToThreads(seekingTransient, null);
            }
        }
        else
        {
            for(Object hashIDo : seekers.keySet())
            {
                String hashID = ((String)hashIDo).split(Pattern.quote("|"))[1];
                String mode = ((String)hashIDo).split(Pattern.quote("|"))[0];
                long seekTime = (long)seekers.get(hashID);
                if(mode.equals("SEEK") && time() - seekTime > seekTolerance)
                {
                    seekers.put("CONTACT|" + usr, time());
                    String transientBody = "forcecontact+-+" + getUserID();
                    OutputJobInfo seekingTransient = new OutputJobInfo(OutputJobInfo.JobType.MULTIPLE_DISPATCH, transientBody, "JFLYTRANSIENT");
                    sendJobToThreads(seekingTransient, null);
                }
                else if(time() - seekTime > contactTolerance)
                {
                    seekers.remove(hashID);
                    issueTimeout(hashID);
                }
            }
        }
    }
    private void issueTimeout(String hashID)
    {
        for(OneLinkThread deadThread : getThreadsForUser(hashID))
        {
            try
            {
                deadThread.stop(false);
            }
            catch(IOException e) {}
        }
        blockManager.authorBlock(BlockchainNodeManager.SharedStateBlock.ContentType.USER_LEFT, myNCS.getUserDataFromID(hashID) + "+-+presumed_disconnect_timeout");
        for(String iD : myNCS.getUserIDs())
        {
            if(getThreadsForUser(iD) == null) { crossNetworkSeekNode("USERHASHID|" + iD); }
        }
    }
    private ExecutorService receivePool = null;
    private int myListenPort = 44665;
    public int getManualListenPort()
    {
        return myListenPort;
    }
    public void setManualListenPort(int port)
    {
        if(port > 65535 || port < 0) { myListenPort = defaultPort; }
        else { myListenPort = port; }
    }
    public void openReceiveAndWait() throws IOException
    {
        startPinger();
        startCoordinator();
        myGUI = new FlyChatGUI(this);
        if(myListenPort > 65535 || myListenPort < 0) { myListenPort = defaultPort; }
        blockManager.authorBlock(BlockchainNodeManager.SharedStateBlock.ContentType.GENESIS, "");
        usr = JOptionPane.showInputDialog(null, "Choose a username!", "Input username", JOptionPane.INFORMATION_MESSAGE);
        usr = TextUtility.sanitizeText(usr);
        if(usr == null || usr.isEmpty())
        {
            usr = "IP User " + hostAddr();
        }
        NetworkConfigurationState.UserInfo me = new NetworkConfigurationState.UserInfo(hostAddr(), "", usr);
        blockManager.authorBlock(BlockchainNodeManager.SharedStateBlock.ContentType.USER_JOINED, me.toString());
        receivePool = Executors.newFixedThreadPool(500);
        try (ServerSocket listener = new ServerSocket(myListenPort)) {
            while (!shuttingDown()) {
                receivePool.execute(new ServerStyleThread(listener.accept(), this));
            }
        }
    }
    private FlyChatGUI myGUI = null;
    public FlyChatGUI getGUI()
    {
        return myGUI;
    }
    public void sendConnectAndOpen(String iP, int rPort) throws IOException
    {
        startPinger();
        startCoordinator();
        myGUI = new FlyChatGUI(this);
        blockManager.addRegistrarTolerance(1);
        ClientStyleThread connectThread = new ClientStyleThread(new Object[] { iP, rPort }, this, false);
        connectThread.setDemandIntroduction();
        new Thread(connectThread).start();
        receivePool = Executors.newFixedThreadPool(500);
        try (ServerSocket listener = new ServerSocket(defaultPort)) {
            while (!shuttingDown()) {
                receivePool.execute(new ServerStyleThread(listener.accept(), this));
            }
        }
    }
    public static class OutputJobInfo
    {
        enum JobType { SINGLE_DISPATCH, MULTIPLE_DISPATCH, INTERNAL_LOCK };
        private JobType type;
        private String data;
        private String header;
        private Object token;
        public OutputJobInfo(JobType myType, String myData, String myHeader)
        {
            type = myType;
            data = myData;
            header = myHeader;
        }
        public void setToken(Object myToken)
        {
            token = myToken;
        }
        public Object getToken()
        {
            return token;
        }
        public JobType getType()
        {
            return type;
        }
        public String getData()
        {
            return data;
        }
        public String getHeader()
        {
            return header;
        }
    }
    public static class ServerStyleThread extends OneLinkThread
    {
        public ServerStyleThread(Socket myAcceptedConnection, JFlyNode myNode)
        {
            super(myNode);
            try
            {
                mySocket = myAcceptedConnection;
                inLine = new Scanner(mySocket.getInputStream());
                outLine = new PrintWriter(mySocket.getOutputStream(), true);
                OutputJobInfo oneAck = new OutputJobInfo(OutputJobInfo.JobType.SINGLE_DISPATCH, "Response_ack_to:" + mySocket.getInetAddress().getHostAddress(), "JFLYMSGACK");
                oneDispatch(oneAck);
                myNode.blockManager.authorBlock(BlockchainNodeManager.SharedStateBlock.ContentType.GROUP_REGISTRAR, myNode.getNCS().getRegistrar());
            }
            catch(IOException e)
            {
                try
                {
                    stop(false);
                }
                catch(IOException e2) {}
            }
        }
    }
    public static class ClientStyleThread extends OneLinkThread
    {
        protected Boolean threadQuesting = false;
        public ClientStyleThread(Object[] params, JFlyNode myNode, Boolean questing)
        {
            super(myNode);
            if(threadQuesting) { outputLock.lock(); }
            try
            {
                String ipAddr = (String)params[0];
                int port = (int)params[1];
                mySocket = new Socket();
                InetSocketAddress mySockAddr = new InetSocketAddress(ipAddr, port);
                try
                {
                    mySocket.connect(mySockAddr, 4000);
                }
                catch(IOException e) { }
                if(!mySocket.isConnected())
                {
                    if(threadQuesting) { outputLock.unlock(); }
                    System.out.println("Here");
                    JOptionPane.showMessageDialog(myNode.getGUI(), "Could not connect to the specified IP...");
                    myNode.shutdownNode(true);
                }
                else
                {
                    inLine = new Scanner(mySocket.getInputStream());
                    outLine = new PrintWriter(mySocket.getOutputStream(), true);
                }
            }
            catch(IOException e)
            {
                try
                {
                    stop(false);
                }
                catch(IOException e2) {}
            }
        }
        @Override
        public void run()
        {
            if(!threadQuesting) { super.run(); }
            else
            {
                while(inLine.hasNextLine())
                {
                    inputLock.lock();
                    try
                    {
                        String received = inLine.nextLine();
                        String[] datParts = received.split(":~:", -1);
                        if(!datParts[0].equals("JFLYMSGACK"))
                        {
                            OutputJobInfo ack = new OutputJobInfo(OutputJobInfo.JobType.SINGLE_DISPATCH, "Response_ack_to:" + mySocket.getInetAddress().getHostAddress(), "JFLYMSGACK");
                            oneDispatch(ack);
                        }
                        switch(datParts[0])               
                        {
                            case "JFLYTRANSIENT":
                                if(jNode.allowTransientForwarding(datParts[1]))
                                {
                                    doPanthreadDispatch(datParts[1], "JFLYTRANSIENT");
                                }
                                break;
                            case "JFLYDISCONNECTCOURTESY":
                                try
                                {
                                    stop(false);
                                }
                                catch(IOException e) {}
                                break;
                            case "JFLYQUESTERRESPONSE":
                                threadQuesting = false;
                                inputLock.unlock();
                                outputLock.unlock();
                                super.run();
                                break;
                            default:
                                break;
                        }
                    }
                    finally { inputLock.unlock(); }
                    if(stopping) { break; }
                }
            }
        }
    }
}
