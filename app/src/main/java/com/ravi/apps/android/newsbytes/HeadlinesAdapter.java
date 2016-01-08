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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Cursor adapter storing the news story data.
 */
public class HeadlinesAdapter extends CursorAdapter {

    // Tag for logging messages.
    private static final String LOG_TAG = HeadlinesAdapter.class.getSimpleName();

    // Keys for the dynamically generated transition names.
    public static final String THUMBNAIL_TRANSITION_NAME = "thumbnail_transition_name";
    public static final String HEADLINE_TRANSITION_NAME = "headline_transition_name";

    // Cache of the child views for a headlines list item.
    public static class ViewHolder {
        public final ImageView thumbnailView;
        public final TextView headlineView;

        public ViewHolder(View view) {
            thumbnailView = (ImageView) view.findViewById(R.id.thumbnail_imageview);
            headlineView = (TextView) view.findViewById(R.id.main_headlines_textview);
        }
    }

    public HeadlinesAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate the view.
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_headlines, parent, false);

        // Create the view holder and set it as the tag for the view.
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Extract the view holder.
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        // Get the mark as favorite flag.
        int isFavorite = cursor.getInt(HeadlinesFragment.COL_IS_FAVORITE);

        // If it's marked as favorite load image from byte stream, else use Picasso for loading.
        if(isFavorite == 1) {
            // Get the thumbnail blob from cursor.
            byte[] thumbnailByteStream = cursor.getBlob(HeadlinesFragment.COL_THUMBNAIL);

            // Check if blob for thumbnail is non null.
            if(thumbnailByteStream != null) {
                // Get the thumbnail bitmap from byte array.
                Bitmap thumbnailBitmap = BitmapFactory.decodeByteArray(thumbnailByteStream, 0, thumbnailByteStream.length);

                // Set the thumbnail bitmap into the image view.
                viewHolder.thumbnailView.setImageBitmap(thumbnailBitmap);
                viewHolder.thumbnailView.setScaleType(ImageView.ScaleType.FIT_XY);
            } else {
                // Show thumbnail placeholder icon and log error message.
                viewHolder.thumbnailView.setImageResource(R.drawable.thumbnail_placeholder);
                Log.e(LOG_TAG, context.getString(R.string.msg_err_no_thumbnail));
            }
        } else {
            // Get the thumbnail uri.
            String urlThumbnail = cursor.getString(HeadlinesFragment.COL_URI_THUMBNAIL);

            // Check if url for thumbnail is non null.
            if(urlThumbnail != null) {
                // Remove escape characters from the string.
                String url = Utility.removeCharsFromString(urlThumbnail, "\\");

                // Load the thumbnail into image view using Picasso.
                Picasso.with(context)
                        .load(url)
                        .placeholder(R.drawable.thumbnail_placeholder)
                        .fit()
                        .into(viewHolder.thumbnailView);
            } else {
                // Show thumbnail placeholder icon and log error message.
                viewHolder.thumbnailView.setImageResource(R.drawable.thumbnail_placeholder);
                Log.e(LOG_TAG, context.getString(R.string.msg_err_no_thumbnail));
            }
        }

        // Get the headline and set it onto the text view.
        String headline = cursor.getString(HeadlinesFragment.COL_HEADLINE);

        // Check if headline is non null and non empty.
        if(headline != null && !headline.isEmpty()) {
            viewHolder.headlineView.setText(headline);
            viewHolder.headlineView.setContentDescription(headline);
        } else {
            // Display headline not available message and log error message.
            viewHolder.headlineView.setText(context.getString(R.string.msg_err_no_headline));
            viewHolder.headlineView.setContentDescription(context.getString(R.string.msg_err_no_headline));
            Log.e(LOG_TAG, context.getString(R.string.msg_err_no_headline));
        }

        // Generate transition names for shared elements for devices running lollipop or above.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            viewHolder.thumbnailView.setTransitionName(THUMBNAIL_TRANSITION_NAME + cursor.getPosition());
            viewHolder.headlineView.setTransitionName(HEADLINE_TRANSITION_NAME + cursor.getPosition());
        }
    }
}


