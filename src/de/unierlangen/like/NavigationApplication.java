package de.unierlangen.like;

import de.unierlangen.like.rfid.ReaderIntents;
import de.unierlangen.like.rfid.ReaderService;
import de.unierlangen.like.serialport.CommunicationManager;
import android.app.Application;
import android.content.Intent;

public class NavigationApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        CommunicationManager.init(getApplicationContext());

        Intent intent = new Intent(this, ReaderService.class);
        intent.setAction(ReaderIntents.ACTION_READ_TAGS);
        startService(intent);
    }
}
