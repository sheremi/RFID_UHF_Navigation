package de.unierlangen.like.preferences;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.github.androidutils.logger.Logger;
import com.github.androidutils.logger.Logger.LogLevel;

import de.unierlangen.like.R;

public class LoggingPreferencesFragment extends PreferenceFragment {
    private final Logger log = Logger.getDefaultLogger();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.logging_preferences);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.getKey().contains("log_")) {
            String className = preference.getKey().split("_")[1];
            boolean shouldLog = ((CheckBoxPreference) preference).isChecked();
            log.setLogLevel(className, shouldLog ? LogLevel.DBG : LogLevel.WRN);
            log.d((shouldLog ? "enabled" : "disabled") + " logging for " + className);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
