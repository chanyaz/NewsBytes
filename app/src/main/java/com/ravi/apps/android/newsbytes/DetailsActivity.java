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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionInflater;
import android.widget.ImageView;
import android.widget.TextView;

public class DetailsActivity extends AppCompatActivity {

    // Tag for logging messages.
    private static final String LOG_TAG = DetailsActivity.class.getSimpleName();

    // Toolbar.
    private Toolbar mToolbar;

    // Starting shared element transition views.
    private ImageView mThumbnailView;
    private TextView mHeadlineView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the content view.
        setContentView(R.layout.activity_details);

        // Get the toolbar and set it as the action bar.
        mToolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(mToolbar);

        // Enable the up button on the toolbar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(savedInstanceState == null) {
            // Extract extras from intent.
            Bundle bundle = getIntent().getExtras();

            // Create the details fragment object.
            DetailsFragment detailsFragment = new DetailsFragment();

            // Set arguments containing news details.
            detailsFragment.setArguments(bundle);

            // Shared element transition - check if device is running on lollipop or above
            // and if the transition names for the shared element transition are available.
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                    && bundle.containsKey(HeadlinesAdapter.THUMBNAIL_TRANSITION_NAME)
                    && bundle.containsKey(HeadlinesAdapter.HEADLINE_TRANSITION_NAME)) {
                // Create the starting views for the shared element transition.
                createTransitionStartViews(bundle);

                // Set the shared element transition.
                detailsFragment.setSharedElementEnterTransition(TransitionInflater.from(this).inflateTransition(R.transition.change_image_transform));
                detailsFragment.setEnterTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.fade));

                // Set the transition names.
                ViewCompat.setTransitionName(mThumbnailView, bundle.getString(HeadlinesAdapter.THUMBNAIL_TRANSITION_NAME));
                ViewCompat.setTransitionName(mHeadlineView, bundle.getString(HeadlinesAdapter.HEADLINE_TRANSITION_NAME));

                // Add the fragment onto the container.
                getFragmentManager().beginTransaction()
                        .replace(R.id.news_details_container, detailsFragment, DetailsFragment.DETAILS_FRAGMENT_TAG)
                        .addSharedElement(mThumbnailView, bundle.getString(HeadlinesAdapter.THUMBNAIL_TRANSITION_NAME))
                        .addSharedElement(mHeadlineView, bundle.getString(HeadlinesAdapter.HEADLINE_TRANSITION_NAME))
                        .commit();
            } else {
                // Add the fragment onto the container.
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out,
                                android.R.animator.fade_in, android.R.animator.fade_out)
                        .add(R.id.news_details_container, detailsFragment, DetailsFragment.DETAILS_FRAGMENT_TAG)
                        .commit();
            }
        }
    }

    /**
     * Creates the starting views required for the shared element transition.
     */
    private void createTransitionStartViews(Bundle bundle) {
        // Create the view objects.
        mThumbnailView = new ImageView(this);
        mHeadlineView = new TextView(this);

        // Set the thumbnail view parameters.
        mThumbnailView.setX(bundle.getFloat(MainActivity.IMAGE_XPOS));
        mThumbnailView.setY(bundle.getFloat(MainActivity.IMAGE_YPOS));
        byte[] thumbnailByteStream = bundle.getByteArray(MainActivity.IMAGE);
        if(thumbnailByteStream != null && thumbnailByteStream.length != 0) {
            // Get the thumbnail bitmap from byte array.
            Bitmap thumbnailBitmap = BitmapFactory.decodeByteArray(thumbnailByteStream, 0, thumbnailByteStream.length);

            // Set the thumbnail bitmap into the image view.
            if(thumbnailBitmap != null) {
                mThumbnailView.setImageBitmap(thumbnailBitmap);
                mThumbnailView.setScaleType(ImageView.ScaleType.FIT_XY);
            }
        }

        // Set the thumbnail view parameters.
        mHeadlineView.setX(bundle.getFloat(MainActivity.TEXT_XPOS));
        mHeadlineView.setY(bundle.getFloat(MainActivity.TEXT_YPOS));
        mHeadlineView.setText(bundle.getString(MainActivity.TEXT));
    }
}
