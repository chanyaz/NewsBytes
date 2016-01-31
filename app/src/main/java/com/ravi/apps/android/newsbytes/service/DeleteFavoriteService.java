/*
 * Copyright (C) 2016 Ravi
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

package com.ravi.apps.android.newsbytes.service;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;

import com.ravi.apps.android.newsbytes.DetailsFragment;
import com.ravi.apps.android.newsbytes.News;
import com.ravi.apps.android.newsbytes.data.NewsContract;

/**
 * Deletes favorite news stories in the database through a content provider.
 */
public class DeleteFavoriteService extends IntentService {

    // Content resolver.
    private ContentResolver mContentResolver;

    public DeleteFavoriteService() {
        super("DeleteFavoriteService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Get the content resolver.
        mContentResolver = getContentResolver();

        // Check if a specific news story is to be deleted or all stories determined
        // by the presence or absence of the news object extra.
        if(intent.hasExtra(DetailsFragment.NEWS_FAVORITE)) {
            // Get the news object from the parcel and delete corresponding news story.
            deleteFavorite((News) intent.getParcelableExtra(DetailsFragment.NEWS_FAVORITE));
        } else {
            // Delete all favorite news stories from the database.
            deleteAllFavorites();
        }
    }

    // Deletes specific favorite news story from database.
    private void deleteFavorite(News news) {
        // Get headline from the news object.
        String headline = news.getHeadline();

        // Selection criteria and arguments.
        final String selection = NewsContract.NewsEntry.COLUMN_IS_FAVORITE + "=? AND " +
                NewsContract.NewsEntry.COLUMN_HEADLINE + "=?";
        String[] selectionArgs;
        selectionArgs = new String[]{Integer.toString(1), headline};

        // Delete from database through content provider.
        mContentResolver.delete(NewsContract.NewsEntry.CONTENT_URI, selection, selectionArgs);
    }

    // Deletes all favorite news stories from database.
    private void deleteAllFavorites() {
        // Selection criteria and arguments.
        final String selection = NewsContract.NewsEntry.COLUMN_IS_FAVORITE + "=?";
        String[] selectionArgs;
        selectionArgs = new String[]{Integer.toString(1)};

        // Delete from database through content provider.
        mContentResolver.delete(NewsContract.NewsEntry.CONTENT_URI, selection, selectionArgs);
    }
}
