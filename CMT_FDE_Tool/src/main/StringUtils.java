package main;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
	
	/**
	 * Returns a cleaned version of inputTextContent, for compatibility with Mac,
	 *  with the following substitution:<br>
	 * -each ' — ' become ' - '<br>
	 * @param inputTextContent - the String to be cleaned
	 * @return - a new cleaned String
	 */
	public static String cleanTextCompatibilityForMac(String inputTextContent) {
		Matcher m = null;

//        Pattern p0 = Pattern.compile("\u0097");
//        Pattern p0 = Pattern.compile("—");
//        Pattern p0 = Pattern.compile();
		
        Pattern p0 = Pattern.compile("‚Äî");         
        m = p0.matcher(inputTextContent);
        inputTextContent = m.replaceAll("—");	

//        System.out.println("Exiting cleanTextCompatibility");
//        inputTextContent = ""+0xEF+0xBB+0xBF+inputTextContent;
//        inputTextContent = '\ufeff'+inputTextContent;

        return inputTextContent;
	}
	
	/**
	 * Returns a cleaned version of inputTextContent, for compatibility with Windows,
	 *  with the following substitution:<br>
	 * -each ' — ' become ' - '<br>
	 * @param inputTextContent - the String to be cleaned
	 * @return - a new cleaned String
	 */
	public static String cleanTextCompatibilityForWindows(String inputTextContent) {
		Matcher m = null;

		Pattern p0 = Pattern.compile("﻿â€”");         
        m = p0.matcher(inputTextContent);
        inputTextContent = m.replaceAll("—");	

        return inputTextContent;
	}


}
