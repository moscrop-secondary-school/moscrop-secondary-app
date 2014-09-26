package com.ivon.moscropsecondary.rss;

import com.ivon.moscropsecondary.util.StringUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailCardProcessor extends HtmlCardProcessor {

    public EmailCardProcessor() {
        super();
    }

    public EmailCardProcessor(int color) {
        super(color);
    }

	@Override
	public String toProcessedTitle(String s) {
		s = s.replace("FW: ", "")
			 .replace("Fwd: ", "")
			 .replace("Student Bulletin ", "");
		return StringUtil.processSpecialChars(s);
	}

	@Override
	public String toProcessedDescription(String s) {
		String patternString = "\\<b>.*?</b>";
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(s);
	
		String processedDescription = "";
		
		int subtitlesAddedCount = 0;
		
		final int SUBTITLES_ADDED_LIMIT = 4;	// Only show this many subtitles
		
		// If string contains any of these it is not considered a subtitle
		final String[] blacklist = {
				"Moscrop Secondary Student Bulletin",
				"Word of the day",
				"General",
				"Events & opportunities"
		};
		
		final int CHAR_REQUIREMENT = 30;		// String must contain at least this many chars to be considered a subtitle
		
		while(matcher.find()) {
			String unprocessed = matcher.group();
			String processedSubtitle = super.toProcessedDescription(unprocessed);
			
			boolean addToList = true;
			
			for(String filterString : blacklist) {
				
				// Check against blacklist
				if(processedSubtitle.equalsIgnoreCase(filterString)) {
					addToList = false;
					break;
				}
				
				// If it contains "Day" and "period" it is part of the header of the email, and not considered a subtitle
				if(processedSubtitle.contains("Day") && processedSubtitle.contains("period")) {
					addToList = false;
					break;
				}
					
				// Drop the string if it's too short (less than 30 chars)
				if(processedSubtitle.length() < CHAR_REQUIREMENT) {
					addToList = false;
					break;
				}
					
				// Make sure there is at least one space, meaning multiple words
				if(!processedSubtitle.contains(" ")) {
					addToList = false;
					break;
				}
				
				// Remove all whitespaces in copy string
				// If after remove there are no characters left, fail check
				String copy = processedSubtitle;
				copy = copy.replaceAll("\\s","");
				if(copy.length() == 0) {
					addToList = false;
					break;
				}
			}
			
			// Passed all checks, add to description
			if(addToList) {
				if(subtitlesAddedCount < SUBTITLES_ADDED_LIMIT) {
					subtitlesAddedCount++;
					if(processedDescription.length() != 0) {		// String already has something, add processedSubtitle to new line
						processedDescription += "\n\n" + processedSubtitle;
					} else {										// String is empty, do not add space.
						processedDescription = processedSubtitle;
					}
				} else {
					processedDescription += "\n\nAnd more...";
					break;
				}
			}
				
		}
		return processedDescription;
	}

	@Override
	public int getMaxLines() {
		return 100;
	}

	@Override
	public int getCardColor() {
		return mColor;
	}

}
