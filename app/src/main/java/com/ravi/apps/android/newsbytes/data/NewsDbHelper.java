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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ravi.apps.android.newsbytes.data.NewsContract.NewsEntry;

/**
 * Creates, upgrades and deletes the local news database.
 */
public class NewsDbHelper extends SQLiteOpenHelper {

    // Database schema version.
    public static final int DATABASE_VERSION = 1;

    // Database name.
    public static final String DATABASE_NAME = "news.db";

    // SQL statement for creating the movie table.
    private static final String SQL_CREATE_NEWS_TABLE =
            "CREATE TABLE " + NewsEntry.TABLE_NAME + " (" +
                    NewsEntry._ID + " INTEGER PRIMARY KEY, " +
                    NewsEntry.COLUMN_HEADLINE + " TEXT NOT NULL, " +
                    NewsEntry.COLUMN_SUMMARY + " TEXT, " +
                    NewsEntry.COLUMN_URI_STORY + " TEXT, " +
                    NewsEntry.COLUMN_AUTHOR + " TEXT, " +
                    NewsEntry.COLUMN_DATE + " TEXT, " +
                    NewsEntry.COLUMN_URI_THUMBNAIL + " TEXT, " +
                    NewsEntry.COLUMN_THUMBNAIL + " BLOB, " +
                    NewsEntry.COLUMN_CAPTION_THUMBNAIL + " TEXT, " +
                    NewsEntry.COLUMN_COPYRIGHT_THUMBNAIL + " TEXT, " +
                    NewsEntry.COLUMN_URI_PHOTO + " TEXT, " +
                    NewsEntry.COLUMN_PHOTO + " BLOB, " +
                    NewsEntry.COLUMN_CAPTION_PHOTO + " TEXT, " +
                    NewsEntry.COLUMN_COPYRIGHT_PHOTO + " TEXT, " +
                    NewsEntry.COLUMN_IS_FAVORITE + " INTEGER NOT NULL);";

    // SQL statement for deleting the movie table.
    private static final String SQL_DELETE_NEWS_TABLE =
            "DROP TABLE IF EXISTS " + NewsEntry.TABLE_NAME;

    public NewsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the movie, trailer and review tables.
        db.execSQL(SQL_CREATE_NEWS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Delete the data on upgrade as it's a cache for web data.
        db.execSQL(SQL_DELETE_NEWS_TABLE);

        // Create the database with new schema.
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}





