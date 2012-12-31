package de.unierlangen.like.rfid;

import java.util.ArrayList;
import java.util.Map;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import de.unierlangen.like.R;
import de.unierlangen.like.Intents;
import de.unierlangen.like.rfid.Reader.Configuration;
import de.unierlangen.like.rfid.Reader.ReaderClient;
import de.unierlangen.like.rfid.Reader.ReaderStatus;

public class ReaderService extends IntentService {

    private static final int NOTIFICATION_ID = 1;

    private Reader reader;

    private Handler handler;

    private NotificationManager notificationManager;

    public ReaderService() {
        super("ReaderService");
    }

    @Override
    public void onCreate() {
        reader = new Reader(new ReaderClientImpl());
        handler = new Handler(new HandlerImpl());
        handler.sendEmptyMessage(HandlerImpl.CHECK_READER_HEALTH);
        handler.sendEmptyMessage(HandlerImpl.THREAD_EVENT_READ_TAGS);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        super.onCreate();
    }

    // Receives the intent for each start request, so we can do the background
    // work
    @Override
    protected void onHandleIntent(Intent intent) {
        if (Intents.ACTION_READ_TAGS.equals(intent.getAction())) {
            // TODO
        }
    }

    /**
     * This handler is used only to handle delayed messages to read tags every
     * now and then
     */
    private final class HandlerImpl implements Handler.Callback {
        static final int CHECK_READER_HEALTH = 0;
        static final int THREAD_EVENT_READ_TAGS = 4;
        private static final int READ_TAGS_INTERVAL = 3000;

        @Override
        public boolean handleMessage(Message msg) {
            // log.d ("handleMessage(" + msg.what + ")");
            switch (msg.what) {
            case THREAD_EVENT_READ_TAGS:
                reader.performRound();
                handler.sendEmptyMessageDelayed(THREAD_EVENT_READ_TAGS, READ_TAGS_INTERVAL);
                return true;

            case CHECK_READER_HEALTH:
                reader.shakeHands();
                reader.displayRegisters();
                reader.initialize(Configuration.DEFAULT);
                handler.sendEmptyMessageDelayed(CHECK_READER_HEALTH, READ_TAGS_INTERVAL * 2);
                return true;

            default:
                return false;
            }

        }
    }

    // Implementation of ReaderClient
    // Here we handle all the stuff coming from the Reader
    private final class ReaderClientImpl implements ReaderClient {
        @Override
        public void onTagsReceived(ArrayList<GenericTag> readTagsFromReader) {
            if (!readTagsFromReader.isEmpty()) {
                Intent intent = new Intent(Intents.ACTION_TAGS);
                intent.putParcelableArrayListExtra(Intents.EXTRA_TAGS, readTagsFromReader);
                sendBroadcast(intent);
            }
        }

        @Override
        public void onRegsReceived(Map<String, String> registers) {
            // so far ignore it TODO
        }

        @Override
        public void onReaderStatus(ReaderStatus status) {
            Notification notification = null;
            switch (status) {
            case ALL_FFS:
                notification = new Notification.Builder(getApplicationContext())
                        .setContentTitle("All FFs on SPI")
                        .setContentText("Cable appears to be connected the wrong way")
                        .setTicker("All FFs on SPI").setSmallIcon(R.drawable.info)
                        // .setLargeIcon(aBitmap)
                        .getNotification();
                break;
            case ALL_ZEROS:
                notification = new Notification.Builder(getApplicationContext())
                        .setContentTitle("All zeros on SPI")
                        .setContentText("Cable appears to not connected or not properly connected")
                        .setTicker("All zeros on SPI").setSmallIcon(R.drawable.info)
                        // .setLargeIcon(aBitmap)
                        .getNotification();
                break;
            case OSC_FAIL:
                notification = new Notification.Builder(getApplicationContext())
                        .setContentTitle("Oscillator failed")
                        .setContentText("Check voltage and configuration")
                        .setTicker("Oscillator failed").setSmallIcon(R.drawable.info)
                        // .setLargeIcon(aBitmap)
                        .getNotification();
                break;
            case PLL_FAIL:
                notification = new Notification.Builder(getApplicationContext())
                        .setContentTitle("PLL failed")
                        .setContentText("Check voltage and configuration").setTicker("PLL failed")
                        .setSmallIcon(R.drawable.info)
                        // .setLargeIcon(aBitmap)
                        .getNotification();
                reader.initialize(Configuration.DEFAULT);
                break;
            case SOMETHING_ELSE:
                notification = new Notification.Builder(getApplicationContext())
                        .setContentTitle("Reader failed").setContentText("Something is wrong")
                        .setTicker("Reader failed").setSmallIcon(R.drawable.info)
                        // .setLargeIcon(aBitmap)
                        .getNotification();
                break;
            case ALL_GOOD:
                notification = new Notification.Builder(getApplicationContext())
                        .setContentTitle("Reader is up and running")
                        .setContentText("We are good to go").setTicker("Reader is up and running")
                        .setSmallIcon(R.drawable.test_mode)
                        // .setLargeIcon(aBitmap)
                        .getNotification();
                break;
            case UNINTIALZED:
                notification = new Notification.Builder(getApplicationContext())
                        .setContentTitle("Reader is not initialized")
                        .setContentText("Cable appears to be connected the wrong way")
                        .setTicker("Reader is not initialized").setSmallIcon(R.drawable.prefs)
                        // .setLargeIcon(aBitmap)
                        .getNotification();
                break;
            }
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    };
}
