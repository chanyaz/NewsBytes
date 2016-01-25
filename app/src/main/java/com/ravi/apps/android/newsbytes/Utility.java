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
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Provides various utility methods.
 */
public class Utility {
    // Tag for logging messages.
    public static final String LOG_TAG = Utility.class.getSimpleName();

    /**
     * Returns a boolean signifying whether the notifications preference is on.
     */
    public static boolean getNewsNotificationsPreference(Context context, String key) {
        // Get shared preferences.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // Get the news notifications key and default value from resources.
        String newsNotificationsKey = null;
        if(key != null) {
            newsNotificationsKey = key;
        } else {
            newsNotificationsKey = context.getString(R.string.pref_notifications_key);
        }
        String newsNotificationsDefault = context.getString(R.string.pref_notifications_default);
        boolean defaultValue = newsNotificationsDefault
                .equals(context.getString(R.string.pref_notifications_true))? true : false;

        // Retrieve the news notifications value from shared preferences.
        boolean newsNotificationsValue = sharedPreferences.getBoolean(newsNotificationsKey, defaultValue);

        return newsNotificationsValue;
    }

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

    /**
     * Converts and returns the corresponding byte array for the bitmap
     * linked to the image view passed in.
     */
    public static byte[] convertToByteArray(ImageView imageView) {
        // Check if image view is valid and has associated drawable. If not return null.
        if(imageView == null || imageView.getDrawable() == null) {
            return null;
        }

        // Get bitmap from the image view.
        Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();

        // Convert bitmap into byte array.
        byte[] thumbnailByteArray = null;
        if(bitmap != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            thumbnailByteArray = stream.toByteArray();
        }

        return thumbnailByteArray;
    }

    /**
     * Extracts, formats and returns the date in string format from the raw string passed in.
     */
    public static String getFormattedDate(Context context, String inputDate) {
        try {
            // Holds the final converted date string.
            String date = null;

            // Create a string builder to hold the intermediate result string.
            StringBuilder result = new StringBuilder();

            // Convert source string into char array and iterate through it. Break upon finding
            // the character 'T'.
            for(char c : inputDate.toCharArray()) {
                if(c == 'T') {
                    break;
                } else {
                    result.append(c);
                }
            }

            // Parse the string and get the corresponding date object.
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date parse = simpleDateFormat.parse(result.toString());

            // Set the date into the calendar object.
            Calendar c = Calendar.getInstance();
            c.setTime(parse);

            // Compose the date string.
            date = c.get(Calendar.DATE) + " " +
                    getMonthForInt(c.get(Calendar.MONTH)) + ", " +
                    c.get(Calendar.YEAR);

            return date;

        } catch(ParseException e) {
            Log.e(LOG_TAG, context.getString(R.string.msg_err_invalid_date) + e.getLocalizedMessage());
        }

        return null;
    }

    /**
     * Helper method to get month name give the month number.
     */
    private static String getMonthForInt(int num) {
        // Holds the month name.
        String month = "";

        // Get the date format symbols for months and extract relevant month.
        DateFormatSymbols dateFormatSymbols = new DateFormatSymbols();
        String[] months = dateFormatSymbols.getMonths();
        if(num >= 0 && num <= 11 ) {
            month = months[num];
        }

        return month;
    }
}
