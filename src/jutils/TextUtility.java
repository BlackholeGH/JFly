/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jutils;

import java.util.regex.Pattern;

/**
 *
 * @author dg7239p
 */
public class TextUtility {
    public static String sanitizeText(String text)
    {
        if(text == null) { return null; }
        text = text.replace(":~:", "[JFLYMESSAGESEPARATOR]");
        text = text.replace("+-+", "[NCSUSERINFOSEPARATOR]");
        text = text.replace("|", "[BNMCHAINBLOCKSEPARATOR]");
        text = text.replace("/-/", "[NCSPRINTOUTSEPARATOR]");
        return text;
    }
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
