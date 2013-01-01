package de.unierlangen.like.navigation;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PointF;
import android.os.IBinder;
import android.os.Parcelable;
import android.widget.Toast;

import com.github.androidutils.logger.Logger;

import de.unierlangen.like.Intents;
import de.unierlangen.like.rfid.GenericTag;

public class NavigationService extends Service {
    private static final float ZONE_RADIUS = 2.0f;
    private final Logger log = Logger.getDefaultLogger();
    private MapBuilder mapBuilder;
    private DijkstraRouter dijkstraRouter;
    private Navigation navigation;
    private TagsDatabase tagsDatabase;
    private ArrayList<Tag> tags;
    private PointF destination;
    private PointF currentPosition;

    @Override
    public void onCreate() {
        super.onCreate();
        log.d("starting navigation service");
        try {
            mapBuilder = new MapBuilder("/sdcard/like/map.txt");
        } catch (IOException e) {
            Toast.makeText(this, "Sorry, current file is not readable or not found",
                    Toast.LENGTH_SHORT).show();
            mapBuilder = new MapBuilder("1,1,2,2;2,2,3,3;", true);
            log.e("oops", e);
        }

        navigation = new Navigation(mapBuilder.getWalls(), mapBuilder.getDoors());
        dijkstraRouter = new DijkstraRouter();
        tagsDatabase = new TagsDatabase();

        IntentFilter filter = new IntentFilter(Intents.ACTION_TAGS);
        registerReceiver(readerReceiver, filter);
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
    public int onStartCommand(Intent intent, int flags, int startId) {
        log.d(intent.getAction());
        if (intent.getAction().equals(Intents.ACTION_SET_DESTINATION)) {
            destination = intent.getParcelableExtra(Intents.EXTRA_DESTINATION);
            if (currentPosition != null) {
                ArrayList<PointF> route = dijkstraRouter.findRoute(currentPosition, destination);
                broadcastValues(Intents.ACTION_ROUTE_FOUND, Intents.EXTRA_ROUTE, route);
            }

        } else if (Intents.ACTION_TAGS.equals(intent.getAction())) {
            // since now we know that Intent action is ACTION_TAGS, we know
            // that
            // array of tags is attached to the intent as an extra with key
            // EXTRA_TAGS
            ArrayList<GenericTag> readTagsFromReader = intent
                    .getParcelableArrayListExtra(Intents.EXTRA_TAGS);
            ArrayList<Tag> newTags = new ArrayList<Tag>();
            newTags.addAll(tagsDatabase.getTags(readTagsFromReader));

            if (!newTags.isEmpty()) {
                navigation.setTags(newTags);
                currentPosition = navigation.getReaderPosition();
                if (destination != null) {
                    ArrayList<PointF> routingPath = dijkstraRouter.findRoute(currentPosition,
                            destination);
                    broadcastValues(Intents.ACTION_ROUTE_FOUND, Intents.EXTRA_ROUTE, routingPath);
                }
                broadcastValues(Intents.ACTION_TAGS_ON_WALLS, Intents.EXTRA_TAGS_ON_WALLS,
                        newTags);
                broadcastValues(Intents.ACTION_ZONES, Intents.EXTRA_ZONES,
                        navigation.getZones(ZONE_RADIUS));
                broadcast(Intents.ACTION_LOCATION_FOUND, Intents.EXTRA_POSITION, currentPosition);
            }
        } else if (Intents.ACTION_START_NAVIGATION.equals(intent.getAction())) {
            log.d("Navigation start was requested");
        }
        return START_STICKY;
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