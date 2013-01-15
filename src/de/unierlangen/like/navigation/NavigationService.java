package de.unierlangen.like.navigation;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PointF;
import android.nfc.NfcAdapter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;

import com.github.androidutils.logger.Logger;

import de.unierlangen.like.Intents;
import de.unierlangen.like.rfid.GenericTag;

public class NavigationService extends Service implements Handler.Callback {
    // private static final int

    private static final int FIND_ROUTE = 1;
    private static final float ZONE_RADIUS = 2.0f;
    private final Logger log = Logger.getDefaultLogger();
    private MapBuilder mapBuilder;
    private DijkstraRouter dijkstraRouter;
    private Navigation navigation;
    private TagsDatabase tagsDatabase;
    private ArrayList<Tag> tags;
    private PointF destination;
    private PointF currentPosition;
    private Handler mHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        log.d("starting navigation service");
        mapBuilder = MapBuilder.getInstance(this);

        navigation = new Navigation(mapBuilder.getWalls(), mapBuilder.getDoors());
        dijkstraRouter = new DijkstraRouter(this);
        tagsDatabase = new TagsDatabase();

        IntentFilter filter = new IntentFilter(Intents.ACTION_TAGS);
        filter.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        registerReceiver(readerReceiver, filter);
        HandlerThread thread = new HandlerThread("NavigationServiceThread");
        thread.start();
        Looper looper = thread.getLooper();
        mHandler = new Handler(looper, this);
    }

    private final BroadcastReceiver readerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO remove this
            log.d(intent.getAction());
            intent.setClass(getApplicationContext(), NavigationService.class);
            // here we start the NavigationService to handle this on a separate
            // thread
            startService(intent);
        }
    };

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        log.d(intent.getAction());
        if (intent.getAction().equals(Intents.ACTION_SET_DESTINATION)) {
            destination = intent.getParcelableExtra(Intents.EXTRA_DESTINATION);
            if (currentPosition != null) {
                findRouteInBackGround();
            }

        } else if (Intents.ACTION_TAGS.equals(intent.getAction())) {
            // since now we know that Intent action is ACTION_TAGS, we
            // know
            // that
            // array of tags is attached to the intent as an extra with
            // key
            // EXTRA_TAGS
            ArrayList<GenericTag> readTagsFromReader = intent
                    .getParcelableArrayListExtra(Intents.EXTRA_TAGS);
            ArrayList<Tag> newTags = new ArrayList<Tag>();
            newTags.addAll(tagsDatabase.getTags(readTagsFromReader));

            handleTags(newTags);

        } else if (Intents.ACTION_START_NAVIGATION.equals(intent.getAction())) {
            log.d("Navigation start was requested");

        } else {
            android.nfc.Tag nfcTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            ArrayList<GenericTag> readTagsFromReader = new ArrayList<GenericTag>();
            readTagsFromReader.add(new GenericTag(Arrays.toString(nfcTag.getId()), 0, true));
            handleTags(tagsDatabase.getTags(readTagsFromReader));

        }
        return START_STICKY;
    }

    private void handleTags(ArrayList<Tag> newTags) {
        if (!newTags.isEmpty()) {
            navigation.setTags(newTags);
            currentPosition = navigation.getReaderPosition();
            if (destination != null) {
                findRouteInBackGround();
            }
            broadcastValues(Intents.ACTION_TAGS_ON_WALLS, Intents.EXTRA_TAGS_ON_WALLS, newTags);
            broadcastValues(Intents.ACTION_ZONES, Intents.EXTRA_ZONES,
                    navigation.getZones(ZONE_RADIUS));
            if (!mHandler.hasMessages(FIND_ROUTE)) {
                broadcast(Intents.ACTION_LOCATION_FOUND, Intents.EXTRA_POSITION, currentPosition);
            }
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
        case FIND_ROUTE:
            ArrayList<PointF> routingPath = dijkstraRouter.findRoute(currentPosition, destination);
            broadcastValues(Intents.ACTION_ROUTE_FOUND, Intents.EXTRA_ROUTE, routingPath);
            broadcast(Intents.ACTION_LOCATION_FOUND, Intents.EXTRA_POSITION, currentPosition);
            break;

        default:
            break;
        }
        return false;
    }

    private void findRouteInBackGround() {
        // TODO make sure we are right
        mHandler.removeMessages(1);
        mHandler.sendEmptyMessage(1);
    }

    private void broadcast(String action, String extra, Parcelable value) {
        Intent intent = new Intent(action);
        intent.putExtra(extra, value);
        sendBroadcast(intent);
    }

    private void broadcastValues(String action, String extra, ArrayList<? extends Parcelable> values) {
        Intent intent = new Intent(action);
        intent.putExtra(extra, values);
        sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // we do not use binding
        return null;
    }
}