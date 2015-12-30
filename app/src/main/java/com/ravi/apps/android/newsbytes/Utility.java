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
    public static String getNewsCategoryPreference(Context context) {
        // Get shared preferences.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // Get the news category key and default value from resources.
        String newsCategoryKey = context.getString(R.string.pref_news_category_key);
        String newsCategoryDefault = context.getString(R.string.pref_news_category_world);

        // Retrieve the news category value from shared preferences.
        String newsCategoryValue = sharedPreferences.getString(newsCategoryKey, newsCategoryDefault);

        return newsCategoryValue;
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
