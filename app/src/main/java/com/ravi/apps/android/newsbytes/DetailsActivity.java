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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class DetailsActivity extends AppCompatActivity {

    // Toolbar.
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Start animation.
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        // Set the content view.
        setContentView(R.layout.activity_details);

        // Get the toolbar and set it as the action bar.
        mToolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(mToolbar);

        // Enable the up button on the toolbar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            // Extract extras from intent.
            Bundle bundle = getIntent().getExtras();

            // Create the details fragment object.
            DetailsFragment detailsFragment = new DetailsFragment();

            // Set arguments containing news details.
            detailsFragment.setArguments(bundle);

            // Add the fragment onto the container.
            getFragmentManager().beginTransaction()
                    .add(R.id.news_details_container, detailsFragment, DetailsFragment.DETAILS_FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Start animation.
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
