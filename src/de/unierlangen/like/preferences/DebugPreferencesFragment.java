package de.unierlangen.like.preferences;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import de.unierlangen.like.R;

public class DebugPreferencesFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.debug_preferences);
    }
}
