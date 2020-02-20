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
    public static class UserInfo
    {
        private String iP;
        private String identityHash;
        private String userName;
        public String getID()
        {
            return identityHash;
        }
        public void setID(String idHash)
        {
            identityHash = idHash;
        }
        public String getIP()
        {
            return iP;
        }
        public String getUserName()
        {
            return userName;
        }
        public UserInfo(String userIP, String userHash, String userUserName)
        {
            iP = userIP;
            identityHash = userHash;
            userName = userUserName;
        }
        public static UserInfo fromString(String str)
        {
            String[] attr = str.split("+-+");
            return new UserInfo(attr[0], attr[2], attr[1]);
        }
        @Override
        public String toString()
        {
            return iP + "+-+" + userName + "+-+" + identityHash;
        }
    }
    public String getRegistrar()
    {
        String s = "";
        for(UserInfo ui : myUsers)
        {
            if(!s.equals("")) { s += "/-/"; }
            s += ui.toString();
        }
        return s;
    }
    private ArrayList<UserInfo> myUsers = new ArrayList<UserInfo>();
    public int getTotalNetworkMembers()
    {
        return myUsers.size();
    }
    public void addUser(UserInfo u)
    {
        myUsers.add(u);
    }
    public void remUser(UserInfo u)
    {
        myUsers.remove(u);
    }
    public void reWriteAll(ArrayList<UserInfo> nUI)
    {
        myUsers = nUI;
    }
    public String getUserNameFromID(String iD)
    {
        for(UserInfo ui : myUsers)
        {
            if(ui.getID() == iD) { return ui.getUserName(); }
        }
        return "UNKNOWN_USER";
    }
    public ArrayList<UserInfo> getUsers()
    {
        ArrayList<UserInfo> newUsers = new ArrayList<UserInfo>();
        for(UserInfo ui : myUsers)
        {
            newUsers.add(new UserInfo(ui.getIP(), ui.getID(), ui.getUserName()));
        }
        return newUsers;
    }
    public NetworkConfigurationState()
    {
                
    }
    
}