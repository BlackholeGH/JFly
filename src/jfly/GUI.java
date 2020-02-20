package jfly;

//import com.sun.javafx.collections.ListListenerHelper; //messed
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
//import javafx.scene.layout.Border; //messed
import java.text.DecimalFormat;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class GUI extends JFrame {

    //Class references
//**************************************************************************************
//    Graph G = new Graph();
    CommonCode cc = new CommonCode();

//    LinkedList LL = new LinkedList();
//**************************************************************************************
    //Class variables
//**************************************************************************************
    JButton sendButton;
    JScrollPane sp;
    JScrollPane sp2;
    JScrollPane sp3;
    JScrollPane sp4;
    JScrollPane sp5;
    JTable table;
    JTable table1;
    JTable table2;
    JTable table3;
    DecimalFormat numberFormat;
    Double bisectionAns;

    JButton calcButton;
    JButton exiButton;
    JFrame mainFrame;
    JPanel East;
    JLabel Title;
    JButton button;
    JButton clear;
    JTextField textField1;
    JTextField msgBox;
    JTextArea txtArea1;
    JTextArea txtArea2;
    JComboBox FunctionBox;
    JComboBox RootBox;
    JPanel mainPanel;
    int buttonClicked;
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
    Font fnt = new Font("Georgia", Font.PLAIN, 20);
    Font fnt2 = new Font("Georgia", Font.PLAIN, 40);

    ArrayList<Double> xvalueArr = new ArrayList();
    ArrayList<Double> yvalueArr = new ArrayList();

    //GUI prg =
    // Using MVC
    JFlyNode myNode = null;
    public GUI(JFlyNode jNode) { //making the constructor
        myNode = jNode;
        model();
        view();
    }

    private void model() {
        ListenForButton lforButton = new ListenForButton(); //creating an object for action listener

        //Configuring the Function Jcombobox defined above as class variable
//****************************************************************************************************************
        FunctionBox = new JComboBox(optNames);
        FunctionBox.setName("Functions");
        FunctionBox.setFont(fnt);
        FunctionBox.addActionListener(lforButton); //adding action listener 
        FunctionBox.setActionCommand("Funcbox"); //adding actionc command for listener to refer to (switch case)
//****************************************************************************************************************

        //Configuring the Algorithm Jcombobox defined above as class variable
//****************************************************************************************************************
        RootBox = new JComboBox(rootNames);
        RootBox.setFont(fnt);
        RootBox.addActionListener(lforButton); //adding action listener 
        RootBox.setActionCommand("Algbox"); //adding actionc command for listener to refer to (switch case)
//****************************************************************************************************************     

        //setting up the button for Alg solving
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

//****************************************************************************************************************
        //setting up the button for function solving
//****************************************************************************************************************
        calcButton = new JButton();
        calcButton.setFont(fnt);
        calcButton.setToolTipText("Calculates Selected Function");
        calcButton.setText("Calc");
        calcButton.addActionListener(lforButton);
//****************************************************************************************************************

        //creating an icon button using commoncode
//****************************************************************************************************************
        button = cc.makeButton("Wizard", "Click me for the graph!", "Graph");
        button.addActionListener(lforButton);
//****************************************************************************************************************

        //configuring a clear button defined above as a class variable
//****************************************************************************************************************
        clear = new JButton();
        clear.setFont(fnt);
        clear.setText("Clear");
        clear.addActionListener(lforButton);
        // msgBox.addActionListener(lforButton);
//****************************************************************************************************************
    }

    /*
     public void positionColumn(JTable table, int col_Index) {
     table.moveColumn(table.getColumnCount() - 1, col_Index);
     }
     /*/
    private void view() {

        //using Borderfactory to create a titled border, at setting its position
//===================================================================================================================
        TitledBorder borderTitle = BorderFactory.createTitledBorder("Main Chat");
        TitledBorder borderTitle2 = BorderFactory.createTitledBorder("Root Value");
        TitledBorder borderTitle3 = BorderFactory.createTitledBorder("Table Values");
        borderTitle.setTitleJustification(borderTitle.CENTER);

        borderTitle2.setTitleJustification(borderTitle2.CENTER);
        borderTitle3.setTitleJustification(borderTitle3.CENTER);
//===================================================================================================================

        //tk can be used to get screen size, GUI stuff
        Toolkit tk = Toolkit.getDefaultToolkit();

        //this makes the mainframe for all the panels to be placed within
        //Configuring up the frame
//===================================================================================================================
        mainFrame = new JFrame("Function");
        mainFrame.setSize(1050, 700);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setResizable(false);
        mainFrame.setBackground(Color.GRAY);
        mainFrame.setVisible(true);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//===================================================================================================================

        //setting up a North Jpanel for the title to go into, then adding it to mainframe
//===================================================================================================================
        JPanel north = new JPanel(new FlowLayout()); //used a flow layout
        north.setBackground(Color.pink);
        Title = new JLabel("JFly");
        Title.setFont(fnt2);
        north.add(Title, BorderLayout.NORTH);
        mainFrame.add(north, BorderLayout.NORTH);
//===================================================================================================================

        //setting up a south Jpanel for stuff to go into, then adding it to mainframe
//===================================================================================================================
        JPanel South = new JPanel(new BorderLayout()); //uses a flow layout
        South.setBackground(Color.LIGHT_GRAY);
        msgBox = new JTextField("Enter Text Here");
        //titleSouth.setFont(fnt2);
        South.add(msgBox);

        // South.add(button);
        mainFrame.add(South, BorderLayout.SOUTH);

//===================================================================================================================
        //using model for jtable, probably wont be needed... I hope
        /**
         * ****************************************************************************************************************
         * table= new JTable(model); model.addColumn("Code");
         * model.addColumn("Name"); model.addColumn("Quantity");
         * model.addColumn("Unit Price"); model.addColumn("Price");
         * model.addRow(new Object[]{"Column 1", "Column 2", "Column 3","Column
         * 4","Column 5"});
         * /***************************************************************************************************************************
         */
        //JTable attributes
//--------------------------------------------------------------------------------------------------------------------      
        String[] columnNames5 = {"Username", "ID"};//,"f(x(i-1))","f(x(i))","x(i+1)"}; 
        table = new JTable(data, columnNames5);

        JTableHeader header = table.getTableHeader(); //for colour
        header.setBackground(Color.LIGHT_GRAY);
        sp = new JScrollPane(table); //adding table to the scrollpane
        //table.setPreferredScrollableViewportSize(table.getPreferredSize());
        table.setRowHeight(30);
        sp.setPreferredSize(new Dimension(250, 600));
//        sp.setBorder(borderTitle3);
//        sp.setBackground(Color.GRAY);

//--------------------------------------------------------------------------------------------------------------------
        //setting up a east Jpanel for the table to go into, then adding it to mainframe
//--------------------------------------------------------------------------------------------------------------------
        East = new JPanel(new BorderLayout());//(new BorderLayout()); //uses a flow layout
        East.setBackground(Color.WHITE);
        East.add(sp);

        mainFrame.add(East, BorderLayout.EAST);
//--------------------------------------------------------------------------------------------------------------------

        //Setting up the JLabel to select a function
//--------------------------------------------------------------------------------------------------------------------
        JLabel label_select_function = new JLabel("Select Function:");
        label_select_function.setFont(fnt);
//--------------------------------------------------------------------------------------------------------------------

        //Setting up the JLabel to select a function
//--------------------------------------------------------------------------------------------------------------------
        JLabel label_select_algorithm = new JLabel("Select Algorithm:");
        label_select_algorithm.setFont(fnt);
//--------------------------------------------------------------------------------------------------------------------

        //set up of a textArea1 (x+f(x) with scroll pane
//--------------------------------------------------------------------------------------------------------------------
        txtArea1 = new JTextArea(15, 20);
        txtArea1.setLineWrap(true); //skips to second line
        txtArea1.setWrapStyleWord(true);
        JScrollPane scrollPane1 = new JScrollPane(txtArea1, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);//adding txt area to scroll pane
        txtArea1.setFont(fnt);
        txtArea1.setForeground(Color.BLACK); //Text colour
        txtArea1.setBackground(Color.pink);
        scrollPane1.setBorder(borderTitle); //adds a border to the scroll pane    
        scrollPane1.setBackground(Color.red);
//--------------------------------------------------------------------------------------------------------------------

        //set up of a textArea2 (root) with scroll pane
//--------------------------------------------------------------------------------------------------------------------
        txtArea2 = new JTextArea(15, 20);
        txtArea2.setLineWrap(true); //skips to second line
        txtArea2.setWrapStyleWord(true);
        JScrollPane scrollPane2 = new JScrollPane(txtArea2, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);//adding txt area to scroll pane
        txtArea2.setFont(fnt);
        txtArea2.setForeground(Color.BLACK); //Text colour
        txtArea2.setBackground(Color.LIGHT_GRAY);
        scrollPane2.setBorder(borderTitle2); //adds a border to the scroll pane    
        scrollPane2.setBackground(Color.gray);
//--------------------------------------------------------------------------------------------------------------------

        //Setting x,y,width,height positions for GUI features
//--------------------------------------------------------------------------------------------------------------------
        sendButton.setBounds(410, 150, 120, 50);
        calcButton.setBounds(410, 105, 120, 50);
        button.setBounds(480, 150, 120, 50);
        FunctionBox.setBounds(0, 0, 200, 50);
        RootBox.setBounds(200, 150, 200, 50);
        label_select_function.setBounds(40, 50, 150, 50);
        label_select_algorithm.setBounds(35, 150, 200, 50);
        scrollPane1.setBounds(0, 45, 785, 525);
        scrollPane2.setBounds(300, 250, 250, 200);

//--------------------------------------------------------------------------------------------------------------------
        //settin up a Jpanel mainPanel as the main panel for things to go into
        //Adding componants to mainPanel
//--------------------------------------------------------------------------------------------------------------------
        mainPanel = new JPanel();
        mainPanel.setLayout(null);

        mainPanel.setBackground(Color.GRAY);
        // mainPanel.add(label_select_function);
        mainPanel.add(FunctionBox);
        // mainPanel.add(label_select_algorithm);
        // mainPanel.add(RootBox);
        // mainPanel.add(sendButton);
        South.add(sendButton, BorderLayout.EAST);
        South.add(exiButton, BorderLayout.WEST);
        // mainFrame.add(calcButton);
        // mainPanel.add(clear);
        //mainPanel.add(textField1);
        mainPanel.add(scrollPane1);
        //mainPanel.add(scrollPane2);
        //mainPanel.setVisible(true);

        mainFrame.add(mainPanel);
        mainFrame.revalidate();
        mainFrame.repaint();

        //mainFrame.pack(); //compresses GUI
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
    }
    
    public void conv(double ans2) {
//        String pattern = "0.1";
//        String SecantDP = JOptionPane.showInputDialog("Enter DP");
//        double DPParseDouble = Double.parseDouble(SecantDP);
//        for (int i = 0; i < DPParseDouble; i++) {
//            pattern += "0";
//            numberFormat = new DecimalFormat(pattern);
//            txtArea2.setText(numberFormat.format(ans2)+ "   to: " + SecantDP + ".d.p");
//
//        }
//        System.out.println(pattern);

    }
    //--------------------------------------------------------------------------------------------------------------------   

    //Listeners 
    public class ListenForButton implements ActionListener {

        Font fnt = new Font("Georgia", Font.PLAIN, 20);

        @Override
        public void actionPerformed(ActionEvent e) {

            //Calculating algorithms using specific functions using switch caee
//--------------------------------------------------------------------------------------------------------------------
            String startPoint2;
            String startPoint1;
            String optString = FunctionBox.getSelectedItem().toString(); //gets selected item from the Rootbox

            if (e.getSource() == FunctionBox) {

                switch (optString) {

                    //Newton Case
                    //----------------------------------------------------------------------------------------------------
                    case "Option1":
                        try {
                            JOptionPane.showMessageDialog(rootPane, "Option1");

                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(rootPane, "Invalid Response, Enter a Value");
                            break;
                        }

                        //Adding values to table
                        break;
                    //---------------------------------------------------------------------------------

                    //----------------------------------------------------------------------------------------------------                    //----------------------------------------------------------------------------------------------------
                    //Secant Case
                    //----------------------------------------------------------------------------------------------------
                    case "Option2":

                        try {

                            JOptionPane.showMessageDialog(rootPane, "This is Option2");

                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(rootPane, "Invalid Response, Enter a Value");
                            break;
                        }
                        break;

                    //Adding secant to a table
                    //----------------------------------------------------------------------------------------------------   
                    //----------------------------------------------------------------------------------------------------  
                    //Bisection Case
                    //----------------------------------------------------------------------------------------------------
                    case "Option3":
                        try {
                            JOptionPane.showMessageDialog(rootPane, "This is Option3");

                            //txtArea2.setText(bisectionAns.toString());
                        } catch (Exception ex) {

                            JOptionPane.showMessageDialog(rootPane, "Invalid Response, Enter a Value");

                            break;
                        }
                        break;

                    //Adding Bisection to a table
                    //---------------------------------------------------------------------------------------------------- 
                    //----------------------------------------------------------------------------------------------------                    //----------------------------------------------------------------------------------------------------                    //----------------------------------------------------------------------------------------------------                    //----------------------------------------------------------------------------------------------------
                    //Research Case
                    //----------------------------------------------------------------------------------------------------
                    case "Research":

                }//end of switch
            } //end of solve button if case

            //button to clear jtext1
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
                    mainFrame.dispose();

                } catch (Exception fj) {
                    JOptionPane.showMessageDialog(rootPane, "Error");
                }
            }

//--------------------------------------------------------------------------------------------------------------------
            //Events for calculating the functions
//--------------------------------------------------------------------------------------------------------------------
            if (e.getSource() == sendButton) {
                myNode.sendMessage(msgBox.getText().toString());
            }
//--------------------------------------------------------------------------------------------------------------------

        }//PB actioned performed
    }//the listener class
}//Public Class
