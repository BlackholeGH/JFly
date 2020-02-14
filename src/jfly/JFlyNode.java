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
import java.util.concurrent.locks.ReentrantLock;
/**
 *
 * @author Blackhole
 */
public class JFlyNode {
    public static final int defaultPort = 44665;
    private int myID = 0;
    private BlockchainNodeManager blockManager;
    private ArrayList ConnectionThreadDirectory;
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
        new JFlyNode();
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
        private String type;
        private String data;
        private String header;
        public OutputJobInfo(String myType, String myData, String myHeader)
        {
            type = myType;
            data = myData;
            header = myHeader;
        }
        public String getType()
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
        private volatile Boolean stopping = false;
        protected JFlyNode jNode;
        protected Scanner inLine;
        protected PrintWriter outLine;
        protected ReentrantLock jqLock = new ReentrantLock();
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
            jqLock.lock();
            try
            {
                
            }
            finally
            {
                jqLock.unlock();
            }
        }
        public void queueDispatch(Queue<OutputJobInfo> myJobs)
        {
            jqLock.lock();
            try
            {
                for(OutputJobInfo tji : myJobs)
                {

                }
            }
            finally
            {
                jqLock.unlock();
            }
        }
        public void run()
        {
            /* In progress
            while(stopping == false)
            {
                try
                {
                    inLine.
                }
                catch(InterruptedException e) { }
                Boolean jobReadyNow = false;
                if(jqLock.tryLock())
                {
                    try
                    {
                        if(jobQueue.size() > 0)
                        {
                            jobReadyNow = true;
                        }
                    }
                    finally
                    {
                        jqLock.unlock();
                    }
                }
                if(jobReadyNow)
                {
                    jqLock.lock();
                    try
                    {
                        for(ThreadJobInfo tji : jobQueue)
                        {

                        }
                    }
                    finally
                    {
                        jqLock.unlock();
                    }
                    jobReadyNow = false;
                }
                if(inLine.hasNextLine())
                {
                    
                }
                this.wait(1000);
            }
            */
        }
        public void stop()
        {
            stopping = true;
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
