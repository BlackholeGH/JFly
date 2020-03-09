/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfly;

import jutils.TextUtility;
import jutils.NetworkConfigurationState;
import java.util.ArrayList;
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
public class NetworkConfigurationStateIT {
    
    public NetworkConfigurationStateIT() {
    }
    
    static NetworkConfigurationState instance;
    static NetworkConfigurationState.UserInfo u;
    @BeforeClass
    public static void setUpClass() {
        instance = new NetworkConfigurationState();
        u = new NetworkConfigurationState.UserInfo("192.168.0.1", "A Hash", "Gagan");
        instance.addUser(u);
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
     * Test of getRegistrar method, of class NetworkConfigurationState.
     */
    @Test
    public void testGetRegistrar() {
        System.out.println("getRegistrar");    
        String expResult = "192.168.0.1+-+Gagan+-+A Hash";
        String result = instance.getRegistrar();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getTotalNetworkMembers method, of class NetworkConfigurationState.
     */
    @Test
    public void testGetTotalNetworkMembers() {
        System.out.println("getTotalNetworkMembers");      
        int expResult = 1;
        int result = instance.getTotalNetworkMembers();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
       // fail("The test case is a prototype.");
        
    }

    /**
     * Test of addUser method, of class NetworkConfigurationState.
     */
    @Test
    public void testAddUser() {
        System.out.println("addUser");
        NetworkConfigurationState.UserInfo u = null;
        NetworkConfigurationState instance = new NetworkConfigurationState();
        instance.addUser(u);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of remUser method, of class NetworkConfigurationState.
     */
    @Test
    public void testRemUser() {
        System.out.println("remUser");
        NetworkConfigurationState.UserInfo u = null;
        NetworkConfigurationState instance = new NetworkConfigurationState();
        instance.remUser(u);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of reWriteAll method, of class NetworkConfigurationState.
     */
    @Test
    public void testReWriteAll() {
        System.out.println("reWriteAll");
        ArrayList<NetworkConfigurationState.UserInfo> nUI = null;
        NetworkConfigurationState instance = new NetworkConfigurationState();
        instance.reWriteAll(nUI);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getUserNameFromID method, of class NetworkConfigurationState.
     */
    @Test
    public void testGetUserNameFromID() {
        System.out.println("getUserNameFromID");
        String iD = "A Hash";
        String expResult = "Gagan";
        String result = instance.getUserNameFromID(iD);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getUserDataFromID method, of class NetworkConfigurationState.
     */
    @Test
    public void testGetUserDataFromID() {
        System.out.println("getUserDataFromID");
        String iD = "A Hash";       
        String expResult = "192.168.0.1+-+Gagan+-+A Hash";
        String result = instance.getUserDataFromID(iD);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getUserFromID method, of class NetworkConfigurationState.
     */
    @Test
    public void testGetUserFromID() {
        System.out.println("getUserFromID");
        String iD = "A Hash";       
        NetworkConfigurationState.UserInfo expResult = u;
        NetworkConfigurationState.UserInfo result = instance.getUserFromID(iD);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getIDFromIP method, of class NetworkConfigurationState.
     */
    @Test
    public void testGetIDFromIP() {
        System.out.println("getIDFromIP");
        String iP = "192.168.0.1";
        String expResult = "A Hash";
        String result = instance.getIDFromIP(iP);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getIPFromID method, of class NetworkConfigurationState.
     */
    @Test
    public void testGetIPFromID() {
        System.out.println("getIPFromID");
        String iD = "A Hash";       
        String expResult = "192.168.0.1";
        String result = instance.getIPFromID(iD);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
        
    }

    /**
     * Test of getUsers method, of class NetworkConfigurationState.
     */
    @Test
    public void testGetUsers() {
        System.out.println("getUsers");      
        ArrayList<NetworkConfigurationState.UserInfo> expResult = new ArrayList<NetworkConfigurationState.UserInfo>();
        expResult.add(u);
        
        ArrayList<NetworkConfigurationState.UserInfo> result = instance.getUsers();
        assertEquals(((NetworkConfigurationState.UserInfo)expResult.toArray()[0]).getID(), ((NetworkConfigurationState.UserInfo)result.toArray()[0]).getID());
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
       
    }

    /**
     * Test of getUserIDs method, of class NetworkConfigurationState.
     */
    @Test
    public void testGetUserIDs() {
        System.out.println("getUserIDs");      
        ArrayList<String> expResult = new ArrayList<String>();
        expResult.add(u.getID());
        ArrayList<String> result = instance.getUserIDs();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
       // fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class NetworkConfigurationState.
     */
    @Test
    public void testToString() {
        System.out.println("toString");      
        String expResult = "1: 192.168.0.1, A Hash, Gagan\n";
        String result = instance.toString();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getTableData method, of class NetworkConfigurationState.
     */
    @Test
    public void testGetTableData() {
        System.out.println("getTableData");     
        String[][] expResult = new String[][] { { "Gagan", "A Hash"} };
        String[][] result = instance.getTableData();
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
        //assertSame(expResult, result);
    }
    
}
