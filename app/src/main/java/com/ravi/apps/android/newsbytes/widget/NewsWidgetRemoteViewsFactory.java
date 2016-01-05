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

package com.ravi.apps.android.newsbytes.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.ravi.apps.android.newsbytes.HeadlinesFragment;
import com.ravi.apps.android.newsbytes.R;
import com.ravi.apps.android.newsbytes.Utility;
import com.ravi.apps.android.newsbytes.data.NewsContract;

/**
 * Remote adapter that binds to the list view in the news headlines collection widget.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class NewsWidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    // Tag for logging messages.
    private static final String LOG_TAG = NewsWidgetRemoteViewsFactory.class.getSimpleName();

    // Projection for the query.
    public static final String[] NEWS_PROJECTION = {
            NewsContract.NewsEntry._ID,
            NewsContract.NewsEntry.COLUMN_HEADLINE
    };

    // Column indices tied to the query projection.
    public static final int COL_ID = 0;
    public static final int COL_HEADLINE = 1;

    // Store the cursor containing news headlines data.
    private Cursor mCursor = null;
    // Store the context.
    private Context mContext;
    // Store the intent.
    private Intent mIntent;

    NewsWidgetRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mIntent = intent;
    }

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, mContext.getString(R.string.log_on_create));
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, mContext.getString(R.string.log_on_destroy));

        // Check and close the cursor.
        if(mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
    }

    @Override
    public int getCount() {
        // Return the number of rows in the cursor.
        return mCursor != null ? mCursor.getCount() : 0;
    }

    @Override
    public int getViewTypeCount() {
        // Only one view type.
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public RemoteViews getLoadingView() {
        return new RemoteViews(mContext.getPackageName(), R.layout.widget_news_list_item);
    }

    @Override
    public long getItemId(int i) {
        // Check if cursor is valid and has the requested row.
        if(mCursor != null && mCursor.moveToPosition(i)) {
            return mCursor.getLong(COL_ID);
        }

        return i;
    }

    @Override
    public RemoteViews getViewAt(int i) {
        // Check if it's a valid position and cursor exists.
        if(i == AdapterView.INVALID_POSITION || mCursor == null || !mCursor.moveToPosition(i)) {
            return null;
        }

        // Create the remote view corresponding to the list view item.
        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(),
                R.layout.widget_news_list_item);

        // Get the headline and set it onto the text view.
        String headline = mCursor.getString(HeadlinesFragment.COL_HEADLINE);

        // Check if headline is non null and non empty. If so, set headline & content description.
        if(headline != null && !headline.isEmpty()) {
            remoteViews.setTextViewText(R.id.widget_headline, headline);
            remoteViews.setContentDescription(R.id.widget_headline,headline);
        } else {
            // TODO: Display headline not available message.
        }

        // Create and set the fill in intent.
        // Pass in the position of the widget news item clicked as intent extra.
        final Intent fillInIntent = new Intent();
        fillInIntent.putExtra(mContext.getString(R.string.extra_widget_item_position), i);
        remoteViews.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);

        return remoteViews;
    }

    @Override
    public void onDataSetChanged() {
        Log.d(LOG_TAG, mContext.getString(R.string.log_on_dataset_changed));

        // Check and close cursor.
        if(mCursor != null) {
            mCursor.close();
            mCursor = null;
        }

        // Clear the calling identity (app widget host).
        final long callingIdentityToken = Binder.clearCallingIdentity();

        // Sort order for the query.
        String sortOrder = NewsContract.NewsEntry.COLUMN_DATE +
                mContext.getString(R.string.descending_sort_order);

        // Selection criteria and arguments.
        final String selection = NewsContract.NewsEntry.COLUMN_IS_FAVORITE + "=?";
        String[] selectionArgs;

        // Set selection based on whether news category is favorite.
        if(Utility.getNewsCategoryPreference(mContext, null)
                .equals(mContext.getResources().getString(R.string.pref_news_category_favorites))) {
            // Only get the favorite news stories.
            selectionArgs = new String[]{Integer.toString(1)};
        } else {
            // Get the news stories other than favorites.
            selectionArgs = new String[]{Integer.toString(0)};
        }

        // Query the content provider.
        mCursor = mContext.getContentResolver().query(
                NewsContract.NewsEntry.CONTENT_URI,
                NEWS_PROJECTION,
                selection,
                selectionArgs,
                sortOrder);

        // Restore the calling identity.
        Binder.restoreCallingIdentity(callingIdentityToken);
    }
}

