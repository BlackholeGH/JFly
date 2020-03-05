/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfly;

import java.awt.BorderLayout;
import static java.awt.Component.CENTER_ALIGNMENT;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import static jfly.FlyLauncher.getLogoIcons;

/**
 *
 * @author Blackhole
 */
public class FlyListenOpts extends FlyLauncher
{
    public FlyListenOpts(JFlyNode node)
    {
        super(node);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        viewOptions();
    }
    public void viewOptions()
    {
        setIconImages(getLogoIcons());
        
        JPanel optionsPanel = new JPanel(new BorderLayout());
        
        JPanel topLabel = new JPanel();
        topLabel.setLayout(new BoxLayout(topLabel, BoxLayout.Y_AXIS));
        
        JLabel introDialog = new JLabel("Node listener options:");
        introDialog.setFont(fntBold);
        introDialog.setHorizontalAlignment(JLabel.CENTER);
        introDialog.setAlignmentX(CENTER_ALIGNMENT);
        topLabel.add(Box.createVerticalStrut(10));
        topLabel.add(introDialog);
        topLabel.add(Box.createVerticalStrut(10));
        optionsPanel.add(topLabel, BorderLayout.NORTH);
        
        JPanel mainOptions = new JPanel();
        mainOptions.setLayout(new BoxLayout(mainOptions, BoxLayout.Y_AXIS));
        
        JLabel listExpl = new JLabel("<html><p align=\"center\">You can set a custom port for JFly to listen to incoming connections on. Note that the default JFly port is 44665. <b>Only change this if you know that you will be receiving connections from a different port.</b></p></html>");
        listExpl.setFont(fntMid);
        listExpl.setAlignmentX(CENTER_ALIGNMENT);
        listExpl.setHorizontalAlignment(JLabel.CENTER);
        mainOptions.add(listExpl);
        mainOptions.add(Box.createVerticalStrut(10));
        
        JLabel portLabel = new JLabel("Listener port");
        portLabel.setFont(fntBold);
        portLabel.setHorizontalAlignment(JLabel.CENTER);
        portLabel.setAlignmentX(CENTER_ALIGNMENT);
        mainOptions.add(portLabel);
        mainOptions.add(Box.createVerticalStrut(10));
        JTextField portField = new JTextField(20);
        portField.setText("44665");
        portField.setHorizontalAlignment(JTextField.CENTER);
        portField.setAlignmentX(CENTER_ALIGNMENT);
        portField.setMaximumSize(new Dimension(100, 20));
        portField.setActionCommand("setport");
        portField.addActionListener(this);
        portField.addFocusListener(fL);
        mainOptions.add(portField);
        mainOptions.add(Box.createVerticalStrut(10));
        
        JLabel iPexpl = new JLabel("<html><p align=\"center\">External connections must also be directed towards your machine's IP address.</p></html>");
        iPexpl.setFont(fntMid);
        iPexpl.setHorizontalAlignment(JLabel.CENTER);
        iPexpl.setAlignmentX(CENTER_ALIGNMENT);
        mainOptions.add(iPexpl);
        mainOptions.add(Box.createVerticalStrut(10));
        
        JLabel locIP = new JLabel("<html><p align=\"center\">Your current local IP address is: <b>" + myNode.hostAddr() + "</b></p></html>");
        locIP.setFont(fnt);
        locIP.setHorizontalAlignment(JLabel.CENTER);
        locIP.setAlignmentX(CENTER_ALIGNMENT);
        mainOptions.add(locIP);
        mainOptions.add(Box.createVerticalStrut(10));
        
        //Maybe check out JEditorPane and HyperLinkListener
        JLabel pubExpl = new JLabel("<html><p align=\"center\">If you have port forwarding enabled, JFly can also accept internet connections targetting your public IP. You can find your public IP at: <b><a href=\"https://whatismyipaddress.com/\">https://whatismyipaddress.com/</a></b></p></html>");
        pubExpl.setFont(fntMid);
        pubExpl.setHorizontalAlignment(JLabel.CENTER);
        pubExpl.setAlignmentX(CENTER_ALIGNMENT);
        mainOptions.add(pubExpl);
        mainOptions.add(Box.createVerticalStrut(10));
        
        JLabel interNode = new JLabel("<html><p align=\"center\">Receiving an external JFly connection will automatically reinitialize your node as an internet node. Be aware that your public IP may be subject to change by your ISP.</p></html>");
        interNode.setFont(fntMid);
        interNode.setAlignmentX(CENTER_ALIGNMENT);
        interNode.setHorizontalAlignment(JLabel.CENTER);
        mainOptions.add(interNode);
        
        mainOptions.setBorder(new EmptyBorder(10,10,10,10));
        
        optionsPanel.add(mainOptions, BorderLayout.CENTER);
        
        add(optionsPanel);
        
        setPreferredSize(new Dimension(600, 500));
        pack();
        
        setTitle("JFly Options");
        setLocationRelativeTo(null);

        setVisible(true);  // Needed to ensure that the items can be seen.
    }
    @Override
    public void actionPerformed(ActionEvent ae)
    {
        switch(ae.getActionCommand())
        {
            case "setport":
                super.actionPerformed(ae);
                myNode.setManualListenPort(targetPort);
                break;
        }
    }
}
