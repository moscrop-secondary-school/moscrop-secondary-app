package com.moscropsecondary.official.rss;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public final String metadata;

    public RSSItem(long date, String title, String content, String[] tags, String url) {
        this(date, title, content, null, tags, url);
    }

    public RSSItem(long date, String title, String content, String preview, String[] tags, String url) {
        this(date, title, content, preview, tags, url, null);
    }


    public RSSItem(long date, String title, String content, String preview, String[] tags, String url, String metadata) {
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
        if (metadata != null) {
            this.metadata = metadata;
        } else {
            this.metadata = generateDisplayMetaData(content);
        }
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

    private String generateDisplayMetaData(String content) {

        Random random = new Random();
        int i = random.nextInt(4);

        String s = i + "";

        Pattern pattern = Pattern.compile("src=\\\"([^\\\"]+)");
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            String find = matcher.group(1);
            s += ",";
            s += find;
        }

        return s;
    }
}
