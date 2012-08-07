package de.unierlangen.like;

import de.unierlangen.like.serialport.CommunicationManager;
import android.app.Application;

public class NavigationApplication extends Application {
    @Override
    public void onCreate() {
        CommunicationManager.init(getApplicationContext());
        super.onCreate();
    }
}
