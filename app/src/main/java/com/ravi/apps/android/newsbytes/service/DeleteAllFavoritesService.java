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

import com.ravi.apps.android.newsbytes.data.NewsContract;

/**
 * Deletes all favorite news stories in the database through a content provider.
 */
public class DeleteAllFavoritesService extends IntentService {

    // Content resolver.
    private ContentResolver mContentResolver;

    public DeleteAllFavoritesService() {
        super("DeleteAllFavoritesService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Get the content resolver.
        mContentResolver = getContentResolver();

        // Delete all favorite news stories from the database.
        deleteAllFavorites();
    }

    private void deleteAllFavorites() {
        // Selection criteria and arguments.
        final String selection = NewsContract.NewsEntry.COLUMN_IS_FAVORITE + "=?";
        String[] selectionArgs;
        selectionArgs = new String[]{Integer.toString(1)};

        // Delete from database through content provider.
        mContentResolver.delete(NewsContract.NewsEntry.CONTENT_URI, selection, selectionArgs);
    }
}
