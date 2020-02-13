/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfly;

import java.awt.*;
import java.util.concurrent.*;
import java.net.Socket;
import java.net.SocketAddress;
import java.awt.event.*;
import java.io.*;
import java.net.ServerSocket;
import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.time.*;
/**
 *
 * @author dg7239p
 */
public class NetworkConfigurationState {
    private int nextIDNumber = 0;
    private int totalNetworkMembers = 1;
    public int getNextIDNumber()
    {
        return nextIDNumber;
    }
    public int getTotalNetworkMembers()
    {
        return totalNetworkMembers;
    }
    public NetworkConfigurationState()
    {
                
    }
}
