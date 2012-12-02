package de.unierlangen.like.preferences;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.better.wakelock.Logger;
import com.better.wakelock.Logger.LogLevel;

import de.unierlangen.like.R;

public class LoggingPreferencesFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.logging_preferences);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.getKey().contains("log_")) {
            String className = preference.getKey().split("_")[1];
            boolean log = ((CheckBoxPreference) preference).isChecked();
            Logger.getDefaultLogger().setLogLevel(className, log ? LogLevel.DEBUG : LogLevel.WARN);
            Logger.d((log ? "enabled" : "disabled") + " logging for " + className);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
