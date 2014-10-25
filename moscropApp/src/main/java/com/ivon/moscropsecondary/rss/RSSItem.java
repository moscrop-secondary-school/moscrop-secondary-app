package com.ivon.moscropsecondary.rss;

/**
 * Created by ivon on 20/10/14.
 */
public class RSSItem {

    public final long date;
    public final String title;
    public final String content;
    public final String preview;
    public final String[] tags;
    public final String url;

    public RSSItem(long date, String title, String content, String[] tags, String url) {
        this(date, title, content, null, tags, url);
    }


    public RSSItem(long date, String title, String content, String preview, String[] tags, String url) {
        this.date = date;
        this.title = title;
        this.content = content;
        if (preview != null) {
            this.preview = preview;
        } else {
            this.preview = generatePreview(content, tags);
        }
        this.tags = tags;
        this.url = url;
    }

    private String generatePreview(String content, String[] tags) {
        String s = "";
        for (int i = 0; i < tags.length; i++) {
            s += tags[i];
            if (!(i == tags.length - 1)) {
                s += ",";
            }
        }
        return "Tags: " + s;
    }
}
