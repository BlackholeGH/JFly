/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfly;

import java.util.HashMap;
import java.util.Stack;
import java.util.Random;
import java.util.Date;
import java.util.ArrayList;
import java.util.regex.Pattern;


/**
 *
 * @author dg7239p
 */
public class BlockchainNodeManager {
    private HashMap sharedStateBlocks = new HashMap();
    private Stack<String> hashChain = new Stack<String>();
    private JFlyNode myNode = null;
    private ArrayList<String> validRegistrars = new ArrayList();
    public String lastHash()
    {
        if(hashChain.size() > 0) { return hashChain.peek(); }
        else { return ""; }
    }
    private Boolean validRegistrarHash(String hash)
    {
        for(String h : validRegistrars)
        {
            if(h.equals(hash)) { return true; }
        }
        return false;
    }
    public void calculateConfigs(NetworkConfigurationState cur, int depth)
    {
        ArrayList<NetworkConfigurationState.UserInfo> newUsers = cur.getUsers();
        Stack<String> hashClone = (Stack<String>)hashChain.clone();
        if(depth < 0) { depth = hashClone.size(); }
        Stack<String> hashCloneReOrder = new Stack<String>();
        for(int i = 0; i < depth; i++)
        {
            hashCloneReOrder.add(hashClone.pop());
        }
        for(int i = 0; i < depth; i++)
        {
            SharedStateBlock current = (SharedStateBlock)sharedStateBlocks.get(hashCloneReOrder.pop());
            if(current.getContentType() == SharedStateBlock.ContentType.GROUP_REGISTRAR && validRegistrarHash(current.getHash()))
            {
                String[] regiUsers = current.getContentData().split(Pattern.quote("/-/"), -1);
                for(String usr : regiUsers)
                {
                    NetworkConfigurationState.UserInfo newUser = NetworkConfigurationState.UserInfo.fromString(usr);
                    newUsers.add(newUser);
                }
            }
            else if(current.getContentType() == SharedStateBlock.ContentType.USER_JOINED)
            {
                NetworkConfigurationState.UserInfo newUser = NetworkConfigurationState.UserInfo.fromString(current.getContentData());
                newUser.setID(SharedStateBlock.getHash(current.getHash()));
                newUsers.add(newUser);
            }
            else if(current.getContentType() == SharedStateBlock.ContentType.USER_LEFT)
            {
                NetworkConfigurationState.UserInfo newUser = NetworkConfigurationState.UserInfo.fromString(current.getContentData());
                NetworkConfigurationState.UserInfo oldUser = null;
                for(NetworkConfigurationState.UserInfo u : newUsers)
                {
                    if(u.getID() == newUser.getID())
                    {
                        oldUser = u;
                        break;
                    }
                }
                if(oldUser != null) { newUsers.remove(oldUser); }
            }
        }
        cur.reWriteAll(newUsers);
        System.out.println(cur);
    }
    public String[] getLast(int depth)
    {
        ArrayList<String> msgs = new ArrayList<String>();
        if(depth > hashChain.size()) { depth = hashChain.size(); }
        Stack<String> hashClone = (Stack<String>)hashChain.clone();
        Stack<String> hashCloneReOrder = new Stack<String>();
        for(int i = 0; i < depth; i++)
        {
            hashCloneReOrder.add(hashClone.pop());
        }
        for(int i = 0; i < depth; i++)
        {
            SharedStateBlock current = (SharedStateBlock)sharedStateBlocks.get(hashCloneReOrder.pop());
            if(current.getContentType() == SharedStateBlock.ContentType.MESSAGE)
            {
                Date mDate = new Date(current.getCreationTime());
                msgs.add(myNode.getNCS().getUserNameFromID(current.getOUID()) + " (" + mDate.toString() + ") : " + current.getContentData());
            }
        }
        String[] out = new String[msgs.size()];
        for(int i = 0; i < out.length; i++)
        {
            out[i] = msgs.get(i);
        }
        return out;
    }
    public JFlyNode getJNode()
    {
        return myNode;
    }
    public BlockchainNodeManager(JFlyNode associatedNode)
    {
        myNode = associatedNode;
    }
    public void authorBlock(SharedStateBlock.ContentType newContentType, String newContentData)
    {
        SharedStateBlock newBlock = new SharedStateBlock(this, newContentType, newContentData, lastHash());
        int adder = addExtantBlockToChain(newBlock.toString());
        System.out.println("Block author status: " + adder);
        if(adder == 0 || adder == 1)
        {
            JFlyNode.OutputJobInfo afterAuthorJob = new JFlyNode.OutputJobInfo(JFlyNode.OutputJobInfo.JobType.MULTIPLE_DISPATCH, newBlock.toString(), "JFLYCHAINBLOCK");
            myNode.sendJobToThreads(afterAuthorJob, null);
        }
        //calculateConfigs(myNode.getNCS(), 1);
    }
    public String tryOneHash()
    {
        Random rnd = new Random();
        return (new SharedStateBlock(this, rnd.nextDouble() + "")).getHash();
    }
    public String getByHash(String hash)
    {
        if(sharedStateBlocks.containsKey(hash)) { return ((SharedStateBlock)sharedStateBlocks.get(hash)).toString(); }
        else { return null; }
    }
    /*
    0: Added as expected
    1: Was added but BlockChain was empty?
    2: Failed to add, no matching PrevHash in chain, request more blocks
    3: Couldn't construct new block. Verification failed?
    4: Block already present or hash collision
    */
    private int lastDepth = 0;
    private int registrarTolerance = 0;
    public void addRegistrarTolerance(int incr)
    {
        registrarTolerance += incr;
    }
    public int addExtantBlockToChain(String blockData)
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
                if(sharedStateBlocks.containsKey(extBlock.getHash()))
                {
                    calculateConfigs(myNode.getNCS(), lastDepth);
                    lastDepth = 0;
                    return 4;
                }
                String lastBlockHash = lastHash();
                Stack<SharedStateBlock> poppedBlocks = new Stack<SharedStateBlock>();
                while(!extBlock.getLastBlockHash().equals(lastBlockHash) || lastBlockHash.length() == 0)
                {
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
                        return 2;
                    }
                    if(lastBlockHash.length() > 0)
                    {
                        String toPop = hashChain.pop();
                        poppedBlocks.add((SharedStateBlock)sharedStateBlocks.get(toPop));
                        sharedStateBlocks.remove(toPop);
                    }
                    lastBlockHash = hashChain.peek();
                }
                Boolean reinsertTriggered = false; //Maybe rework this mechanism later to be more elegant
                if(poppedBlocks.size() > 0)
                {
                    reinsertTriggered = true;
                    Boolean extPut = false;
                    for(int i = poppedBlocks.size(); i > 0; i--)
                    {
                        SharedStateBlock curReInsert = poppedBlocks.pop();
                        if(!extPut) { extBlock.setLastBlockHash(lastBlockHash); }
                        curReInsert.setLastBlockHash(lastBlockHash);
                        if(!extPut && ((extBlock.getCreationTime() < curReInsert.getCreationTime()) || (extBlock.getCreationTime() == curReInsert.getCreationTime() && extBlock.getHash().compareTo(curReInsert.getHash()) < 0)))
                        {
                            lastDepth++;
                            lastBlockHash = extBlock.getHash();
                            hashChain.add(lastBlockHash);
                            sharedStateBlocks.put(lastBlockHash, extBlock);
                            curReInsert.setLastBlockHash(lastBlockHash);
                            extPut = true;
                        }
                        if(!extPut && extBlock.getHash().compareTo(curReInsert.getHash()) == 0) { System.out.println("Warning: Unexpected hash collision during blockchain insertion(?!)."); }
                        lastDepth++;
                        lastBlockHash = curReInsert.getHash();
                        hashChain.add(lastBlockHash);
                        sharedStateBlocks.put(lastBlockHash, curReInsert);
                    }
                }
                else
                {
                    lastDepth++;
                    lastBlockHash = extBlock.getHash();
                    hashChain.add(lastBlockHash);
                    sharedStateBlocks.put(lastBlockHash, extBlock);
                }
                if(extBlock.getContentType() == SharedStateBlock.ContentType.GROUP_REGISTRAR && registrarTolerance > 0)
                {
                    validRegistrars.add(extBlock.getHash());
                    System.out.println("Added valid registrar");
                    registrarTolerance--;
                }
                calculateConfigs(myNode.getNCS(), reinsertTriggered ? -1 : lastDepth);
                lastDepth = 0;
                return 0;
            }
            else
            {
                String extBlockHash = extBlock.getHash();
                lastDepth++;
                hashChain.add(extBlockHash);
                sharedStateBlocks.put(extBlockHash, extBlock);
                if(extBlock.getContentType() == SharedStateBlock.ContentType.GROUP_REGISTRAR && registrarTolerance > 0)
                {
                    validRegistrars.add(extBlockHash);
                    System.out.println("Added valid registrar");
                    registrarTolerance--;
                }
                calculateConfigs(myNode.getNCS(), lastDepth);
                lastDepth = 0;
                return 1;
            }
        }
        else
        {
            calculateConfigs(myNode.getNCS(), lastDepth);
            lastDepth = 0;
            return 3;
        }
    }
    public static class SharedStateBlock
    {
        public static class StateBlockVerificationException extends Exception
        {
            public StateBlockVerificationException(String message)
            {
                super(message);
            }
        }
        public static enum ContentType
        {
            MESSAGE, USER_JOINED, USER_LEFT, SYSTEM_UTIL, GENESIS, GROUP_REGISTRAR;
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
        public SharedStateBlock(BlockchainNodeManager bnManager)
        {
            myManager = bnManager;
        }
        public SharedStateBlock(BlockchainNodeManager bnManager, String prevHash)
        {
            myManager = bnManager;
            localPrevBlockHash = prevHash;
        }
        public SharedStateBlock(BlockchainNodeManager bnManager, ContentType newContentType, String newContentData, String prevHash)
        {
            myManager = bnManager;
            localPrevBlockHash = prevHash;
            contentType = newContentType;
            Date createDate = new Date(JFlyNode.time());
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
        public void setLastBlockHash(String prevHash)
        {
            localPrevBlockHash = prevHash;
        }
        public long getCreationTime()
        {
            return updateTime;
        }
        public ContentType getContentType()
        {
            return contentType;
        }
        public String getContentData()
        {
            return contentData;
        }
        public String getOUID()
        {
            return originatingUserID;
        }
        public String getLastBlockHash()
        {
            return localPrevBlockHash;
        }
        public String getHash()
        {
            return getRawHash(getRawHash(getRawHash(myDat())));
        }
        public static String getHash(String data)
        {
            return getRawHash(getRawHash(getRawHash(data)));
        }
        private String myDat()
        {
            return localPrevBlockHash +
                    "|" + updateTime +
                    "|" + originatingUserID +
                    "|" + contentType.toString() +
                    "|" + contentData;
        }
        @Override
        public String toString()
        {
            return myDat() + "|" + getHash();
        }
        public String selfInitialize(String initData)
        {
            String[] dataSegs = initData.split("\\|", -1);
            System.out.println(initData);
            localPrevBlockHash = dataSegs[0];
            updateTime = Long.parseLong(dataSegs[1]);
            originatingUserID = dataSegs[2];
            contentType = ContentType.fromString(dataSegs[3]);
            contentData = dataSegs[4];
            return dataSegs[5];
        }
        public SharedStateBlock getOneBlock(String initData) throws StateBlockVerificationException
        {
            return getOneBlock(myManager, initData);
        }
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
