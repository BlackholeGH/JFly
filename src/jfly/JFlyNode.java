/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfly;

import java.awt.*;
import java.util.concurrent.*;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.net.SocketAddress;
import java.awt.event.*;
import java.io.*;
import java.net.ServerSocket;
import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;
/**
 *
 * @author Blackhole
 * TO DO:
 * Let socket connections time out and dispose the thread if so as unsuccessful
 * issueExistenceTransient()
 * crossNetworkSeekNode(), including returning existence transient listening, and issuing disconnect notif if not found, from some check in queryReplies()?
 * Actual transient handling
 */
public class JFlyNode {
    public static long time()
    {
        Date date = new Date();
        return date.getTime();
    }
    public static String hostAddr()
    {
        try
        {
            return java.net.InetAddress.getLocalHost().getHostAddress();
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
        Runnable finalShutdownThread = () -> 
        {
            Thread.currentThread().setName("Shutdown cleanup thread");
            if(pingerThread != null) { pingerThread.interrupt(); }
            leaveCluster();
            pShutdown = true;
            if(receivePool != null)
            {
                receivePool.shutdownNow();
            }
            if(myGUI != null) { myGUI.dispose(); }
            if(launcher != null) { launcher.dispose(); }
            System.exit(0);
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
                myNode.pingThreads();
                try
                {
                    Thread.sleep(5000);
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
            System.out.println("Closing");
            for(int i = 0; i < ConnectionThreadDirectory.size(); i++)
            {    
                if(ConnectionThreadDirectory.size() == 0) { break; }
                OneLinkThread olt = (OneLinkThread)ConnectionThreadDirectory.get(0);
                olt.stop(true);
            }
        }
        catch(IOException e) { System.out.println(e.getMessage()); }
        finally { threadListLock.unlock(); }
        System.out.println("Closing fin");
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
        return blockManager.getLast(num);
    }
    public void sendMessage(String message)
    {
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
        int attemptAdd = blockManager.addExtantBlockToChain(data);
        if(attemptAdd == 2)
        {
            return "FAILED_REQUEST_PREVIOUS";
        }
        else if(attemptAdd == 0 || attemptAdd == 1)
        {

            return "SUCCESSFULLY_INTEGRATED";
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
                    for(OneLinkThread blackThread : blacklist)
                    {
                        if(blackThread == trueThread) { continue; }
                    }
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
    private FlyInterface launcher = null;
    public JFlyNode()
    {
        ConnectionThreadDirectory = new ArrayList();
        blockManager = new BlockchainNodeManager(this);
        launcher = new FlyInterface(this);
        /*try
        {
            openReceiveAndWait(-1);
        }
        catch(Exception e) { }*/
    }
    public void wipeLauncher(FlyInterface fl)
    {
        if(fl == launcher)
        {
            launcher = null;
        }
    }
    public void issueExistenceTransient()
    {
        
    }
    private ExecutorService receivePool = null;
    public void openReceiveAndWait(int myPort) throws IOException
    {
        //new Thread(new GUIThread(this)).start();
        startPinger();
        myGUI = new GUI(this);
        if(myPort > 65535 || myPort < 0) { myPort = defaultPort; }
        blockManager.authorBlock(BlockchainNodeManager.SharedStateBlock.ContentType.GENESIS, "");
        usr = JOptionPane.showInputDialog(null, "Choose a username!", "Input username", JOptionPane.INFORMATION_MESSAGE);
        if(usr == null || usr.isEmpty())
        {
            usr = "IP User " + java.net.InetAddress.getLocalHost().getHostAddress();
        }
        NetworkConfigurationState.UserInfo me = new NetworkConfigurationState.UserInfo(java.net.InetAddress.getLocalHost().getHostAddress(), "", usr);
        blockManager.authorBlock(BlockchainNodeManager.SharedStateBlock.ContentType.USER_JOINED, me.toString());
        receivePool = Executors.newFixedThreadPool(500);
        try (ServerSocket listener = new ServerSocket(myPort)) {
            while (!shuttingDown()) {
                receivePool.execute(new ServerStyleThread(listener.accept(), this));
            }
        }
    }
    private GUI myGUI = null;
    public GUI getGUI()
    {
        return myGUI;
    }
    public void sendConnectAndOpen(String iP, int rPort) throws IOException
    {
        //new Thread(new GUIThread(this)).start();
        startPinger();
        myGUI = new GUI(this);
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
    public static abstract class OneLinkThread implements Runnable
    {
        public static class RemoteBlockIntegrationException extends Exception
        {
            enum FailureType { MissingRemoteHashOnRequest, PostCascadeNonIntegration }
            FailureType myFailure;
            public RemoteBlockIntegrationException(String message, FailureType type)
            {
                super(message);
                myFailure = type;
            }
            public FailureType getFailure()
            {
                return myFailure;
            }
        }
        protected volatile Boolean stopping = false;
        protected JFlyNode jNode;
        protected Scanner inLine;
        protected PrintWriter outLine;
        protected ReentrantLock outputLock = new ReentrantLock();
        protected ReentrantLock inputLock = new ReentrantLock();
        protected ArrayList<String> recentDispatchLog = new ArrayList<String>();
        public OneLinkThread(JFlyNode myNode)
        {
            jNode = myNode;
            myNode.registerThread(this);
        }
        protected Socket mySocket;
        public String getConnectionAddr()
        {
            return mySocket.getInetAddress().getHostAddress() + ":" + mySocket.getPort();
        }
        protected int missed = 0;
        protected long markedCourtesy = -1;
        public void queryReplies()
        {
            if(markedCourtesy > 0 && JFlyNode.time() - markedCourtesy > 60)
            {
                try
                {
                    stop(false);
                    return;
                }
                catch(Exception e) { }
            }
            else if(markedCourtesy > 0) { return; }
            ArrayList<String> clonedDL = new ArrayList<String>();
            outputLock.lock();
            try
            {
                clonedDL = (ArrayList<String>)recentDispatchLog.clone();
            }
            finally { outputLock.unlock(); }
            for(String rec : clonedDL)
            {
                String[] datSeg = rec.split(Pattern.quote(":~:"), -1);
                long timeSent = Long.decode(datSeg[0]);
                if(JFlyNode.time() - timeSent > 5000) { missed++; }
                if(missed > 0 && missed < 3)
                {
                    OutputJobInfo missedJob = new OutputJobInfo(OutputJobInfo.JobType.SINGLE_DISPATCH, "Query-ack", "QUERYACK");
                    oneDispatch(missedJob);
                }
                else if(missed == 3)
                {
                    missed = 4;
                    if(!jNode.getNCS().getIDFromIP(mySocket.getInetAddress().getHostAddress()).equals("UNKNOWN_USER"))
                    {
                        crossNetworkSeekNode(jNode.getNCS().getIDFromIP(mySocket.getInetAddress().getHostAddress()));
                    }
                }
            }
        }
        public void oneDispatch(OutputJobInfo myJob)
        {
            Boolean bypass = false;
            if(myJob.type == OutputJobInfo.JobType.INTERNAL_LOCK && myJob.token == outputLock) { bypass = true; }
            if(!bypass) { outputLock.lock(); }
            try
            {
                String outData = myJob.getHeader() + ":~:" + myJob.getData();
                outLine.println(outData);
                recentDispatchLog.add(JFlyNode.time() + ":~:" + getConnectionAddr() + ":~:" + outData);
            }
            finally
            {
                if(!bypass) { outputLock.unlock(); }
            }
        }
        public void queueDispatch(Queue<OutputJobInfo> myJobs)
        {
            for(OutputJobInfo tji : myJobs)
            {
                oneDispatch(tji);
            }
        }
        protected void crossNetworkSeekNode(String userHashIdentity)
        {
            
        }
        protected void doPanthreadDispatch(String block, String header)
        {
            OutputJobInfo onForward = new OutputJobInfo(OutputJobInfo.JobType.MULTIPLE_DISPATCH, block, header);
            jNode.sendJobToThreads(onForward, new OneLinkThread[] { this });
        }
        public void setDemandIntroduction()
        {
            introduction = false;
        }
        Boolean introduction = true;
        protected void performNextLineOperation(String nextLine) throws RemoteBlockIntegrationException, UnknownHostException
        {
            markedCourtesy = -1;
            missed = 0;
            outputLock.lock();
            try
            {
                recentDispatchLog.clear();
            }
            finally { outputLock.unlock(); }
            String[] datParts = nextLine.split(":~:", -1);
            if(!datParts[0].equals("JFLYMSGACK"))
            {
                OutputJobInfo ack = new OutputJobInfo(OutputJobInfo.JobType.SINGLE_DISPATCH, "Response_ack", "JFLYMSGACK");
                oneDispatch(ack);
            }
            switch(datParts[0])               
            {
                case "JFLYMSGACK":
                    outputLock.lock();
                    try
                    {
                        recentDispatchLog = new ArrayList<String>();
                        missed = 0;
                    }
                    finally { outputLock.unlock(); }
                    break;
                case "JFLYTRANSIENT":
                    if(jNode.allowTransientForwarding(datParts[1]))
                    {
                        
                        doPanthreadDispatch(datParts[1], "JFLYTRANSIENT");
                    }
                    break;
                case "JFLYDISCONNECTCOURTESY":
                    markedCourtesy = JFlyNode.time();
                    break;
                case "JFLYQUESTERREQUEST":
                    if(!jNode.queryAcceptQuester(mySocket.getInetAddress().getHostAddress()))
                    {
                        OutputJobInfo turnDownQuester = new OutputJobInfo(OutputJobInfo.JobType.SINGLE_DISPATCH, "quester_response_disconnect_courtesy", "JFLYDISCONNECTCOURTESY");
                        oneDispatch(turnDownQuester);
                        if(!jNode.getNCS().getIDFromIP(mySocket.getInetAddress().getHostAddress()).equals("UNKNOWN_USER"))
                        {
                            crossNetworkSeekNode(jNode.getNCS().getIDFromIP(mySocket.getInetAddress().getHostAddress()));
                        }
                    }
                    else
                    {
                        OutputJobInfo acceptQuester = new OutputJobInfo(OutputJobInfo.JobType.SINGLE_DISPATCH, "quester_response_accepted_notice", "JFLYQUESTERRESPONSE");
                        oneDispatch(acceptQuester);
                        jNode.issueExistenceTransient();
                    }
                    break;
                case "JFLYCHAINBLOCK":                  
                    handleNewBlock(nextLine, datParts);
                    break;
                case "JFLYCHAINBLOCKREQUEST":
                    String search = jNode.pullOneBlockByHash(datParts[1]);
                    OutputJobInfo requestResponseJob = new OutputJobInfo(OutputJobInfo.JobType.SINGLE_DISPATCH, datParts[1] + ":~:" + search, "JFLYCHAINBLOCKRESPONSE");
                    oneDispatch(requestResponseJob);
                    break;
                case "JFLYCHAINBLOCKRESPONSE":
                    System.out.println("Warning: Received chain block request response outside of any request task...");
                    break;
                case "JFLYQUESTERRESPONSE":
                    System.out.println("Warning: Received quester request response while not questing...");
                    break;
                default:
                    break;
            }
            jNode.getGUI().remoteSetTextBox(jNode.getLastMessages(30));
        }
        protected void handleNewBlock(String nextLine, String[] datParts) throws RemoteBlockIntegrationException, UnknownHostException
        {
            LinkedList<String> receivedDuringBlocking = new LinkedList<String>();
            String result = jNode.tryOneBlock(datParts[1]);
            if(result.equals("FAILED_REQUEST_PREVIOUS"))
            {
                outputLock.lock();
                try
                {
                    OutputJobInfo prevReqJob = new OutputJobInfo(OutputJobInfo.JobType.INTERNAL_LOCK, datParts[1].split("|")[0], "JFLYCHAINBLOCKREQUEST");
                    prevReqJob.setToken(outputLock);
                    oneDispatch(prevReqJob);
                    while(inLine.hasNextLine())
                    {
                        String received = inLine.nextLine();
                        String[] responseParts = nextLine.split(":~:");
                        if(responseParts[0].equals("JFLYCHAINBLOCKRESPONSE") && responseParts[1].equals(datParts[1].split("|")[0]))
                        {
                            if(responseParts[2].equals("BLOCK_HASH_NOT_FOUND"))
                            {
                                throw new RemoteBlockIntegrationException("BLOCK_HASH_NOT_FOUND", RemoteBlockIntegrationException.FailureType.MissingRemoteHashOnRequest);
                            }
                            else { performNextLineOperation("JFLYCHAINBLOCK:~:" + responseParts[2]); }
                            break;
                        }
                        else { receivedDuringBlocking.add(received); }
                    }
                    String secondResult = jNode.tryOneBlock(datParts[1]);
                    if(!secondResult.equals("SUCCESSFULLY_INTEGRATED")) { throw new RemoteBlockIntegrationException(secondResult, RemoteBlockIntegrationException.FailureType.PostCascadeNonIntegration); }
                    else
                    {
                        if(!introduction)
                        {
                            jNode.setLocalUsername(JOptionPane.showInputDialog(null, "Choose a username!", "Input username", JOptionPane.INFORMATION_MESSAGE));
                            if(jNode.getLocalUsername() == null || jNode.getLocalUsername().isEmpty())
                            {
                                jNode.setLocalUsername("IP User " + java.net.InetAddress.getLocalHost().getHostAddress());
                            }
                            NetworkConfigurationState.UserInfo me = new NetworkConfigurationState.UserInfo(java.net.InetAddress.getLocalHost().getHostAddress(), "", jNode.getLocalUsername());
                            jNode.blockManager.authorBlock(BlockchainNodeManager.SharedStateBlock.ContentType.USER_JOINED, me.toString());
                            introduction = true;
                        }
                        doPanthreadDispatch(datParts[1], datParts[0]);
                    }
                }
                finally { outputLock.unlock(); }
            }
            else if(result.equals("SUCCESSFULLY_INTEGRATED"))
            {
                if(!introduction)
                {
                    jNode.setLocalUsername(JOptionPane.showInputDialog(null, "Choose a username!", "Input username", JOptionPane.INFORMATION_MESSAGE));
                    if(jNode.getLocalUsername() == null || jNode.getLocalUsername().isEmpty())
                    {
                        jNode.setLocalUsername("IP User " + java.net.InetAddress.getLocalHost().getHostAddress());
                    }
                    NetworkConfigurationState.UserInfo me = new NetworkConfigurationState.UserInfo(java.net.InetAddress.getLocalHost().getHostAddress(), "", jNode.getLocalUsername());
                    jNode.blockManager.authorBlock(BlockchainNodeManager.SharedStateBlock.ContentType.USER_JOINED, me.toString());
                    introduction = true;
                }
                doPanthreadDispatch(datParts[1], datParts[0]);
            }
            while(receivedDuringBlocking.size() > 0)
            {
                performNextLineOperation(receivedDuringBlocking.pop());
            }
        }
        Boolean nameSet = false;
        public void run()
        {
            if(!nameSet)
            {
                Thread.currentThread().setName("Socket read thread");
                nameSet = true;
            }
            while(inLine.hasNextLine())
            {
                inputLock.lock();
                try
                {
                    String received = inLine.nextLine();
                    System.out.println(received);
                    try
                    {
                        try
                        {
                            performNextLineOperation(received);
                        }
                        catch(UnknownHostException UHE) {}
                    }
                    catch(RemoteBlockIntegrationException rbie)
                    {
                        if(rbie.myFailure == RemoteBlockIntegrationException.FailureType.MissingRemoteHashOnRequest)
                        {
                            JOptionPane.showMessageDialog(null, "A block was received from an external cluster or node with an orphaned blockchain. Cannot accept at this time.");
                        }
                    }
                }
                finally { inputLock.unlock(); }
                if(stopping) { break; }
            }
        }
        public void stop(Boolean skipBlockUnregister) throws IOException
        {
            stopping = true;
            OutputJobInfo disCourt = new OutputJobInfo(OutputJobInfo.JobType.SINGLE_DISPATCH, "stopping_disconnect_courtesy", "JFLYDISCONNECTCOURTESY");
            oneDispatch(disCourt);
            mySocket.close();
            inLine.close();
            jNode.unregisterThread(this, skipBlockUnregister);
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
                mySocket = new Socket(ipAddr, port);
                inLine = new Scanner(mySocket.getInputStream());
                outLine = new PrintWriter(mySocket.getOutputStream(), true);
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
