/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfly;

import flyutils.NetworkConfigurationState;
import static jfly.NetworkConfigurationStateIT.instance;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author gk8560y
 */
public class BlockchainNodeManagerIT {
    
    public BlockchainNodeManagerIT() {
    }
    static BlockchainNodeManager instance;
    static BlockchainNodeManager.SharedStateBlock u;
    static BlockchainNodeManager.SharedStateBlock u2;
    @BeforeClass
    public static void setUpClass() {
        JFlyNode tempnode = JFlyNode.makeJFlyNode();
        instance = new BlockchainNodeManager(tempnode);
        u = new BlockchainNodeManager.SharedStateBlock(instance, BlockchainNodeManager.SharedStateBlock.ContentType.USER_JOINED, "192.168.0.1+-+Gagan+-+A Hash", "Another Hash");
        instance.addExtantBlockToChain(u.toString());
        u2 = new BlockchainNodeManager.SharedStateBlock(instance, BlockchainNodeManager.SharedStateBlock.ContentType.MESSAGE, "A message!", u.getHash());
        instance.addExtantBlockToChain(u2.toString());
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of lastHash method, of class BlockchainNodeManager.
     */
    @Test
    public void testLastHash() {
        System.out.println("lastHash");
        
        String expResult = u2.getHash();
        String result = instance.lastHash();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of calculateConfigs method, of class BlockchainNodeManager.
     */
    @Test
    public void testCalculateConfigs() {
        System.out.println("calculateConfigs");
        NetworkConfigurationState cur = new NetworkConfigurationState();
        int depth = -1;
        instance.calculateConfigs(cur, depth);
        NetworkConfigurationState.UserInfo test = cur.getUserFromID(cur.getIDFromIP("192.168.0.1"));
        System.out.println(test);
        assertEquals(test.getUserName(), "Gagan");
        
        // TODO review the generated test code and remove the default call to fail.
       //fail("The test case is a prototype.");
    }

    /**
     * Test of getLast method, of class BlockchainNodeManager.
     */
    @Test
    public void testGetLast() {
        System.out.println("getLast");
        int depth = -1;      
        String[] expResult = null;
        String[] result = instance.getLast(depth);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
       
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getJNode method, of class BlockchainNodeManager.
     */
    @Test
    public void testGetJNode() {
        System.out.println("getJNode");
        BlockchainNodeManager instance = null;
        JFlyNode expResult = null;
        JFlyNode result = instance.getJNode();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of authorBlock method, of class BlockchainNodeManager.
     */
    @Test
    public void testAuthorBlock() {
        System.out.println("authorBlock");
        BlockchainNodeManager.SharedStateBlock.ContentType newContentType = null;
        String newContentData = "";
        BlockchainNodeManager instance = null;
        instance.authorBlock(newContentType, newContentData);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of tryOneHash method, of class BlockchainNodeManager.
     */
    @Test
    public void testTryOneHash() {
        System.out.println("tryOneHash");
        BlockchainNodeManager instance = null;
        String expResult = "";
        String result = instance.tryOneHash();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getByHash method, of class BlockchainNodeManager.
     */
    @Test
    public void testGetByHash() {
        System.out.println("getByHash");
        String expResult = u.toString();
        String result = instance.getByHash(u.getHash());
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of addRegistrarTolerance method, of class BlockchainNodeManager.
     */
    @Test
    public void testAddRegistrarTolerance() {
        System.out.println("addRegistrarTolerance");
        int incr = 0;
        instance.addRegistrarTolerance(incr);
        // TODO review the generated test code and remove the default call to fail.
        assertEquals(incr, this);
        //fail("The test case is a prototype.");
    }

    /**
     * Test of addExtantBlockToChain method, of class BlockchainNodeManager.
     */
    @Test
    public void testAddExtantBlockToChain() {
        System.out.println("addExtantBlockToChain");
        String blockData = "";
        BlockchainNodeManager instance = null;
        Object[] expResult = null;
        Object[] result = instance.addExtantBlockToChain(blockData);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
