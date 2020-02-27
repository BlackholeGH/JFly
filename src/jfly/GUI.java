package jfly;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.text.DecimalFormat;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class GUI extends JFrame {

//**************************************************************************************

    JScrollPane sp;
    JScrollPane sp2;
    JScrollPane sp3;
    JScrollPane sp4;
    JScrollPane sp5;
    JTable table;
    JTable table1;
    JTable table2;
    JTable table3;


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
    DecimalFormat decimalFormat;
//**************************************************************************************

//**************************************************************************************
    String[] optNames = {"Option1", "Option2", "Option3"};
    String[] rootNames = {"Newton-Raphson", "Secant", "Bisection", "Research"};
//**************************************************************************************
    ArrayList bob;
    //Jtable Stuff
//**************************************************************************************
    String data[][] = {{"x", "x", "x", "x"},
    {"x", "x", "x", "x"},
    {"x", "x", "x", "x"},
    {"x", "x", "x", "x"}};

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
    public GUI(JFlyNode jNode) { //making the constructor
        addWindowListener(FlyInterface.getExitListener(jNode));
        myNode = jNode;
        model();
        view();
    }

    ListenForButton lforButton = new ListenForButton();
    private void model() { //creating an object for action listener

        //Configuring the Function Jcombobox defined above as class variable
//****************************************************************************************************************

//****************************************************************************************************************

        menuBar = new JMenuBar();
        JMenu menu  = new JMenu("Options");
        menu.setFont(fnt);
        menuItem  = new JMenuItem("Option1");
        menuItem.addActionListener(lforButton);
        menu.add(menuItem);
        menuBar.add(menu);
        
        

     
//****************************************************************************************************************
        sendButton = new JButton();
        sendButton.setFont(fnt);
        sendButton.setToolTipText("Sends your message");
        sendButton.setText("Send");
        sendButton.addActionListener(lforButton);

        exiButton = new JButton();
        exiButton.setFont(fnt);
        exiButton.setToolTipText("Exits the application");
        exiButton.setText("Exit");
        exiButton.addActionListener(lforButton);

    }

    private void view() {

        //using Borderfactory to create a titled border, at setting its position
//===================================================================================================================
        TitledBorder borderTitle = BorderFactory.createTitledBorder("Main Chat");
        borderTitle.setTitleColor(Color.pink);
        borderTitle.setTitleJustification(borderTitle.CENTER);

//===================================================================================================================

        //tk can be used to get screen size, GUI stuff
        Toolkit tk = Toolkit.getDefaultToolkit();

        //this makes the mainframe for all the panels to be placed within
        //Configuring up the frame
//===================================================================================================================
        mainFrame = new JFrame("JFly - Java Facillitates Limitless Yelling");
        mainFrame.setIconImage(FlyInterface.getLogoIcon());
        mainFrame.setSize(1050, 700);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setResizable(false);
        mainFrame.setBackground(Color.PINK);
        mainFrame.setVisible(true);
        mainFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        mainFrame.addWindowListener(FlyInterface.getExitListener(myNode));
        mainFrame.setJMenuBar(menuBar);
        setBackground(Color.PINK);
//===================================================================================================================

        //setting up a North Jpanel for the title to go into, then adding it to mainframe
//===================================================================================================================
        JPanel north = new JPanel(new FlowLayout()); //used a flow layout
        north.setBackground(Color.pink);
        Title = new JLabel("JFly");
        Title.setFont(fnt3);
        north.add(Title, BorderLayout.NORTH);       
        mainFrame.add(north, BorderLayout.NORTH);
//===================================================================================================================

        //setting up a south Jpanel for stuff to go into, then adding it to mainframe
//===================================================================================================================
        JPanel South = new JPanel(new BorderLayout()); //uses a flow layout
        South.setBackground(Color.PINK);
        msgBox = new JTextField("");
        msgBox.addActionListener(lforButton);
        South.add(msgBox);
        mainFrame.add(South, BorderLayout.SOUTH);

        
        //JTable attributes
//--------------------------------------------------------------------------------------------------------------------      
        String[] columnNames5 = {"Username", "ID"};//,"f(x(i-1))","f(x(i))","x(i+1)"}; 
        table = new JTable(data, columnNames5);
        table.setBackground(Color.pink);

        JTableHeader header = table.getTableHeader(); //for colour
        header.setBackground(Color.black);
        header.setForeground(Color.PINK);
       
        sp = new JScrollPane(table); //adding table to the scrollpane
        //table.setPreferredScrollableViewportSize(table.getPreferredSize());
        table.setRowHeight(30);
        sp.setPreferredSize(new Dimension(250, 600));
        sp.setForeground(Color.black);
        sp.getViewport().setBackground(Color.PINK);
    

        East = new JPanel(new BorderLayout());//(new BorderLayout()); //uses a flow layout
        East.setBackground(Color.PINK);
        East.add(sp);
        mainFrame.add(East, BorderLayout.EAST);
//--------------------------------------------------------------------------------------------------------------------



        //set up of a textArea1 (x+f(x) with scroll pane
//--------------------------------------------------------------------------------------------------------------------
        txtArea1 = new JTextArea(153, 20);
        txtArea1.setLineWrap(true); //skips to second line
        txtArea1.setWrapStyleWord(true);
        JScrollPane scrollPane1 = new JScrollPane(txtArea1, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);//adding txt area to scroll pane
        txtArea1.setFont(fnt2);
        txtArea1.setForeground(Color.BLACK); //Text colour
        txtArea1.setBackground(Color.pink);
        scrollPane1.setBorder(borderTitle); //adds a border to the scroll pane    
        scrollPane1.setBackground(Color.black);
//--------------------------------------------------------------------------------------------------------------------


        //Setting x,y,width,height positions for GUI features
//--------------------------------------------------------------------------------------------------------------------
        sendButton.setBounds(410, 150, 120, 50);
        scrollPane1.setBounds(0, 0, 785, 570);
    
     

//--------------------------------------------------------------------------------------------------------------------
        //settin up a Jpanel mainPanel as the main panel for things to go into
        //Adding componants to mainPanel
//--------------------------------------------------------------------------------------------------------------------
        mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setBackground(Color.pink);  
        South.add(sendButton, BorderLayout.EAST);
        South.add(exiButton, BorderLayout.WEST);
        mainPanel.add(scrollPane1);    
        mainFrame.add(mainPanel);
        mainFrame.revalidate();
        mainFrame.repaint();

        
    }
    //Method to add my values to a Jtable via arraylist
    //--------------------------------------------------------------------------------------------------------------------  

    public void makeTable(JPanel p1, JTable table, JScrollPane sp, ArrayList<Double> array1, ArrayList<Double> array2, ArrayList<Double> array3,
            Object data[][], String[] Columns) {

        for (int i = 0; i < 5; i++) {
            data[i][0] = i;
            data[i][1] = array1.get(i);
            data[i][2] = array2.get(i);
            data[i][3] = array3.get(i);
            //data = (Object[][]) data[i][i];

        }
        DefaultTableModel model = new DefaultTableModel(data, Columns);

        table = new JTable(model);
        sp = new JScrollPane(table); //adding table to the scrollpane
        table.removeAll();
        p1.removeAll();
        p1.add(sp);
        p1.revalidate();
        p1.repaint();
        array1.clear();
        array2.clear();
        array3.clear();

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
    public class ListenForButton implements ActionListener {

        Font fnt = new Font("Georgia", Font.PLAIN, 20);

        @Override
        public void actionPerformed(ActionEvent e) {



            //button to clear jtext1
            if(e.getSource() == menuItem){
                JOptionPane.showMessageDialog(rootPane, "clicked!");
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
        
    }//the listener class
}//Public Class

