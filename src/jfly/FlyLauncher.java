/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfly;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import java.util.ArrayList;
import java.awt.BorderLayout;
import java.io.*;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.ImageIcon;

/**
 * The FlyLauncher class extends JFrame and represents a GUI element that displays JFly launcher options.
 * @author Blackhole (dg7239p)
 */
public class FlyLauncher extends JFrame implements ActionListener
{
    protected JFlyNode myNode;
    /**
     * The FlyLauncher constructor.
     * @param node The associated JFlyNode for this FlyLauncher instance.
     */
    public FlyLauncher(JFlyNode node)
    {
        myNode = node;
        if(!(this instanceof FlyListenOpts))
        {
            //The default close operation is do nothing, because...
            setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            //...on close, this window should ask the associated JFlyNode to manage application shutdown.
            addWindowListener(FlyLauncher.getExitListener(node));
            //Display the launcher window.
            viewLauncher();
        }
    }
    /**
     * Returns a new WindowListener object that will shutdown the JFlyNode if the window it is added to is exited.
     * @param myNode The JFlyNode to shutdown.
     * @return The new WindowListener implementation instance.
     */
    public static WindowListener getExitListener(JFlyNode myNode)
    {
        //Remember this anonymous class syntax. Very useful.
        WindowListener exiter = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e)
            {
                myNode.shutdownNode();
            }
        };
        return exiter;
    }
// 
//--------------------------------------------------------------------------------------------------------------------
    FocusVerifier fL = new FocusVerifier();
    //Various fonts are created.
    Font fnt = new Font("Ariel", Font.PLAIN, 18);
    Font fntBold = new Font("Ariel", Font.BOLD, 18);
    Font fntMid = new Font("Ariel", Font.PLAIN, 14);
    Font fntSmall = new Font("Ariel", Font.PLAIN, 12);
    //The main application logo file location.
    static String fLogo = System.getProperty("user.dir") + "\\Img\\JFlogo2.png";
    //The simplified logo is more easily distinguished when used as a small window or program icon.
    static String fLogoSimple = System.getProperty("user.dir") + "\\Img\\JFlogoSimplified.png";
    /**
     * Gets the JFly logo icon in 50x50 size.
     * @return The JFly logo icon.
     */
    public static Image getLogoIcon()
    {
        return getLogoIcon(50, 50);
    }
    /**
     * Gets the JFly logo icon in specified dimensions.
     * @param width The scaled width of the icon to get.
     * @param height The scaled height of the icon to get.
     * @return The JFly logo icon scaled to the specified dimensions.
     */
    public static Image getLogoIcon(int width, int height)
    {
        try
        {
            BufferedImage logoPic = ImageIO.read(new File(fLogo));
            return logoPic.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        }
        catch(IOException e) { return null; }
    }
    /**
     * Gets the simplified JFly logo icon in specified dimensions.
     * @param width The scaled width of the icon to get.
     * @param height The scaled height of the icon to get.
     * @return The simplified JFly logo icon scaled to the specified dimensions.
     */
    public static Image getSimpleLogoIcon(int width, int height)
    {
        try
        {
            BufferedImage logoPic = ImageIO.read(new File(fLogoSimple));
            return logoPic.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        }
        catch(IOException e) { return null; }
    }
    /**
     * Gets a list of simplified JFly logo icons to be used as window icons.
     * @return A list of simplified JFly logo icons in multiple sizes.
     */
    public static ArrayList<Image> getLogoIcons()
    {
        ArrayList<Image> out = new ArrayList();
        //The sizes selected are standard icon sizes.
        out.add(getSimpleLogoIcon(64, 64));
        out.add(getSimpleLogoIcon(32, 32));
        out.add(getSimpleLogoIcon(16, 16));
        return out;
    }
    /**
     * Creates and displays the FlyLauncher GUI interface.
     */
    public void viewLauncher()
    {
        //The window icons are set.
        setIconImages(getLogoIcons());
        
        //Creating the main launcher JPanel.
        JPanel launcherPanel = new JPanel(new BorderLayout());
        
        JPanel topLabel = new JPanel();
        topLabel.setLayout(new BoxLayout(topLabel, BoxLayout.Y_AXIS));
        
        topLabel.add(Box.createVerticalStrut(20));
        System.out.println(System.getProperty("user.dir"));
        try
        {
            //The application logo is displayed in the launcher.
            JLabel logoLabel = new JLabel(new ImageIcon(getLogoIcon(150, 150)));
            logoLabel.setAlignmentX(CENTER_ALIGNMENT);
            topLabel.add(logoLabel);
            topLabel.add(Box.createVerticalStrut(10));
        }
        catch(Exception e) {}
        
        //Displaying explanatory dialogue labels.
        JLabel introDialog = new JLabel("Welcome to JFly. Please enter a node IP and access port to connect to a cluster.");
        introDialog.setFont(fnt);
        introDialog.setHorizontalAlignment(JLabel.CENTER);
        introDialog.setAlignmentX(CENTER_ALIGNMENT);
        topLabel.add(Box.createVerticalStrut(10));
        topLabel.add(introDialog);
        
        //Local IP and port are displayed for user convenience. These labels are html formatted.
        JLabel myLocalIP = new JLabel("<html>Your local IP is: <b>" + myNode.hostAddr() + "</b></html>");
        myLocalIP.setFont(fntMid);
        myLocalIP.setHorizontalAlignment(JLabel.CENTER);
        myLocalIP.setAlignmentX(CENTER_ALIGNMENT);
        JLabel defPort = new JLabel("<html>Default JFly port: <b>44665</b></html>");
        defPort.setFont(fntMid);
        defPort.setHorizontalAlignment(JLabel.CENTER);
        defPort.setAlignmentX(CENTER_ALIGNMENT);
        topLabel.add(Box.createVerticalStrut(10));
        topLabel.add(myLocalIP);
        topLabel.add(defPort);
        
        topLabel.add(Box.createVerticalStrut(10));
        launcherPanel.add(topLabel, BorderLayout.NORTH);
        
        //A new JPanel is created to display options widgets.
        JPanel launcherOptions = new JPanel();
        launcherOptions.setLayout(new BoxLayout(launcherOptions, BoxLayout.Y_AXIS));
        
        //Create label and text field for entering the connection IP.
        JLabel ipLabel = new JLabel("IP Address");
        ipLabel.setFont(fntBold);
        ipLabel.setAlignmentX(CENTER_ALIGNMENT);
        launcherOptions.add(ipLabel);
        launcherOptions.add(Box.createVerticalStrut(10));
        JTextField ipField = new JTextField(20);
        ipField.setHorizontalAlignment(JTextField.CENTER);
        ipField.setMaximumSize(new Dimension(200, 20));
        ipField.setActionCommand("setip");
        ipField.addActionListener(this);
        ipField.addFocusListener(fL);
        launcherOptions.add(ipField);
        launcherOptions.add(Box.createVerticalStrut(10));
        
        //Create label and text field for entering the connection target port.
        JLabel portLabel = new JLabel("Target port");
        portLabel.setFont(fntBold);
        portLabel.setAlignmentX(CENTER_ALIGNMENT);
        launcherOptions.add(portLabel);
        launcherOptions.add(Box.createVerticalStrut(10));
        JTextField portField = new JTextField(20);
        //The default JFly port is always 44665.
        portField.setText("44665");
        portField.setHorizontalAlignment(JTextField.CENTER);
        portField.setMaximumSize(new Dimension(100, 20));
        portField.setActionCommand("setport");
        portField.addActionListener(this);
        portField.addFocusListener(fL);
        launcherOptions.add(portField);
        launcherOptions.add(Box.createVerticalStrut(10));
        
        //Create the button to connect to a remote node.
        JButton connectButton = new JButton("Connect");
        connectButton.setToolTipText("Press to connect to a JFly node.");
        connectButton.setActionCommand("connect");
        connectButton.addActionListener(this);
        connectButton.setAlignmentX(CENTER_ALIGNMENT);
        launcherOptions.add(Box.createVerticalStrut(10));
        
        launcherOptions.add(connectButton);
        launcherPanel.add(launcherOptions, BorderLayout.CENTER);
        
        //Create a new JPanel to hold options to launch as an initial JFly cluster node.
        JPanel startSoloPanel = new JPanel();
        startSoloPanel.setLayout(new BoxLayout(startSoloPanel, BoxLayout.Y_AXIS));
        
        //Create explanatory dialogue label.
        JLabel cluster = new JLabel("Alternatively, you can launch the application without connecting to a node and become the first node of your own cluster.");
        cluster.setFont(fntSmall);
        cluster.setAlignmentX(CENTER_ALIGNMENT);
        startSoloPanel.add(cluster);
        startSoloPanel.add(Box.createVerticalStrut(10));
        
        //Create button to launch as an initial node (i.e. start the application without initiating a connection to a remote node).
        JButton clusterButton = new JButton("Launch as an initial node");
        clusterButton.setToolTipText("Press to launch the application as an initial cluster node.");
        clusterButton.setActionCommand("cluster");
        clusterButton.addActionListener(this);
        clusterButton.setAlignmentX(CENTER_ALIGNMENT);
        startSoloPanel.add(clusterButton);
        startSoloPanel.add(Box.createVerticalStrut(10));
        
        //Create button to open additional node lister options dialogue.
        JButton optionsButton = new JButton("Node listener options");
        optionsButton.setToolTipText("Press to view node listener options.");
        optionsButton.setActionCommand("options");
        optionsButton.addActionListener(this);
        optionsButton.setAlignmentX(CENTER_ALIGNMENT);
        startSoloPanel.add(optionsButton);
        startSoloPanel.add(Box.createVerticalStrut(20));
        
        //Create button to exit out of the application.
        JButton closeButton = new JButton("Exit application");
        closeButton.setToolTipText("Press close and exit JFly.");
        closeButton.setActionCommand("close");
        closeButton.addActionListener(this);
        closeButton.setAlignmentX(CENTER_ALIGNMENT);
        startSoloPanel.add(closeButton);
        startSoloPanel.add(Box.createVerticalStrut(50));
        
        launcherPanel.add(startSoloPanel, BorderLayout.SOUTH);
        
        add(launcherPanel);
        
        //Set window size.
        setPreferredSize(new Dimension(1000, 700));
        pack();
        
        //Set window title.
        setTitle("JFly Launcher - Java Facillitates Limitless Yelling");
        setLocationRelativeTo(null);

        //Display launcher window.
        setVisible(true);  // Needed to ensure that the items can be seen.
    }
    Boolean sharedWarningFlag = false;
    //The default JFly port is 44665.
    protected int targetPort = 44665;
    String targetIP = "";
    /**
     * actionPerformed override for handling ActionEvents.
     * @param ae The ActionEvent to be handled.
     */
    @Override
    public void actionPerformed(ActionEvent ae)
    {
        switch(ae.getActionCommand())
        {
            //If options button pressed; display node listener options pane FlyListenOpts
            case "options":
                new FlyListenOpts(myNode);
                break;
            //If connect hutton is pressed;
            case "connect":
                //Ensure port and IP for target are valid.
                if(targetPort == -1 || targetIP.equals(""))
                {
                    if(!sharedWarningFlag)
                    {
                        sharedWarningFlag = true;
                        JOptionPane.showMessageDialog(null, "Please enter a valid target IP address and port number to connect.");
                        sharedWarningFlag = false;
                    }
                }
                else
                {
                    //If target is valid, attempt to connect to remote node and start application by running sendConnectAndOpen() on the JFlyNode with the port/ip parameters.
                    setVisible(false);
                    Runnable connectTask = () -> {
                        Thread.currentThread().setName("Incoming connection listener");
                        try
                        {
                            myNode.sendConnectAndOpen(targetIP, targetPort);
                        }
                        catch(IOException e) { System.out.println(e.toString()); }
                    };
                    new Thread(connectTask).start();
                    //Then dispose of this GUI window.
                    myNode.wipeLauncher(this);
                    dispose();
                }
                break;
            //If "launch as an initial node" is pressed;
            case "cluster":
                setVisible(false);
                Runnable openTask = () -> {
                    Thread.currentThread().setName("Incoming connection listener");
                    try
                    {
                        //Open application and listen for incoming connections by running openReceiveAndWait() on the JFlyNode.
                        myNode.openReceiveAndWait();
                    }
                    catch(IOException e) { System.out.println(e.toString()); }
                };
                new Thread(openTask).start();
                //Then dispose of this GUI window.
                myNode.wipeLauncher(this);
                dispose();
                break;
            //If the user attempts to set a new port, ensure that the format is valid.
            case "setport":
                String portText = ((JTextField)ae.getSource()).getText();
                if(portText.length() == 0) { break; }
                int portNum = -1;
                try
                {
                    portNum = Integer.decode(portText);
                }
                catch(Exception e) {}
                //If the port number is invalid, it is not accepted and a warning is displayed.
                if(portNum > 65535 || portNum < 0)
                {
                    if(!sharedWarningFlag)
                    {
                        sharedWarningFlag = true;
                        JOptionPane.showMessageDialog(null, "Warning: A valid port number must be a positive integer between 0 and 65535 (inclusive).");
                        sharedWarningFlag = false;
                    }
                }
                //If the format is valid then the targetPort variable is newly set.
                else
                {
                    targetPort = portNum;
                }
                break;
            //If the user attempts to set a new connection IP, ensure that the format is valid.
            case "setip":
                String ipText = ((JTextField)ae.getSource()).getText();
                if(ipText.length() == 0) { break; }
                Boolean validIP = false;
                String[] ipBlocks = ipText.split("\\.");
                if(ipBlocks.length == 4)
                {
                    validIP = true;
                    for(int i = 0; i < 4; i++)
                    {
                        try
                        {
                            int ipBlock = Integer.decode(ipBlocks[i]);
                            if(ipBlock >= 0 && ipBlock <= 255) { continue; }
                        }
                        catch(Exception e) {}
                        validIP = false;
                        break;
                    }
                }
                //If the IP number is invalid, it is not accepted and a warning is displayed.
                if(validIP == false)
                {
                    if(!sharedWarningFlag)
                    {
                        sharedWarningFlag = true;
                        JOptionPane.showMessageDialog(null, "Warning: A valid IPv4 address must be denoted by four values from 0-255, separated by period characters.");
                        sharedWarningFlag = false;
                    }
                }
                //If the format is valid then the targetIP variable is newly set.
                else
                {
                    targetIP = ipText;
                }
                break;
            //If the close button is pressed; shutdownNode() is called on the JFlyNode to close the application.
            case "close":
                myNode.shutdownNode();
                break;
        }
    }
}
