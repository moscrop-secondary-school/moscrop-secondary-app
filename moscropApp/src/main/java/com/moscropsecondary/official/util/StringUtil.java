package com.moscropsecondary.official.util;

public class StringUtil {
	
	public static String processSpecialChars(String s) {
		s = s.replaceAll("&quot;", "\"");
		s = s.replaceAll("&#39;", "\'");
		s = s.replaceAll("&amp;", "&");
		s = s.replaceAll("&lt;", "<");
		s = s.replaceAll("&gt;", ">");
		s = s.replaceAll("&nbsp;", " ");
		
		return s;
	}
	
	public static String removeHtmlTags(String s) {
		return s.replaceAll("\\<.*?>","");
	}
}
