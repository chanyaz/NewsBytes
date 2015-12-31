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

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;

import com.ravi.apps.android.newsbytes.data.NewsContract;

/**
 * Adds a favorite news story into the database through a content provider.
 */
public class AddFavoriteService extends IntentService {

    // Favorite news story to store in the database.
    private News mNews;

    // Content resolver.
    private ContentResolver mContentResolver;

    public AddFavoriteService() {
        super("AddFavoriteService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Get the news object from the parcel.
        mNews = intent.getParcelableExtra(DetailsFragment.NEWS_FAVORITE);

        // Get the content resolver.
        mContentResolver = getContentResolver();

        // Insert the favorite news story into the database.
        insertNews();
    }

    private void insertNews() {
        // Create content values.
        ContentValues newsValues = new ContentValues();

        // Add news details into content values.
        newsValues.put(NewsContract.NewsEntry.COLUMN_HEADLINE, mNews.getHeadline());
        newsValues.put(NewsContract.NewsEntry.COLUMN_SUMMARY, mNews.getSummary());
        newsValues.put(NewsContract.NewsEntry.COLUMN_URI_STORY, mNews.getUriStory());
        newsValues.put(NewsContract.NewsEntry.COLUMN_AUTHOR, mNews.getAuthor());
        newsValues.put(NewsContract.NewsEntry.COLUMN_DATE, mNews.getDate());
        newsValues.put(NewsContract.NewsEntry.COLUMN_URI_THUMBNAIL, mNews.getUriThumbnail());
        newsValues.put(NewsContract.NewsEntry.COLUMN_THUMBNAIL, mNews.getThumbnail());
        newsValues.put(NewsContract.NewsEntry.COLUMN_CAPTION_THUMBNAIL, mNews.getCaptionThumbnail());
        newsValues.put(NewsContract.NewsEntry.COLUMN_COPYRIGHT_THUMBNAIL, mNews.getCopyrightThumbnail());
        newsValues.put(NewsContract.NewsEntry.COLUMN_URI_PHOTO, mNews.getUriPhoto());
        newsValues.put(NewsContract.NewsEntry.COLUMN_PHOTO, mNews.getPhoto());
        newsValues.put(NewsContract.NewsEntry.COLUMN_CAPTION_PHOTO, mNews.getCaptionPhoto());
        newsValues.put(NewsContract.NewsEntry.COLUMN_COPYRIGHT_PHOTO, mNews.getCopyrightPhoto());
        newsValues.put(NewsContract.NewsEntry.COLUMN_IS_FAVORITE, mNews.getIsFavorite());

        // Insert into database through content provider.
        mContentResolver.insert(NewsContract.NewsEntry.CONTENT_URI, newsValues);
    }
}
