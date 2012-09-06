package de.unierlangen.like.ui;

import de.unierlangen.like.R;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class CommunicationPreferences extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {

    public static final String KEY_COMM_TYPE_PREFERENCE = "COMM_TYPE";

    private ListPreference commTypes;

    /**
     * Interface
     */
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        preference.setSummary((String) newValue);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.communication_preferences);
        commTypes = (ListPreference) findPreference("COMM_TYPE");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Setup the initial values
        commTypes.setSummary(commTypes.getValue());
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(
                this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Set summary, when preference value changes
        if (key.equals(KEY_COMM_TYPE_PREFERENCE)) {
            commTypes.setSummary(commTypes.getValue());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_comm_prefs, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.your_location:
            startActivity(new Intent(CommunicationPreferences.this, MainYourLocationActivity.class));
            break;
        case R.id.console:
            startActivity(new Intent(CommunicationPreferences.this, ConsoleActivity.class));
            break;
        default:
            UserMessages.showMsg((String) item.getTitle(), this);
        }
        return false;
    }
}
