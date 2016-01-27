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

package com.ravi.apps.android.newsbytes.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.ravi.apps.android.newsbytes.R;
import com.ravi.apps.android.newsbytes.data.NewsContract.NewsEntry;

/**
 * News content provider.
 */
public class NewsProvider extends ContentProvider {

    // URI matcher.
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    // News db helper class.
    private NewsDbHelper mNewsDbHelper;

    // Constants to match each of the URIs supported by this content provider.
    static final int NEWS = 100;

   // Builds and returns the uri matcher.
    private static UriMatcher buildUriMatcher() {
        // Instantiate the uri matcher.
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = NewsContract.CONTENT_AUTHORITY;

        // Define the mapping from the URIs to the constants.
        uriMatcher.addURI(authority, NewsContract.PATH_NEWS, NEWS);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        // Create and hold the news db helper.
        mNewsDbHelper = new NewsDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        // Determine the type of URI passed in.
        final int uriMatch = sUriMatcher.match(uri);

        // Return the appropriate type.
        switch (uriMatch) {
            case NEWS:
                return NewsContract.NewsEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException(getContext()
                        .getString(R.string.msg_err_unknown_uri) + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // Get a readable database.
        final SQLiteDatabase readDb = mNewsDbHelper.getReadableDatabase();

        // Get the match for the passed in URI.
        final int uriMatch = sUriMatcher.match(uri);

        // Create a cursor to hold the result of the database query.
        Cursor resultCursor;

        // Perform appropriate query.
        switch(uriMatch) {
            case NEWS: {
                // Query the database.
                resultCursor = readDb.query(
                        NewsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            default: {
                throw new UnsupportedOperationException(getContext()
                        .getString(R.string.msg_err_unknown_uri) + uri);
            }
        }

        // Set notification.
        resultCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return resultCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // Get a writable database.
        final SQLiteDatabase writeDb = mNewsDbHelper.getWritableDatabase();

        // Get the uri match for the passed in URI.
        final int uriMatch = sUriMatcher.match(uri);

        // Uri to hold the result.
        Uri resultUri;

        switch (uriMatch) {
            case NEWS: {
                // Insert values into news table.
                long id = writeDb.insert(NewsEntry.TABLE_NAME, null, values);

                // Check if insert was successful.
                if (id > 0) {
                    resultUri = NewsEntry.buildNewsUri(id);
                } else {
                    // Throw sql exception.
                    throw new android.database.SQLException(getContext()
                            .getString(R.string.err_insert_failed) + uri);
                }

                break;
            }
            default: {
                // Throw unsupported operation exception.
                throw new UnsupportedOperationException(getContext()
                        .getString(R.string.msg_err_unknown_uri) + uri);
            }
        }

        // Notify any observers of the change.
        getContext().getContentResolver().notifyChange(uri, null);

        return resultUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        // Get a writable database.
        final SQLiteDatabase writeDb = mNewsDbHelper.getWritableDatabase();

        // Get the uri match for the passed in URI.
        final int uriMatch = sUriMatcher.match(uri);

        switch (uriMatch) {
            case NEWS: {
                // Begin the db transaction.
                writeDb.beginTransaction();

                // Count the insertions made.
                int insertCount = 0;
                try {
                    for(ContentValues value : values) {
                        // Insert values into news table.
                        long id = writeDb.insert(NewsEntry.TABLE_NAME, null, value);

                        // Check if insert was successful and increment count.
                        if (id != -1) {
                            insertCount++;
                        }
                    }
                    // Transaction successful.
                    writeDb.setTransactionSuccessful();
                } finally {
                    // End the db transaction.
                    writeDb.endTransaction();
                }

                // Notify observers of change.
                getContext().getContentResolver().notifyChange(uri, null);

                return insertCount;
            }
            default: {
                return super.bulkInsert(uri, values);
            }
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Get a writable database.
        final SQLiteDatabase writeDb = mNewsDbHelper.getWritableDatabase();

        // Get the uri match for the passed in URI.
        final int uriMatch = sUriMatcher.match(uri);

        // Number of rows updated.
        int rowsUpdated = 0;

        switch (uriMatch) {
            case NEWS: {
                // Update values in news table.
                rowsUpdated = writeDb.update(NewsEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            default: {
                throw new UnsupportedOperationException(getContext()
                        .getString(R.string.msg_err_unknown_uri) + uri);
            }
        }

        // Notify any observers only if any updates were made.
        if (rowsUpdated != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get a writable database.
        final SQLiteDatabase writeDb = mNewsDbHelper.getWritableDatabase();

        // Get the uri match for the passed in URI.
        final int uriMatch = sUriMatcher.match(uri);

        // Number of rows deleted.
        int rowsDeleted = 0;

        if(selection == null) selection = "1";

        switch(uriMatch) {
            case NEWS: {
                // Delete from news table.
                rowsDeleted = writeDb.delete(NewsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default: {
                throw new UnsupportedOperationException(getContext()
                        .getString(R.string.msg_err_unknown_uri) + uri);
            }
        }

        // Notify any observers only if any deletions were made.
        if(rowsDeleted != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        return rowsDeleted;
    }
}

