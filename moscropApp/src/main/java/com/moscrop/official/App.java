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

        // Enable Local Datastore
        Parse.enableLocalDatastore(this);
        // Initialize Parse
        Parse.initialize(this, ApiConfig.API_KEY, ApiConfig.CLIENT_KEY);
    }
}
