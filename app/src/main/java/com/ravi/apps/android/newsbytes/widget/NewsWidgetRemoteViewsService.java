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

package com.ravi.apps.android.newsbytes.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViewsService;

import com.ravi.apps.android.newsbytes.R;

/**
 * Provides the data for the scrollable news headlines collection widget.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class NewsWidgetRemoteViewsService extends RemoteViewsService {

    // Tag for logging messages.
    private static final String LOG_TAG = NewsWidgetRemoteViewsService.class.getSimpleName();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.d(LOG_TAG, getString(R.string.on_get_view_factory));

        // Create the remote views factory object.
        NewsWidgetRemoteViewsFactory newsWidgetRemoteViewsFactory = new
                NewsWidgetRemoteViewsFactory(getApplicationContext(), intent);

        // Return the remote views factory object.
        return newsWidgetRemoteViewsFactory;
    }
}
