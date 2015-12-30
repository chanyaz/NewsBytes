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
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.ravi.apps.android.newsbytes.data.NewsContract.NewsEntry;

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

    // Cursor adapter supplying headlines to the list.
    private HeadlinesAdapter mHeadlinesAdapter;

    // Current list view item position.
    private int mPosition = ListView.INVALID_POSITION;

    // Current news category preference.
    private String mNewsCategoryPreference;

    // Position of selected item or first visible item in the list view.
    private int mListPosition;

    public HeadlinesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreateView");
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
        mNewsCategoryPreference = Utility.getNewsCategoryPreference(getActivity());

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

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.d(LOG_TAG, "onActivityCreated");

        // Initialize the news cursor loader.
        getLoaderManager().initLoader(NEWS_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(LOG_TAG, "onCreateLoader");

        // Sort order for the query.
        final String sortOrder = NewsEntry.COLUMN_DATE + " DESC";

        // Selection criteria and arguments.
        final String selection = NewsEntry.COLUMN_IS_FAVORITE + "=?";
        String[] selectionArgs;

        // Set selection based on whether news category is favorite.
        if(mNewsCategoryPreference.equals(getString(R.string.pref_news_category_favorites))) {
            Log.d(LOG_TAG, "onCreateLoader: Querying favs...");

            // Only get the favorite news stories.
            selectionArgs = new String[]{Integer.toString(1)};
        } else {
            Log.d(LOG_TAG, "onCreateLoader: Querying non-favs...");

            // Get the news stories other favorites.
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
        Log.d(LOG_TAG, "onLoadFinished");

        // Check if valid cursor was returned.
        if(data != null) {
            // Check if the cursor is empty.
            if(data.moveToFirst() == false) {
                // Display appropriate status message to user and return.
//                mEmptyListView.setText(getString(R.string.msg_err_no_favorites));
                mEmptyListView.setText("Empty cursor returned");
                Log.d(LOG_TAG, "onLoadFinished: Empty cursor returned");

                return;
            }

            Log.d(LOG_TAG, "onLoadFinished: cursor size: " + data.getCount());

            // Assign the cursor to the adapter.
            mHeadlinesAdapter.swapCursor(data);

            // Move to appropriate list item position.
            if(mListPosition != ListView.INVALID_POSITION) {
                mListView.setSelection(mListPosition);
            }
        } else {
            // Display appropriate error message to user.
//            mEmptyListView.setText(getString(R.string.msg_err_fetch_favorites));
            mEmptyListView.setText("Null cursor returned");
            Log.d(LOG_TAG, "onLoadFinished: Null cursor returned");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(LOG_TAG, "onLoaderReset");

        // Relinquish the cursor attached to the adapter.
        mHeadlinesAdapter.swapCursor(null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(LOG_TAG, "onItemClick");

    }

    /**
     * Public interface that needs to be implemented by the hosting activity to receive
     * notifications from fragment whenever the user selects/taps on a news headline.
     */
    public interface OnHeadlineSelectedListener {
        void onHeadlineSelected(News news);
    }
}
