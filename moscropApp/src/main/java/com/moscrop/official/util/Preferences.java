package com.moscrop.official.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ivon on 9/16/14.
 */
public class Preferences {

    public static class Default {
        public static final String THEME = "0";
        public static final Set<String> TAGS = new HashSet<String>(Arrays.asList(new String[] { "Official" }));
        public static final int LOAD_LIMIT = 24;
        public static final boolean AUTO_REFRESH = true;
    }

    public static class Keys {
        // Default shared preferences
        public static final String THEME = "theme_selector";
        public static final String TAGS = "tag_chooser";
        public static final String LOAD_LIMIT = "load_limit";
        public static final String AUTO_REFRESH = "auto_refresh";
    }

    public static class App {

        public static final String NAME = "moscrop_secondary";

        public static class Default {
            public static final long GCAL_LAST_UPDATED = 0;
            public static final String GCAL_VERSION = "no gcal version info";
            public static final String STAFF_DB_VERSION = "no version info";
            public static final long RSS_LAST_UPDATED = 0;
            public static final String RSS_VERSION = "no rss version info";
            public static final String RSS_LAST_TAG = "Subscribed";
            public static final boolean FIRST_LAUNCH = true;
            public static final String TAG_LIST_VERSION = "0";
        }

        public static class Keys {
            public static final String GCAL_LAST_UPDATED = "gcal_last_updated";
            public static final String GCAL_VERSION = "gcal_version";
            public static final String STAFF_DB_VERSION = "staff_db_version";
            public static final String RSS_LAST_UPDATED = "rss_last_updated";
            public static final String RSS_VERSION = "rss_version";
            public static final String RSS_LAST_TAG = "rss_last_tag";
            public static final String FIRST_LAUNCH = "first_launch";
            public static final String TAG_LIST_VERSION = "tag_list_version";
        }
    }
}
