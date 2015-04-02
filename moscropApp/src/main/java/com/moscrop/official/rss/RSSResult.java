package com.moscrop.official.rss;

import java.util.List;

/**
 * Created by ivon on 11/2/14.
 */
public class RSSResult {

    public static final int RESULT_OK           = 0;
    public static final int RESULT_REDUNDANT    = 1;
    public static final int RESULT_REDO_ONLINE  = 2;
    public static final int RESULT_FAIL         = 3;

    public final String version;
    public final int resultCode;
    public final List<RSSItem> items;
    public final boolean append;

    public RSSResult(String version, int resultCode, List<RSSItem> items, boolean append) {
        this.version = version;
        this.resultCode = resultCode;
        this.items = items;
        this.append = append;
    }
}
