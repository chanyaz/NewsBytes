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

package com.ravi.apps.android.newsbytes.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ravi.apps.android.newsbytes.R;
import com.ravi.apps.android.newsbytes.sync.NewsSyncAdapter;

/**
 * Triggers an immediate data fetch using the sync adapter upon device boot completion.
 */
public class NewsReceiver extends BroadcastReceiver {

    // Tag for logging messages.
    private static final String LOG_TAG = NewsReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        // Get the intent action.
        String action = intent.getAction();
        Log.d(LOG_TAG, context.getString(R.string.log_on_receive) + action);

        // Check if the device was booted.
        if(Intent.ACTION_BOOT_COMPLETED.equals(action) || Intent.ACTION_REBOOT.equals(action)) {
            // Initialize the sync adapter and trigger an immediate sync.
            NewsSyncAdapter.initializeSyncAdapter(context);
            NewsSyncAdapter.syncImmediately(context);
        }
    }
}
