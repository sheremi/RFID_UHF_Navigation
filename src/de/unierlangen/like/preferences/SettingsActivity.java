package de.unierlangen.like.preferences;

import android.app.Activity;
import android.os.Bundle;
import de.unierlangen.like.DynamicThemeHandler;

public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(DynamicThemeHandler.getInstance().getIdForName(this.getClass().getName()));
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment()).commit();
    }
}