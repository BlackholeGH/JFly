/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfly;

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
public class JFlyNodeTest {
    
    public JFlyNodeTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
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
     * Test of time method, of class JFlyNode.
     */
    @Test
    public void testTime() {
        System.out.println("time");
        long expResult = 0L;
        long result = JFlyNode.time();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of resetAddress method, of class JFlyNode.
     */
    @Test
    public void testResetAddress() {
        System.out.println("resetAddress");
        String addr = "";
        BlockchainNodeManager myManager = null;
        JFlyNode instance = new JFlyNode();
        instance.resetAddress(addr, myManager);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDefaultAddress method, of class JFlyNode.
     */
    @Test
    public void testSetDefaultAddress() {
        System.out.println("setDefaultAddress");
        String addr = "";
        JFlyNode instance = new JFlyNode();
        instance.setDefaultAddress(addr);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of hostAddr method, of class JFlyNode.
     */
    @Test
    public void testHostAddr() {
        System.out.println("hostAddr");
        JFlyNode instance = new JFlyNode();
        String expResult = "";
        String result = instance.hostAddr();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getUserID method, of class JFlyNode.
     */
    @Test
    public void testGetUserID() {
        System.out.println("getUserID");
        JFlyNode instance = new JFlyNode();
        String expResult = "";
        String result = instance.getUserID();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of shuttingDown method, of class JFlyNode.
     */
    @Test
    public void testShuttingDown() {
        System.out.println("shuttingDown");
        JFlyNode instance = new JFlyNode();
        Boolean expResult = null;
        Boolean result = instance.shuttingDown();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of shutdownNode method, of class JFlyNode.
     */
    @Test
    public void testShutdownNode_0args() {
        System.out.println("shutdownNode");
        JFlyNode instance = new JFlyNode();
        instance.shutdownNode();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of shutdownNode method, of class JFlyNode.
     */
    @Test
    public void testShutdownNode_Boolean() {
        System.out.println("shutdownNode");
        Boolean relaunch = null;
        JFlyNode instance = new JFlyNode();
        instance.shutdownNode(relaunch);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of pingThreads method, of class JFlyNode.
     */
    @Test
    public void testPingThreads() {
        System.out.println("pingThreads");
        JFlyNode instance = new JFlyNode();
        instance.pingThreads();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of closeAll method, of class JFlyNode.
     */
    @Test
    public void testCloseAll() {
        System.out.println("closeAll");
        JFlyNode instance = new JFlyNode();
        instance.closeAll();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of allowTransientForwarding method, of class JFlyNode.
     */
    @Test
    public void testAllowTransientForwarding() {
        System.out.println("allowTransientForwarding");
        String transientPackage = "";
        JFlyNode instance = new JFlyNode();
        Boolean expResult = null;
        Boolean result = instance.allowTransientForwarding(transientPackage);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getNCS method, of class JFlyNode.
     */
    @Test
    public void testGetNCS() {
        System.out.println("getNCS");
        JFlyNode instance = new JFlyNode();
        NetworkConfigurationState expResult = null;
        NetworkConfigurationState result = instance.getNCS();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getBNM method, of class JFlyNode.
     */
    @Test
    public void testGetBNM() {
        System.out.println("getBNM");
        JFlyNode instance = new JFlyNode();
        BlockchainNodeManager expResult = null;
        BlockchainNodeManager result = instance.getBNM();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getThreadsForUser method, of class JFlyNode.
     */
    @Test
    public void testGetThreadsForUser() {
        System.out.println("getThreadsForUser");
        String hashIdentifier = "";
        JFlyNode instance = new JFlyNode();
        OneLinkThread[] expResult = null;
        OneLinkThread[] result = instance.getThreadsForUser(hashIdentifier);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of attemptContact method, of class JFlyNode.
     */
    @Test
    public void testAttemptContact() {
        System.out.println("attemptContact");
        String hashIdentifier = "";
        JFlyNode instance = new JFlyNode();
        instance.attemptContact(hashIdentifier);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of queryAcceptQuester method, of class JFlyNode.
     */
    @Test
    public void testQueryAcceptQuester() {
        System.out.println("queryAcceptQuester");
        String questerIP = "";
        JFlyNode instance = new JFlyNode();
        Boolean expResult = null;
        Boolean result = instance.queryAcceptQuester(questerIP);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of leaveCluster method, of class JFlyNode.
     */
    @Test
    public void testLeaveCluster() {
        System.out.println("leaveCluster");
        JFlyNode instance = new JFlyNode();
        instance.leaveCluster();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLastMessages method, of class JFlyNode.
     */
    @Test
    public void testGetLastMessages() {
        System.out.println("getLastMessages");
        int num = 0;
        JFlyNode instance = new JFlyNode();
        String[] expResult = null;
        String[] result = instance.getLastMessages(num);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of sendMessage method, of class JFlyNode.
     */
    @Test
    public void testSendMessage() {
        System.out.println("sendMessage");
        String message = "";
        JFlyNode instance = new JFlyNode();
        instance.sendMessage(message);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of pullOneBlockByHash method, of class JFlyNode.
     */
    @Test
    public void testPullOneBlockByHash() {
        System.out.println("pullOneBlockByHash");
        String hash = "";
        JFlyNode instance = new JFlyNode();
        String expResult = "";
        String result = instance.pullOneBlockByHash(hash);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of tryOneBlock method, of class JFlyNode.
     */
    @Test
    public void testTryOneBlock() {
        System.out.println("tryOneBlock");
        String data = "";
        JFlyNode instance = new JFlyNode();
        String expResult = "";
        String result = instance.tryOneBlock(data);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of sendJobToThreads method, of class JFlyNode.
     */
    @Test
    public void testSendJobToThreads() {
        System.out.println("sendJobToThreads");
        JFlyNode.OutputJobInfo job = null;
        OneLinkThread[] blacklist = null;
        JFlyNode instance = new JFlyNode();
        instance.sendJobToThreads(job, blacklist);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of registerThread method, of class JFlyNode.
     */
    @Test
    public void testRegisterThread() {
        System.out.println("registerThread");
        OneLinkThread thread = null;
        JFlyNode instance = new JFlyNode();
        instance.registerThread(thread);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of unregisterThread method, of class JFlyNode.
     */
    @Test
    public void testUnregisterThread() {
        System.out.println("unregisterThread");
        OneLinkThread thread = null;
        Boolean skipBlocking = null;
        JFlyNode instance = new JFlyNode();
        instance.unregisterThread(thread, skipBlocking);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLocalUsername method, of class JFlyNode.
     */
    @Test
    public void testGetLocalUsername() {
        System.out.println("getLocalUsername");
        JFlyNode instance = new JFlyNode();
        String expResult = "";
        String result = instance.getLocalUsername();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setLocalUsername method, of class JFlyNode.
     */
    @Test
    public void testSetLocalUsername() {
        System.out.println("setLocalUsername");
        String username = "";
        JFlyNode instance = new JFlyNode();
        instance.setLocalUsername(username);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of main method, of class JFlyNode.
     */
    @Test
    public void testMain() {
        System.out.println("main");
        String[] args = null;
        JFlyNode.main(args);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of wipeLauncher method, of class JFlyNode.
     */
    @Test
    public void testWipeLauncher() {
        System.out.println("wipeLauncher");
        FlyLauncher fl = null;
        JFlyNode instance = new JFlyNode();
        instance.wipeLauncher(fl);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of issueExistenceTransient method, of class JFlyNode.
     */
    @Test
    public void testIssueExistenceTransient() {
        System.out.println("issueExistenceTransient");
        JFlyNode instance = new JFlyNode();
        instance.issueExistenceTransient();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of crossNetworkSeekNode method, of class JFlyNode.
     */
    @Test
    public void testCrossNetworkSeekNode() {
        System.out.println("crossNetworkSeekNode");
        String userIDorResponseTransient = "";
        JFlyNode instance = new JFlyNode();
        instance.crossNetworkSeekNode(userIDorResponseTransient);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getManualListenPort method, of class JFlyNode.
     */
    @Test
    public void testGetManualListenPort() {
        System.out.println("getManualListenPort");
        JFlyNode instance = new JFlyNode();
        int expResult = 0;
        int result = instance.getManualListenPort();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setManualListenPort method, of class JFlyNode.
     */
    @Test
    public void testSetManualListenPort() {
        System.out.println("setManualListenPort");
        int port = 0;
        JFlyNode instance = new JFlyNode();
        instance.setManualListenPort(port);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of openReceiveAndWait method, of class JFlyNode.
     */
    @Test
    public void testOpenReceiveAndWait() throws Exception {
        System.out.println("openReceiveAndWait");
        JFlyNode instance = new JFlyNode();
        instance.openReceiveAndWait();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getGUI method, of class JFlyNode.
     */
    @Test
    public void testGetGUI() {
        System.out.println("getGUI");
        JFlyNode instance = new JFlyNode();
        FlyChatGUI expResult = null;
        FlyChatGUI result = instance.getGUI();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of sendConnectAndOpen method, of class JFlyNode.
     */
    @Test
    public void testSendConnectAndOpen() throws Exception {
        System.out.println("sendConnectAndOpen");
        String iP = "";
        int rPort = 0;
        JFlyNode instance = new JFlyNode();
        instance.sendConnectAndOpen(iP, rPort);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
