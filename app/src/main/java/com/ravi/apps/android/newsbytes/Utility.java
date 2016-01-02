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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Provides various utility methods.
 */
public class Utility {
    /**
     * Returns a string containing the current news category preference
     * retrieved from the shared preferences.
     */
    public static String getNewsCategoryPreference(Context context, String key) {
        // Get shared preferences.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // Get the news category key and default value from resources.
        String newsCategoryKey = null;
        if(key != null) {
            newsCategoryKey = key;
        } else {
            newsCategoryKey = context.getString(R.string.pref_news_category_key);
        }
        String newsCategoryDefault = context.getString(R.string.pref_news_category_world);

        // Retrieve the news category value from shared preferences.
        String newsCategoryValue = sharedPreferences.getString(newsCategoryKey, newsCategoryDefault);

        return newsCategoryValue;
    }

    /**
     * Returns the appropriate label for the current news category preference
     * retrieved from the shared preferences.
     */
    public static String getNewsCategoryLabel(Context context) {
        // Get shared preferences.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // Get the news category key and default value from resources.
        String newsCategoryKey = context.getString(R.string.pref_news_category_key);
        String newsCategoryDefault = context.getString(R.string.pref_news_category_world);

        // Retrieve the news category value from shared preferences.
        String newsCategoryValue = sharedPreferences.getString(newsCategoryKey, newsCategoryDefault);

        // Get the appropriate label for the current news category.
        String newsCategoryLabel = null;
        if(newsCategoryValue.equals(context.getString(R.string.pref_news_category_world))) {
            newsCategoryLabel = context.getString(R.string.label_news_category_world);
        } else if(newsCategoryValue.equals(context.getString(R.string.pref_news_category_business))) {
            newsCategoryLabel = context.getString(R.string.label_news_category_business);
        } else if(newsCategoryValue.equals(context.getString(R.string.pref_news_category_technology))) {
            newsCategoryLabel = context.getString(R.string.label_news_category_technology);
        } else if(newsCategoryValue.equals(context.getString(R.string.pref_news_category_health))) {
            newsCategoryLabel = context.getString(R.string.label_news_category_health);
        } else if(newsCategoryValue.equals(context.getString(R.string.pref_news_category_travel))) {
            newsCategoryLabel = context.getString(R.string.label_news_category_travel);
        } else if(newsCategoryValue.equals(context.getString(R.string.pref_news_category_sports))) {
            newsCategoryLabel = context.getString(R.string.label_news_category_sports);
        } else if(newsCategoryValue.equals(context.getString(R.string.pref_news_category_favorites))) {
            newsCategoryLabel = context.getString(R.string.label_news_category_favorites);
        }

        return newsCategoryLabel;
    }

    /**
     * Sends a local broadcast notifying that the data has been updated.
     */
    public static void sendDataUpdatedBroadcast(Context context) {
        // Create the intent to send only to components within the app.
        Intent dataUpdatedIntent = new Intent()
                .setAction(context.getString(R.string.action_data_updated))
                .setPackage(context.getPackageName());

        // Send local broadcast.
        context.sendBroadcast(dataUpdatedIntent);
    }

    /**
     * Returns a string after removing all occurrences of a specific string
     * from the source string.
     */
    public static String removeCharsFromString(String source, String remove) {
        // Create a string builder to hold the result string.
        StringBuilder result = new StringBuilder();

        // Convert source string into char array and iterate through it.
        for(char c : source.toCharArray()) {
            if(remove.indexOf(c) == -1) {
                result.append(c);
            }
        }

        return result.toString();
    }
}
