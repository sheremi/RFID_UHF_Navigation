package de.unierlangen.like.rfid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcelable;
import android.os.SystemClock;

import com.github.androidutils.logger.Logger;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

import de.unierlangen.like.Intents;
import de.unierlangen.like.R;
import de.unierlangen.like.rfid.Reader.Configuration;
import de.unierlangen.like.rfid.Reader.ReaderClient;
import de.unierlangen.like.rfid.Reader.ReaderStatus;

public class ReaderService extends Service {

    private static final int NOTIFICATION_ID = 1;

    private Reader reader;

    private Handler handler;

    private NotificationManager notificationManager;

    private TagsAggregator tagsAggregator;

    @Override
    public void onCreate() {
        tagsAggregator = new TagsAggregator(this);
        reader = new Reader(new ReaderClientImpl());
        handler = new Handler(new DelayedMessageHandler());
        handler.sendEmptyMessage(DelayedMessageHandler.CHECK_READER_HEALTH);
        handler.sendEmptyMessage(DelayedMessageHandler.THREAD_EVENT_READ_TAGS);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        super.onCreate();
    }

    // Receives the intent for each start request, so we can do the background
    // work
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Implementation of ReaderClient
    // Here we handle all the stuff coming from the Reader
    private final class ReaderClientImpl implements ReaderClient {
        @Override
        public void onTagsReceived(ArrayList<GenericTag> readTagsFromReader) {
            tagsAggregator.processTags(readTagsFromReader);
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
                        .setTicker("All FFs on SPI").setSmallIcon(R.drawable.info1)
                        // .setLargeIcon(aBitmap)
                        .getNotification();
                break;
            case ALL_ZEROS:
                notification = new Notification.Builder(getApplicationContext())
                        .setContentTitle("All zeros on SPI")
                        .setContentText("Cable appears to not connected or not properly connected")
                        .setTicker("All zeros on SPI").setSmallIcon(R.drawable.info1)
                        // .setLargeIcon(aBitmap)
                        .getNotification();
                break;
            case OSC_FAIL:
                notification = new Notification.Builder(getApplicationContext())
                        .setContentTitle("Oscillator failed")
                        .setContentText("Check voltage and configuration")
                        .setTicker("Oscillator failed").setSmallIcon(R.drawable.info1)
                        // .setLargeIcon(aBitmap)
                        .getNotification();
                break;
            case PLL_FAIL:
                notification = new Notification.Builder(getApplicationContext())
                        .setContentTitle("PLL failed")
                        .setContentText("Check voltage and configuration").setTicker("PLL failed")
                        .setSmallIcon(R.drawable.info1)
                        // .setLargeIcon(aBitmap)
                        .getNotification();
                reader.initialize(Configuration.DEFAULT);
                break;
            case SOMETHING_ELSE:
                notification = new Notification.Builder(getApplicationContext())
                        .setContentTitle("Reader failed").setContentText("Something is wrong")
                        .setTicker("Reader failed").setSmallIcon(R.drawable.info1)
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
    }

    /**
     * This handler is used only to handle delayed messages to read tags every
     * now and then
     */
    private final class DelayedMessageHandler implements Handler.Callback {
        static final int CHECK_READER_HEALTH = 0;
        static final int THREAD_EVENT_READ_TAGS = 4;
        private static final int READ_TAGS_INTERVAL = 250;

        @Override
        public boolean handleMessage(Message msg) {
            // log.d ("handleMessage(" + msg.what + ")");
            switch (msg.what) {
            case THREAD_EVENT_READ_TAGS:
                reader.performRound();
                handler.sendEmptyMessageDelayed(THREAD_EVENT_READ_TAGS, READ_TAGS_INTERVAL);
                return true;

            default:
            case CHECK_READER_HEALTH:
                reader.shakeHands();
                reader.displayRegisters();
                reader.initialize(Configuration.DEFAULT);
                handler.sendEmptyMessageDelayed(CHECK_READER_HEALTH, READ_TAGS_INTERVAL * 2);
                return true;
            }
        }
    }

    // Implementation of ReaderClient
    // Here we handle all the stuff coming from the Reader
    private static class TagsAggregator implements Handler.Callback {
        private static final int TAGS_EXPIRATION_TIME = 2500;
        private static final int TAG_EXPIRED = 5;
        private final Logger log = Logger.getDefaultLogger();
        private final ContextWrapper contextWrapper;
        private final Handler handler;

        private final Map<GenericTag, Long> tags;

        public TagsAggregator(ContextWrapper contextWrapper) {
            this.contextWrapper = contextWrapper;
            handler = new Handler(this);
            tags = new HashMap<GenericTag, Long>();
        }

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
            case TAG_EXPIRED:
                remove();
                return true;

            default:
                return false;
            }

        }

        public void processTags(ArrayList<GenericTag> readTagsFromReader) {

            int oldSize = tags.size();

            // update all delayed messages:
            for (GenericTag genericTag : readTagsFromReader) {
                tags.put(genericTag, SystemClock.uptimeMillis() + TAGS_EXPIRATION_TIME);
            }

            if (oldSize != tags.size()) {
                notifyListeners();
            }

            handler.removeMessages(TAG_EXPIRED);
            // find next scheduled remove:
            Long next = Collections.min(new ArrayList<Long>(tags.values()));
            handler.sendEmptyMessageAtTime(TAG_EXPIRED, next);
        }

        private void remove() {
            int oldSize = tags.size();

            Map<GenericTag, Long> forRemoval = Maps.filterEntries(tags,
                    new Predicate<Entry<GenericTag, Long>>() {
                        @Override
                        public boolean apply(Entry<GenericTag, Long> input) {
                            return input.getValue() - SystemClock.uptimeMillis() <= 0;
                        }
                    });

            for (GenericTag entry : new ArrayList<GenericTag>(forRemoval.keySet())) {
                tags.remove(entry);
            }

            if (oldSize != tags.size()) {
                notifyListeners();
            }
        }

        private void notifyListeners() {
            log.d("Notifying: " + tags.toString());
            Intent intent = new Intent(Intents.ACTION_TAGS);
            intent.putParcelableArrayListExtra(Intents.EXTRA_TAGS,
                    new ArrayList<Parcelable>(tags.keySet()));
            contextWrapper.sendBroadcast(intent);
        }
    }
}
