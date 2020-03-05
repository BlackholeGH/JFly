package jfly;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.text.DecimalFormat;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class FlyChatGUI extends JFrame {

//**************************************************************************************

    JScrollPane sp;
    JScrollPane sp2;
    JScrollPane sp3;
    JScrollPane sp4;
    JScrollPane sp5;
    JScrollPane scrollPane1;
    JTable table;
    JTable table1;
    JTable table2;
    JTable table3;
    JTableHeader header;
    JPanel north;
    JPanel inNorth;
    TitledBorder borderTitle;

    JButton sendButton;
    JButton exiButton;
    JFrame mainFrame;
    JPanel East;
    JLabel Title;
    JButton clear;
    JTextField textField1;
    JTextField msgBox;
    JTextArea txtArea1; 
    JMenuBar menuBar;
    JPanel mainPanel;
    JMenuItem menuItem;
    JMenuItem menuItem2;
    DecimalFormat decimalFormat;
    Color white = Color.WHITE;
    Color black = Color.BLACK;
    
//**************************************************************************************

//**************************************************************************************
    String[] optNames = {"Option1", "Option2", "Option3"};
    String[] rootNames = {"Newton-Raphson", "Secant", "Bisection", "Research"};
//**************************************************************************************
    ArrayList bob;
    //Jtable Stuff
//**************************************************************************************
    String data[][] = {};

    String[] columnNames = {};
//**************************************************************************************

    /*
     String crse = "";
     String crsework = "";
     String courseReq = "";
     String allCourse = "";
     /*/
    Font fnt = new Font("Century Gothic", Font.PLAIN, 20);
    Font fnt2 = new Font("Century Gothic", Font.PLAIN, 15);
    Font fnt3 = new Font("Century Gothic", Font.PLAIN, 40);
    

    ArrayList<Double> xvalueArr = new ArrayList();
    ArrayList<Double> yvalueArr = new ArrayList();

    //GUI prg =
    // Using MVC
    JFlyNode myNode = null;
    public FlyChatGUI(JFlyNode jNode) { //making the constructor
        addWindowListener(FlyLauncher.getExitListener(jNode));
        myNode = jNode;
        model();
        view();
    }
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
   //     header.setBackground(black);
        mainFrame.repaint();
        mainFrame.revalidate();

    }
    
        public void setlightcolor() {
        setBackground(white);
        
        Title.setBackground(black);
        Title.setForeground(Color.pink);
        msgBox.setBackground(Color.pink);
        sp.setForeground(white);
        sp.getViewport().setBackground(Color.pink);
       scrollPane1.getVerticalScrollBar().setBackground(white);
        txtArea1.setBackground(Color.pink); 
        txtArea1.setForeground(Color.WHITE);
        table.setBackground(Color.pink);
        table.setForeground(white);
//        header.setBackground(white);
        inNorth.setBackground(black);
        borderTitle.setTitleColor(Color.white);
        header.setForeground(Color.WHITE);
        mainFrame.repaint();
        mainFrame.revalidate();

    }

    public void setlightcolor(String light, String dark) {
        setBackground(white);

    }

    ListenForButton lforButton = new ListenForButton();
    private void model() { //creating an object for action listener

        //Configuring the Function Jcombobox defined above as class variable
//****************************************************************************************************************

//****************************************************************************************************************

        menuBar = new JMenuBar();
        JMenu menu  = new JMenu("Options");
        menu.setFont(fnt);
        menuItem  = new JMenuItem("LightMode");
        menuItem2 = new JMenuItem("DarkMode");
        menuItem.addActionListener(lforButton);
        menuItem2.addActionListener(lforButton);
        menu.add(menuItem);
        menu.add(menuItem2);
        menuBar.add(menu);
        
        

     
//****************************************************************************************************************
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

    public void closeMainframe()
    {
        mainFrame.dispose();
    }
    
    private void view() {
        
        

        //using Borderfactory to create a titled border, at setting its position
//===================================================================================================================
        borderTitle = BorderFactory.createTitledBorder("Main Chat");
        borderTitle.setTitleColor(Color.pink);
        borderTitle.setTitleJustification(borderTitle.CENTER);

//===================================================================================================================

        //tk can be used to get screen size, GUI stuff
        Toolkit tk = Toolkit.getDefaultToolkit();

        //this makes the mainframe for all the panels to be placed within
        //Configuring up the frame
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
//===================================================================================================================
        mainFrame = new JFrame("JFly - Java Facillitates Limitless Yelling");
        mainFrame.setIconImages(FlyLauncher.getLogoIcons());
        mainFrame.setSize(1050, 700);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setResizable(false);
        mainFrame.setBackground(Color.PINK);
        mainFrame.setVisible(true);
        mainFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        mainFrame.addWindowListener(FlyLauncher.getExitListener(myNode));
        mainFrame.setJMenuBar(menuBar);
        setBackground(Color.PINK);
//===================================================================================================================

        //setting up a North Jpanel for the title to go into, then adding it to mainframe
//===================================================================================================================

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
//===================================================================================================================

        //setting up a south Jpanel for stuff to go into, then adding it to mainframe
//===================================================================================================================
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

        
        //JTable attributes
//--------------------------------------------------------------------------------------------------------------------
       
        sp = new JScrollPane(); //adding table to the scrollpane
        //table.setPreferredScrollableViewportSize(table.getPreferredSize());
        sp.setPreferredSize(new Dimension(260, 30));
        sp.setForeground(Color.black);
        sp.getViewport().setBackground(Color.black);
        updateTable(data);
    

        East = new JPanel(new BorderLayout());//(new BorderLayout()); //uses a flow layout
        East.setBackground(Color.PINK);
        East.add(sp);
        mainFrame.add(East, BorderLayout.EAST);
//--------------------------------------------------------------------------------------------------------------------



        //set up of a textArea1 (x+f(x) with scroll pane
//--------------------------------------------------------------------------------------------------------------------
        txtArea1 = new JTextArea(10, 20);
        txtArea1.setLineWrap(true); //skips to second line
        txtArea1.setWrapStyleWord(true);
        scrollPane1 = new JScrollPane(txtArea1, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);//adding txt area to scroll pane
        scrollPane1.getVerticalScrollBar().setBackground(Color.BLACK);
        txtArea1.setFont(fnt2);
        txtArea1.setForeground(Color.pink); //Text colour
        txtArea1.setBackground(Color.BLACK);
        txtArea1.setEditable(false);
        scrollPane1.setBorder(borderTitle); //adds a border to the scroll pane    
        scrollPane1.setBackground(Color.black);
//--------------------------------------------------------------------------------------------------------------------


        //Setting x,y,width,height positions for GUI features
//--------------------------------------------------------------------------------------------------------------------
        sendButton.setBounds(410, 150, 120, 50);
        scrollPane1.setBounds(0, 0, 785, 542);
    
     

//--------------------------------------------------------------------------------------------------------------------
        //settin up a Jpanel mainPanel as the main panel for things to go into
        //Adding componants to mainPanel
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
    //Method to add my values to a Jtable via arraylist
    //--------------------------------------------------------------------------------------------------------------------  
    public void updateTable(Object data[][])
    {
        updateTable(data, new String[] {"Users", "User IDs"});
    }
    
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
    

    //--------------------------------------------------------------------------------------------------------------------   

    //Listeners 
    public class ListenForButton implements ActionListener, KeyListener {

        Font fnt = new Font("Georgia", Font.PLAIN, 20);

        @Override
        public void actionPerformed(ActionEvent e) {



            //button to clear jtext1
            if(e.getSource() == menuItem){
                JOptionPane.showMessageDialog(rootPane, "clicked!");
                setlightcolor();
                
            }
            if (e.getSource() == menuItem2) {
                JOptionPane.showMessageDialog(rootPane, "clickeddxrfxgbfgb!");
                setdarkcolor();

            }
            //--------------------------------------------------------------------------------------------------------------------
            if (e.getSource() == clear && txtArea1.getText().isEmpty()) {
                JOptionPane.showMessageDialog(rootPane, "Already Clear");
            } else if (e.getSource() == clear) {
                txtArea1.setText("");
                JOptionPane.showMessageDialog(rootPane, "Cleared");
            }
//--------------------------------------------------------------------------------------------------------------------

            //button for graph
//--------------------------------------------------------------------------------------------------------------------
            if (e.getSource() == exiButton) {
                try {
                    myNode.shutdownNode();

                } catch (Exception fj) {
                    JOptionPane.showMessageDialog(rootPane, "Error");
                }
            }

//--------------------------------------------------------------------------------------------------------------------
            //Events for calculating the functions
//--------------------------------------------------------------------------------------------------------------------
            if (e.getSource() == sendButton || e.getSource() == msgBox) {
                String msg = msgBox.getText().toString();
                if(!msg.isEmpty())
                {
                    myNode.sendMessage(msg);
                    msgBox.setText("");
                }
            }
//--------------------------------------------------------------------------------------------------------------------

        }//PB actioned performed

        @Override
        public void keyTyped(KeyEvent e) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyChar() == 38 && msgBox.hasFocus()){
                JOptionPane.showMessageDialog(rootPane, "up arrow");
               
                
            }
            
                
            
        }

        @Override
        public void keyReleased(KeyEvent e) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
    }//the listener class
}//Public Class
