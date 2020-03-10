package jfly;
import com.sun.corba.se.impl.naming.namingutil.CorbalocURL;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.text.DecimalFormat;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * The FlyChatGUI extends JFrame and is the main user interface component for the JFly chat program.
 * @author gk8560 and dg7239p
 */
public class FlyChatGUI extends JFrame {

//**************************************************************************************

    JScrollPane sp; 
    JScrollPane scrollPane1;  
    JTable table;
    JTableHeader header;
    JPanel north;
    JPanel inNorth;   
    TitledBorder borderTitle;
    TitledBorder borderTitle2;  
    JButton sendButton;
    JButton exiButton;
    JFrame mainFrame;
    JPanel East;
    JLabel Title;      
    JTextField msgBox;
    JTextArea txtArea1; 
    JMenuBar menuBar;
    JPanel mainPanel;
    JMenuItem menuItem;
    JMenuItem menuItem2;
    JMenuItem menuItem3;
   
    Color white = Color.WHITE;
    Color black = Color.BLACK;
    
    String data[][] = {};
    String[] columnNames = {};
    
    Font fnt = new Font("Century Gothic", Font.PLAIN, 20);
    Font fnt2 = new Font("Century Gothic", Font.PLAIN, 15);
    Font fnt3 = new Font("Century Gothic", Font.PLAIN, 40);
    

    JFlyNode myNode = null;
    /**
     * The FlyChatGUI constructor.
     * @param jNode The associated JFlyNode for this FlyChatGUI.
     */
    public FlyChatGUI(JFlyNode jNode) { //making the constructor
        addWindowListener(FlyLauncher.getExitListener(jNode));
        myNode = jNode;
        model();
        view();
    }
    /**
     * Sets the GUI elements to the dark colour theme.
     */
    public void setdarkcolor() {
        setBackground(black);
        Title.setForeground(Color.black);
        inNorth.setBackground(Color.pink);
        msgBox.setBackground(black);
        sp.setForeground(black);
        sp.getViewport().setBackground(black);
        scrollPane1.getVerticalScrollBar().setBackground(black);
        txtArea1.setBackground(black);
        txtArea1.setForeground(Color.pink);

        table.setBackground(black);
        table.setForeground(Color.pink);
        header.setForeground(Color.PINK);

        header.setBackground(Color.black);
        borderTitle2.setTitleColor(Color.pink);
        //     header.setBackground(black);
        mainFrame.repaint();
        mainFrame.revalidate();

    }
    /**
     * Sets the GUI elements to the light colour theme.
     */
    public void setlightcolor() {
        setBackground(white);

        Title.setBackground(black);
        Title.setForeground(Color.pink);
        msgBox.setBackground(Color.black);

        sp.setForeground(white);
        sp.getViewport().setBackground(Color.pink);
        scrollPane1.getVerticalScrollBar().setBackground(white);
        txtArea1.setBackground(Color.pink);
        txtArea1.setForeground(black);
        table.setBackground(Color.pink);
        table.setForeground(black);
//        header.setBackground(white);
        inNorth.setBackground(black);
        borderTitle.setTitleColor(Color.PINK);
        header.setForeground(Color.black);
        header.setBackground(Color.PINK);
        borderTitle2.setTitleColor(Color.pink);
        mainFrame.repaint();
        mainFrame.revalidate();

    }
        public void setcolour(Color FG, Color BG){
                      
        }

    public void setlightcolor(String light, String dark) {
        setBackground(white);

    }

    ListenForButton lforButton = new ListenForButton();
    /**
     * Creates button elements for the GUI frame.
     */
    private void model() { 

       


//Setting up the JMenu Options
//-------------------------------------------------------------------------------------------
        menuBar = new JMenuBar();
        JMenu menu  = new JMenu("Options");
        menu.setFont(fnt);
        menuItem  = new JMenuItem("LightMode");
        menuItem2 = new JMenuItem("DarkMode");
        menuItem3 = new JMenuItem("Export Blockchain");
        menuItem3.setFont(fnt2);
        menuItem.addActionListener(lforButton);
        menuItem2.addActionListener(lforButton);
        menuItem3.addActionListener(lforButton);
        menu.add(menuItem);
        menu.add(menuItem2);
        menu.add(menuItem3);
        menuBar.add(menu);
        
        

//Setting up the button to send messages, as well as exiting the program    
//-------------------------------------------------------------------------------------------
        sendButton = new JButton();
        sendButton.setFont(fnt);
        sendButton.setToolTipText("Sends your message");
        sendButton.setText("Send");
        sendButton.addActionListener(lforButton);

        exiButton = new JButton();
        exiButton.setFont(fnt2);
        exiButton.setToolTipText("Exits the application");
        exiButton.setText("Exit");
        exiButton.addActionListener(lforButton);
        
        

    }

    /**
     * Closes the main GUI frame.
     */
    public void closeMainframe() //method to close the mainframe
    {
        mainFrame.dispose();
    }
    
    /**
     * Creates and displays the FlyChatGUI user interface pane.
     */
    private void view() {
        
        

//using Borderfactory to create a titled border, defining it's appropriate title 
//-------------------------------------------------------------------------------------------
       
        borderTitle2 = BorderFactory.createTitledBorder("User List");
        borderTitle2.setTitleColor(Color.pink);
        borderTitle2.setTitleJustification(borderTitle.CENTER);
        borderTitle = BorderFactory.createTitledBorder("Main Chat");
        borderTitle.setTitleColor(Color.pink);
        borderTitle.setTitleJustification(borderTitle.CENTER);

//-------------------------------------------------------------------------------------------

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
//-------------------------------------------------------------------------------------------

//configuring the mainframe
//-------------------------------------------------------------------------------------------
        mainFrame = new JFrame("JFly - Java Facillitates Limitless Yelling");
        mainFrame.setIconImages(FlyLauncher.getLogoIcons());
        mainFrame.setSize(1050, 700);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setResizable(false);
        mainFrame.setBackground(Color.PINK);
        mainFrame.setVisible(true);
        mainFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        mainFrame.addWindowListener(FlyLauncher.getExitListener(myNode)); //adding a listener
        mainFrame.setJMenuBar(menuBar);
        setBackground(Color.PINK);
//-------------------------------------------------------------------------------------------

//setting up a North and inNorth Jpanel for the title, logo and buttons to go into
//-------------------------------------------------------------------------------------------

        north = new JPanel(new BorderLayout());
        inNorth = new JPanel(new FlowLayout());
        inNorth.setBackground(Color.pink);
        JLabel logo = new JLabel(new ImageIcon(FlyLauncher.getLogoIcon(40, 40)));
        logo.setHorizontalAlignment(JLabel.RIGHT);
        inNorth.add(logo);
        Title = new JLabel("JFly");
        Title.setFont(fnt3);
        Title.setForeground(Color.black);
        Title.setHorizontalAlignment(JLabel.LEFT);
        inNorth.add(Title);
        north.add(inNorth, BorderLayout.CENTER);
        mainFrame.add(north, BorderLayout.NORTH);
//-------------------------------------------------------------------------------------------

//setting up a south Jpanel for stuff to go into, then adding it to mainframe
//-------------------------------------------------------------------------------------------
        JPanel South = new JPanel(new BorderLayout()); //uses a flow layout
        South.setBackground(Color.PINK);
        msgBox = new JTextField("");
        msgBox.setBorder(BorderFactory.createCompoundBorder(msgBox.getBorder(), BorderFactory.createEmptyBorder(0,10,0,0)));
        msgBox.addActionListener(lforButton);
        msgBox.setBackground(Color.BLACK);
        msgBox.setForeground(Color.WHITE);
        msgBox.setFont(fnt2);
        South.add(msgBox);
        mainFrame.add(South, BorderLayout.SOUTH);

        

//Configuring the scrollpane for the user list table ot go into
//--------------------------------------------------------------------------------------------------------------------
       
        sp = new JScrollPane(); //adding table to the scrollpane
        sp.setPreferredSize(new Dimension(260, 450));
        sp.setForeground(Color.black);
        sp.getViewport().setBackground(Color.black);
        sp.setBorder(borderTitle2);
        sp.setBackground(Color.BLACK);
        updateTable(data);
    
//Configuring the east panel for the table scroll pane to go into
//--------------------------------------------------------------------------------------------------------------------
        East = new JPanel(new BorderLayout());
        East.setBackground(Color.PINK);
        East.add(sp);
        mainFrame.add(East, BorderLayout.EAST);
//--------------------------------------------------------------------------------------------------------------------



//setting up the textarea for messages to be displayed
//--------------------------------------------------------------------------------------------------------------------
        txtArea1 = new JTextArea(10, 20);
        txtArea1.setLineWrap(true); 
        txtArea1.setWrapStyleWord(true);
        scrollPane1 = new JScrollPane(txtArea1, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);//adding txt area to scroll pane
        scrollPane1.getVerticalScrollBar().setBackground(Color.BLACK);
        txtArea1.setFont(fnt2);
        txtArea1.setForeground(Color.pink);
        txtArea1.setBackground(Color.BLACK);
        txtArea1.setEditable(false);
        scrollPane1.setBorder(borderTitle);    
        scrollPane1.setBackground(Color.black);
//--------------------------------------------------------------------------------------------------------------------


// setting up the dimensions for a button and the SP   
//--------------------------------------------------------------------------------------------------------------------
        sendButton.setBounds(410, 150, 120, 50);
        scrollPane1.setBounds(0, 0, 785, 532);
       
    
 //settin up a Jpanel mainPanel as the main panel for things to go into, and adding componants to mainPanel  
//--------------------------------------------------------------------------------------------------------------------
        mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setBackground(Color.pink);  
        South.add(sendButton, BorderLayout.EAST);
        north.add(exiButton, BorderLayout.EAST);
        mainPanel.add(scrollPane1);    
        mainFrame.add(mainPanel);
        mainFrame.revalidate();
        mainFrame.repaint();

        
    }
//Method to add my headers to our Jtable
//--------------------------------------------------------------------------------------------------------------------  
    public void updateTable(Object data[][])
    {
        updateTable(data, new String[] {"Users", "User IDs"});
    }
  
//Method to add my values to our Jtable, whilst customising the layout
//--------------------------------------------------------------------------------------------------------------------  
    public void updateTable(Object data[][], String[] Columns)
    {
        DefaultTableModel model = new DefaultTableModel(data, Columns);
        table = new JTable(model);
        table.setBackground(Color.black);
        table.setForeground(Color.pink);
        header = table.getTableHeader(); //for colour
        header.setBackground(Color.black);
        header.setForeground(Color.PINK);
        table.setRowHeight(30);
        table.getColumnModel().getColumn(0).setMinWidth(150);
        sp.setViewportView(table);      
    }

//Method to add my values to our textarea.
//--------------------------------------------------------------------------------------------------------------------  
    public void remoteSetTextBox(String[] text)
    {
        String s = "";
        for(int i = 0; i < text.length; i++)
        {
            s += text[i] + "\n";
        }
        txtArea1.setText(s);
        //repaint();
        //System.out.println("Attempted text write...");
    }
    

//Listener class
//--------------------------------------------------------------------------------------------------------------------   
    /**
     * The ListenForButton class implements ActionListener and KeyListener, and contains methods to handle ActionEvents generated by the GUI.
     */
    public class ListenForButton implements ActionListener, KeyListener { 

        /**
         * actionPerformed override to handle ActionEvents.
         * @param e The ActionEvent to be handled.
         */
        @Override
        public void actionPerformed(ActionEvent e) {



//actions to set light and dark mode in the menu bar options
//--------------------------------------------------------------------------------------------------------------------
            if(e.getSource() == menuItem){             
                setlightcolor();               
            }
            if (e.getSource() == menuItem2) {
                setdarkcolor();

            }        
            
//button to exit the program 
//--------------------------------------------------------------------------------------------------------------------
            if (e.getSource() == exiButton) {
                try {
                    myNode.shutdownNode();

                } catch (Exception fj) {
                    JOptionPane.showMessageDialog(rootPane, "Error");
                }
            }
            
//Events to send the message when clicking the send putton, or clicking enter whilst focused on the msgBox
//--------------------------------------------------------------------------------------------------------------------
            if (e.getSource() == sendButton || e.getSource() == msgBox) {
                String msg = msgBox.getText().toString();
                if(!msg.isEmpty())
                {
                    myNode.sendMessage(msg);
                    msgBox.setText("");
                }
            }

        }//PB actioned performed                

        /**
         * keyTyped() override to implement KeyListener.
         * @param e The KeyEvent to be handled.
         */
        @Override
        public void keyTyped(KeyEvent e) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        /**
         * keyPressed() override to implement KeyListener.
         * @param e The KeyEvent to be handled.
         */
        @Override
        public void keyPressed(KeyEvent e) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        /**
         * keyReleased() override to implement KeyListener.
         * @param e The KeyEvent to be handled.
         */
        @Override
        public void keyReleased(KeyEvent e) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }//the listener class
}//Public Class

