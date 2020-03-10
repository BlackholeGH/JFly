package flyutils;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.util.ArrayList;
import java.util.regex.Pattern;
/**
 * The NetworkConfigurationState class holds details for everyone connected to a network, in this case a JFly cluster.
 * @author Blackhole (dg7239p)
 */
public class NetworkConfigurationState {
    /**
     * The UserInfo class represents the information for a given user connected to the network.
     */
    public static class UserInfo
    {
        private String iP;
        //The term identityHash is used as in JFly the unique identifier of a user is the hash of the hash value of the blockchain block that records them joining the cluster.
        //This ensures that every user is directly linked to a given login event.
        private String identityHash;
        private String userName;
        /**
         * Gets the unique identifier for a user.
         * @return The user's ID.
         */
        public String getID()
        {
            return identityHash;
        }
        /**
         * Sets the unique identifier for a user.
         * @param idHash The user ID to be set.
         */
        public void setID(String idHash)
        {
            identityHash = idHash;
        }
        /**
         * Gets the IP for the user.
         * @return The user IP.
         */
        public String getIP()
        {
            return iP;
        }
        /**
         * Gets the username for the user.
         * @return The user's username.
         */
        public String getUserName()
        {
            return userName;
        }
        /**
         * The UserInfo constructor.
         * @param userIP The IP of the user this UserInfo represents.
         * @param userHash The hash (unique ID) of the user this UserInfo represents.
         * @param userUserName The username of the user this UserInfo represents.
         */
        public UserInfo(String userIP, String userHash, String userUserName)
        {
            iP = userIP;
            identityHash = userHash;
            userName = userUserName;
        }
        /**
         * Returns a UserInfo instance constructed from a given String representation.
         * @param str The String representation of a UserInfo instance.
         * @return The constructed UserInfo.
         */
        public static UserInfo fromString(String str)
        {
            String[] attr = str.split(Pattern.quote("+-+"), -1);
            if(attr.length == 3)
            {
                return new UserInfo(attr[0], attr[2], attr[1]);
            }
            else { return null; }
        }
        /**
         * toString() override that returns a String representation of the contents of this UserInfo instance.
         * @return 
         */
        @Override
        public String toString()
        {
            return iP + "+-+" + userName + "+-+" + identityHash;
        }
    }
    /**
     * Generates a registrar String for this NetworkConfigurationState, that contains all the user data derived from this NetworkConfigurationState's stored UserInfo instances.
     * @return The generated registrar String.
     */
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
    /**
     * Gets the total number of users in this network representation.
     * @return The number of users in this network.
     */
    public int getTotalNetworkMembers()
    {
        return myUsers.size();
    }
    /**
     * Directly adds a user to this network representation.
     * @param u UserInfo instance representing the user to be added.
     */
    public void addUser(UserInfo u)
    {
        myUsers.add(u);
    }
    /**
     * Directly removes a user from this network representation.
     * @param u UserInfo instance representing the user to be removed.
     */
    public void remUser(UserInfo u)
    {
        myUsers.remove(u);
    }
    /**
     * Manually re-writes the stored users for this network representation.
     * @param nUI A list of UserInfo instances for the users to be stored in this NetworkConfigurationState.
     */
    public void reWriteAll(ArrayList<UserInfo> nUI)
    {
        myUsers = nUI;
    }
    /**
     * Returns a username given the associated unique ID.
     * @param iD A user's unique ID.
     * @return The user's name, or "Unknown User" if not found.
     */
    public String getUserNameFromID(String iD)
    {
        for(UserInfo ui : myUsers)
        {
            if(ui.getID().equals(iD)) { return ui.getUserName(); }
        }
        return "Unknown User";
    }
    /**
     * Returns a user's data given the associated unique ID.
     * @param iD A user's unique ID.
     * @return The data representation of the associated UserInfo instance, or "Unknown User" if not found.
     */
    public String getUserDataFromID(String iD)
    {
        for(UserInfo ui : myUsers)
        {
            if(ui.getID().equals(iD)) { return ui.toString(); }
        }
        return "Unknown User";
    }
    /**
     * Returns a UserInstance given the associated unique ID.
     * @param iD A user's unique ID.
     * @return The associated UserInfo instance, or null if not found.
     */
    public UserInfo getUserFromID(String iD)
    {
        for(UserInfo ui : myUsers)
        {
            if(ui.getID().equals(iD)) { return ui; }
        }
        return null;
    }
    /**
     * Returns a user's unique ID given the associated IP address.
     * @param iP A user's IP address.
     * @return The user's unique ID, or "Unknown User" if not found.
     */
    public String getIDFromIP(String iP)
    {
        for(UserInfo ui : myUsers)
        {
            if(ui.getIP().equals(iP)) { return ui.getID(); }
        }
        return "Unknown User";
    }
    /**
     * Returns a user's IP address given the associated unique ID.
     * @param iD A user's unique ID.
     * @return The user's IP address, or "Unknown User" if not found.
     */
    public String getIPFromID(String iD)
    {
        for(UserInfo ui : myUsers)
        {
            if(ui.getID().equals(iD)) { return ui.getIP(); }
        }
        return "Unknown User";
    }
    /**
     * Returns a list of all users stored in this NetworkConfigurationState.
     * @return A list of all stored UserInfo instances.
     */
    public ArrayList<UserInfo> getUsers()
    {
        ArrayList<UserInfo> newUsers = new ArrayList<UserInfo>();
        for(UserInfo ui : myUsers)
        {
            newUsers.add(new UserInfo(ui.getIP(), ui.getID(), ui.getUserName()));
        }
        return newUsers;
    }
    /**
     * Returns a list of all the User IDs of all the users stored in this NetworkConfigurationState.
     * @return A list of all the users' User IDs as Strings.
     */
    public ArrayList<String> getUserIDs()
    {
        ArrayList<String> users = new ArrayList<String>();
        for(UserInfo ui : myUsers)
        {
            users.add(ui.getID());
        }
        return users;
    }
    /**
     * toString override that returns a String representation of the contents of this NetworkConfigurationState.
     * @return A String representation of the contents of this NetworkConfigurationState.
     */
    @Override
    public String toString()
    {
        String out = "";
        int i = 0;
        for(UserInfo ui : myUsers)
        {
            i++;
            out = out + i + ": " + ui.getIP() + ", " + ui.getID() + ", " + ui.getUserName() + "\n";
        }
        //Generally, this is used to print out a list of users on the network.
        return out;
    }
    /**
     * Gets a 2D array of data to be displayed in a table, based on the usernames and user IDs of users recorded in this NetworkConfigurationState.
     * @return The table data to be displayed.
     */
    public String[][] getTableData()
    {
        String[][] data = new String[myUsers.size()][2];
        int i = 0;
        for(UserInfo ui : myUsers)
        {
            data[i][0] = TextUtility.desanitizeText(ui.getUserName());
            data[i][1] = ui.getID();
            i++;
        }
        //This table data can then be displayed using a JTable.
        return data;
    }
    /**
     * The NetworkConfigurationState constructor.
     */
    public NetworkConfigurationState()
    {
                
    } 
}
