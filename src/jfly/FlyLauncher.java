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
 *
 * @author Blackhole
 */
public class FlyLauncher extends JFrame implements ActionListener
{
    protected JFlyNode myNode;
    public FlyLauncher(JFlyNode node)
    {
        myNode = node;
        if(!(this instanceof FlyListenOpts))
        {
            setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            addWindowListener(FlyLauncher.getExitListener(node));
            viewLauncher();
        }
    }
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
    FocusVerifier fL = new FocusVerifier();
    Font fnt = new Font("Ariel", Font.PLAIN, 18);
    Font fntBold = new Font("Ariel", Font.BOLD, 18);
    Font fntMid = new Font("Ariel", Font.PLAIN, 14);
    Font fntSmall = new Font("Ariel", Font.PLAIN, 12);
    static String fLogo = System.getProperty("user.dir") + "\\Img\\JFlogo2.png";
    static String fLogoSimple = System.getProperty("user.dir") + "\\Img\\JFlogoSimplified.png";
    public static Image getLogoIcon()
    {
        return getLogoIcon(50, 50);
    }
    public static Image getLogoIcon(int width, int height)
    {
        try
        {
            BufferedImage logoPic = ImageIO.read(new File(fLogo));
            return logoPic.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        }
        catch(IOException e) { return null; }
    }
    public static Image getSimpleLogoIcon(int width, int height)
    {
        try
        {
            BufferedImage logoPic = ImageIO.read(new File(fLogoSimple));
            return logoPic.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        }
        catch(IOException e) { return null; }
    }
    public static ArrayList<Image> getLogoIcons()
    {
        ArrayList<Image> out = new ArrayList();
        out.add(getSimpleLogoIcon(64, 64));
        out.add(getSimpleLogoIcon(32, 32));
        out.add(getSimpleLogoIcon(16, 16));
        return out;
    }
    public void viewLauncher()
    {
        setIconImages(getLogoIcons());
        JMenuBar menuBar = new JMenuBar();
        JMenu optionsMen = new JMenu();
        //Creating options for the application.
        optionsMen = new JMenu("Options");
        optionsMen.setToolTipText("Application options dropdown");
        optionsMen.setFont(fnt);
        
        menuBar.add(optionsMen);
        
        //this.setJMenuBar(menuBar);
        
        JPanel launcherPanel = new JPanel(new BorderLayout());
        
        JPanel topLabel = new JPanel();
        topLabel.setLayout(new BoxLayout(topLabel, BoxLayout.Y_AXIS));
        
        topLabel.add(Box.createVerticalStrut(20));
        System.out.println(System.getProperty("user.dir"));
        try
        {
            JLabel logoLabel = new JLabel(new ImageIcon(getLogoIcon(150, 150)));
            logoLabel.setAlignmentX(CENTER_ALIGNMENT);
            topLabel.add(logoLabel);
            topLabel.add(Box.createVerticalStrut(10));
        }
        catch(Exception e) {}
        
        JLabel introDialog = new JLabel("Welcome to JFly. Please enter a node IP and access port to connect to a cluster.");
        introDialog.setFont(fnt);
        introDialog.setHorizontalAlignment(JLabel.CENTER);
        introDialog.setAlignmentX(CENTER_ALIGNMENT);
        topLabel.add(Box.createVerticalStrut(10));
        topLabel.add(introDialog);
        
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
        
        JPanel launcherOptions = new JPanel();
        launcherOptions.setLayout(new BoxLayout(launcherOptions, BoxLayout.Y_AXIS));
        
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
        
        JLabel portLabel = new JLabel("Target port");
        portLabel.setFont(fntBold);
        portLabel.setAlignmentX(CENTER_ALIGNMENT);
        launcherOptions.add(portLabel);
        launcherOptions.add(Box.createVerticalStrut(10));
        JTextField portField = new JTextField(20);
        portField.setText("44665");
        portField.setHorizontalAlignment(JTextField.CENTER);
        portField.setMaximumSize(new Dimension(100, 20));
        portField.setActionCommand("setport");
        portField.addActionListener(this);
        portField.addFocusListener(fL);
        launcherOptions.add(portField);
        launcherOptions.add(Box.createVerticalStrut(10));
        
        JButton connectButton = new JButton("Connect");
        connectButton.setToolTipText("Press to connect to a JFly node.");
        connectButton.setActionCommand("connect");
        connectButton.addActionListener(this);
        connectButton.setAlignmentX(CENTER_ALIGNMENT);
        launcherOptions.add(Box.createVerticalStrut(10));
        
        launcherOptions.add(connectButton);
        launcherPanel.add(launcherOptions, BorderLayout.CENTER);
        
        JPanel startSoloPanel = new JPanel();
        startSoloPanel.setLayout(new BoxLayout(startSoloPanel, BoxLayout.Y_AXIS));
        
        JLabel cluster = new JLabel("Alternatively, you can launch the application without connecting to a node and become the first node of your own cluster.");
        cluster.setFont(fntSmall);
        cluster.setAlignmentX(CENTER_ALIGNMENT);
        startSoloPanel.add(cluster);
        startSoloPanel.add(Box.createVerticalStrut(10));
        
        JButton clusterButton = new JButton("Launch as an initial node");
        clusterButton.setToolTipText("Press to launch the application as an initial cluster node.");
        clusterButton.setActionCommand("cluster");
        clusterButton.addActionListener(this);
        clusterButton.setAlignmentX(CENTER_ALIGNMENT);
        startSoloPanel.add(clusterButton);
        startSoloPanel.add(Box.createVerticalStrut(10));
        
        JButton optionsButton = new JButton("Node listener options");
        optionsButton.setToolTipText("Press to view node listener options.");
        optionsButton.setActionCommand("options");
        optionsButton.addActionListener(this);
        optionsButton.setAlignmentX(CENTER_ALIGNMENT);
        startSoloPanel.add(optionsButton);
        startSoloPanel.add(Box.createVerticalStrut(20));
        
        JButton closeButton = new JButton("Exit application");
        closeButton.setToolTipText("Press close and exit JFly.");
        closeButton.setActionCommand("close");
        closeButton.addActionListener(this);
        closeButton.setAlignmentX(CENTER_ALIGNMENT);
        startSoloPanel.add(closeButton);
        startSoloPanel.add(Box.createVerticalStrut(50));
        
        launcherPanel.add(startSoloPanel, BorderLayout.SOUTH);
        
        add(launcherPanel);
        
        setPreferredSize(new Dimension(1000, 700));
        pack();
        
        setTitle("JFly Launcher - Java Facillitates Limitless Yelling");
        setLocationRelativeTo(null);

        setVisible(true);  // Needed to ensure that the items can be seen.
    }
    Boolean sharedWarningFlag = false;
    protected int targetPort = 44665;
    String targetIP = "";
    @Override
    public void actionPerformed(ActionEvent ae)
    {
        switch(ae.getActionCommand())
        {
            case "options":
                new FlyListenOpts(myNode);
                break;
            case "connect":
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
                    myNode.wipeLauncher(this);
                    dispose();
                }
                break;
            case "cluster":
                setVisible(false);
                Runnable openTask = () -> {
                    Thread.currentThread().setName("Incoming connection listener");
                    try
                    {
                        myNode.openReceiveAndWait();
                    }
                    catch(IOException e) { System.out.println(e.toString()); }
                };
                new Thread(openTask).start();
                myNode.wipeLauncher(this);
                dispose();
                break;
            case "setport":
                String portText = ((JTextField)ae.getSource()).getText();
                if(portText.length() == 0) { break; }
                int portNum = -1;
                try
                {
                    portNum = Integer.decode(portText);
                }
                catch(Exception e) {}
                if(portNum > 65535 || portNum < 0)
                {
                    if(!sharedWarningFlag)
                    {
                        sharedWarningFlag = true;
                        JOptionPane.showMessageDialog(null, "Warning: A valid port number must be a positive integer between 0 and 65535 (inclusive).");
                        sharedWarningFlag = false;
                    }
                }
                else
                {
                    targetPort = portNum;
                }
                break;
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
                if(validIP == false)
                {
                    if(!sharedWarningFlag)
                    {
                        sharedWarningFlag = true;
                        JOptionPane.showMessageDialog(null, "Warning: A valid IPv4 address must be denoted by four values from 0-255, separated by period characters.");
                        sharedWarningFlag = false;
                    }
                }
                else
                {
                    targetIP = ipText;
                }
                break;
            case "close":
                myNode.shutdownNode();
                break;
        }
    }
}
