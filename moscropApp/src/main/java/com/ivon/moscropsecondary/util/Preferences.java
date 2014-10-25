package com.ivon.moscropsecondary.util;

/**
 * Created by ivon on 9/16/14.
 */
public class Preferences {

    public static class Default {
        /*public static final float STEP_SIZE = 70.0f;            // cm
        public static final float WEIGHT = 70.0f;               // kg
        public static final boolean NOTIFICATION = true;
        public static final int GOAL = 10000;
        public static final boolean AUTO_START = true;
        public static final boolean USE_IMPERIAL = false;
        public static final String THEME = "0";*/

        // Nothing here yet
    }

    public static class Keys {
        /*
        // Default shared preferences
        public static final String STEP_SIZE = "step_size";
        public static final String WEIGHT = "weight";
        public static final String NOTIFICATION = "show_notification";
        public static final String GOAL = "set_goal";
        public static final String AUTO_START = "auto_start";
        public static final String USE_IMPERIAL = "use_imperial";
        public static final String THEME = "theme_selector";
        public static final String RESET = "reset";*/

        // Nothing here yet
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
