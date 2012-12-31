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
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.github.androidutils.logger.Logger;

import de.unierlangen.like.R;
import de.unierlangen.like.customviews.MapView;
import de.unierlangen.like.navigation.DijkstraRouter;
import de.unierlangen.like.navigation.Door;
import de.unierlangen.like.navigation.MapBuilder;
import de.unierlangen.like.navigation.Navigation;
import de.unierlangen.like.navigation.RoomsDatabase;
import de.unierlangen.like.navigation.Tag;
import de.unierlangen.like.navigation.TagsDatabase;
import de.unierlangen.like.navigation.Wall;
import de.unierlangen.like.rfid.GenericTag;
import de.unierlangen.like.rfid.ReaderIntents;

public class MainYourLocationActivity extends OptionsMenuActivity /*
                                                                   * OnGestureListener
                                                                   * implements
                                                                   * OnClickListener
                                                                   * ,
                                                                   * OnLongClickListener
                                                                   */{
    private final Logger log = Logger.getDefaultLogger();
    private static final String TAG = "MainYourLocationActivity";
    private static final float ZONE_RADIUS = 4.0f;
    private static final int REQUEST_ROOM = 1;
    private MapView mapView;
    private Navigation navigation;
    private DijkstraRouter dijkstraRouter;
    private MapBuilder mapBuilder;
    private ZoomControls zoomControls;

    private final TagsDatabase tagsDatabase = new TagsDatabase();
    private PointF roomCoordinates;

    private WakeLock wakeLock;

    private final BroadcastReceiver readerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            log.d(intent.getAction());
            if (ReaderIntents.ACTION_TAGS.equals(intent.getAction())) {
                // since now we know that Intent action is ACTION_TAGS, we know
                // that
                // array of tags is attached to the intent as an extra with key
                // EXTRA_TAGS
                ArrayList<GenericTag> readTagsFromReader = intent
                        .getParcelableArrayListExtra(ReaderIntents.EXTRA_TAGS);

                ArrayList<Tag> arrayOfTags = new ArrayList<Tag>();
                arrayOfTags.addAll(tagsDatabase.getTags(readTagsFromReader));
                if (!arrayOfTags.isEmpty()) {
                    navigation.setTags(arrayOfTags);
                    mapView.setRectFTags(navigation.getAreaWithTags());
                    mapView.setTags(arrayOfTags);
                    mapView.setZones(navigation.getZones(ZONE_RADIUS));
                    PointF readerPosition = navigation.getReaderPosition();
                    mapView.setReaderPosition(readerPosition);
                    if (roomCoordinates != null) {
                        Path routingPath = dijkstraRouter
                                .findRoute(readerPosition, roomCoordinates);
                        mapView.setRoute(routingPath);
                    }
                }
            }
        }
    };

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
        navigation = new Navigation(walls, doors);
        dijkstraRouter = new DijkstraRouter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        log.d("onResume() in MainYourLocationActivity called");
        IntentFilter filter = new IntentFilter(ReaderIntents.ACTION_TAGS);
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
            String roomName = (String) data.getExtras().get(FindRoomActivity.ROOM_NAME_EXTRA);
            RoomsDatabase roomsDatabase = RoomsDatabase.getRoomsDatabase();
            roomCoordinates = roomsDatabase.getRoomCoordinates(roomName);
            StringBuilder sb = new StringBuilder()
                    .append("Activity.RESULT_OK; room's name and coordinates: ");
            sb.append(roomName + ", " + "{" + roomCoordinates.x + ";" + roomCoordinates.y + "}");
            log.d(sb.toString());
            super.onActivityResult(requestCode, resultCode, data);
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