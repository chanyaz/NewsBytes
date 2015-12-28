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

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines URIs, table and column names for the news database.
 */
public class NewsContract {

    // Content authority for the content provider.
    public static final String CONTENT_AUTHORITY = "com.ravi.apps.android.newsbytes";

    // Base URI for the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Path that can be appended to the base content URI for framing the complete URI.
    public static final String PATH_NEWS = "news";

    /**
     * Defines the news table contents.
     */
    public static final class NewsEntry implements BaseColumns {
        // News table name.
        public static final String TABLE_NAME = "news";

        // Headline, stored as string.
        public static final String COLUMN_HEADLINE = "headline";

        // Summary, stored as string.
        public static final String COLUMN_SUMMARY = "summary";

        // URI for the story, stored as string.
        public static final String COLUMN_URI_STORY = "uri_story";

        // Author, stored as string.
        public static final String COLUMN_AUTHOR = "author";

        // Date, stored as string.
        public static final String COLUMN_DATE = "date";

        // URI for the thumbnail, stored as string.
        public static final String COLUMN_URI_THUMBNAIL = "uri_thumbnail";

        // Thumbnail, stored as blob.
        public static final String COLUMN_THUMBNAIL = "thumbnail";

        // Caption for the thumbnail, stored as string.
        public static final String COLUMN_CAPTION_THUMBNAIL = "caption_thumbnail";

        // Copyright for the thumbnail, stored as string.
        public static final String COLUMN_COPYRIGHT_THUMBNAIL = "copyright_thumbnail";

        // URI for the photo, stored as string.
        public static final String COLUMN_URI_PHOTO = "uri_photo";

        // Photo, stored as blob.
        public static final String COLUMN_PHOTO = "photo";

        // Caption for the photo, stored as string.
        public static final String COLUMN_CAPTION_PHOTO = "caption_photo";

        // Caption for the photo, stored as string.
        public static final String COLUMN_COPYRIGHT_PHOTO = "copyright_photo";

        // Flag to depict whether it is marked as favorite, stored as integer (0 - false, 1 - true).
        public static final String COLUMN_IS_FAVORITE = "is_favorite";

        // Build the base news URI.
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_NEWS).build();

        // Directory content type.
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NEWS;

        // Item content type.
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NEWS;

        public static Uri buildNewsUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        // Append the news id to the URI.
        public static Uri appendNewsIdToUri(int newsId) {
            return CONTENT_URI.buildUpon().appendPath(((Integer) newsId).toString()).build();
        }

        // Extract the news id from the URI.
        public static int getNewsIdFromUri(Uri uri) {
            return Integer.parseInt(uri.getPathSegments().get(1));
        }
    }
}


