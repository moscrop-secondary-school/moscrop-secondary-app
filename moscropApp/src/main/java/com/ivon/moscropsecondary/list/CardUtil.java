package com.ivon.moscropsecondary.list;

/**
 * Created by ivon on 30/06/14.
 */
public class CardUtil {

    public static final int TYPE_NEWS_CARD = 0;
    public static final int TYPE_EMAIL_CARD = 1;
    public static final int TYPE_SUBS_CARD = 2;

    public interface CardProcessor {
        public abstract String toProcessedTitle(String s);
        public abstract String toProcessedDescription(String s);
        public abstract int getMaxLines();
        public abstract int getCardColor();
    }

    private static final HtmlCardProcessor mGenericCardProcessor = new HtmlCardProcessor();
    private static final HtmlCardProcessor mNewsCardProcessor = new HtmlCardProcessor(0xff33b5e5);
    private static final EmailCardProcessor mEmailCardProcessor = new EmailCardProcessor(0xffaa66cc);
    private static final HtmlCardProcessor mSubsCardProcessor = new HtmlCardProcessor(0xffcc0000);

    public static CardProcessor getCardProcessor(int type) {
        switch(type) {

            case TYPE_NEWS_CARD:
                return mNewsCardProcessor;

            case TYPE_EMAIL_CARD:
                return mEmailCardProcessor;

            case TYPE_SUBS_CARD:
                return mSubsCardProcessor;

            default:
                return mGenericCardProcessor;
        }
    }
}
