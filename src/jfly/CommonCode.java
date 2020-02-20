package jfly;


import java.io.File;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;


public class CommonCode {
    // These allow the system date to be accessed in ordered, UK and US formats.

    // These are some useful items.
//    public final String userName = System.getProperty("user.name");
//    public final String appDir = System.getProperty("user.dir");
//    public final String os = System.getProperty("os.name");
//    public final String fileSeparator = System.getProperty("file.separator");
    
    //Allows me to make a button
//--------------------------------------------------------------------------------------------------------------------
 public  JButton makeButton(
            String imageName,
            String toolTipText,
            String altText) {

        //Create and initialize the button.
        JButton button = new JButton();


        //Look for the image.
        String imgLocation = System.getProperty("user.dir")
                + "\\icons\\"
                + imageName
                + ".png";

        File fyle = new File(imgLocation);
        if (fyle.exists() && !fyle.isDirectory()) {
            // image found
            Icon img;
            img = new ImageIcon(imgLocation);
            button.setIcon(img);
        } else {
            // image NOT found
            button.setText(altText);
            System.err.println("Resource not found: " + imgLocation);
        }

        return button;
    }
  

}
//--------------------------------------------------------------------------------------------------------------------



    // This reads a text file into an ArrayList of Strings. The path to the
    // file has to be added. Use appDir if the files are in the application
    // directory. Use fileSeperator if the app may be running under a
    // different OS.


   

