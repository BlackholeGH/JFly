/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfly;

import flyutils.TextUtility;
import flyutils.NetworkConfigurationState;
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
 * The JFlyNode class represents the top level network "node" object for this application. Creating a JFlyNode instance starts the application.
 * Only one instance should exist at a time, and a singleton pattern is used to ensure this.
 * The JFlyNode manages all the other data classes and connection threads, and performs operations and data handling.
 * Most other class instances will have a reference to the application's JFlyNode instance, and can influence it via public methods.
 * The JFlyNode holds references to any GUI windows and will update them with application data to be displayed.
 * @author Blackhole (dg7239p)
 */
public class JFlyNode {
    /**
     * Returns the unix time as a long.
     * @return The current unix epoch time as a long.
     */
    public static long time()
    {
        Date date = new Date();
        return date.getTime();
    }
    private String recordedHostAddress = "";
    /**
     * Manually set the set host address for the node to a new address.
     * @param addr The new host address.
     * @param myManager The BlockchainNodeManager object that is performing ths request.
     */
    public void resetAddress(String addr, BlockchainNodeManager myManager)
    {
        if(myManager == blockManager) { recordedHostAddress = addr; }
    }
    /**
     * Set a new record of the local host address if none has yet been set.
     * @param addr The new local host address.
     */
    public void setDefaultAddress(String addr)
    {
        if(recordedHostAddress.isEmpty())
        {
            recordedHostAddress = addr;
        }
    }
    /**
     * Get the internal record of the host address of this node.
     * @return The host address record, or the environment default if none is set.
     */
    public String hostAddr()
    {
        try
        {
            if(recordedHostAddress.isEmpty()) { return java.net.InetAddress.getLocalHost().getHostAddress(); }
            else { return recordedHostAddress; }
        }
        catch(Exception e) { return "Retrieval failure"; }
    }
    /**
     * Get the hash ID of the node user.
     * @return The user ID hash from IP lookup via the NetworkConfigurationState.
     */
    public String getUserID()
    {
        return myNCS.getIDFromIP(hostAddr());
    }
    private Boolean pShutdown = false;
    /**
     * Is this node shutting down?
     * @return Boolean true/false for shutdown state.
     */
    public Boolean shuttingDown()
    {
        return pShutdown;
    }
    /**
     * Shutdown this node and close the application.
     */
    public void shutdownNode()
    {
        shutdownNode(false);
    }
    /**
     * Shutdown this node and exit the application.
     * @param relaunch True/false for if you want the application/node to then restart.
     */
    public void shutdownNode(Boolean relaunch)
    {
        //A new runnable implementation is created
        Runnable finalShutdownThread = () -> 
        {
            //Various threads are shutdown and GUI elements disposed.
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
    /**
     * Ping all active OneLinkThreads to ensure that their remote targets are still online.
     */
    public void pingThreads()
    {
        ArrayList ctdCopy = null;
        threadListLock.lock();
        try
        {
            //The ConnectionThreadDirectory is cloned.
            if(!ConnectionThreadDirectory.isEmpty()) 
            {
                ctdCopy = (ArrayList)ConnectionThreadDirectory.clone();
            }
        }
        finally { threadListLock.unlock(); }
        //Each OneLinkThread is "pinged" by having queryReplies called upon it.
        if(ctdCopy != null)
        {
            for(Object o : ctdCopy)
            {
                OneLinkThread olt = (OneLinkThread)o;
                olt.queryReplies();
            }
        }
    }
    /**
     * Start the "pinger" thread for pinging OneLinkThreads
     */
    private void startPinger()
    {
        pingerThread = new Thread(new pingerRunnable(this));
        pingerThread.start();
    }
    Thread pingerThread = null;
    /**
     * This class is the Runnable implementation that runs the "connection autoping thread" operations.
     */
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
            //Threads will periodically be pinged every five seconds.
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
    /**
     * Start the "coordinator" thread.
     */
    private void startCoordinator()
    {
        coordinatorThread = new Thread(new coordinatorRunnable(this));
        coordinatorThread.start();
    }
    /**
     * This class is the Runnable implementation that checks whether the current user should be the coordinator, and attempts to contact other network nodes periodically if this is the case.
     */
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
            //The coordinator layer thread will cycle through every ten seconds.
            while(!myNode.shuttingDown())
            {
                try
                {
                    ArrayList<String> usrIDs = myNode.getNCS().getUserIDs();
                    if(usrIDs.size() > 0)
                    {
                        usrIDs.sort(null);
                        //The coordinator is selected by choosing the node on the network that has the unique ID that is alphabetically first.
                        //As these IDs are based on a unique hash, collisions are extremely unlikely.
                        if(myNode.getUserID().equals(usrIDs.get(0)))
                        {
                            System.out.println("I am coordinator!");
                            //The coordinator will periodically attempt to contact other nodes on the network.
                            for(int i = 1; i < usrIDs.size(); i++)
                            {
                                System.out.println("CSeek: " + usrIDs.get(i));
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
    /**
     * Close all open OneLinkThreads for this JFlyNode.
     */
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
    /**
     * threadListLock is a Reentrant lock that ensures that access to the thread list (ConnectionThreadDirectory) is thread safe.
     */
    private ReentrantLock threadListLock = new ReentrantLock();
    private NetworkConfigurationState myNCS = new NetworkConfigurationState();
    private ArrayList<String> transientMessages = new ArrayList<String>();
    /**
     * Check whether this JFlyNode should forward a given transient message, and add forwarded transients to a list so they will not be forwarded in the future.
     * @param transientPackage The contents of the transient message.
     * @return True/false value for whether the transient message should be forwarded.
     */
    public synchronized Boolean allowTransientForwarding(String transientPackage)
    {
        //A given transient package is given a unique identifier by hashing it.
        String tpHash = BlockchainNodeManager.SharedStateBlock.getHash(transientPackage);
        String foundStr = null;
        ArrayList<String> expiredTransients = new ArrayList();
        //The existing record of transients that have passed through this node is referenced
        for(String s : transientMessages)
        {
            String[] strs = s.split(Pattern.quote(":~:"), -1);
            long timeSent = Long.decode(strs[0]);
            //If the transient was recorded more than 30 seconds ago, the record is discarded
            if(time() - timeSent > 30000) { expiredTransients.add(s); }
            if(strs[1].equals(tpHash))
            {
                foundStr = s;
            }
        }
        //Removing the expired transients
        for(String eT : expiredTransients)
        {
            //if(eT == foundStr) { continue; }
            transientMessages.remove(foundStr);
        }
        if(foundStr != null)
        {
            //transientMessages.remove(foundStr);
            //Transients already on the list should not be forwarded again
            return false;
        }
        else
        {
            //Otherwise, new transients are added to the list and then can be forwarded.
            //This mechanism means that a given transient message only propogates through the network once.
            transientMessages.add(time() + ":~:" + tpHash);
            return true;
        }
    }
    /**
     * Gets the NetworkConfigurationState for this JFlyNode.
     * @return The NetworkConfigurationState instance.
     */
    public NetworkConfigurationState getNCS()
    {
        return myNCS;
    }
    /**
     * Gets the BlockchainNodeManager instance for this JFlyNode.
     * @return The BlockchainNodeManager instance.
     */
    public BlockchainNodeManager getBNM()
    {
        return blockManager;
    }
    /**
     * Returns a list of all OneLinkThread objects that represent a connection to a user with the given hash ID.
     * @param hashIdentifier The hash ID of the user to search for.
     * @return An array of OneLinkThreads that represent a connection to the user with the given hash ID.
     */
    public OneLinkThread[] getThreadsForUser(String hashIdentifier)
    {
        //The user's hash ID is used to retrieve an IP address, if one is recorded.
        String tryGetIP = myNCS.getIPFromID(hashIdentifier);
        if(tryGetIP.equals("UNKNOWN_USER")) { return null; }
        ArrayList<OneLinkThread> foundThreads = new ArrayList();
        threadListLock.lock();
        //Any OneLinkThreads connected to this IP are then searched for.
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
            //The OneLinkThreads are returned if any are found.
            return foundThreads.toArray(new OneLinkThread[0]);
        }
    }
    /**
     * Attempt to directly contact a given user on the network, both via open threads or a new connection.
     * @param hashIdentifier The hash ID for the user to attempt to contact.
     */
    public void attemptContact(String hashIdentifier)
    {
        //When attempting to contact a node directly, first we check for any open sockets from this node to that node.
        OneLinkThread[] openConnects = getThreadsForUser(hashIdentifier);
        if(openConnects != null)
        {
            //If a socket connection already exists, the remote node is queried.
            OutputJobInfo missedJob = new OutputJobInfo(OutputJobInfo.JobType.SINGLE_DISPATCH, "Query-ack", "QUERYACK");
            for(OneLinkThread olt : openConnects)
            {
                olt.oneDispatch(missedJob);
            }
        }
        else
        {
            //If no current connection exists, a new ClientStyleThread is created to try and contact that node.
            String iP = myNCS.getIPFromID(hashIdentifier);
            if(!iP.equals("UNKNOWN_USER"))
            {
                //The thread is set to "Quester" mode, where it will try to contact the node and indicate that it is trying to establish a connection in this way.
                //The remote note can decline this sort of connection if it determines it is unneccessary.
                ClientStyleThread questThread = new ClientStyleThread(new Object[] { iP, defaultPort }, this, true);
                new Thread(questThread).start();
            }
        }
    }
    private String questNode = "";
    /**
     * Checks whether a quester connection request should be accepted, based on if a quester has recently been accepted.
     * @param questerIP The IP of the requesting quester.
     * @return True/false whether the quester should be accepted.
     */
    public Boolean queryAcceptQuester(String questerIP)
    {
        Boolean accept = false;
        //A node should only accept one quester request at a time, so one will not be accepted if one has been accepted in the past 30 seconds.
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
            //When a quester is accepted, the time of the acceptance and the IP of the quester are recorded.
            questNode = JFlyNode.time() + ":" + questerIP;
            return true;
        }
        else { return false; }
    }
    /**
     * Causes this JFlyNode to disconnect from its cluster and close all connections.
     */
    public void leaveCluster()
    {
        //When the node leaves a cluster, it publishes a blockchain block to indicate this.
        blockManager.authorBlock(BlockchainNodeManager.SharedStateBlock.ContentType.USER_LEFT, myNCS.getUserDataFromID(getUserID()));
        closeAll();
    }
    public synchronized void updateChatWindow()
    {
        //Only the last chat messages to a depth of 50 are retrieved.
        if(myGUI != null) { myGUI.remoteSetTextBox(getLastMessages(50)); }
    }
    /**
     * Retrieve the last x chat messages from the Blockchain.
     * @param num The number of chat messages to retrieve.
     * @return An array of the last x messages.
     */
    public String[] getLastMessages(int num)
    {
        String[] out = blockManager.getLast(num);
        for(int i = 0; i < out.length; i++)
        {
            //Retrieved messages are desanitized to re-add filtered delimiter strings
            out[i] = TextUtility.desanitizeText(out[i]);
        }
        return out;
    }
    /**
     * Sends a new message from this node.
     * @param message The text of the new message to send.
     */
    public void sendMessage(String message)
    {
        //Messages to be sent have to be sanitized to remove delimiter strings
        message = TextUtility.sanitizeText(message);
        //A new message block is authored to the blockchain.
        blockManager.authorBlock(BlockchainNodeManager.SharedStateBlock.ContentType.MESSAGE, message);
    }
    /**
     * Returns a Blockchain block from a given hash.
     * @param hash The hash of the block to retrieve.
     * @return The String representation of a Blockchain block.
     */
    public synchronized String pullOneBlockByHash(String hash)
    {
        String pulledBlock = blockManager.getByHash(hash);
        if(pulledBlock == null) { return "BLOCK_HASH_NOT_FOUND"; }
        else{ return pulledBlock; }
    }
    /**
     * Attempt to integrate a retrieved block into the Blockchain.
     * @param data The String representation of a Blockchain block.
     * @return A String representing the result of the attempted integration.
     * "SUCCESSFULLY_INTEGRATED:[HASH]" : The block with hash [HASH] was successfully integrated.
     * "FAILED_REQUEST_PREVIOUS" : The block integration failed, as the previous block in the chain was not present and should be requested.
     * "BLOCK_ALREADY_ADDED" : Block not added as the Blockchain already contained it.
     * "FAILED_OTHER_UNSPECIFIED" : The block integration failed for an unspecified reason.
     */
    public synchronized String tryOneBlock(String data)
    {
        //The string result is determined from the return of addExtantBlockToChain().
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
    /**
     * Send an OutputJobInfo to this JFlyNode's OneLinkThreads.
     * @param job The OutputJobInfo to send.
     * @param blacklist An array of any OneLinkThread objects that the job should not be sent to.
     */
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
    /**
     * Add a new OneLinkThread object to the registry of this JFlyNode.
     * @param thread The OneLinkThread to register.
     */
    public void registerThread(OneLinkThread thread)
    {
        threadListLock.lock();
        try
        {
            if(!ConnectionThreadDirectory.contains(thread)) { ConnectionThreadDirectory.add(thread); }
        }
        finally { threadListLock.unlock(); }
    }
    /**
     * Unregister a OneLinkThread from the registry of this JFlyNode.
     * @param thread The thread to unregister.
     * @param skipBlocking True/false value for whether or not to skip the threadlist blocking (lock object).
     */
    public void unregisterThread(OneLinkThread thread, Boolean skipBlocking)
    {
        //Blocking may need to be skipped if this method is called from within another iteration operation through the ConnectionThreadDirectory.
        if(!skipBlocking) { threadListLock.lock(); }
        try
        {
            if(ConnectionThreadDirectory.contains(thread)) { ConnectionThreadDirectory.remove(thread); }
        }
        finally { if(!skipBlocking) { threadListLock.unlock(); } }
    }
    String usr = null;
    /**
     * Get the local record of the username for this node's user.
     * @return The local username record.
     */
    public String getLocalUsername()
    {
        return usr;
    }
    /**
     * Set a new local record of the username for this node's user.
     * @param username The new username to set.
     */
    public void setLocalUsername(String username)
    {
        usr = username;
    }
    /**
     * The entry point for this program.
     * @param args Command line arguments.
     */
    public static void main(String[] args)
    {
        JFlyNode.makeJFlyNode();
    }
    private FlyLauncher launcher = null;
    private static Boolean single = false;
    /**
     * Create the instance of JFlyNode, singleton style.
     */
    public static JFlyNode makeJFlyNode()
    {
        if(!single)
        {
            single = true;
            return new JFlyNode();
        }
        else { return null; }
    }
    /**
     * The JFlyNode constructor. This initialises the directory of OneLinkThreads, the BlockchainNodeManager and starts the FlyLauncher.
     */
    private JFlyNode()
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
    /**
     * Wipes the registered FlyLauncher object from the JFlyNode if it is the same as the FlyLauncher passed.
     * @param fl The FlyLauncher object to test for potential wiping.
     */
    public void wipeLauncher(FlyLauncher fl)
    {
        if(fl == launcher)
        {
            launcher = null;
        }
    }
    /**
     * Issues a new transient message to the network indicating that the given JFlyNode is still connected.
     */
    public void issueExistenceTransient()
    {
        String transientBody = "responseping+-+" + getUserID() + "+-+" + time();
        OutputJobInfo existTransient = new OutputJobInfo(OutputJobInfo.JobType.MULTIPLE_DISPATCH, transientBody, "JFLYTRANSIENT");
        sendJobToThreads(existTransient, null);
    }
    private Hashtable seekers = new Hashtable();
    private final int seekTolerance = 2000;
    private final int contactTolerance = 2000;
    /**
     * Perform seeking operations across the network for a given user.
     * @param userIDorResponseTransient Either the user hash ID for the user to find, or a response transient to check whether it is a user response from a seek operation.
     */
    public void crossNetworkSeekNode(String userIDorResponseTransient)
    {
        crossNetworkSeekNode(userIDorResponseTransient, null, false);
    }
     /**
     * Perform seeking operations across the network for a given user.
     * @param userIDorResponseTransient Either the user hash ID for the user to find, or a response transient to check whether it is a user response from a seek operation.
     * @param fullWipe True/false value to temporarily wipe the seekers record.
     */
    public void crossNetworkSeekNode(String userIDorResponseTransient, Boolean fullWipe)
    {
        crossNetworkSeekNode(userIDorResponseTransient, null, fullWipe);
    }
    /**
     * Perform seeking operations across the network for a given user.
     * @param userIDorResponseTransient Either the user hash ID for the user to find, or a response transient to check whether it is a user response from a seek operation.
     * @param purge A user to manually remove from the seekers list.
     * @param fullWipe True/false value to temporarily wipe the seekers record.
     */
    public synchronized void crossNetworkSeekNode(String userIDorResponseTransient, String purge, Boolean fullWipe)
    {
        //The purge functionality is used to remove a node from the seekers list, i.e. if a new USER_LEFT block is published for that node.
        if(purge != null)
        {
            if(seekers.containsKey("SEEK|" + purge)) { seekers.remove("SEEK|" + purge); }
            else if(seekers.containsKey("CONTACT|" + purge)) { seekers.remove("CONTACT|" + purge); }
        }
        if(fullWipe) { seekers.clear(); }
        //If the input is a transient message, then it is checked to see if it is a response ping from a node being sought.
        if(userIDorResponseTransient.startsWith("JFLYTRANSIENT"))
        {
            String findUsrID = (userIDorResponseTransient.split(Pattern.quote(":~:"), -1)[1]).split(Pattern.quote("+-+"), -1)[1];
            if(seekers.containsKey("SEEK|" + findUsrID))
            {
                seekers.put("SEEK|" + findUsrID, -time());
            }
            else if(seekers.containsKey("CONTACT|" + findUsrID))
            {
                seekers.put("CONTACT|" + findUsrID, -time());
            }
        }
        //If the input is a user's hash ID, then we attempt to contact that user with a transient message to be relayed.
        else if(userIDorResponseTransient.startsWith("USERHASHID"))
        {
            String userHashID = userIDorResponseTransient.replaceAll(Pattern.quote("USERHASHID|"), "");
            Boolean containsAlready = true;
            long contents = 0;
            if(seekers.containsKey("SEEK|" + userHashID)) { contents = (long)seekers.get("SEEK|" + userHashID); }
            else if(seekers.containsKey("CONTACT|" + userHashID)) { contents = (long)seekers.get("CONTACT|" + userHashID); }
            else { containsAlready = false; }
            if(!containsAlready || (containsAlready && contents < 0));
            {
                seekers.put("SEEK|" + userHashID, time());
                String transientBody = "seeking+-+" + userHashID + "+-+" + time();
                OutputJobInfo seekingTransient = new OutputJobInfo(OutputJobInfo.JobType.MULTIPLE_DISPATCH, transientBody, "JFLYTRANSIENT");
                sendJobToThreads(seekingTransient, null);
            }
        }
        //Otherwise, the seekers list is simply updated.
        else
        {
            if(seekers.size() > 0)
            {
                Hashtable seekersCl = (Hashtable)seekers.clone();
                for(Object hashIDo : seekersCl.keySet())
                {
                    System.out.println(hashIDo);
                    String hashID = ((String)hashIDo).split(Pattern.quote("|"))[1];
                    String mode = ((String)hashIDo).split(Pattern.quote("|"))[0];
                    long seekTime = 0;
                    try
                    {
                        seekTime = (long)seekers.get(hashIDo);
                    }
                    catch(Exception e)
                    {
                        System.out.println(hashIDo);
                        System.out.println(seekers);
                    }
                    if(seekTime >= 0)
                    {
                        //If the seek time has expired, then a transient is sent out with instructions for other nodes to attempt to directly contact the missing node.
                        if(mode.equals("SEEK") && time() - seekTime > seekTolerance)
                        {
                            seekers.remove(hashIDo);
                            seekers.put("CONTACT|" + hashID, time());
                            String transientBody = "forcecontact+-+" + hashID + "+-+" + time();
                            OutputJobInfo seekingTransient = new OutputJobInfo(OutputJobInfo.JobType.MULTIPLE_DISPATCH, transientBody, "JFLYTRANSIENT");
                            sendJobToThreads(seekingTransient, null);
                            attemptContact(hashID);
                        }
                        else if(mode.equals("CONTACT") && time() - seekTime > contactTolerance)
                        {
                            seekers.remove(hashIDo);
                            issueTimeout(hashID);
                        }
                    }
                    else
                    {
                        if(seekTime - -time() > 4000) { seekers.remove(hashIDo); }
                    }
                }
            }
        }
    }
    /**
     * Author a disconnect on behalf of a network user who has timed out.
     * @param hashID The user hash ID for the timed out user.
     */
    private void issueTimeout(String hashID)
    {
        if(getThreadsForUser(hashID) != null)
        {
            for(OneLinkThread deadThread : getThreadsForUser(hashID))
            {
                try
                {
                    deadThread.stop(false);
                }
                catch(IOException e) {}
            }
        }
        //If a user has timed out and cannot be contacted, a USER_LEFT block is authored on their behalf.
        blockManager.authorBlock(BlockchainNodeManager.SharedStateBlock.ContentType.USER_LEFT, myNCS.getUserDataFromID(hashID) + "+-+presumed_disconnect_timeout");
        for(String iD : myNCS.getUserIDs())
        {
            if(iD.equals(getUserID())) { continue; }
            if(getThreadsForUser(iD) == null) { crossNetworkSeekNode("USERHASHID|" + iD); }
        }
    }
    private ExecutorService receivePool = null;
    private int myListenPort = 44665;
    /**
     * Get the manually set connection listener port.
     * @return The port number.
     */
    public int getManualListenPort()
    {
        return myListenPort;
    }
    /**
     * Manually set the incoming connection listen port for this JFlyNode.
     * @param port The new port to listen on.
     */
    public void setManualListenPort(int port)
    {
        if(port > 65535 || port < 0) { myListenPort = defaultPort; }
        else { myListenPort = port; }
    }
    /**
     * Start the network protocol for an initial cluster node (waits for an external connection).
     * @throws IOException 
     */
    public void openReceiveAndWait() throws IOException
    {
        //The pinger and coordinator threads are started.
        startPinger();
        startCoordinator();
        myGUI = new FlyChatGUI(this);
        if(myListenPort > 65535 || myListenPort < 0) { myListenPort = defaultPort; }
        //As this node is starting a new cluster, it initializes the Blockchain with a GENESIS block.
        blockManager.authorBlock(BlockchainNodeManager.SharedStateBlock.ContentType.GENESIS, "");
        usr = JOptionPane.showInputDialog(null, "Choose a username!", "Input username", JOptionPane.INFORMATION_MESSAGE);
        usr = TextUtility.sanitizeText(usr);
        if(usr == null || usr.isEmpty())
        {
            usr = "IP User " + hostAddr();
        }
        //The user info for this node is obtained.
        NetworkConfigurationState.UserInfo me = new NetworkConfigurationState.UserInfo(hostAddr(), "", usr);
        //Then a USER_JOINED block is authored for this user.
        blockManager.authorBlock(BlockchainNodeManager.SharedStateBlock.ContentType.USER_JOINED, me.toString());
        //New connections are then listened for.
        receivePool = Executors.newFixedThreadPool(500);
        try (ServerSocket listener = new ServerSocket(myListenPort)) {
            while (!shuttingDown()) {
                receivePool.execute(new ServerStyleThread(listener.accept(), this));
            }
        }
    }
    private FlyChatGUI myGUI = null;
    /**
     * Gets the FlyChatGUI instance for this JFlyNode.
     * @return The FlyChatGUI instance.
     */
    public FlyChatGUI getGUI()
    {
        return myGUI;
    }
    /**
     * Start the network protocol for a remote connection (connects to a remote node, before also listening for incoming connections).
     * @throws IOException 
     */
    public void sendConnectAndOpen(String iP, int rPort) throws IOException
    {
        //The pinger and coordinator threads are started.
        startPinger();
        startCoordinator();
        myGUI = new FlyChatGUI(this);
        blockManager.addRegistrarTolerance(1);
        //We attempt to connect to an existing node.
        ClientStyleThread connectThread = new ClientStyleThread(new Object[] { iP, rPort }, this, false);
        connectThread.setDemandIntroduction();
        new Thread(connectThread).start();
        //New connections are then listened for.
        receivePool = Executors.newFixedThreadPool(500);
        try (ServerSocket listener = new ServerSocket(defaultPort)) {
            while (!shuttingDown()) {
                receivePool.execute(new ServerStyleThread(listener.accept(), this));
            }
        }
    }
    /**
     * This class contains information for an job to be dispatched by a OneLinkNode.
     */
    public static class OutputJobInfo
    {
        /**
         * Enumeration representing the different dispatch job types that can exist.
         * SINGLE_DISPATCH: The data will be dispatched on a single connection thread.
         * MULTIPLE_DISPATCH: The data will be dispatched on multiple connection threads.
         * INTERNAL_LOCK: This indicates that the OutputJobInfo contains a token object that can be used to bypass the outputLock ReentrantLock.
         */
        enum JobType { SINGLE_DISPATCH, MULTIPLE_DISPATCH, INTERNAL_LOCK };
        private JobType type;
        private String data;
        private String header;
        private Object token;
        /**
         * The OutputJobInfo constructor.
         * @param myType The JobType for this OutputJobInfo.
         * @param myData The data to be sent for this job.
         * @param myHeader The header for the data to be sent.
         */
        public OutputJobInfo(JobType myType, String myData, String myHeader)
        {
            type = myType;
            data = myData;
            header = myHeader;
        }
        /**
         * Set the token object for this OutputJobInfo.
         * @param myToken The token object to be set.
         */
        public void setToken(Object myToken)
        {
            token = myToken;
        }
        /**
         * Get the token object for this OutputJobInfo.
         * @return The token object. This is a lock object used to bypass a synchronization lock when the message is to be sent.
         */
        public Object getToken()
        {
            return token;
        }
        /**
         * Get the JobType for this OutputJobInfo.
         * @return The JobType for this instance.
         */
        public JobType getType()
        {
            return type;
        }
        /**
         * Get the data to be sent for this OutputJobInfo.
         * @return The data String.
         */
        public String getData()
        {
            return data;
        }
        /**
         * Get the data header to be used for this OutputJobInfo.
         * @return The data header String.
         */
        public String getHeader()
        {
            return header;
        }
    }
    /**
     * This class is an extension of OneLinkThread that handles incoming connections.
     */
    public static class ServerStyleThread extends OneLinkThread
    {
        /**
         * The ServerStyleThread constructor.
         * @param myAcceptedConnection The accepted Socket connection.
         * @param myNode The associated JFlyNode for this ServerStyleThread.
         */
        public ServerStyleThread(Socket myAcceptedConnection, JFlyNode myNode)
        {
            super(myNode);
            try
            {
                //ServerStyleThreads accept already established socket connections.
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
    /**
     * This class is an extension of OneLinkThread that handles outgoing connections.
     */
    public static class ClientStyleThread extends OneLinkThread
    {
        protected Boolean threadQuesting = false;
        /**
         * The ClientStyleThread constructor.
         * @param params The connection parameter array, containing the connection IP address and port.
         * @param myNode The associated JFlyNode for this ClientStyleThread.
         * @param questing True/false value for whether this ClientStyleThread is a "questing" thread (attempting to reconnect to a disconnected node).
         */
        public ClientStyleThread(Object[] params, JFlyNode myNode, Boolean questing)
        {
            super(myNode);
            threadQuesting = questing;
            if(threadQuesting)
            {
                outputLock.lock();
                System.out.println("Opened quester thread.");
            }
            try
            {
                String ipAddr = (String)params[0];
                int port = (int)params[1];
                mySocket = new Socket();
                //ClientStyleThreads must open their own socket connections.
                InetSocketAddress mySockAddr = new InetSocketAddress(ipAddr, port);
                try
                {
                    mySocket.connect(mySockAddr, 4000);
                }
                catch(IOException e) { }
                //A ClientStyleThread can have the socket connection fail, and if so the application restarts.
                if(!mySocket.isConnected())
                {
                    if(threadQuesting)
                    {
                        outputLock.unlock();
                        stop(false);
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(myNode.getGUI(), "Could not connect to the specified IP...");
                        myNode.shutdownNode(true);
                    }
                }
                else
                {
                    inLine = new Scanner(mySocket.getInputStream());
                    outLine = new PrintWriter(mySocket.getOutputStream(), true);
                    if(threadQuesting)
                    {
                        OutputJobInfo quest = new OutputJobInfo(OutputJobInfo.JobType.SINGLE_DISPATCH, "Sending_reconnect_quester", "JFLYQUESTERREQUEST");
                        oneDispatch(quest);
                    }
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
        /**
         * The run override specific to ClientStyleThread. This calls the run() operation of the superclass unless the ClientStyleThread is questing.
         * When questing, the thread runs a reduced set of responses that are designed to handle quester responses. If the quester attempt is accepted, the superclass run() function is again called.
         */
        @Override
        public void run()
        {
            if(!threadQuesting) { super.run(); }
            else
            {
                //A questing thread has a reduced number of possible responses, designed to only handle quester responses and transients.
                while(inLine != null && inLine.hasNextLine())
                {
                    inputLock.lock();
                    try
                    {
                        String received = inLine.nextLine();
                        System.out.println("Data received: " + received);
                        String[] datParts = received.split(":~:", -1);
                        //Replies with ACKs to messages.
                        if(!datParts[0].equals("JFLYMSGACK"))
                        {
                            OutputJobInfo ack = new OutputJobInfo(OutputJobInfo.JobType.SINGLE_DISPATCH, "Response_ack_to:" + mySocket.getInetAddress().getHostAddress(), "JFLYMSGACK");
                            oneDispatch(ack);
                            OutputJobInfo quest = new OutputJobInfo(OutputJobInfo.JobType.SINGLE_DISPATCH, "Sending_reconnect_quester", "JFLYQUESTERREQUEST");
                            oneDispatch(quest);
                        }
                        switch(datParts[0])               
                        {
                            //Transients received are forwarded if allowed.
                            case "JFLYTRANSIENT":
                                handleTransient(datParts[1]);
                                if(jNode.allowTransientForwarding(datParts[1]))
                                {
                                    doPanthreadDispatch(datParts[1], "JFLYTRANSIENT");
                                }
                                break;
                            //A quester that receives a disconnect courtesy disconnects instantly.
                            case "JFLYDISCONNECTCOURTESY":
                                try
                                {
                                    stop(false);
                                }
                                catch(IOException e) {}
                                break;
                            //If the quester request has been accepted, the superclass run() is called and the OneLinkThread operates normally.
                            case "JFLYQUESTERRESPONSE":
                                jNode.crossNetworkSeekNode("", jNode.getNCS().getIDFromIP(mySocket.getInetAddress().getHostAddress()), false);
                                threadQuesting = false;
                                inputLock.unlock();
                                outputLock.unlock();
                                break;
                            default:
                                break;
                        }
                    }
                    finally { inputLock.unlock(); }
                    if(stopping || !threadQuesting) { break; }
                }
                super.run();
            }
        }
    }
}
