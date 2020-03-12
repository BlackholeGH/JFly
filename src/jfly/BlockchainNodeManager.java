/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfly;

import flyutils.NetworkConfigurationState;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.Stack;
import java.util.Random;
import java.util.Date;
import java.util.ArrayList;
import java.util.regex.Pattern;


/**
 * The BlockchainNodeManager class is responsible for maintaining, writing to, and reading from the local JFly blockchain.
 * @author Blackhole (dg7239p)
 */
public class BlockchainNodeManager {
    private HashMap sharedStateBlocks = new HashMap();
    private Stack<String> hashChain = new Stack<String>();
    private JFlyNode myNode = null;
    private ArrayList<String> validRegistrars = new ArrayList();
    /**
     * Returns the hash of the last block that was written.
     * @return 
     */
    public String lastHash()
    {
        if(hashChain.size() > 0) { return hashChain.peek(); }
        else { return ""; }
    }
    /**
     * Checks whether or not the hash of a given registrar block is included in the internal list of valid registrars to read data from.
     * @param hash The hash of the registrar block.
     * @return True/false value for whether the registrar block is recorded as valid.
     */
    private Boolean validRegistrarHash(String hash)
    {
        for(String h : validRegistrars)
        {
            if(h.equals(hash)) { return true; }
        }
        return false;
    }
    /**
     * Reads from the blockchain to update the contents of a NetworkConfigurationState.
     * @param cur The NetworkConfigurationState to update.
     * @param depth The depth to which the blockchain should be read to update the NetworkConfigurationState. Set to -1 to read the entire blockchain.
     */
    public void calculateConfigs(NetworkConfigurationState cur, int depth)
    {
        ArrayList<NetworkConfigurationState.UserInfo> newUsers = cur.getUsers();
        Stack<String> hashClone = (Stack<String>)hashChain.clone();
        //A negative depth means the whole blockchain will be scanned.
        if(depth < 0)
        {
            depth = hashClone.size();
            newUsers.clear();
        }
        Stack<String> hashCloneReOrder = new Stack<String>();
        //The hash list is referenced in order to retrieve the blocks. The order of hashes is inverted to retrieve blocks in chronological order.
        for(int i = 0; i < depth; i++)
        {
            hashCloneReOrder.add(hashClone.pop());
        }
        for(int i = 0; i < depth; i++)
        {
            SharedStateBlock current = (SharedStateBlock)sharedStateBlocks.get(hashCloneReOrder.pop());
            //A GROUP_REGISTRAR block is only read from if it is marked as "valid", in practice being only the initial initialization registrar received by this JFly node.
            if(current.getContentType() == SharedStateBlock.ContentType.GROUP_REGISTRAR && validRegistrarHash(current.getHash()))
            {
                //A GROUP_REGISTRAR block contains data for all users on the network at the time that it was published.
                String[] regiUsers = current.getContentData().split(Pattern.quote("/-/"), -1);
                for(String usr : regiUsers)
                {
                    NetworkConfigurationState.UserInfo newUser = NetworkConfigurationState.UserInfo.fromString(usr);
                    newUsers.add(newUser);
                }
            }
            //Parsing a USER_JOINED block adds a newly joined user to the list of users.
            //The individual user ID for a given user will be a hash of the hash of their USER_JOINED block, linking their identity intrisically to the blockchain.
            else if(current.getContentType() == SharedStateBlock.ContentType.USER_JOINED)
            {
                NetworkConfigurationState.UserInfo newUser = NetworkConfigurationState.UserInfo.fromString(current.getContentData());
                if(newUser == null) { continue; }
                newUser.setID(SharedStateBlock.getHash(current.getHash()));
                newUsers.add(newUser);
            }
            //Parsing a USER_JOINED block removes a user who has left from the list of users.
            else if(current.getContentType() == SharedStateBlock.ContentType.USER_LEFT)
            {
                NetworkConfigurationState.UserInfo newUser = NetworkConfigurationState.UserInfo.fromString(current.getContentData());
                if(newUser == null) { continue; }
                NetworkConfigurationState.UserInfo oldUser = null;
                for(NetworkConfigurationState.UserInfo u : newUsers)
                {
                    if(u.getID().equals(newUser.getID()))
                    {
                        oldUser = u;
                        break;
                    }
                }
                if(oldUser != null)
                {
                    newUsers.remove(oldUser);
                    //If a user has left the cluster, then any attempts to contact them must be cancelled with a seeker purge operation on the JFlyNode.
                    myNode.crossNetworkSeekNode("", oldUser.getID(), false);
                }
            }
        }
        //The ammended user list is written to this node's NetworkConfigurationState.
        cur.reWriteAll(newUsers);
        FlyChatGUI myGUI = myNode.getGUI();
        if(myGUI != null)
        {
            //The user list on the GUI is updated after these config states are updated.
            myGUI.updateTable(cur.getTableData());
        }
        System.out.println("Network configuration database updated (Depth of " + depth + "): ");
        System.out.println(cur);
    }
    /**
     * Reads from the blockchain to get the last x messages that should be displayed.
     * @param depth The depth to which the blockchain should be read to retrieve messages.
     * @return A String array of messages to be displayed.
     */
    public String[] getLast(int depth)
    {
        ArrayList<String> msgs = new ArrayList<String>();
        if(depth > hashChain.size()) { depth = hashChain.size(); }
        Stack<String> hashClone = (Stack<String>)hashChain.clone();
        Stack<String> hashCloneReOrder = new Stack<String>();
        //The hash list is referenced in order to retrieve the blocks. The order of hashes is inverted to retrieve blocks in chronological order.
        for(int i = 0; i < depth; i++)
        {
            hashCloneReOrder.add(hashClone.pop());
        }
        for(int i = 0; i < depth; i++)
        {
            SharedStateBlock current = (SharedStateBlock)sharedStateBlocks.get(hashCloneReOrder.pop());
            //MESSAGE blocks are displayed with the author via username lookup, author time and message text.
            if(current.getContentType() == SharedStateBlock.ContentType.MESSAGE)
            {
                Date mDate = new Date(current.getCreationTime());
                DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
                msgs.add(myNode.getNCS().getUserNameFromID(current.getOUID()) + " (" + dateFormat.format(mDate) + ") : " + current.getContentData());
            }
            //User joining notifications are also displayed in the chat window.
            else if(current.getContentType() == SharedStateBlock.ContentType.USER_JOINED)
            {
                Date mDate = new Date(current.getCreationTime());
                msgs.add(myNode.getNCS().getUserNameFromID(SharedStateBlock.getHash(current.getHash())) + " joined this cluster at " + mDate.toString() + ".");
            }
            //User leaving notifications are also displayed in the chat window.
            else if(current.getContentType() == SharedStateBlock.ContentType.USER_LEFT)
            {
                //Date mDate = new Date(current.getCreationTime());
                msgs.add((current.getContentData().split(Pattern.quote("+-+")))[1] + " left this cluster.");
            }
            //System utility messages are also displayed in the chat window.
            else if(current.getContentType() == SharedStateBlock.ContentType.SYSTEM_UTIL)
            {
                msgs.add(current.getContentData());
            }
        }
        String[] out = new String[msgs.size()];
        //The retrieved message display Strings are returned as an array.
        for(int i = 0; i < out.length; i++)
        {
            out[i] = msgs.get(i);
        }
        return out;
    }
    /**
     * Gets the associated JFlyNode for this BlockchainNodeManager.
     * @return The associated JFlyNode.
     */
    public JFlyNode getJNode()
    {
        return myNode;
    }
    /**
     * The BlockchainNodeManager constructor.
     * @param associatedNode The associated JFlyNode for this BlockchainNodeManager.
     */
    public BlockchainNodeManager(JFlyNode associatedNode)
    {
        myNode = associatedNode;
    }
    /**
     * Attempts to author a new block on this blockchain.
     * @param newContentType The content type of the new block to be authored.
     * @param newContentData The data of the new block to be authored.
     */
    public void authorBlock(SharedStateBlock.ContentType newContentType, String newContentData)
    {
        System.out.println("Attempting to author a new block: " + newContentType.toString() + " : " + newContentData);
        //A new block is created with the data for an attempted author operation.
        SharedStateBlock newBlock = new SharedStateBlock(this, newContentType, newContentData, lastHash());
        //We attempt to add the new block to the blockchain.
        Object[] authorRes = addExtantBlockToChain(newBlock.toString());
        int adder = (int)authorRes[0];
        //If the block was successfully authored and integrated, it is forwarded to all other connected nodes.
        if(adder == 0 || adder == 1)
        {
            JFlyNode.OutputJobInfo afterAuthorJob = new JFlyNode.OutputJobInfo(JFlyNode.OutputJobInfo.JobType.MULTIPLE_DISPATCH, newBlock.toString(), "JFLYCHAINBLOCK");
            myNode.sendJobToThreads(afterAuthorJob, null);
            //If the newly authored block is a USER_JOINED block, then the IP of this node's user is confirmed and written to the JFlyNode.
            if(newBlock.getContentType() == SharedStateBlock.ContentType.USER_JOINED)
            {
                NetworkConfigurationState.UserInfo authoredUser = NetworkConfigurationState.UserInfo.fromString(newBlock.getContentData());
                myNode.resetAddress(authoredUser.getIP(), this);
            }
        }
        else
        {
            System.out.println("Block author failed with code: " + adder);
        }
        //Authoring a new block updates the chat window.
        System.out.println("Block authored!");
        myNode.updateChatWindow();
        //calculateConfigs(myNode.getNCS(), 1);
    }
    /**
     * Returns a random SharedStateBlock hash value.
     * @return The random hash value.
     */
    public String tryOneHash()
    {
        //This function is useful for testing the hashing function.
        Random rnd = new Random();
        return (new SharedStateBlock(this, rnd.nextDouble() + "")).getHash();
    }
    /**
     * Retrieves the contents of a blockchain block, indexed by its hash.
     * @param hash The hash of the block to retrieve.
     * @return 
     */
    public String getByHash(String hash)
    {
        if(sharedStateBlocks.containsKey(hash)) { return ((SharedStateBlock)sharedStateBlocks.get(hash)).toString(); }
        else { return null; }
    }
    private int lastDepth = 0;
    private int registrarTolerance = 0;
    /**
     * Increase the number of registrar blocks that the BlockchainNodeManager can accept to be valid to read from.
     * @param incr The degree to which the registrar tolerance number should be increased.
     */
    public void addRegistrarTolerance(int incr)
    {
        registrarTolerance += incr;
    }
    /**
     * Attempts to add an existing blockchain data block to the local blockchain.
     * @param blockData The data of the extant block to add.
     * @return The result of the attempted adding operation:
     * 0: Added as expected
     * 1: Was added but BlockChain was empty?
     * 2: Failed to add, no matching PrevHash in chain, request more blocks
     * 3: Couldn't construct new block. Verification failed?
     * 4: Block already present or hash collision
     */
    public Object[] addExtantBlockToChain(String blockData)
    {
        SharedStateBlock extBlock = new SharedStateBlock(this);
        try
        {
            extBlock = extBlock.getOneBlock(blockData);
        }
        catch(SharedStateBlock.StateBlockVerificationException e) {};
        if(extBlock != null)
        {
            if(hashChain.size() > 0)
            {
                //A block is not added to the chain if the chain already seems to contain it (derived from hash).
                if(sharedStateBlocks.containsKey(extBlock.getHash()))
                {
                    calculateConfigs(myNode.getNCS(), lastDepth);
                    lastDepth = 0;
                    return new Object[] { 4, extBlock.getHash() };
                }
                String lastBlockHash = lastHash();
                Stack<SharedStateBlock> poppedBlocks = new Stack<SharedStateBlock>();
                //If the block to be added does not follow on from the previous block (by hash record), we check whether it should be placed further down in the blockchain.
                //The depth to which a block can be inserted could be limited as desired to prevent retroactive blockchain modification.
                while(!extBlock.getLastBlockHash().equals(lastBlockHash) || lastBlockHash.length() == 0)
                {
                    //The block hashes are retrieved in an attempt to locate an insertion point.
                    System.out.println("Popping block...");
                    if(hashChain.size() == 1)
                    {
                        for(int i = poppedBlocks.size(); i > 0; i--)
                        {
                            lastDepth++;
                            SharedStateBlock curReInsert = poppedBlocks.pop();
                            lastBlockHash = curReInsert.getHash();
                            hashChain.add(lastBlockHash);
                            sharedStateBlocks.put(lastBlockHash, curReInsert);
                        }
                        calculateConfigs(myNode.getNCS(), lastDepth);
                        lastDepth = 0;
                        //If the previous hash of the block to be added is not in the blockchain, this method returns and requests the missing previous block.
                        return new Object[] { 2, extBlock.getHash() };
                    }
                    if(lastBlockHash.length() > 0)
                    {
                        String toPop = hashChain.pop();
                        poppedBlocks.add((SharedStateBlock)sharedStateBlocks.get(toPop));
                        sharedStateBlocks.remove(toPop);
                    }
                    lastBlockHash = hashChain.peek();
                }
                Boolean reinsertTriggered = false;
                //If blocks have been popped to reinsert the block further down in the blockchain, then they must be sorted through to place the block in the correct location chronologically.
                //This is because this sort of discrepancy means that the blockchain has temporarily forked on two different nodes. The two forks must be re-merged as per their author times.
                if(poppedBlocks.size() > 0)
                {
                    System.out.println("Performing retrieval chain (ebct = " + extBlock.getCreationTime() + "):");
                    reinsertTriggered = true;
                    Boolean extPut = false;
                    for(int i = poppedBlocks.size(); i > 0; i--)
                    {
                        SharedStateBlock curReInsert = poppedBlocks.pop();
                        String comparisonHash = "";
                        if(!extPut)
                        {
                            extBlock.setLastBlockHash("");
                            comparisonHash = extBlock.getHash();
                            extBlock.setLastBlockHash(lastBlockHash);
                        }
                        curReInsert.setLastBlockHash(lastBlockHash);
                        //The block is added where it's author time fits in chronologically.
                        //If there is a time collision, then the order of insertion depends on the alphabetical order of the hashes of the block data.
                        if(!extPut && ((extBlock.getCreationTime() < curReInsert.getCreationTime()) || (extBlock.getCreationTime() == curReInsert.getCreationTime() && comparisonHash.compareTo(curReInsert.getHash()) < 0)))
                        {
                            System.out.println("Putting extblock");
                            lastDepth++;
                            lastBlockHash = extBlock.getHash();
                            hashChain.add(lastBlockHash);
                            sharedStateBlocks.put(lastBlockHash, extBlock);
                            curReInsert.setLastBlockHash(lastBlockHash);
                            extPut = true;
                            System.out.println(extBlock.toString());
                        }
                        if(!extPut && comparisonHash.compareTo(curReInsert.getHash()) == 0) { System.out.println("Warning: Unexpected hash collision during blockchain insertion(?!)."); }
                        lastDepth++;
                        lastBlockHash = curReInsert.getHash();
                        hashChain.add(lastBlockHash);
                        sharedStateBlocks.put(lastBlockHash, curReInsert);
                        System.out.println(curReInsert.toString());
                    }
                    //If the new block has still not yet been added, it is added.
                    if(!extPut)
                    {
                        System.out.println("Putting extblock");
                        extBlock.setLastBlockHash(lastBlockHash);
                        lastDepth++;
                        lastBlockHash = extBlock.getHash();
                        hashChain.add(lastBlockHash);
                        sharedStateBlocks.put(lastBlockHash, extBlock);
                        extPut = true;
                        System.out.println(extBlock.toString());
                    }
                }
                //If the block did not need to be retroactively reinserted, then the new block is simply added to the chain.
                else
                {
                    lastDepth++;
                    lastBlockHash = extBlock.getHash();
                    hashChain.add(lastBlockHash);
                    sharedStateBlocks.put(lastBlockHash, extBlock);
                }
                //If the tolerance value allows it, then a newly integrated GROUP_REGISTRAR is marked as valid to be read from to receive configs (i.e. if it is the initializing registrar block)
                if(extBlock.getContentType() == SharedStateBlock.ContentType.GROUP_REGISTRAR && registrarTolerance > 0)
                {
                    validRegistrars.add(extBlock.getHash());
                    System.out.println("Added valid registrar");
                    registrarTolerance--;
                }
                //Adding a new block causes Network Config state to be recalculated. lastDepth indicates the number of new blocks to be scanned.
                //If a reinsert occurred, then the configs are calculated from the entire blockchain in order to prevent repeated processing.
                calculateConfigs(myNode.getNCS(), reinsertTriggered ? -1 : lastDepth);
                lastDepth = 0;
                if(reinsertTriggered) { System.out.println("Finished retrival chaining and putting..."); }
                return new Object[] { 0, extBlock.getHash() };
            }
            //If the blockchain does not yet contain any blocks, the new block is written immediately (to initialize this node).
            else
            {
                String extBlockHash = extBlock.getHash();
                lastDepth++;
                hashChain.add(extBlockHash);
                sharedStateBlocks.put(extBlockHash, extBlock);
                //If the tolerance value allows it, then a newly integrated GROUP_REGISTRAR is marked as valid to be read from to receive configs (i.e. if it is the initializing registrar block)
                if(extBlock.getContentType() == SharedStateBlock.ContentType.GROUP_REGISTRAR && registrarTolerance > 0)
                {
                    validRegistrars.add(extBlockHash);
                    System.out.println("Added valid registrar");
                    registrarTolerance--;
                }
                //Adding a new block causes Network Config state to be recalculated. lastDepth indicates the number of new blocks to be scanned.
                //If a reinsert occurred, then the configs are calculated from the entire blockchain in order to prevent repeated processing.
                calculateConfigs(myNode.getNCS(), lastDepth);
                lastDepth = 0;
                return new Object[] { 1, extBlock.getHash() };
            }
        }
        else
        {
            //Adding a new block causes Network Config state to be recalculated. lastDepth indicates the number of new blocks to be scanned.
            //If a reinsert occurred, then the configs are calculated from the entire blockchain in order to prevent repeated processing.
            calculateConfigs(myNode.getNCS(), lastDepth);
            lastDepth = 0;
            return new Object[] { 3, extBlock.getHash() };
        }
    }
    /**
     * The SharedStateBlock class is a class that represents the contents of a single blockchain block.
     */
    public static class SharedStateBlock
    {
        /**
         * A StateBlockVerificationException is thrown if a newly reconstructed SharedStateBlock from received String data does not properly verify its hash.
         */
        public static class StateBlockVerificationException extends Exception
        {
            public StateBlockVerificationException(String message)
            {
                super(message);
            }
        }
        /**
         * The ContentType enumeration details the type of content that this SharedStateBlock contains.
         * MESSAGE: A chat message.
         * USER_JOINED: A user joining the JFly cluster.
         * USER_LEFT: A user leaving the JFly cluster.
         * SYSTEM_UTIL: A system message.
         * GENESIS: The initial source block for a JFly blockchain.
         * GROUP_REGISTRAR: Contains current user data for everyone in the cluster, authored when a new user joins.
         */
        public static enum ContentType
        {
            MESSAGE, USER_JOINED, USER_LEFT, SYSTEM_UTIL, GENESIS, GROUP_REGISTRAR;
            /**
             * Returns a ContentType value from its equivalent String representation.
             * @param cTypeStr The String representation of a ContentType value.
             * @return The equivalent ContentType value.
             */
            public static ContentType fromString(String cTypeStr)
            {
                switch(cTypeStr.toUpperCase())
                {
                    case "MESSAGE":
                        return MESSAGE;
                    case "USER_JOINED":
                        return USER_JOINED;
                    case "USER_LEFT":
                        return USER_LEFT;
                    case "SYSTEM_UTIL":
                        return SYSTEM_UTIL;
                    case "GENESIS":
                        return GENESIS;
                    case "GROUP_REGISTRAR":
                        return GROUP_REGISTRAR;
                }
                return null;
            }
        }
        String localPrevBlockHash = "";
        ContentType contentType;
        String contentData = "";
        String originatingUserID = "";
        long updateTime = 0;
        BlockchainNodeManager myManager;
        /**
         * The SharedStateBlock constructor.
         * @param bnManager The owning BlockchainNodeManager for this SharedStateBlock instance.
         */
        public SharedStateBlock(BlockchainNodeManager bnManager)
        {
            myManager = bnManager;
        }
        /**
         * The SharedStateBlock constructor.
         * @param bnManager The owning BlockchainNodeManager for this SharedStateBlock instance.
         * @param prevHash The hash of the previous block in the chain.
         */
        public SharedStateBlock(BlockchainNodeManager bnManager, String prevHash)
        {
            myManager = bnManager;
            localPrevBlockHash = prevHash;
        }
        /**
         * The SharedStateBlock constructor.
         * @param bnManager The owning BlockchainNodeManager for this SharedStateBlock instance.
         * @param newContentType The COntentType of the content of this SharedStateBlock.
         * @param newContentData The data contained within this SharedStateBlock.
         * @param prevHash The hash of the previous block in the chain.
         */
        public SharedStateBlock(BlockchainNodeManager bnManager, ContentType newContentType, String newContentData, String prevHash)
        {
            myManager = bnManager;
            localPrevBlockHash = prevHash;
            contentType = newContentType;
            Date createDate = new Date(JFlyNode.time());
            //A JFly blockchain genesis block contains default data to initialize the blockchain.
            if(newContentType == ContentType.GENESIS)
            {
                contentData = "JFly blockchain GENESIS block. New cluster was created at " + createDate.toString() + ". #BIGDUCKROBOT";
            }
            else
            {
                contentData = newContentData;
                originatingUserID = myManager.getJNode().getUserID();
            }
            updateTime = createDate.getTime();          
        }
        /**
         * The initial hashing algorithm for JFly blockchain blocks.
         * This hashing operation is repeated to get the "true" hash value for some data.
         * @param temp The initial data to hash.
         * @return The hashed data.
         */
        public static String getRawHash(String temp)
        {          
            int blockSize = 0;
            for(int i = 0; i < temp.length(); i++)
            {
                blockSize += Integer.bitCount((int)temp.charAt(i));
            }
            blockSize = (blockSize % 8) + 3;
            int rolling = blockSize;
            int tempTotal = 0;
            String collate = "";
            //The hashing function is based on repeated modulus arithmatic on various totals derived from the char values of the data.
            do
            {
                blockSize++;
                for(char c : temp.toCharArray())
                {
                    rolling--;
                    if(rolling > 0)
                    {
                        tempTotal += c;
                    }
                    else
                    {
                        char nConstr = (char)((tempTotal % 90) + 33);
                        collate = collate + nConstr;
                        tempTotal = c;
                        rolling = blockSize;
                    }
                }
            }
            while(collate.length() < 32);
            collate = collate.substring(0, 32);
            return collate;
        }
        /**
         * Sets the last block's hash value for this SharedStateBlock.
         * @param prevHash The hash of the previous block to be set.
         */
        public void setLastBlockHash(String prevHash)
        {
            localPrevBlockHash = prevHash;
        }
        /**
         * Gets the author time for the blockchain block this SharedStateBlock represents.
         * @return The author time as a long, as per the Unix epoch time.
         */
        public long getCreationTime()
        {
            return updateTime;
        }
        /**
         * Gets the content type for the blockchain block this SharedStateBlock represents.
         * @return The content type.
         */
        public ContentType getContentType()
        {
            return contentType;
        }
        /**
         * Gets the data for the blockchain block this SharedStateBlock represents.
         * @return The SharedStateBlock's data.
         */
        public String getContentData()
        {
            return contentData;
        }
        /**
         * Gets the hash ID of the user who authored the blockchain block this SharedStateBlock represents.
         * @return The authoring user's hash ID.
         */
        public String getOUID()
        {
            return originatingUserID;
        }
        /**
         * Gets the hash of the block preceding on the blockchain the blockchain block this SharedStateBlock represents.
         * @return The hash of the preceding block.
         */
        public String getLastBlockHash()
        {
            return localPrevBlockHash;
        }
        /**
         * Generates the true hash of the blockchain block this SharedStateBlock represents.
         * @return The true hash of this block.
         */
        public String getHash()
        {
            return getRawHash(getRawHash(getRawHash(myDat())));
        }
        /**
         * Generates a true hash of any String data.
         * @param data The data to be hashed.
         * @return The hash of the data.
         */
        public static String getHash(String data)
        {
            return getRawHash(getRawHash(getRawHash(data)));
        }
        /**
         * Creates a string representation of the data contained within this SharedStateBlock.
         * @return A String representing the data contained within this SharedStateBlock. 
         */
        private String myDat()
        {
            return localPrevBlockHash +
                    "|" + updateTime +
                    "|" + originatingUserID +
                    "|" + contentType.toString() +
                    "|" + contentData;
        }
        /**
         * toString() override that converts this SharedStateBlock to a string representation in the form Data|Hash.
         * @return The String representation of this SharedStateBlock.
         */
        @Override
        public String toString()
        {
            return myDat() + "|" + getHash();
        }
        /**
         * Initializes this SharedStateBlock to data received in String format.
         * @param initData The data to be used to initialize this SharedStateBlock.
         * @return The hash value included in the initializing data, for verification purposes.
         */
        public String selfInitialize(String initData)
        {
            String[] dataSegs = initData.split("\\|", -1);
            localPrevBlockHash = dataSegs[0];
            updateTime = Long.parseLong(dataSegs[1]);
            originatingUserID = dataSegs[2];
            contentType = ContentType.fromString(dataSegs[3]);
            contentData = dataSegs[4];
            return dataSegs[5];
        }
        /**
         * Creates a new SharedStateBlock and initializes it from given String initialization data, verifying it against its hash.
         * The new block's owning BlockchainNodeManager will be the same as the SharedStateBlock that this was called upon.
         * @param initData The initialization data.
         * @return The newly created SharedStateBlock.
         * @throws jfly.BlockchainNodeManager.SharedStateBlock.StateBlockVerificationException 
         */
        public SharedStateBlock getOneBlock(String initData) throws StateBlockVerificationException
        {
            return getOneBlock(myManager, initData);
        }
        /**
         * Creates a new SharedStateBlock and initializes it from given String initialization data, verifying it against its hash.
         * @param rqManager The BlockchainNodeManager that should own the new SharedStateBlock.
         * @param initData The initialization data.
         * @return The newly created SharedStateBlock.
         * @throws jfly.BlockchainNodeManager.SharedStateBlock.StateBlockVerificationException 
         */
        public static SharedStateBlock getOneBlock(BlockchainNodeManager rqManager, String initData) throws StateBlockVerificationException
        {
            SharedStateBlock oneBlock = new SharedStateBlock(rqManager);
            String queryHash = oneBlock.selfInitialize(initData);
            if(queryHash.equals(oneBlock.getHash()))
            {
                return oneBlock;
            }
            else
            {
                throw new StateBlockVerificationException("Received SharedStateBlock failed hash verification and could be corrupted.");
            }
        }
    }
}
