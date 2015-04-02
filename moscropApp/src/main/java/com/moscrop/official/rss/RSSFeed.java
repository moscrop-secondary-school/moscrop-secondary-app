package com.moscrop.official.rss;

import java.util.List;

/**
 * Created by ivon on 20/10/14.
 */
public class RSSFeed {

    public final String version;
    public final List<RSSItem> items;

    public RSSFeed(String version, List<RSSItem> items) {
        this.version = version;
        this.items = items;
    }
}
