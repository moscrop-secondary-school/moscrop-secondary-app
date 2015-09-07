package com.moscrop.official;

import android.app.Application;

import com.moscrop.official.util.ApiConfig;
import com.parse.Parse;

/**
 * Created by ivon on 08/08/15.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        /**
         * Not using local datastore because it doesn't
         * seem to handle incomplete objects properly.
         *
         * Instead, I'm using ParseQuery.setCachePolicy(),
         * which requires local datastore to be disabled.
         */

        // Enable Local Datastore
        //Parse.enableLocalDatastore(this);

        // Initialize Parse
        Parse.initialize(this, ApiConfig.Parse.API_KEY, ApiConfig.Parse.CLIENT_KEY);
    }
}
