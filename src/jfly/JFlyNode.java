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
    public static final int defaultPort = 44665;
    private int myID = 0;
    private BlockchainNodeManager blockManager;
    private ArrayList ConnectionThreadDirectory;
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
        else if(attemptAdd == 0 || attemptAdd == 1) { return "SUCCESSFULLY_INTEGRATED"; }
        else if(attemptAdd == 4) { return "BLOCK_ALREADY_ADDED"; }
        else { return "FAILED_OTHER_UNSPECIFIED"; }
    }
    public synchronized void registerThread(OneLinkThread thread)
    {
        if(!ConnectionThreadDirectory.contains(thread)) { ConnectionThreadDirectory.add(thread); }
    }
    public synchronized void unregisterThread(OneLinkThread thread)
    {
        if(ConnectionThreadDirectory.contains(thread)) { ConnectionThreadDirectory.remove(thread); }
    }
    public static void main(String[] args)
    {
        new GUI();
    }
    public JFlyNode()
    {
        ConnectionThreadDirectory = new ArrayList();
        blockManager = new BlockchainNodeManager(this);
        FlyInterface startUpInter = new FlyInterface(this, 0);
    }
    public void openReceiveAndWait(int myPort) throws IOException
    {
        if(myPort > 65535 || myPort < 0) { myPort = defaultPort; }
        ExecutorService receivePool = Executors.newFixedThreadPool(500);
        try (ServerSocket listener = new ServerSocket(myPort)) {
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
        protected volatile Boolean stopping = false;
        protected JFlyNode jNode;
        protected Scanner inLine;
        protected PrintWriter outLine;
        protected ReentrantLock jqLock = new ReentrantLock();
        protected ArrayList<String> dispatchLog = new ArrayList<String>();
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
        public void oneDispatch(OutputJobInfo myJob)
        {
            Boolean bypass = false;
            if(myJob.type == OutputJobInfo.JobType.INTERNAL_LOCK && myJob.token == jqLock) { bypass = true; }
            if(!bypass) { jqLock.lock(); }
            try
            {
                String outData = myJob.getHeader() + ":~:" + myJob.getData();
                outLine.println(outData);
                dispatchLog.add(JFlyNode.time() + ":~TIME~:" + outData);
            }
            finally
            {
                if(!bypass) { jqLock.unlock(); }
            }
        }
        public void queueDispatch(Queue<OutputJobInfo> myJobs)
        {
            for(OutputJobInfo tji : myJobs)
            {
                oneDispatch(tji);
            }
        }
        protected void performNextLineOperation(String nextLine)
        {
            LinkedList<String> receivedDuringBlocking = new LinkedList<String>();
            String[] datParts = nextLine.split(":~:");
            switch(datParts[0])               
            {
                case "JFLYCHAINBLOCK":                  
                    String result = jNode.tryOneBlock(datParts[1]);
                    if(result.equals("FAILED_REQUEST_PREVIOUS"))
                    {
                        jqLock.lock();
                        try
                        {
                            OutputJobInfo prevReqJob = new OutputJobInfo(OutputJobInfo.JobType.INTERNAL_LOCK, datParts[1].split("|")[0], "JFLYCHAINBLOCKREQUEST");
                            prevReqJob.setToken(jqLock);
                            oneDispatch(prevReqJob);
                            while(inLine.hasNextLine())
                            {
                                String received = inLine.nextLine();
                                String[] responseParts = nextLine.split(":~:");
                                if(responseParts[0].equals("JFLYCHAINBLOCKRESPONSE") && responseParts[1].equals(datParts[1].split("|")[0]))
                                {
                                    if(responseParts[2].equals("BLOCK_HASH_NOT_FOUND")) { }
                                    else { performNextLineOperation("JFLYCHAINBLOCK:~:" + responseParts[2]); }
                                }
                                else { receivedDuringBlocking.add(received); }
                            }
                        }
                        finally { jqLock.unlock(); }
                    }
                    else if(result.equals("SUCCESSFULLY_INTEGRATED"))
                    {
                        //Dispatch to all other node connection threads here
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
        }
        public void run()
        {
            while(inLine.hasNextLine())
            {
                String received = inLine.nextLine();
                performNextLineOperation(received);
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
