/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfly;


import java.awt.event.*;
import javax.swing.*;
/**
 * The FocusVerifier class implements FocusListener and causes JTextFields to trigger their ActionEvent when they lose mouse focus, ensuring that checks are applied and the user cannot input invalid values.
 * @author Blackhole (dg7239p)
 */
public class FocusVerifier implements FocusListener {
    
    /**
     * focusLost() override that causes the triggering JTextField to post its ActionEvent when it loses focus.
     * @param e The FocusEvent that has been triggered.
     */
    @Override
    public void focusLost(FocusEvent e) {
        ((JTextField)e.getSource()).postActionEvent();
    }
    /**
     * focusGained() override to implement FocusListener.
     * @param e 
     */
    @Override
    public void focusGained(FocusEvent e) {
        
    }
    
}
