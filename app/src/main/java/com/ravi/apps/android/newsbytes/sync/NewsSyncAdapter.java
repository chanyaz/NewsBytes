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

package com.ravi.apps.android.newsbytes.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.ravi.apps.android.newsbytes.MainActivity;
import com.ravi.apps.android.newsbytes.R;
import com.ravi.apps.android.newsbytes.Utility;
import com.ravi.apps.android.newsbytes.data.NewsContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/**
 * Sync adapter to query the news data from the New York Times server.
 */
public class NewsSyncAdapter extends AbstractThreadedSyncAdapter {

    // Tag for logging messages.
    public static final String LOG_TAG = NewsSyncAdapter.class.getSimpleName();

    // Base URL for the query.
    private final String NYT_BASE_URL = "http://api.nytimes.com/svc/topstories/v1/";

    // Query parameter.
    private final String NYT_API_KEY_PARAM = "api-key";

    // Query parameter values.
    private final String NYT_SECTION_WORLD = "world";
    private final String NYT_SECTION_BUSINESS = "business";
    private final String NYT_SECTION_TECHNOLOGY = "technology";
    private final String NYT_SECTION_HEALTH = "health";
    private final String NYT_SECTION_TRAVEL = "travel";
    private final String NYT_SECTION_SPORTS = "sports";
    private final String NYT_RESPONSE_FORMAT = ".json";
    private final String NYT_API_KEY = getContext().getString(R.string.nyt_api_key);

    // JSON fields in the query response.
    private final String NYT_STATUS = "status";
    private final String NYT_RESULTS = "results";
    private final String NYT_TITLE = "title";
    private final String NYT_ABSTRACT = "abstract";
    private final String NYT_URL = "url";
    private final String NYT_BYLINE = "byline";
    private final String NYT_PUBLISHED_DATE = "published_date";
    private final String NYT_MULTIMEDIA = "multimedia";
    private final String NYT_FORMAT = "format";
    private final String NYT_CAPTION = "caption";
    private final String NYT_COPYRIGHT = "copyright";

    // JSON format field values.
    private final String NYT_FORMAT_STANDARD_THUMBNAIL = "Standard Thumbnail";
    private final String NYT_FORMAT_NORMAL = "Normal";

    // Interval at which to sync with the news server, in seconds.
    public static final int SYNC_INTERVAL = 30 * 60;    // One hour.
//    public static final int SYNC_INTERVAL = 60 * 10;    // Three minutes.

    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;

    // News notification id.
    private static final int NEWS_NOTIFICATION_ID = 1234;

    public NewsSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    public static void initializeSyncAdapter(Context context) {
        Log.d(LOG_TAG, context.getString(R.string.log_initialize_sync_adapter));

        getSyncAccount(context);
    }

    /**
     * Gets the fictitious account to be used with the sync adapter, or makes a new one
     * if the fictitious account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the android account manager.
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account.
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist. Add the account.
        if(null == accountManager.getPassword(newAccount)) {
            // Add the account and account type, no password or user data.
            if(!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    /**
     * Schedules periodic execution of the sync adapter.
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        // Get account and authority.
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);

        // Set inexact timer for the periodic sync for KitKat and above.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SyncRequest request = new SyncRequest.Builder()
                    .syncPeriodic(syncInterval, flexTime)
                    .setSyncAdapter(account, authority)
                    .setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account, authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Instructs the sync adapter to sync immediately.
     */
    public static void syncImmediately(Context context) {
        Log.d(LOG_TAG, context.getString(R.string.log_sync_immediately));

        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, getContext().getString(R.string.log_on_perform_sync));

        HttpURLConnection httpURLConnection = null;
        BufferedReader bufferedReader = null;

        try {
            String newsJsonStr = null;

            // Get the news category preference from shared preferences.
            String newsCategoryPreference = Utility.getNewsCategoryPreference(getContext(), null);

            // Set the news section query param value based on preference.
            String newsCategory = null;
            if(newsCategoryPreference
                    .equals(getContext().getString(R.string.pref_news_category_favorites))) {
                // If favorites category is selected, no need to do a sync - simply return.
                return;
            } else if(newsCategoryPreference
                    .equals(getContext().getString(R.string.pref_news_category_world))) {
                newsCategory = NYT_SECTION_WORLD;
            } else if(newsCategoryPreference
                    .equals(getContext().getString(R.string.pref_news_category_business))) {
                newsCategory = NYT_SECTION_BUSINESS;
            } else if(newsCategoryPreference
                    .equals(getContext().getString(R.string.pref_news_category_technology))) {
                newsCategory = NYT_SECTION_TECHNOLOGY;
            } else if(newsCategoryPreference
                    .equals(getContext().getString(R.string.pref_news_category_health))) {
                newsCategory = NYT_SECTION_HEALTH;
            } else if(newsCategoryPreference
                    .equals(getContext().getString(R.string.pref_news_category_travel))) {
                newsCategory = NYT_SECTION_TRAVEL;
            } else if(newsCategoryPreference
                    .equals(getContext().getString(R.string.pref_news_category_sports))) {
                newsCategory = NYT_SECTION_SPORTS;
            }

            // Build the uri for querying data from NYT api.
            Uri uri = Uri.parse(NYT_BASE_URL + newsCategory + NYT_RESPONSE_FORMAT)
                    .buildUpon()
                    .appendQueryParameter(NYT_API_KEY_PARAM, NYT_API_KEY)
                    .build();

            // Create the url for connecting to NYT server.
            URL url = new URL(uri.toString());
            Log.d(LOG_TAG, url.toString());

            // Create the request to NYT server and open the connection.
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();

            // Read the response input stream into a string buffer.
            InputStream inputStream = httpURLConnection.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuffer stringBuffer = new StringBuffer();
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }

            // Convert the string buffer to string.
            newsJsonStr = stringBuffer.toString();
            Log.d(LOG_TAG, newsJsonStr);

            // Parse the JSON string and extract the news data.
            getNewsDataFromJson(newsJsonStr);

            // Send a local broadcast informing the widget to refresh it's data.
            Utility.sendDataUpdatedBroadcast(getContext());

            // Send a notification to the user that fresh news updates are now available.
            sendNewsNotification(getContext());

        } catch(IOException e) {
            Log.e(LOG_TAG, getContext().getString(R.string.log_on_perform_sync_error)
                    + e.getLocalizedMessage());
        } catch(JSONException e) {
            Log.e(LOG_TAG, getContext().getString(R.string.log_on_perform_sync_error)
                    + e.getLocalizedMessage());
        } finally {
            // Close url connection, if open.
            if(httpURLConnection != null) {
                httpURLConnection.disconnect();
            }

            // Close the buffered reader, if open.
            if(bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch(final IOException e) {
                    Log.e(LOG_TAG, getContext().getString(R.string.log_on_perform_sync_error)
                            + e.getLocalizedMessage());
                }
            }
        }
        return;
    }

    /**
     * Parses the input string in JSON format, extracts the news data and then writes it
     * into the content provider. Throws a JSON exception in case of any error.
     */
    private void getNewsDataFromJson(String newsJsonStr) throws JSONException {
        try {
            // Create the JSON object from the input string.
            JSONObject newsJson = new JSONObject(newsJsonStr);

            // Get the JSON array containing the results.
            JSONArray resultsJsonArray = null;
            if(newsJson.has(NYT_RESULTS)
                    && newsJson.get(NYT_RESULTS) instanceof JSONArray
                    && newsJson.getJSONArray(NYT_RESULTS) != null
                    && newsJson.getJSONArray(NYT_RESULTS).length() != 0) {
                resultsJsonArray = newsJson.getJSONArray(NYT_RESULTS);
            } else {
                // Query did not return any results, log message and return.
                Log.d(LOG_TAG, getContext().getString(R.string.msg_err_zero_results));
                return;
            }

            // Get length of the results array.
            int resultsLength = resultsJsonArray.length();

            // Create a vector of content values to hold the news stories.
            Vector<ContentValues> vectorContentValues = new Vector<ContentValues>(resultsLength);

            // Traverse the json array extracting each news story.
            for(int i = 0; i < resultsLength; i++) {
                // Get the JSON object corresponding to a news story.
                JSONObject jsonNewsStory = resultsJsonArray.getJSONObject(i);

                // Extract the headline from the JSON object.
                String headline = null;

                // Check if headline exists and is a non-empty string.
                if(jsonNewsStory.has(NYT_TITLE)
                        && jsonNewsStory.get(NYT_TITLE) instanceof String
                        && !jsonNewsStory.getString(NYT_TITLE).isEmpty()) {
                    headline = jsonNewsStory.getString(NYT_TITLE);
                } else {
                    // No headline found, skip this story.
                    continue;
                }

                // Extract the thumbnail and photo from the JSON object.
                JSONArray multimediaJsonArray = null;
                String uriThumbnail = null;
                byte[] thumbnail = null;    // Stores a blob when marked as favorite, null for now.
                String captionThumbnail = null;
                String copyrightThumbnail = null;
                String uriPhoto = null;
                byte[] photo = null;    // Stores a blob when marked as favorite, null for now.
                String captionPhoto = null;
                String copyrightPhoto = null;

                // Check if multimedia exists and is in json array format.
                if(jsonNewsStory.has(NYT_MULTIMEDIA)
                        && jsonNewsStory.get(NYT_MULTIMEDIA) instanceof JSONArray
                        && jsonNewsStory.getJSONArray(NYT_MULTIMEDIA) != null) {
                    // Get the multimedia json array.
                    multimediaJsonArray = jsonNewsStory.getJSONArray(NYT_MULTIMEDIA);

                    // Traverse the multimedia json array.
                    for(int j = 0; j < multimediaJsonArray.length(); j++) {
                        // Get the JSON object corresponding to an image.
                        JSONObject jsonImage = multimediaJsonArray.getJSONObject(j);

                        // Get the image format.
                        String format = null;
                        if(jsonImage.has(NYT_FORMAT)
                                && jsonImage.get(NYT_FORMAT) instanceof String
                                && jsonImage.getString(NYT_FORMAT) != null
                                && !jsonImage.getString(NYT_FORMAT).isEmpty()) {
                            // Get the image format.
                            format = jsonImage.getString(NYT_FORMAT);
                        } else {
                            // Image format not found, skip this image.
                            continue;
                        }

                        // Extract the image only if it's thumbnail or normal format.
                        if(format.equals(NYT_FORMAT_STANDARD_THUMBNAIL)) {
                            // Extract thumbnail uri.
                            if(jsonImage.has(NYT_URL)
                                    && jsonImage.get(NYT_URL) instanceof String
                                    && jsonImage.getString(NYT_URL) != null
                                    && !jsonImage.getString(NYT_URL).isEmpty()) {
                                uriThumbnail = jsonImage.getString(NYT_URL);
                            } else {
                                // Thumbnail uri not found, skip this image.
                                continue;
                            }

                            // Extract thumbnail caption.
                            if(jsonImage.has(NYT_CAPTION)
                                    && jsonImage.get(NYT_CAPTION) instanceof String
                                    && jsonImage.getString(NYT_CAPTION) != null
                                    && !jsonImage.getString(NYT_CAPTION).isEmpty()) {
                                captionThumbnail = jsonImage.getString(NYT_CAPTION);
                            }

                            // Extract thumbnail copyright.
                            if(jsonImage.has(NYT_COPYRIGHT)
                                    && jsonImage.get(NYT_COPYRIGHT) instanceof String
                                    && jsonImage.getString(NYT_COPYRIGHT) != null
                                    && !jsonImage.getString(NYT_COPYRIGHT).isEmpty()) {
                                copyrightThumbnail = jsonImage.getString(NYT_COPYRIGHT);
                            }
                        } else if(format.equals(NYT_FORMAT_NORMAL)) {
                            // Extract photo uri.
                            if(jsonImage.has(NYT_URL)
                                    && jsonImage.get(NYT_URL) instanceof String
                                    && jsonImage.getString(NYT_URL) != null
                                    && !jsonImage.getString(NYT_URL).isEmpty()) {
                                uriPhoto = jsonImage.getString(NYT_URL);
                            } else {
                                // Photo uri not found, skip this image.
                                continue;
                            }

                            // Extract photo caption.
                            if(jsonImage.has(NYT_CAPTION)
                                    && jsonImage.get(NYT_CAPTION) instanceof String
                                    && jsonImage.getString(NYT_CAPTION) != null
                                    && !jsonImage.getString(NYT_CAPTION).isEmpty()) {
                                captionPhoto = jsonImage.getString(NYT_CAPTION);
                            }

                            // Extract photo copyright.
                            if(jsonImage.has(NYT_COPYRIGHT)
                                    && jsonImage.get(NYT_COPYRIGHT) instanceof String
                                    && jsonImage.getString(NYT_COPYRIGHT) != null
                                    && !jsonImage.getString(NYT_COPYRIGHT).isEmpty()) {
                                copyrightPhoto = jsonImage.getString(NYT_COPYRIGHT);
                            }
                        }
                    }
                } else {
                    // No thumbnail or photo found, skip this story.
                    continue;
                }

                // Extract the summary from the JSON object.
                String summary = null;
                if(jsonNewsStory.has(NYT_ABSTRACT)
                        && jsonNewsStory.get(NYT_ABSTRACT) instanceof String
                        && !jsonNewsStory.getString(NYT_ABSTRACT).isEmpty()) {
                    summary = jsonNewsStory.getString(NYT_ABSTRACT);
                }

                // Extract the uri for the story from the JSON object.
                String uriStory = null;
                if(jsonNewsStory.has(NYT_URL)
                        && jsonNewsStory.get(NYT_URL) instanceof String
                        && !jsonNewsStory.getString(NYT_URL).isEmpty()) {
                    uriStory = jsonNewsStory.getString(NYT_URL);
                }

                // Extract the author from the JSON object.
                String author = null;
                if(jsonNewsStory.has(NYT_BYLINE)
                        && jsonNewsStory.get(NYT_BYLINE) instanceof String
                        && !jsonNewsStory.getString(NYT_BYLINE).isEmpty()) {
                    author = jsonNewsStory.getString(NYT_BYLINE);
                }

                // Extract the date from the JSON object.
                String date = null;
                if(jsonNewsStory.has(NYT_PUBLISHED_DATE)
                        && jsonNewsStory.get(NYT_PUBLISHED_DATE) instanceof String
                        && !jsonNewsStory.getString(NYT_PUBLISHED_DATE).isEmpty()) {
                    date = jsonNewsStory.getString(NYT_PUBLISHED_DATE);
                }

                // Marked as favorite, false for now.
                int isFavorite = 0;

                // Create content values for this news story.
                ContentValues newsValues = new ContentValues();

                newsValues.put(NewsContract.NewsEntry.COLUMN_HEADLINE, headline);
                newsValues.put(NewsContract.NewsEntry.COLUMN_SUMMARY, summary);
                newsValues.put(NewsContract.NewsEntry.COLUMN_URI_STORY, uriStory);
                newsValues.put(NewsContract.NewsEntry.COLUMN_AUTHOR, author);
                newsValues.put(NewsContract.NewsEntry.COLUMN_DATE, date);
                newsValues.put(NewsContract.NewsEntry.COLUMN_URI_THUMBNAIL, uriThumbnail);
                newsValues.put(NewsContract.NewsEntry.COLUMN_THUMBNAIL, thumbnail);
                newsValues.put(NewsContract.NewsEntry.COLUMN_CAPTION_THUMBNAIL, captionThumbnail);
                newsValues.put(NewsContract.NewsEntry.COLUMN_COPYRIGHT_THUMBNAIL, copyrightThumbnail);
                newsValues.put(NewsContract.NewsEntry.COLUMN_URI_PHOTO, uriPhoto);
                newsValues.put(NewsContract.NewsEntry.COLUMN_PHOTO, photo);
                newsValues.put(NewsContract.NewsEntry.COLUMN_CAPTION_PHOTO, captionPhoto);
                newsValues.put(NewsContract.NewsEntry.COLUMN_COPYRIGHT_PHOTO, copyrightPhoto);
                newsValues.put(NewsContract.NewsEntry.COLUMN_IS_FAVORITE, isFavorite);

                // Add the content values into the vector.
                vectorContentValues.add(newsValues);
            }

            // Check rows inserted and deleted.
            int rowsInserted = 0;
            int rowsDeleted = 0;

            // Add the news stories into the database.
            if(vectorContentValues.size() > 0) {
                // Copy the vector values into content values array.
                ContentValues[] arrayContentValues = new ContentValues[vectorContentValues.size()];
                vectorContentValues.toArray(arrayContentValues);

                // Delete the older data before inserting, except those marked as favorite.
                rowsDeleted = getContext().getContentResolver()
                        .delete(NewsContract.NewsEntry.CONTENT_URI,
                                NewsContract.NewsEntry.COLUMN_IS_FAVORITE + "=?",
                                new String[]{Integer.toString(0)});

                // Bulk insert into news table.
                rowsInserted = getContext().getContentResolver()
                        .bulkInsert(NewsContract.NewsEntry.CONTENT_URI, arrayContentValues);
            }

            Log.d(LOG_TAG, getContext().getString(R.string.log_sync_completed));
            Log.d(LOG_TAG, getContext().getString(R.string.log_rows_deleted) + rowsDeleted);
            Log.d(LOG_TAG, getContext().getString(R.string.log_rows_inserted) + rowsInserted);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        // Configure the sync.
        NewsSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        // Enable periodic sync.
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        // Trigger an immediate sync to get the ball rolling!
        syncImmediately(context);
    }

    /**
     * Notifies the user that fresh news updates are now available.
     */
    private void sendNewsNotification(Context context) {
        // Create the intent to open the app when the user taps on the notification.
        Intent mainIntent = new Intent(context, MainActivity.class);

        // Create the stack builder object.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        // Add the intent to the top of the stack.
        stackBuilder.addNextIntent(mainIntent);

        // Get a pending intent containing the entire back stack.
        PendingIntent pendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Create the notification builder object.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        // Set the pending intent onto the notification.
        builder.setContentIntent(pendingIntent);

        // Set the notification to automatically dismiss after the user taps it.
        builder.setAutoCancel(true);

        // Set the notification title and text.
        builder.setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.msg_notification_text));

        // Set the notification ticker.
        builder.setTicker(context.getString(R.string.msg_notification_text));

        // Set the notification large and small icons.
        builder.setSmallIcon(R.drawable.ic_notify_small)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_notify_large));

        // Send the notification.
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(
                context.NOTIFICATION_SERVICE);
        notificationManager.notify(NEWS_NOTIFICATION_ID, builder.build());
    }
}
