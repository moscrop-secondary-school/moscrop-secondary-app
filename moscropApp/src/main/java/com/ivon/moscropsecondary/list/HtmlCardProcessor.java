package com.ivon.moscropsecondary.list;

import com.ivon.moscropsecondary.list.CardUtil.CardProcessor;
import com.ivon.moscropsecondary.util.StringUtil;

public class HtmlCardProcessor implements CardProcessor {

    private final int DEFAULT_COLOR = 0xff33b5e5;

    protected final int mColor;
	
	public HtmlCardProcessor() {
		mColor = DEFAULT_COLOR;
	}
	
	public HtmlCardProcessor(int color) {
	    mColor = color;
	}
	
	@Override
	public String toProcessedTitle(String s) {
		return StringUtil.processSpecialChars(s);
	}

	@Override
	public String toProcessedDescription(String s) {
		String unprocessed = s;
		String stripped = StringUtil.removeHtmlTags(unprocessed);
		String processed = StringUtil.processSpecialChars(stripped);
		return processed;
	}

	@Override
	public int getMaxLines() {
		return 6;
	}

	@Override
	public int getCardColor() {
		return mColor;
	}

}
