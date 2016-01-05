/*
 * Copyright (C) 2015 Ravi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ravi.apps.android.newsbytes;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

/**
 * Holds a single instance of the google analytics tracker instance.
 */
public class NewsApplication extends Application {

    // Holds the single instance of the tracker.
    private Tracker mTracker;

    /**
     * Starts the analytics tracking.
     */
    public void startTracking() {
        // Create the tracker if it doesn't already exist.
        if(mTracker == null) {
            // Get google analytics instance.
            GoogleAnalytics googleAnalytics = GoogleAnalytics.getInstance(this);

            // Create tracker.
            mTracker = googleAnalytics.newTracker(R.xml.tracker_app);

            // Enable auto tracking.
            googleAnalytics.enableAutoActivityReports(this);
        }

    }

    // Returns the tracker instance.
    public Tracker getTracker() {
        // Check if tracker exists. If not create it.
        startTracking();

        return mTracker;
    }
}
