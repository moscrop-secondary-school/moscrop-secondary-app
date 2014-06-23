package com.ivon.moscropsecondary.ui;

import com.ivon.moscropsecondary.ui.RSSAdapter.CardProcessor;
import com.ivon.moscropsecondary.util.StringUtil;

public class HtmlCardProcessor implements CardProcessor {

	private final int cardColor;
	
	public HtmlCardProcessor() {
		this.cardColor = 0xff33b5e5;
	}
	
	public HtmlCardProcessor(int cardColor) {
		this.cardColor = cardColor;
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
		return cardColor;
	}

}
