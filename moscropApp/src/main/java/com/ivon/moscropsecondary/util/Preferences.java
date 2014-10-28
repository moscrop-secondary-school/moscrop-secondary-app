package com.ivon.moscropsecondary.util;

/**
 * Created by ivon on 9/16/14.
 */
public class Preferences {

    public static class Default {
        public static final String THEME = "0";
    }

    public static class Keys {
        // Default shared preferences
        public static final String THEME = "theme_selector";
    }

    public static class App {

        public static final String NAME = "moscrop_secondary";

        public static class Default {
            public static final long GCAL_LAST_UPDATED = 0;
            public static final String GCAL_VERSION = "no gcal version info";
            public static final String STAFF_DB_VERSION = "no version info";
            public static final long RSS_LAST_UPDATED = 0;
            public static final String RSS_VERSION = "no rss version info";
        }

        public static class Keys {
            public static final String GCAL_LAST_UPDATED = "gcal_last_updated";
            public static final String GCAL_VERSION = "gcal_version";
            public static final String STAFF_DB_VERSION = "staff_db_version";
            public static final String RSS_LAST_UPDATED_SUFFIX = "_last_updated";
            public static final String RSS_VERSION_SUFFIX = "_version";
        }
    }
}
