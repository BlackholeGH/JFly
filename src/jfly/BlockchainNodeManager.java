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

/**
 *
 * @author dg7239p
 */
public class BlockchainNodeManager {
    private HashMap sharedStateBlocks = new HashMap();
    private Stack<String> hashChain = new Stack<String>();
    private JFlyNode myNode = null;
    public JFlyNode getJNode()
    {
        return myNode;
    }
    public BlockchainNodeManager(JFlyNode associatedNode)
    {
        myNode = associatedNode;
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
                if(sharedStateBlocks.containsKey(extBlock.getHash())) { return 4; }
                String lastBlockHash = "";
                Stack<SharedStateBlock> poppedBlocks = new Stack<SharedStateBlock>();
                while(!extBlock.getLastBlockHash().equals(lastBlockHash) || lastBlockHash.length() == 0)
                {
                    if(hashChain.size() == 1)
                    {
                        for(int i = poppedBlocks.size(); i > 0; i--)
                        {
                            SharedStateBlock curReInsert = poppedBlocks.pop();
                            lastBlockHash = curReInsert.getHash();
                            hashChain.add(lastBlockHash);
                            sharedStateBlocks.put(lastBlockHash, curReInsert);
                        }
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
                if(poppedBlocks.size() > 0)
                {
                    Boolean extPut = false;
                    for(int i = poppedBlocks.size(); i > 0; i--)
                    {
                        SharedStateBlock curReInsert = poppedBlocks.pop();
                        if(!extPut) { extBlock.setLastBlockHash(lastBlockHash); }
                        curReInsert.setLastBlockHash(lastBlockHash);
                        if(!extPut && ((extBlock.getCreationTime() < curReInsert.getCreationTime()) || (extBlock.getCreationTime() == curReInsert.getCreationTime() && extBlock.getHash().compareTo(curReInsert.getHash()) < 0)))
                        {
                            lastBlockHash = extBlock.getHash();
                            hashChain.add(lastBlockHash);
                            sharedStateBlocks.put(lastBlockHash, extBlock);
                            curReInsert.setLastBlockHash(lastBlockHash);
                            extPut = true;
                        }
                        if(!extPut && extBlock.getHash().compareTo(curReInsert.getHash()) == 0) { System.out.println("Warning: Unexpected hash collision during blockchain insertion(?!)."); }
                        lastBlockHash = curReInsert.getHash();
                        hashChain.add(lastBlockHash);
                        sharedStateBlocks.put(lastBlockHash, curReInsert);
                    }
                }
                else
                {
                    lastBlockHash = extBlock.getHash();
                    hashChain.add(lastBlockHash);
                    sharedStateBlocks.put(lastBlockHash, extBlock);
                }
                return 0;
            }
            else
            {
                String extBlockHash = extBlock.getHash();
                hashChain.add(extBlockHash);
                sharedStateBlocks.put(extBlockHash, extBlock);
                return 1;
            }
        }
        else { return 3; }
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
        public SharedStateBlock(BlockchainNodeManager bnManager, ContentType newContentType, String newContentData)
        {
            myManager = bnManager;
            contentType = newContentType;
            Date createDate = new Date(JFlyNode.time());
            if(newContentType == ContentType.GENESIS)
            {
                contentData = "New cluster was created at " + createDate.toString() + ". #BIGDUCKROBOT";
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
            return localPrevBlockHash + "|" + updateTime + "|" + originatingUserID + "|" + contentType.toString() + "|" + contentData;
        }
        @Override
        public String toString()
        {
            return myDat() + "|" + getHash();
        }
        public String selfInitialize(String initData)
        {
            String[] dataSegs = initData.split("|");
            localPrevBlockHash = dataSegs[0];
            updateTime = Long.getLong(dataSegs[1]);
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
