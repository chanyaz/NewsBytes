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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

/**
 * Enables users to view and change the app settings.
 */
public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    public SettingsFragment() {
        // Required empty public constructor.
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the preferences screen.
        addPreferencesFromResource(R.xml.preferences);

        // Display the current user preference for sort order.
        updatePreferenceSummary(findPreference(getString(R.string.pref_news_category_key)));
    }

    @Override
    public void onResume() {
        super.onResume();

        // Register to receive events upon any changes to the shared preferences.
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        // Unregister from receiving events upon any changes to the shared preferences.
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Display the current user preference for sort order.
        updatePreferenceSummary(findPreference(key));
    }

    /**
     * Displays the current user preference in the preference summary.
     */
    private void updatePreferenceSummary(Preference preference) {
        // Check if the preference is a list preference.
        if(preference instanceof ListPreference) {
            // Get the preference entry and show it in the summary.
            ListPreference listPreference = (ListPreference) preference;
            listPreference.setSummary(listPreference.getEntry());
        }
    }
}
