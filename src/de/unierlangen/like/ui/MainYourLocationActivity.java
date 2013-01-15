package de.unierlangen.like.ui;

import java.io.IOException;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.github.androidutils.logger.Logger;

import de.unierlangen.like.Intents;
import de.unierlangen.like.R;
import de.unierlangen.like.customviews.MapView;
import de.unierlangen.like.navigation.DijkstraRouter;
import de.unierlangen.like.navigation.Door;
import de.unierlangen.like.navigation.MapBuilder;
import de.unierlangen.like.navigation.RoomsDatabase;
import de.unierlangen.like.navigation.Tag;
import de.unierlangen.like.navigation.Wall;
import de.unierlangen.like.navigation.Zone;

public class MainYourLocationActivity extends OptionsMenuActivity /*
                                                                   * OnGestureListener
                                                                   * implements
                                                                   * OnClickListener
                                                                   * ,
                                                                   * OnLongClickListener
                                                                   */{
    private final Logger log = Logger.getDefaultLogger();
    private static final String TAG = "MainYourLocationActivity";
    private static final int REQUEST_ROOM = 1;
    private MapView mapView;
    private ZoomControls zoomControls;
    private WakeLock wakeLock;

    private MapBuilder mapBuilder;

    private DrawMapOverlayPreferenceChangeListener drawMapOverlayPreferenceChangeListener;
    private final BroadcastReceiver readerReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            log.d(intent.getAction());
            if (Intents.ACTION_TAGS_ON_WALLS.equals(intent.getAction())) {
                ArrayList<Tag> arrayOfTags = intent
                        .getParcelableArrayListExtra(Intents.EXTRA_TAGS_ON_WALLS);
                log.d(arrayOfTags.toString());
                mapView.setRectFTags(getAreaWithTags(arrayOfTags));
                mapView.setTags(arrayOfTags);

            } else if (Intents.ACTION_LOCATION_FOUND.equals(intent.getAction())) {
                PointF readerPosition = intent.getParcelableExtra(Intents.EXTRA_POSITION);
                mapView.setReaderPosition(readerPosition);

            } else if (Intents.ACTION_ROUTE_FOUND.equals(intent.getAction())) {
                ArrayList<PointF> routingPath = intent
                        .getParcelableArrayListExtra(Intents.EXTRA_ROUTE);
                log.d(routingPath.toString());
                mapView.setRoute(DijkstraRouter.convertPointsToPath(routingPath));

            } else if (Intents.ACTION_ZONES.equals(intent.getAction())) {
                ArrayList<Zone> zones = intent.getParcelableArrayListExtra(Intents.EXTRA_ZONES);
                mapView.setZones(zones);
            }
        }
    };

    // Methods
    public static RectF getAreaWithTags(ArrayList<Tag> arrayOfTags) {
        // Geometry parameters
        float areaWithTagsX2;
        float areaWithTagsY2;
        float areaWithTagsX1;
        float areaWithTagsY1;
        areaWithTagsX2 = Float.MIN_VALUE;
        areaWithTagsY2 = Float.MIN_VALUE;
        areaWithTagsX1 = Float.MAX_VALUE;
        areaWithTagsY1 = Float.MAX_VALUE;
        for (Tag tag : arrayOfTags) {
            areaWithTagsX1 = Math.min(areaWithTagsX1, tag.getX());
            areaWithTagsY1 = Math.min(areaWithTagsY1, tag.getY());
            areaWithTagsX2 = Math.max(areaWithTagsX2, tag.getX());
            areaWithTagsY2 = Math.max(areaWithTagsY2, tag.getY());
        }
        return new RectF(areaWithTagsX1, areaWithTagsY1, areaWithTagsX2, areaWithTagsY2);
    }

    // ** Called when the activity is first created. *//
    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(
                PowerManager.FULL_WAKE_LOCK, TAG);

        setContentView(R.layout.main_your_location);
        mapView = (MapView) findViewById(R.id.mapView);

        final ActionBar bar = getActionBar();
        bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);

        mapView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent(MainYourLocationActivity.this, FindRoomActivity.class);
                startActivityForResult(intent, REQUEST_ROOM);
                return false;
            }
        });

        // Control elements for zooming
        zoomControls = (ZoomControls) findViewById(R.id.zoomcontrols);
        zoomControls.setOnZoomInClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mapView.setAreaPadding(mapView.getPadding() - 2.0f);
            }
        });

        zoomControls.setOnZoomOutClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mapView.setAreaPadding(mapView.getPadding() + 2.0f);
            }
        });

        try {
            mapBuilder = new MapBuilder("/sdcard/like/map.txt");
        } catch (IOException e) {
            Toast.makeText(this, "Sorry, current file is not readable or not found",
                    Toast.LENGTH_SHORT).show();
            mapBuilder = new MapBuilder("1,1,2,2;2,2,3,3;", true);
            log.e("oops", e);
        }

        ArrayList<Wall> walls = mapBuilder.getWalls();
        ArrayList<Door> doors = mapBuilder.getDoors();
        mapView.setWalls(walls);
        mapView.setDoors(doors);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        drawMapOverlayPreferenceChangeListener = new DrawMapOverlayPreferenceChangeListener();
        sp.registerOnSharedPreferenceChangeListener(drawMapOverlayPreferenceChangeListener);
        mapView.setDrawMapOverlay(sp.getBoolean("draw_map_overlay", false));
    }

    @Override
    protected void onResume() {
        super.onResume();
        log.d("onResume() in MainYourLocationActivity called");
        IntentFilter filter = new IntentFilter(Intents.ACTION_TAGS_ON_WALLS);
        filter.addAction(Intents.ACTION_LOCATION_FOUND);
        filter.addAction(Intents.ACTION_ROUTE_FOUND);
        filter.addAction(Intents.ACTION_ZONES);
        registerReceiver(readerReceiver, filter);
        wakeLock.acquire();

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(readerReceiver);
        wakeLock.release();
        log.d("onPause() in MainYourLocationActivity called");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Comparison of two floats (checking if resultCode == RESULT_OK)
        if (resultCode == Activity.RESULT_OK) {
            String dest = (String) data.getExtras().get(FindRoomActivity.ROOM_NAME_EXTRA);
            RoomsDatabase roomsDatabase = RoomsDatabase.getRoomsDatabase();
            PointF roomCoordinates = roomsDatabase.getRoomCoordinates(dest);
            log.d("Destination: " + dest + " at {" + roomCoordinates.x + ";" + roomCoordinates.y
                    + "}");
            Intent intent = new Intent(Intents.ACTION_SET_DESTINATION);
            intent.putExtra(Intents.EXTRA_DESTINATION, roomCoordinates);
            startService(intent);
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private final class DrawMapOverlayPreferenceChangeListener implements
            OnSharedPreferenceChangeListener {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if ("draw_map_overlay".equals(key)) {
                mapView.setDrawMapOverlay(sharedPreferences.getBoolean("draw_map_overlay", false));
            }
        }
    }

    /*
     * TODO add scrolling
     * 
     * @Override public boolean onTouchEvent(MotionEvent event) {
     * mGestureDetector.onTouchEvent(event); // MotionEvent object holds XY
     * values if (event.getAction() == MotionEvent.ACTION_MOVE) { String text =
     * "You clicked at x = " + event.getRawX() + " and y = " + event.getRawY();
     * log.d(text); Toast.makeText(this, text, Toast.LENGTH_SHORT).show(); }
     * return super.onTouchEvent(event); }
     */

    /*
     * @Override public void onShowPress(MotionEvent e) { String text =
     * "You click at x = " + e.getX() + " and y = " + e.getY(); log.d(TAG,
     * text); Toast.makeText(this, text, Toast.LENGTH_LONG).show();
     * 
     * }
     * 
     * @Override public boolean onScroll(MotionEvent e1, MotionEvent e2, float
     * distanceX, float distanceY) { endX = e2.getX(); endY = e2.getY(); }
     */

    /*
     * public boolean onDown(MotionEvent e) { // TODO Auto-generated method stub
     * return false; }
     * 
     * public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
     * float velocityY) { // TODO Auto-generated method stub return false; }
     * 
     * public void onLongPress(MotionEvent e) { // TODO Auto-generated method
     * stub
     * 
     * }
     * 
     * public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
     * float distanceY) { // TODO Auto-generated method stub return false; }
     * 
     * public void onShowPress(MotionEvent e) { // TODO Auto-generated method
     * stub
     * 
     * }
     * 
     * public boolean onSingleTapUp(MotionEvent e) { // TODO Auto-generated
     * method stub return false; }
     * 
     * public boolean onLongClick(View v) { // TODO Auto-generated method stub
     * return false; }
     * 
     * public void onClick(View v) { // TODO Auto-generated method stub
     * 
     * }
     */
}