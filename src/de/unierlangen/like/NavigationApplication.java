package de.unierlangen.like;

import java.util.Map;
import java.util.Map.Entry;

import android.app.Application;
import android.content.Intent;
import android.preference.PreferenceManager;

import com.better.wakelock.LogcatLogWriter;
import com.better.wakelock.Logger;
import com.better.wakelock.Logger.LogLevel;

import de.unierlangen.like.rfid.ReaderIntents;
import de.unierlangen.like.rfid.ReaderService;
import de.unierlangen.like.serialport.CommunicationManager;

public class NavigationApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        CommunicationManager.init(getApplicationContext());

        Logger logger = Logger.getDefaultLogger();
        logger.addLogWriter(new LogcatLogWriter());

        PreferenceManager.setDefaultValues(this, R.xml.logging_preferences, false);

        Map<String, ?> preferences = PreferenceManager.getDefaultSharedPreferences(
                getApplicationContext()).getAll();

        for (Entry<String, ?> entry : preferences.entrySet()) {
            if (entry.getKey().contains("log_")) {
                String className = entry.getKey().split("_")[1];
                boolean log = ((Boolean) entry.getValue()).booleanValue();
                logger.setLogLevel(className, log ? LogLevel.DEBUG : LogLevel.WARN);
            }
        }

        Intent intent = new Intent(this, ReaderService.class);
        intent.setAction(ReaderIntents.ACTION_READ_TAGS);
        startService(intent);
    }
}
