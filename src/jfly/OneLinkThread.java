/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfly;

import flyutils.TextUtility;
import flyutils.NetworkConfigurationState;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

/**
 * The OneLinkThread class is the abstract class for connection thread handling.
 * A given OneLinkThread manages data sent and received over a single socket connection and handles the various types of data that it receives, informing its associated JFlyNode when necessary.
 * @author Blackhole (dg7239p)
 */
public abstract class OneLinkThread implements Runnable
{
    /**
     * A RemoteBlockIntegrationException is thrown when the JFlyNode's BlockchainNodeManager is unable to integrate a newly received block into the blockchain.
     */
    public static class RemoteBlockIntegrationException extends Exception
    {
        /**
         * The FailureType enumeration details the type of integration failure that occurred.
         * MissingRemoteHashOnRequest: A previous blockchain block was requested by its hash, but the remote node could not locate it.
         * PostCascadeNonIntegration: After a retrieval cascade was performed to retrieve prior blockchain blocks, the original block could still not be integrated.
         */
        enum FailureType { MissingRemoteHashOnRequest, PostCascadeNonIntegration }
        FailureType myFailure;
        /**
         * The RemoteBlockIntegrationException constructor.
         * @param message The exception message.
         * @param type The failure type for this integration exception.
         */
        public RemoteBlockIntegrationException(String message, FailureType type)
        {
            super(message);
            myFailure = type;
        }
        /**
         * Get the failure type for this exception.
         * @return The failure type.
         */
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
    /**
     * The OneLinkThread constructor.
     * @param myNode The associated JFlyNode for this OneLinkThread. The OneLinkThread subclass instance should be an internal class for this instance.
     */
    public OneLinkThread(JFlyNode myNode)
    {
        jNode = myNode;
        myNode.registerThread(this);
    }
    protected Socket mySocket;
    /**
     * Gets the socket connection address of this OneLinkThread.
     * @return The socket connection address in the format IP:Port.
     */
    public String getConnectionAddr()
    {
        return mySocket.getInetAddress().getHostAddress() + ":" + mySocket.getPort();
    }
    protected int missed = 0;
    protected long markedCourtesy = -1;
    /**
     * Check to see when the last ack responses were received over this connection and attempts to seek the remote node if it has not been responding.
     */
    public void queryReplies()
    {
        //A "courtesy" indicates that the OneLinkThread has been told that the connection may have been remotely disconnected.
        //If after receiving a disconnect courtesy the thread doesn't receive data for 60 seconds, the courtesy is confirmed and the thread closes.
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
        //The recent dispatch log records recent messages sent on this thread.
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
            //If an acknowledgement for a sent message was not received, missed is incremented.
            if(JFlyNode.time() - timeSent > 5000) { missed++; }
            //Initially, a missed message only triggers queries.
            if(missed > 0 && missed < 3)
            {
                JFlyNode.OutputJobInfo missedJob = new JFlyNode.OutputJobInfo(JFlyNode.OutputJobInfo.JobType.SINGLE_DISPATCH, "Query-ack", "QUERYACK");
                oneDispatch(missedJob);
            }
            //If the remote node consistently does not respond, the local node is asked to try to contact the remote node by other means.
            else if(missed == 3)
            {
                missed = 4;
                if(!jNode.getNCS().getIDFromIP(mySocket.getInetAddress().getHostAddress()).equals("UNKNOWN_USER"))
                {
                    jNode.crossNetworkSeekNode("USERHASHID|" + jNode.getNCS().getIDFromIP(mySocket.getInetAddress().getHostAddress()));
                }
            }
        }
    }
    /**
     * Dispatches one new output job on this OneLinkThread's socket connection.
     * @param myJob The job to be dispatched.
     */
    public void oneDispatch(JFlyNode.OutputJobInfo myJob)
    {
        if(!mySocket.isConnected()) { return; }
        Boolean bypass = false;
        //The outputLock lock may be bypassed, if this is a call from within another output operation which already holds the lock.
        if(myJob.getType() == JFlyNode.OutputJobInfo.JobType.INTERNAL_LOCK && myJob.getToken() == outputLock) { bypass = true; }
        if(!bypass) { outputLock.lock(); }
        try
        {
            String outData = myJob.getHeader() + ":~:" + myJob.getData();
            System.out.println("Dispatching one: " + outData);
            outLine.println(outData);
            //For any data dispatch, the recentDispatchLog records the time, target and data.
            recentDispatchLog.add(JFlyNode.time() + ":~:" + getConnectionAddr() + ":~:" + outData);
        }
        finally
        {
            if(!bypass) { outputLock.unlock(); }
        }
    }
    /**
     * Dispatches a queue of output jobs on this OneLinkThread's socket connection in sequence.
     * @param myJobs The queue of jobs to be dispatched.
     */
    public void queueDispatch(Queue<JFlyNode.OutputJobInfo> myJobs)
    {
        for(JFlyNode.OutputJobInfo tji : myJobs)
        {
            oneDispatch(tji);
        }
    }
    /**
     * Dispatches a new data block to all OneLinkThreads for this node, except for this one.
     * @param block The data to be sent.
     * @param header The data header.
     */
    protected void doPanthreadDispatch(String block, String header)
    {
        JFlyNode.OutputJobInfo onForward = new JFlyNode.OutputJobInfo(JFlyNode.OutputJobInfo.JobType.MULTIPLE_DISPATCH, block, header);
        jNode.sendJobToThreads(onForward, new OneLinkThread[] { this });
    }
    /**
     * Sets a flag to require that the OneLinkThread connection will require the user to select a username and author a USER_JOINED block.
     */
    public void setDemandIntroduction()
    {
        introduction = false;
    }
    Boolean introduction = true;
    /**
     * Performs an appropriate handling operation for a received transient message.
     * @param transientBody The data contents of the transient message to be handled.
     */
    protected void handleTransient(String transientBody)
    {
        //A transient is a signalling message to be relayed or replied to which is not written to the blockchain.
        String[] brokenTransient = transientBody.split(Pattern.quote("+-+"));
        switch(brokenTransient[0])
        {
            //Response pings are checked so that if they are response to a local seek from this node, the seek can be concluded.
            case "responseping":
                jNode.crossNetworkSeekNode("JFLYTRANSIENT:~:" + transientBody);
                break;
            //If a transient indicates that another node is seeking this one, it is replied to.
            case "seeking":
                if(brokenTransient[1].equals(jNode.getUserID()))
                {
                    jNode.issueExistenceTransient();
                    jNode.allowTransientForwarding(transientBody);
                }
                break;
            //A transient can cause this node to attempt to directly contact another.
            case "forcecontact":
                jNode.attemptContact(brokenTransient[1]);
                break;
        }
    }
    /**
     * Handles the next received data package from the remote node on the socket connection for this OneLinkThread.
     * @param nextLine The next data received.
     * @throws jfly.OneLinkThread.RemoteBlockIntegrationException
     * @throws UnknownHostException 
     */
    protected void performNextLineOperation(String nextLine) throws RemoteBlockIntegrationException, UnknownHostException
    {
        performNextLineOperation(nextLine, false);
    }
    /**
     * Handles the next received data package from the remote node on the socket connection for this OneLinkThread.
     * @param nextLine The next data received.
     * @param recursive True/false flag for whether this call is a recursive one within another line handling operation.
     * @throws jfly.OneLinkThread.RemoteBlockIntegrationException
     * @throws UnknownHostException 
     */
    protected void performNextLineOperation(String nextLine, Boolean recursive) throws RemoteBlockIntegrationException, UnknownHostException
    {
        //The lock is only held and utility variables initialized if this is not a recursive call.
        if(!recursive)
        {
            markedCourtesy = -1;
            missed = 0;
            outputLock.lock();
            try
            {
                recentDispatchLog.clear();
            }
            finally { outputLock.unlock(); }
        }
        String[] datParts = nextLine.split(":~:", -1);
        //All messages are replied to with an acknowledgement, unless the message is itself an acknowledgement.
        if(!datParts[0].equals("JFLYMSGACK") && !recursive)
        {
            JFlyNode.OutputJobInfo ack = new JFlyNode.OutputJobInfo(JFlyNode.OutputJobInfo.JobType.SINGLE_DISPATCH, "Response_ack_to:" + mySocket.getInetAddress().getHostAddress(), "JFLYMSGACK");
            oneDispatch(ack);
        }
        switch(datParts[0])               
        {
            //Acknowledgements are checked in case the IP of this machine as seen by the remote node seems to have been changed.
            case "JFLYMSGACK":
                if(!datParts[1].split(Pattern.quote(":"))[1].equals(jNode.hostAddr()) && !datParts[1].split(Pattern.quote(":"))[1].equals(java.net.InetAddress.getLocalHost().getHostAddress()))
                {
                    //If this node has already introduced itself on the Blockchain and this address changes, then the node must flip to receive the new, internet connection.
                    //This facillitates JFly accepting socket connections over the internet if port forwarding has been set up on the router.
                    if(introduction)
                    {
                        NetworkConfigurationState.UserInfo oldMe = jNode.getNCS().getUserFromID(jNode.getUserID());
                        jNode.getBNM().authorBlock(BlockchainNodeManager.SharedStateBlock.ContentType.USER_LEFT, oldMe.toString());
                        String newIP = datParts[1].split(Pattern.quote(":"))[1];
                        NetworkConfigurationState.UserInfo newMe = new NetworkConfigurationState.UserInfo(newIP, "", oldMe.getUserName().replace(jNode.hostAddr(), newIP));
                        jNode.getBNM().authorBlock(BlockchainNodeManager.SharedStateBlock.ContentType.USER_JOINED, newMe.toString());
                        jNode.getBNM().authorBlock(BlockchainNodeManager.SharedStateBlock.ContentType.SYSTEM_UTIL, newMe.getUserName() + " has become an internetworked node.");
                    }
                    else
                    {
                        jNode.setDefaultAddress(datParts[1].split(Pattern.quote(":"))[1]);
                    }
                }
                outputLock.lock();
                try
                {
                    recentDispatchLog = new ArrayList<String>();
                    missed = 0;
                }
                finally { outputLock.unlock(); }
                break;
            //Transient messages are processed, and forwarded if allowed.
            //A transient message is a signalling message that is not written to the Blockchain.
            case "JFLYTRANSIENT":
                handleTransient(datParts[1]);
                if(jNode.allowTransientForwarding(datParts[1]))
                {
                    doPanthreadDispatch(datParts[1], "JFLYTRANSIENT");
                }
                break;
            //A disconnect courtesy indicates that this OneLinkThread's connection may have been terminated. To prevent fraudulent signalling, it is not acted upon until a timeout completes.
            case "JFLYDISCONNECTCOURTESY":
                markedCourtesy = JFlyNode.time();
                break;
            //A quester request is sent if another node is attempting to reconnect to a missing node via the "quester" system.
            case "JFLYQUESTERREQUEST":
                //Only one quester at a time should be accepted.
                if(!jNode.queryAcceptQuester(mySocket.getInetAddress().getHostAddress()))
                {
                    //However, if a quester is denied, the node should be told to attempt to contact it to make sure it still exists on the network.
                    JFlyNode.OutputJobInfo turnDownQuester = new JFlyNode.OutputJobInfo(JFlyNode.OutputJobInfo.JobType.SINGLE_DISPATCH, "quester_response_disconnect_courtesy", "JFLYDISCONNECTCOURTESY");
                    oneDispatch(turnDownQuester);
                    if(!jNode.getNCS().getIDFromIP(mySocket.getInetAddress().getHostAddress()).equals("UNKNOWN_USER"))
                    {
                        jNode.crossNetworkSeekNode("USERHASHID|" + jNode.getNCS().getIDFromIP(mySocket.getInetAddress().getHostAddress()));
                    }
                }
                else
                {
                    //A quester request can also be accepted.
                    JFlyNode.OutputJobInfo acceptQuester = new JFlyNode.OutputJobInfo(JFlyNode.OutputJobInfo.JobType.SINGLE_DISPATCH, "quester_response_accepted_notice", "JFLYQUESTERRESPONSE");
                    oneDispatch(acceptQuester);
                    //Accepting a quester requires that an existence transient be issued.
                    jNode.issueExistenceTransient();
                }
                break;
            //JFly chain block messages contain data to be written to the blockchain.
            case "JFLYCHAINBLOCK":                  
                handleNewBlock(nextLine, datParts, recursive);
                break;
            //Requests to reply with a block from the blockchain can also be sent.
            case "JFLYCHAINBLOCKREQUEST":
                String search = jNode.pullOneBlockByHash(datParts[1]);
                JFlyNode.OutputJobInfo requestResponseJob = new JFlyNode.OutputJobInfo(JFlyNode.OutputJobInfo.JobType.SINGLE_DISPATCH, datParts[1] + ":~:" + search, "JFLYCHAINBLOCKRESPONSE");
                oneDispatch(requestResponseJob);
                break;
            //Responses to chain block requests should only be received while handling the integration of a chain block, and should not happen here unless an error has occurred.
            case "JFLYCHAINBLOCKRESPONSE":
                System.out.println("Warning: Received chain block request response outside of any request task...");
                break;
            //Responses to quester requests should only be received while a node is questing, and should not happen here unless an error has occurred.
            case "JFLYQUESTERRESPONSE":
                System.out.println("Warning: Received quester request response while not questing...");
                break;
            default:
                break;
        }
        //Receiving new data prompts the thread to tell the node to update its chat window.
        jNode.updateChatWindow();
    }
    LinkedList<String> receivedDuringBlocking = new LinkedList<String>();
    /**
     * Interfaces with this OneLinkThread's associated JFlyNode's BlockChainManager in order to integrate a newly received blockchain block into the local blockchain.
     * @param nextLine The full received data.
     * @param datParts The block data parcel split into segments.
     * @throws jfly.OneLinkThread.RemoteBlockIntegrationException
     * @throws UnknownHostException 
     */
    protected void handleNewBlock(String nextLine, String[] datParts) throws RemoteBlockIntegrationException, UnknownHostException
    {
        handleNewBlock(nextLine, datParts, false);
    }
    /**
     * Interfaces with this OneLinkThread's associated JFlyNode's BlockChainManager in order to integrate a newly received blockchain block into the local blockchain.
     * @param nextLine The full received data.
     * @param datParts The block data parcel split into segments.
     * @param recursive True/false flag for whether this call is a recursive one within another line handling operation.
     * @throws jfly.OneLinkThread.RemoteBlockIntegrationException
     * @throws UnknownHostException 
     */
    protected void handleNewBlock(String nextLine, String[] datParts, Boolean recursive) throws RemoteBlockIntegrationException, UnknownHostException
    {
        if(!recursive) { receivedDuringBlocking = new LinkedList<String>(); }
        //When a new block is received the OneLinkThread asks the node to write it into its blockchain.
        String result = jNode.tryOneBlock(datParts[1]);
        //If block integration fails, the previous block from the thread's remote node needs to be requested.
        if(result.equals("FAILED_REQUEST_PREVIOUS"))
        {
            outputLock.lock();
            try
            {
                //A request for the previous block is sent.
                JFlyNode.OutputJobInfo prevReqJob = new JFlyNode.OutputJobInfo(JFlyNode.OutputJobInfo.JobType.INTERNAL_LOCK, datParts[1].split("|")[0], "JFLYCHAINBLOCKREQUEST");
                prevReqJob.setToken(outputLock);
                oneDispatch(prevReqJob);
                //The input stream is coopted to monitor for the block request response.
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
                        //When the remote block is received it is attempted to be integrated with a recursive call to performNextLineOperation().
                        else { performNextLineOperation("JFLYCHAINBLOCK:~:" + responseParts[2], true); }
                        break;
                    }
                    //Any other data received during this is stored to later be processed.
                    else { receivedDuringBlocking.add(received); }
                }
                String secondResult = jNode.tryOneBlock(datParts[1]);
                if(!secondResult.contains("SUCCESSFULLY_INTEGRATED")) { throw new RemoteBlockIntegrationException(secondResult, RemoteBlockIntegrationException.FailureType.PostCascadeNonIntegration); }
                else
                {
                    //If the introduction flag is not set, then this user still needs to choose a username and publish a USER_JOINED block.
                    if(!introduction)
                    {
                        jNode.setLocalUsername(TextUtility.sanitizeText(JOptionPane.showInputDialog(null, "Choose a username!", "Input username", JOptionPane.INFORMATION_MESSAGE)));
                        if(jNode.getLocalUsername() == null || jNode.getLocalUsername().isEmpty())
                        {
                            jNode.setLocalUsername("IP User " + jNode.hostAddr());
                        }
                        NetworkConfigurationState.UserInfo me = new NetworkConfigurationState.UserInfo(jNode.hostAddr(), "", jNode.getLocalUsername());
                        jNode.getBNM().authorBlock(BlockchainNodeManager.SharedStateBlock.ContentType.USER_JOINED, me.toString());
                        introduction = true;
                    }
                    doPanthreadDispatch(jNode.getBNM().getByHash(result.replace("SUCCESSFULLY_INTEGRATED:", "")), datParts[0]);
                }
            }
            finally { outputLock.unlock(); }
        }
        else if(result.contains("SUCCESSFULLY_INTEGRATED"))
        {
            //If the introduction flag is not set, then this user still needs to choose a username and publish a USER_JOINED block.
            if(!introduction)
            {
                jNode.setLocalUsername(TextUtility.sanitizeText(JOptionPane.showInputDialog(null, "Choose a username!", "Input username", JOptionPane.INFORMATION_MESSAGE)));
                if(jNode.getLocalUsername() == null || jNode.getLocalUsername().isEmpty())
                {
                    jNode.setLocalUsername("IP User " + jNode.hostAddr());
                }
                NetworkConfigurationState.UserInfo me = new NetworkConfigurationState.UserInfo(jNode.hostAddr(), "", jNode.getLocalUsername());
                jNode.getBNM().authorBlock(BlockchainNodeManager.SharedStateBlock.ContentType.USER_JOINED, me.toString());
                introduction = true;
            }
            doPanthreadDispatch(jNode.getBNM().getByHash(result.replace("SUCCESSFULLY_INTEGRATED:", "")), datParts[0]);
        }
        //Any data received while waiting on a response is now properly processed.
        while(!recursive && receivedDuringBlocking.size() > 0)
        {
            performNextLineOperation(receivedDuringBlocking.pop());
        }
    }
    Boolean nameSet = false;
    /**
     * The override for the run() method in Runnable. This contains the loop that receives incoming socket data.
     */
    @Override
    public void run()
    {
        if(!nameSet)
        {
            Thread.currentThread().setName("Socket read thread");
            nameSet = true;
        }
        //Data is repeatedly requested from the socket.
        while(inLine != null && inLine.hasNextLine())
        {
            inputLock.lock();
            try
            {
                String received = inLine.nextLine();
                System.out.println("Data received: " + received);
                try
                {
                    try
                    {
                        //Received data is handled in this method.
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
    /**
     * Stops this OneLinkThread's run() operation and unregisters it from its associated JFlyNode().
     * @param skipBlockUnregister True/false value for whether the lock blocking should be skipped while unregistering the OneLinkThread.
     * @throws IOException 
     */
    public void stop(Boolean skipBlockUnregister) throws IOException
    {
        stopping = true;
        //A OneLinkThread sends a disconnect message to the remote node as a courtesy when stopped.
        JFlyNode.OutputJobInfo disCourt = new JFlyNode.OutputJobInfo(JFlyNode.OutputJobInfo.JobType.SINGLE_DISPATCH, "stopping_disconnect_courtesy", "JFLYDISCONNECTCOURTESY");
        oneDispatch(disCourt);
        //The Thread's socket connection must be shut down, and the Thread unregistered.
        mySocket.close();
        if(inLine != null) { inLine.close(); }
        jNode.unregisterThread(this, skipBlockUnregister);
    }
}
