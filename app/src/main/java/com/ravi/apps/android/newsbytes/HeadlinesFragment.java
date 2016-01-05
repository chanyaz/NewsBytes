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

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.ravi.apps.android.newsbytes.data.NewsContract.NewsEntry;

import java.io.ByteArrayOutputStream;

/**
 * Displays a list of news story headlines and thumbnails retrieved from the New York Times server.
 * It allows the user to view details on any news story by tapping on it.
 */
public class HeadlinesFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>, ListView.OnItemClickListener {

    // Tag for logging messages.
    public static final String LOG_TAG = HeadlinesFragment.class.getSimpleName();

    // Cursor loader to fetch the list of news stories from the news content provider.
    private static final int NEWS_LOADER = 0;

    // Projection for the cursor loader.
    public static final String[] NEWS_PROJECTION = {
            NewsEntry._ID,
            NewsEntry.COLUMN_HEADLINE,
            NewsEntry.COLUMN_SUMMARY,
            NewsEntry.COLUMN_URI_STORY,
            NewsEntry.COLUMN_AUTHOR,
            NewsEntry.COLUMN_DATE,
            NewsEntry.COLUMN_URI_THUMBNAIL,
            NewsEntry.COLUMN_THUMBNAIL,
            NewsEntry.COLUMN_CAPTION_THUMBNAIL,
            NewsEntry.COLUMN_COPYRIGHT_THUMBNAIL,
            NewsEntry.COLUMN_URI_PHOTO,
            NewsEntry.COLUMN_PHOTO,
            NewsEntry.COLUMN_CAPTION_PHOTO,
            NewsEntry.COLUMN_COPYRIGHT_PHOTO,
            NewsEntry.COLUMN_IS_FAVORITE
    };

    // Column indices tied to the cursor loader projection.
    public static final int COL_ID = 0;
    public static final int COL_HEADLINE = 1;
    public static final int COL_SUMMARY = 2;
    public static final int COL_URI_STORY = 3;
    public static final int COL_AUTHOR = 4;
    public static final int COL_DATE = 5;
    public static final int COL_URI_THUMBNAIL = 6;
    public static final int COL_THUMBNAIL = 7;
    public static final int COL_CAPTION_THUMBNAIL = 8;
    public static final int COL_COPYRIGHT_THUMBNAIL = 9;
    public static final int COL_URI_PHOTO = 10;
    public static final int COL_PHOTO = 11;
    public static final int COL_CAPTION_PHOTO = 12;
    public static final int COL_COPYRIGHT_PHOTO  = 13;
    public static final int COL_IS_FAVORITE = 14;

    // List view item position key.
    private static final String LIST_POSITION_KEY = "list_selected_key";

    // List displaying headlines.
    private ListView mListView;

    // Text view for displaying appropriate user message when the list is blank.
    private TextView mEmptyListView;

    // Ad view displaying banner ad.
    private AdView mAdView;

    // Cursor adapter supplying headlines to the list.
    private HeadlinesAdapter mHeadlinesAdapter;

    // Current list view item position.
    private int mPosition = ListView.INVALID_POSITION;

    // Current news category preference.
    private String mNewsCategoryPreference;

    // Position of selected item or first visible item in the list view.
    private int mListPosition = ListView.INVALID_POSITION;

    public HeadlinesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the root view.
        View rootView = inflater.inflate(R.layout.fragment_headlines, container, false);

        // Create the news data adapter.
        mHeadlinesAdapter = new HeadlinesAdapter(getActivity(), null, 0);

        // Get the list view.
        mListView = (ListView) rootView.findViewById(R.id.headlines_listview);

        // Set the adapter for the list view.
        mListView.setAdapter(mHeadlinesAdapter);

        // Set item click event handler for list view.
        mListView.setOnItemClickListener(this);

        // Get the text view for empty list view message and set the text.
        mEmptyListView = (TextView) rootView.findViewById(R.id.status_listview);
        mEmptyListView.setText(getString(R.string.msg_status_loading));

        // Set the text view for empty list view.
        mListView.setEmptyView(mEmptyListView);

        // Get the current news category preference from shared preferences.
        mNewsCategoryPreference = Utility.getNewsCategoryPreference(getActivity(), null);

        // Check if it was a configuration change.
        if(savedInstanceState != null) {
            // Retrieve the saved news category preference.
            String savedNewsCategory = null;
            if(savedInstanceState.containsKey(getString(R.string.pref_news_category_key))) {
                savedNewsCategory = savedInstanceState.getString(getString(R.string.pref_news_category_key));
            }

            // Retrieve the saved list item position only if news category has not changed.
            if(mNewsCategoryPreference.equals(savedNewsCategory)
                    && savedInstanceState.containsKey(LIST_POSITION_KEY)) {
                mListPosition = savedInstanceState.getInt(LIST_POSITION_KEY);
            }
        }

        // Get the banner ad view.
        mAdView = (AdView) rootView.findViewById(R.id.adview);

        // Create an ad request and load it into the ad view.
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Initialize the news cursor loader.
        getLoaderManager().initLoader(NEWS_LOADER, null, this);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check if the news category preference has changed.
        if(!mNewsCategoryPreference.equals(Utility.getNewsCategoryPreference(getActivity(), null))) {
            // Update the news category from the shared preferences.
            mNewsCategoryPreference = Utility.getNewsCategoryPreference(getActivity(), null);

            // Reset the list position index.
            mListPosition = 0;

            // Display status message in empty list view.
            mEmptyListView.setText(getString(R.string.msg_status_loading));

            // Reset the news loader.
            onLoaderReset(null);

            // Restart the news loader.
            getLoaderManager().restartLoader(NEWS_LOADER, null, this);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save the current news category preference.
        outState.putString(getString(R.string.pref_news_category_key), mNewsCategoryPreference);

        // Get the first visible list position and save it in the grid index.
        mListPosition = mListView.getFirstVisiblePosition();

        // Save the grid index.
        if(mListPosition != mListView.INVALID_POSITION) {
            outState.putInt(LIST_POSITION_KEY, mListPosition);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Sort order for the query.
        final String sortOrder = NewsEntry.COLUMN_DATE +
                getActivity().getString(R.string.descending_sort_order);

        // Selection criteria and arguments.
        final String selection = NewsEntry.COLUMN_IS_FAVORITE + "=?";
        String[] selectionArgs;

        // Set selection based on whether news category is favorite.
        if(mNewsCategoryPreference.equals(getString(R.string.pref_news_category_favorites))) {
            // Only get the favorite news stories.
            selectionArgs = new String[]{Integer.toString(1)};
        } else {
            // Get the news stories other than favorites.
            selectionArgs = new String[]{Integer.toString(0)};
        }

        // Create loader to retrieve news data from database through content provider.
        return new CursorLoader(
                getActivity(),
                NewsEntry.CONTENT_URI,
                NEWS_PROJECTION,
                selection,
                selectionArgs,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Check if valid cursor was returned.
        if(data != null) {
            // Check if the cursor is empty.
            if(data.moveToFirst() == false) {
                Log.d(LOG_TAG, getActivity().getString(R.string.log_on_load_finished_empty));

                // TODO: Show error message.
                // Display appropriate status message to user and return.
//                mEmptyListView.setText(getString(R.string.msg_err_no_favorites));
                return;
            }

            // Assign the cursor to the adapter.
            mHeadlinesAdapter.swapCursor(data);

            // Check if widget list item was clicked.
            if(MainActivity.isWidgetItemClicked) {
                Log.d(LOG_TAG, getActivity().getString(R.string.log_on_load_finished_widget_item_clicked));

                // Set list position to widget item click position.
                if(MainActivity.widgetItemClickedPosition != ListView.INVALID_POSITION) {
                    mListPosition = MainActivity.widgetItemClickedPosition;
                }

                // Get the cursor at the click position.
                Cursor cursor = (Cursor) mHeadlinesAdapter.getItem(mListPosition);

                // Create the news object to pass back to the parent activity.
                News news = buildNewsObject(cursor);

                // Notify the parent activity that the user clicked a headline and pass on the news details.
                ((OnHeadlineSelectedListener) getActivity()).onHeadlineSelected(news);

                // Reset the widget list item clicked flag and list position.
                MainActivity.isWidgetItemClicked = false;
                MainActivity.widgetItemClickedPosition = ListView.INVALID_POSITION;
            }

            // Move to appropriate list item position.
            if(mListPosition != ListView.INVALID_POSITION) {
                mListView.setSelection(mListPosition);
            }
        } else {
            Log.d(LOG_TAG, getActivity().getString(R.string.log_on_load_finished_null));

            // TODO: Show error message.
            // Display appropriate error message to user.
//            mEmptyListView.setText(getString(R.string.msg_err_fetch_favorites));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Relinquish the cursor attached to the adapter.
        mHeadlinesAdapter.swapCursor(null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Get the cursor at the click position.
        Cursor newsCursor = (Cursor) parent.getItemAtPosition(position);

        // Create the news object to pass back to the parent activity.
        News news = buildNewsObject(newsCursor);

        // Get the thumbnail image view to extract bitmap for saving into news object.
        ImageView thumbnail = (ImageView) view.findViewById(R.id.thumbnail_imageview);

        // Extract bitmap from thumbnail image view and convert it to byte array.
        byte[] thumbnailByteArray = convertToByteArray(thumbnail);

        // Set the thumbnail byte array into the news object.
        news.setThumbnailByteArray(thumbnailByteArray);

        // Notify the parent activity that the user clicked a headline and pass on the news details.
        ((OnHeadlineSelectedListener) getActivity()).onHeadlineSelected(news);
    }

    /**
     * Public interface that needs to be implemented by the hosting activity to receive
     * notifications from fragment whenever the user selects/taps on a news headline.
     */
    public interface OnHeadlineSelectedListener {
        void onHeadlineSelected(News news);
    }

    /**
     * Builds and returns a news object from the cursor passed in.
     */
    private News buildNewsObject(Cursor cursor) {
        // Create and return the news object by extracting the data from the cursor.
        return new News(
                cursor.getString(COL_HEADLINE),
                cursor.getString(COL_SUMMARY),
                cursor.getString(COL_URI_STORY),
                cursor.getString(COL_AUTHOR),
                cursor.getString(COL_DATE),
                cursor.getString(COL_URI_THUMBNAIL),
                cursor.getBlob(COL_THUMBNAIL),
                cursor.getString(COL_CAPTION_THUMBNAIL),
                cursor.getString(COL_COPYRIGHT_THUMBNAIL),
                cursor.getString(COL_URI_PHOTO),
                cursor.getBlob(COL_PHOTO),
                cursor.getString(COL_CAPTION_PHOTO),
                cursor.getString(COL_COPYRIGHT_PHOTO),
                cursor.getInt(COL_IS_FAVORITE));
    }

    /**
     * Converts and returns the corresponding byte array for the bitmap
     * linked to the image view passed in.
     */
    private byte[] convertToByteArray(ImageView imageView) {
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
}
