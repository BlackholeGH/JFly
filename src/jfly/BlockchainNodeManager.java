/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfly;

import java.util.HashMap;
import java.util.Stack;
import java.util.Random;

/**
 *
 * @author dg7239p
 */
public class BlockchainNodeManager {
    private HashMap sharedStateBlocks = new HashMap();
    private Stack<String> hashChain = new Stack<String>();
    private JFlyNode myNode = null;
    public BlockchainNodeManager(JFlyNode associatedNode)
    {
        myNode = associatedNode;
    }
    public String tryOneHash()
    {
        Random rnd = new Random();
        return (new SharedStateBlock(rnd.nextDouble() + "")).getHash();
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
        SharedStateBlock extBlock = new SharedStateBlock();
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
    private class SharedStateBlock
    {
        private class StateBlockVerificationException extends Exception
        {
            public StateBlockVerificationException(String message)
            {
                super(message);
            }
        }
        String localPrevBlockHash = "";
        String contentType = "";
        String contentData = "";
        String originatingUserID = "";
        long updateTime = 0;
        public SharedStateBlock() { }
        public SharedStateBlock(String prevHash)
        {
            localPrevBlockHash = prevHash;
        }
        public SharedStateBlock(String newContentType, String newContentData)
        {
            
        }
        public String getRawHash(String temp)
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
         public String getHash(String data)
        {
            return getRawHash(getRawHash(getRawHash(data)));
        }
        private String myDat()
        {
            return localPrevBlockHash + "|" + updateTime + "|" + originatingUserID + "|" + contentType + "|" + contentData;
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
            contentType = dataSegs[3];
            contentData = dataSegs[4];
            return dataSegs[5];
        }
        public SharedStateBlock getOneBlock(String initData) throws StateBlockVerificationException
        {
            SharedStateBlock oneBlock = new SharedStateBlock();
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
