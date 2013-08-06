/*
 * Copyright (C) 2010 The Android Open Source Project
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

package de.unierlangen.like.preferences;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;

import com.github.androidutils.logger.Logger;

import de.unierlangen.like.DynamicThemeHandler;
import de.unierlangen.like.R;
import de.unierlangen.like.ui.MainYourLocationActivity;

/**
 * Demonstration of PreferenceActivity to make a top-level preference panel with
 * headers.
 */
public class PreferenceWithHeadersActivity extends PreferenceActivity {
    private final Logger log = Logger.getDefaultLogger();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(DynamicThemeHandler.getInstance().getIdForName(this.getClass().getName()));
        super.onCreate(savedInstanceState);
    }

    /**
     * Populate the activity with the top-level headers.
     */
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    @Override
    public boolean onIsMultiPane() {
        boolean onIsMultiPanePreffered = super.onIsMultiPane();
        if (onIsMultiPanePreffered)
            return true;
        else {
            DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
            log.d("We have " + metrics.widthPixels + " pixels");
            // HACK for nexus 7. If we have more than 700 pixels, go headers!
            return metrics.widthPixels > 1000;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            goBack();
            return true;
        }
        return false;
    }

    private void goBack() {
        // This is called when the Home (Up) button is pressed
        // in the Action Bar.
        Intent parentActivityIntent = new Intent(this, MainYourLocationActivity.class);
        parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(parentActivityIntent);
        finish();
    }
}
