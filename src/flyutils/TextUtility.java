/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package flyutils;

import java.util.regex.Pattern;

/**
 * The TextUtility class is a utility class that contains static methods for processing Strings.
 * @author Blackhole (dg7239p)
 */
public class TextUtility {
    /**
     * Replaces reserved JFly character sequences with standiins.
     * @param text The String to be sanitized.
     * @return The sanitized String.
     */
    public static String sanitizeText(String text)
    {
        if(text == null) { return null; }
        text = text.replace(":~:", "[JFLYMESSAGESEPARATOR]");
        text = text.replace("+-+", "[NCSUSERINFOSEPARATOR]");
        text = text.replace("|", "[BNMCHAINBLOCKSEPARATOR]");
        text = text.replace("/-/", "[NCSPRINTOUTSEPARATOR]");
        return text;
    }
    /**
     * Replaces stand-in character sequences with the reserved JFly character sequences that they represent.
     * @param text The String to be desanitized.
     * @return The desanitized String.
     */
    public static String desanitizeText(String text)
    {
        if(text == null) { return null; }
        text = text.replace("[JFLYMESSAGESEPARATOR]", ":~:");
        text = text.replace("[NCSUSERINFOSEPARATOR]", "+-+");
        text = text.replace("[BNMCHAINBLOCKSEPARATOR]", "|");
        text = text.replace("[NCSPRINTOUTSEPARATOR]", "/-/");
        return text;
    }
}
