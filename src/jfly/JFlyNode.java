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
 */
public class JFlyNode {
    public static long time()
    {
        Date date = new Date();
        return date.getTime();
    }
    public String getUserID()
    {
        try
        {
            return myNCS.getIDFromIP(java.net.InetAddress.getLocalHost().getHostAddress());
        }
        catch(Exception e) { return "Retrieval failure"; }
    }
    public static final int defaultPort = 44665;
    private String myID = "";
    private BlockchainNodeManager blockManager;
    private ArrayList ConnectionThreadDirectory;
    private ReentrantLock threadListLock = new ReentrantLock();
    private NetworkConfigurationState myNCS = new NetworkConfigurationState();
    private ArrayList<String> messageLog = new ArrayList<String>();
    public NetworkConfigurationState getNCS()
    {
        return myNCS;
    }
    public String[] getLastMessages(int num)
    {
        return blockManager.getLast(num);
    }
    public void sendMessage(String message)
    {
        blockManager.authorBlock(BlockchainNodeManager.SharedStateBlock.ContentType.MESSAGE, message);
        getGUI().remoteSetTextBox(getLastMessages(30));
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
    public void unregisterThread(OneLinkThread thread)
    {
        threadListLock.lock();
        try
        {
            if(ConnectionThreadDirectory.contains(thread)) { ConnectionThreadDirectory.remove(thread); }
        }
        finally { threadListLock.unlock(); }
    }
    public static void main(String[] args)
    {
        new JFlyNode();
    }
    public JFlyNode()
    {
        ConnectionThreadDirectory = new ArrayList();
        blockManager = new BlockchainNodeManager(this);
        //new FlyInterface(this, 0);
        try
        {
            openReceiveAndWait(-1);
        }
        catch(Exception e) { }
    }
    public void applyGUI(GUIThread gt)
    {
        GUI getGUI = gt.getGUI();
        if(getGUI != null) { myGUI = getGUI; }
    }
    private class GUIThread implements Runnable
    {
        private GUI mainGUI = null;
        private JFlyNode localNode;
        public GUIThread(JFlyNode myNode)
        {
            localNode = myNode;
        }
        public GUI getGUI()
        {
            return mainGUI;
        }
        @Override
        public void run()
        {
            mainGUI = new GUI(localNode);
            localNode.applyGUI(this);
        }
    }
    public void openReceiveAndWait(int myPort) throws IOException
    {
        //new Thread(new GUIThread(this)).start();
        myGUI = new GUI(this);
        if(myPort > 65535 || myPort < 0) { myPort = defaultPort; }
        blockManager.authorBlock(BlockchainNodeManager.SharedStateBlock.ContentType.GENESIS, "");
        String usr = JOptionPane.showInputDialog("Choose a username!");
        NetworkConfigurationState.UserInfo me = new NetworkConfigurationState.UserInfo(java.net.InetAddress.getLocalHost().getHostAddress(), "", usr);
        blockManager.authorBlock(BlockchainNodeManager.SharedStateBlock.ContentType.USER_JOINED, me.toString());
        ExecutorService receivePool = Executors.newFixedThreadPool(500);
        try (ServerSocket listener = new ServerSocket(myPort)) {
            while (true) {
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
        myGUI = new GUI(this);
        ClientStyleThread connectThread = new ClientStyleThread(new Object[] { iP, rPort }, this);
        new Thread(connectThread).start();
        ExecutorService receivePool = Executors.newFixedThreadPool(500);
        try (ServerSocket listener = new ServerSocket(defaultPort)) {
            while (true) {
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
        public void queryReplies()
        {
            ArrayList<String> clonedDL = new ArrayList<String>();
            outputLock.lock();
            try
            {
                clonedDL = (ArrayList<String>)recentDispatchLog.clone();
            }
            finally { outputLock.unlock(); }
            for(String rec : recentDispatchLog)
            {
                String[] datSeg = rec.split(Pattern.quote(":~:"), -1);
                long timeSent = Long.decode(datSeg[0]);
                if(JFlyNode.time() - timeSent > 5000) { missed++; }
                if(missed < 3)
                {
                    OutputJobInfo missedJob = new OutputJobInfo(OutputJobInfo.JobType.SINGLE_DISPATCH, "Query-ack", "QUERYACK");
                    oneDispatch(missedJob);
                }
                else if(missed == 3)
                {
                    missed = 4;
                    
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
            jNode.getGUI().remoteSetTextBox(jNode.getLastMessages(30));
        }
        public void queueDispatch(Queue<OutputJobInfo> myJobs)
        {
            for(OutputJobInfo tji : myJobs)
            {
                oneDispatch(tji);
            }
        }
        protected void doPanthreadDispatch(String block, String header)
        {
            OutputJobInfo onForward = new OutputJobInfo(OutputJobInfo.JobType.MULTIPLE_DISPATCH, block, header);
            jNode.sendJobToThreads(onForward, new OneLinkThread[] { this });
        }
        Boolean introduction = false;
        protected void performNextLineOperation(String nextLine) throws RemoteBlockIntegrationException, UnknownHostException
        {
            OutputJobInfo ack = new OutputJobInfo(OutputJobInfo.JobType.SINGLE_DISPATCH, "Response_ack", "MSG_ACK");
            oneDispatch(ack);
            LinkedList<String> receivedDuringBlocking = new LinkedList<String>();
            String[] datParts = nextLine.split(":~:", -1);
            switch(datParts[0])               
            {
                case "MSG_ACK":
                    outputLock.lock();
                    try
                    {
                        recentDispatchLog = new ArrayList<String>();
                        missed = 0;
                    }
                    finally { outputLock.unlock(); }
                    break;
                case "JFLYCHAINBLOCK":                  
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
                                    String usr = JOptionPane.showInputDialog("Choose a username!");
                                    NetworkConfigurationState.UserInfo me = new NetworkConfigurationState.UserInfo(java.net.InetAddress.getLocalHost().getHostAddress(), "", usr);
                                    jNode.blockManager.authorBlock(BlockchainNodeManager.SharedStateBlock.ContentType.USER_JOINED, me.toString());
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
                            String usr = JOptionPane.showInputDialog("Choose a username!");
                            NetworkConfigurationState.UserInfo me = new NetworkConfigurationState.UserInfo(java.net.InetAddress.getLocalHost().getHostAddress(), "", usr);
                            jNode.blockManager.authorBlock(BlockchainNodeManager.SharedStateBlock.ContentType.USER_JOINED, me.toString());
                        }
                        doPanthreadDispatch(datParts[1], datParts[0]);
                    }
                    break;
                case "JFLYCHAINBLOCKREQUEST":
                    String search = jNode.pullOneBlockByHash(datParts[1]);
                    OutputJobInfo requestResponseJob = new OutputJobInfo(OutputJobInfo.JobType.SINGLE_DISPATCH, datParts[1] + ":~:" + search, "JFLYCHAINBLOCKRESPONSE");
                    oneDispatch(requestResponseJob);
                    break;
                case "JFLYCHAINBLOCKRESPONSE":
                    System.out.println("Warning: Received chain block request response outside of any request task...");
                    break;
            }
            while(receivedDuringBlocking.size() > 0)
            {
                performNextLineOperation(receivedDuringBlocking.pop());
            }
            jNode.getGUI().remoteSetTextBox(jNode.getLastMessages(30));
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
        public void stop() throws IOException
        {
            stopping = true;
            inLine.close();
            mySocket.close();
            jNode.unregisterThread(this);
        }
    }
    public static class ServerStyleThread extends OneLinkThread
    {
        public ServerStyleThread(Socket myAcceptedConnection, JFlyNode myNode)
        {
            super(myNode);
            mySocket = myAcceptedConnection;
            OutputJobInfo regiJob = new OutputJobInfo(OutputJobInfo.JobType.SINGLE_DISPATCH, myNode.getNCS().getRegistrar(), "JFLYCHAINBLOCK");
            oneDispatch(regiJob);
        }
    }
    public static class ClientStyleThread extends OneLinkThread
    {
        public ClientStyleThread(Object[] params, JFlyNode myNode) throws IOException
        {
            super(myNode);
            String ipAddr = (String)params[0];
            int port = (int)params[1];
            mySocket = new Socket(ipAddr, port);
        }
    }
}
