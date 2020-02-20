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
import java.util.HashMap;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.*;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;

/**
 *
 * @author Blackhole
 */
public class FlyInterface extends JFrame implements ActionListener
{
    private JFlyNode myNode;
    public FlyInterface(JFlyNode node, int launchMode)
    {
        myNode = node;
        if(launchMode == 0) { viewLauncher(); }
    }
    FocusVerifier fL = new FocusVerifier();
    Font fnt = new Font("Ariel", Font.PLAIN, 18);
    Font fntBold = new Font("Ariel", Font.BOLD, 18);
    Font fntSmall = new Font("Ariel", Font.PLAIN, 12);
    public void viewLauncher()
    {
        JMenuBar menuBar = new JMenuBar();
        JMenu optionsMen = new JMenu();
        //Creating options for the application.
        optionsMen = new JMenu("Options");
        optionsMen.setToolTipText("Application options dropdown");
        optionsMen.setFont(fnt);
        
        menuBar.add(optionsMen);
        
        this.setJMenuBar(menuBar);
        
        JPanel launcherPanel = new JPanel(new BorderLayout());
        
        JPanel topLabel = new JPanel(new FlowLayout());
        
        JLabel introDialog = new JLabel("Welcome to JFly. Please enter a node IP and access port to connect to a cluster.");
        introDialog.setFont(fnt);
        introDialog.setHorizontalAlignment(JLabel.CENTER);
        topLabel.add(Box.createVerticalStrut(10));
        topLabel.add(introDialog);
        topLabel.add(Box.createVerticalStrut(30));
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
        startSoloPanel.add(Box.createVerticalStrut(50));
        
        launcherPanel.add(startSoloPanel, BorderLayout.SOUTH);
        
        add(launcherPanel);
        
        setPreferredSize(new Dimension(1000, 400));
        pack();
        
        setTitle("JFly Launcher - Java Facillitates Limitless Yelling");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setVisible(true);  // Needed to ensure that the items can be seen.
    }
    Boolean sharedWarningFlag = false;
    int targetPort = -1;
    String targetIP = "";
    @Override
    public void actionPerformed(ActionEvent ae)
    {
        switch(ae.getActionCommand())
        {
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
                    try
                    {
                        myNode.sendConnectAndOpen(targetIP, targetPort);
                    }
                    catch(IOException e) { System.out.println(e.toString()); }
                }
                break;
            case "cluster":
                setVisible(false);
                Runnable openTask = () -> {
                    Thread.currentThread().setName("Incoming connection listener");
                    try
                    {
                        myNode.openReceiveAndWait(-1);
                    }
                    catch(IOException e) { System.out.println(e.toString()); }
                };
                openTask.run();
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
        }
    }
}
