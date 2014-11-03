package com.moscropsecondary.official.rss;

import java.util.List;

/**
 * Created by ivon on 11/2/14.
 */
public class RSSResult {

    public final List<RSSItem> items;
    public final boolean append;

    public RSSResult(List<RSSItem> items, boolean append) {
        this.items = items;
        this.append = append;
    }
}
