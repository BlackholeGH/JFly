/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfly;


import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.ParseException;
import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;
/**
 *
 * @author dg7239p
 */
//This simple class causes JTextFields to trigger their ActionEvent when they lose mouse focus, ensuring that checks are applied and the user cannot input invalid values
public class FocusVerifier implements FocusListener {
    
    @Override
    public void focusLost(FocusEvent e) {
        ((JTextField)e.getSource()).postActionEvent();
    }
    
    @Override
    public void focusGained(FocusEvent e) {
        
    }
    
}
