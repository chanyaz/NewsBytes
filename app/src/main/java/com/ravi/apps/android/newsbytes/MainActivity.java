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

import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.ravi.apps.android.newsbytes.sync.NewsSyncAdapter;

public class MainActivity extends AppCompatActivity
        implements HeadlinesFragment.OnHeadlineSelectedListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    // Tag for logging messages.
    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    // Keys for the shared element transition.
    public static final String IMAGE_XPOS = "image_xpos";
    public static final String IMAGE_YPOS = "image_ypos";
    public static final String IMAGE = "image";
    public static final String TEXT_XPOS = "text_xpos";
    public static final String TEXT_YPOS = "text_ypos";
    public static final String TEXT = "text";

    // Flag indicating whether widget list item was clicked.
    public static boolean isWidgetItemClicked = false;

    // Position of the widget list item that was clicked.
    public static int widgetItemClickedPosition = ListView.INVALID_POSITION;

    // Holds whether the main activity layout contains two panes.
    private boolean mIsTwoPaneMode;

    // Toolbar.
    private Toolbar mToolbar;

    // Key to save preference changed status flag.
    private static final String PREFERENCE_CHANGED_KEY = "preference_changed_key";

    // Flag indicating whether news category preference changed.
    private boolean mHasPreferenceChanged = false;

    // Ad view displaying banner ad.
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Start analytics tracking.
        ((NewsApplication) getApplication()).startTracking();

        // Set default values only the first time.
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Register to receive events upon any changes to the shared preferences.
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

        // Get the toolbar and set it as the action bar.
        mToolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(mToolbar);

        // Get the banner ad view.
        mAdView = (AdView) findViewById(R.id.adview);

        // Create an ad request and load it into the ad view.
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // Check if the layout has two panes and set the flag accordingly.
        if(findViewById(R.id.news_details_container) != null) {
            mIsTwoPaneMode = true;
        } else {
            mIsTwoPaneMode = false;
        }

        // Check if it was a configuration change.
        if(savedInstanceState == null) {
            // Check if the widget list item was clicked.
            Intent intent = getIntent();
            if (intent != null
                    && intent.getAction() != null
                    && intent.getAction().equals(getString(R.string.action_item_clicked))) {
                Log.d(LOG_TAG, getString(R.string.log_on_create_widget_item_clicked));

                // Set the widget list item clicked flag.
                isWidgetItemClicked = true;

                // Extract the match id and position from the intent.
                widgetItemClickedPosition = intent.getIntExtra(
                        getString(R.string.extra_widget_item_position), ListView.INVALID_POSITION);

                Log.d(LOG_TAG, getString(R.string.log_on_create_widget_item_position)
                        + ((Integer) widgetItemClickedPosition).toString());
            }
        } else {
            // Check if it's in two pane mode and the preference changed status was saved.
            if(mIsTwoPaneMode && savedInstanceState.containsKey(PREFERENCE_CHANGED_KEY)) {
                // Extract preference changed status.
                mHasPreferenceChanged = savedInstanceState.getBoolean(PREFERENCE_CHANGED_KEY);
            }
        }

        // Initialize the sync adapter.
        NewsSyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    protected void onDestroy() {
        // Unregister from receiving events upon any changes to the shared preferences.
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check if it's in two pane mode and preference has been changed.
        // If so, find and remove the details fragment.
        if(mIsTwoPaneMode && mHasPreferenceChanged) {
            // Get the fragment manager.
            FragmentManager fragmentManager = getFragmentManager();

            // Get the details fragment.
            DetailsFragment detailsFragment = (DetailsFragment) fragmentManager
                    .findFragmentByTag(DetailsFragment.DETAILS_FRAGMENT_TAG);

            // Remove the details fragment.
            if(detailsFragment != null) {
                fragmentManager.beginTransaction()
                        .remove(detailsFragment)
                        .commit();
            }

            // Reset the preference changed flag.
            mHasPreferenceChanged = false;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // if it's in two pane mode, then save the preference changed flag.
        if(mIsTwoPaneMode) {
            outState.putBoolean(PREFERENCE_CHANGED_KEY, mHasPreferenceChanged);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // If the news notifications preference was changed, do nothing.
        if(key.equals(getString(R.string.pref_notifications_key))) {
            return;
        }

        // Set the news category preference changed flag if it's in two pane mode.
        if(mIsTwoPaneMode) {
            mHasPreferenceChanged = true;
        }

        // Check the current news category preference.
        if(!Utility.getNewsCategoryPreference(this, key)
                .equals(getString(R.string.pref_news_category_favorites))) {
            // Current preference is not favorites, trigger an immediate sync.
            NewsSyncAdapter.syncImmediately(this);
        }

        // Preference has changed, inform widget to refresh it's data.
        Utility.sendDataUpdatedBroadcast(this);
    }

    @Override
    public void onHeadlineSelected(News news, ImageView thumbnailView, String thumbnailTransition,
                                   TextView headlineView, String headlineTransition) {
        // Check if it's in two pane mode.
        if(mIsTwoPaneMode) {
            // Package the parcelable news data into the arguments bundle.
            Bundle arguments = new Bundle();
            arguments.putParcelable(DetailsFragment.NEWS_DETAILS, news);

            // Create the details fragment object.
            DetailsFragment detailsFragment = new DetailsFragment();

            // Shared element transition - check if device is running on lollipop or above
            // and if ALL of the required parameters for the shared element transition are available.
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                    && thumbnailView != null && thumbnailTransition != null
                    && headlineView != null && headlineTransition != null) {
                  // Set the shared element transition.
                detailsFragment.setSharedElementEnterTransition(TransitionInflater.from(this).inflateTransition(R.transition.change_image_transform));
                detailsFragment.setEnterTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.fade));

                // Add the transition names into the bundle.
                arguments.putString(HeadlinesAdapter.THUMBNAIL_TRANSITION_NAME, thumbnailTransition);
                arguments.putString(HeadlinesAdapter.HEADLINE_TRANSITION_NAME, headlineTransition);

                // Set arguments containing news details.
                detailsFragment.setArguments(arguments);

                // Set the transition names.
                ViewCompat.setTransitionName(thumbnailView, thumbnailTransition);
                ViewCompat.setTransitionName(headlineView, headlineTransition);

                // Add the fragment onto the container.
                getFragmentManager().beginTransaction()
                        .replace(R.id.news_details_container, detailsFragment, DetailsFragment.DETAILS_FRAGMENT_TAG)
                        .addSharedElement(thumbnailView, thumbnailTransition)
                        .addSharedElement(headlineView, headlineTransition)
                        .commit();
            } else {    // For pre-lollipop devices.
                // Set arguments containing news details.
                detailsFragment.setArguments(arguments);

                // Add the fragment onto the container.
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out,
                                android.R.animator.fade_in, android.R.animator.fade_out)
                        .replace(R.id.news_details_container, detailsFragment, DetailsFragment.DETAILS_FRAGMENT_TAG)
                        .commit();
            }
        } else {
            // Create intent to launch details activity.
            Intent intent = new Intent(this, DetailsActivity.class);

            // Add news details into extra.
            intent.putExtra(DetailsFragment.NEWS_DETAILS, news);

            // Shared element transition - check if device is running on lollipop or above
            // and if ALL of the required parameters for the shared element transition are available.
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                    && thumbnailView != null && thumbnailTransition != null
                    && headlineView != null && headlineTransition != null) {
                // Add the transition names into the intent extra.
                intent.putExtra(HeadlinesAdapter.THUMBNAIL_TRANSITION_NAME, thumbnailTransition);
                intent.putExtra(HeadlinesAdapter.HEADLINE_TRANSITION_NAME, headlineTransition);

                // Add the essential attributes of the shared element views into the intent extra.
                addThumbnailDetailsToIntent(thumbnailView, intent);
                addHeadlineDetailsToIntent(headlineView, intent);
            }

            // Start details activity.
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Get id of the menu item selected.
        int id = item.getItemId();

        // Check if the settings item was selected.
        if (id == R.id.action_settings) {
            // Start the settings activity.
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Adds the thumbnail details into the intent extras.
     */
    private void addThumbnailDetailsToIntent(ImageView imageView, Intent intent) {
        // Get the thumbnail position.
        int[] loc = new int[2];
        imageView.getLocationOnScreen(loc);
        float xPos = loc[0];
        float yPos = loc[1];

        // Get the thumbnail image as a byte array.
        byte[] imageByteArray = Utility.convertToByteArray(imageView);
        if(imageByteArray == null) {
            imageByteArray = new byte[0];
        }

        // Add details into intent extras.
        intent.putExtra(IMAGE_XPOS, xPos);
        intent.putExtra(IMAGE_YPOS, yPos);
        intent.putExtra(IMAGE, imageByteArray);
    }

    /**
     * Adds the headline details into the intent extras.
     */
    private void addHeadlineDetailsToIntent(TextView textView, Intent intent) {
        // Get the headline position.
        int[] location = new int[2];
        textView.getLocationOnScreen(location);
        float xPos = location[0];
        float yPos = location[1];

        // Get the text.
        String text = (String) textView.getText();

        // Add details into intent extras.
        intent.putExtra(TEXT_XPOS, xPos);
        intent.putExtra(TEXT_YPOS, yPos);
        intent.putExtra(TEXT, text);
    }
}
