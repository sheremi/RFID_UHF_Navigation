package de.unierlangen.like.ui;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import android.widget.ZoomControls;
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
import de.unierlangen.like.rfid.Reader;
import de.unierlangen.like.rfid.Reader.ReaderException;
import de.unierlangen.like.serialport.SerialPort;

public class MainYourLocationActivity extends OptionsMenuActivity implements OnGestureListener /*
                                                                                                * implements
                                                                                                * OnClickListener
                                                                                                * ,
                                                                                                * OnLongClickListener
                                                                                                */{
    private static final String TAG = "MainYourLocationActivity";
    private static final float ZONE_RADIUS = 4.0f;
    public static final int THREAD_EVENT_READ_TAGS = 4;
    private static final int READ_TAGS_INTERVAL = 3000;
    private static final int REQUEST_ROOM = 1;
    private MapView mapView;
    private Navigation navigation;
    private DijkstraRouter dijkstraRouter;
    private MapBuilder mapBuilder;
    private ZoomControls zoomControls;
    private SerialPort readerSerialPort;
    private Reader reader;
    private TagsDatabase tagsDatabase = new TagsDatabase();
    private PointF roomCoordinates;
    private GestureDetector mGestureDetector = new GestureDetector(this);

    private Handler handler = new Handler() {
        @SuppressWarnings("unchecked")
        @Override
        public void handleMessage(Message msg) {
            // Log.d (TAG, "handleMessage(" + msg.what + ")");
            switch (msg.what) {
            case Reader.RESPONSE_TAGS:
            case Reader.EVENT_TAGS:
                ArrayList<GenericTag> readTagsFromReader = new ArrayList<GenericTag>();
                readTagsFromReader = (ArrayList<GenericTag>) msg.obj;
                ArrayList<Tag> arrayOfTags = new ArrayList<Tag>();
                arrayOfTags.addAll(tagsDatabase.getTags(readTagsFromReader));
                navigation.setTags(arrayOfTags);
                mapView.setRectFTags(navigation.getAreaWithTags());
                mapView.setTags(arrayOfTags);
                mapView.setZones(navigation.getZones(ZONE_RADIUS));
                PointF readerPosition = navigation.getReaderPosition();
                mapView.setReaderPosition(readerPosition);
                if (roomCoordinates != null) {
                    Path routingPath = dijkstraRouter.findRoute(readerPosition, roomCoordinates);
                    mapView.setRoute(routingPath);
                }
                break;
            case Reader.RESPONSE_REGS:
                // TODO implement analysis of RESPONSE_REGS
                break;
            case Reader.WARNING:
                ReaderException e = (ReaderException) msg.obj;
                // FIXME revert this commit later when do not send warnings all
                // the time.
                // Toast.makeText(getApplicationContext(),"Warning: " +
                // e.getMessage(), Toast.LENGTH_LONG).show();
                break;
            case Reader.ERROR:
                ReaderException e1 = (ReaderException) msg.obj;
                Log.d(TAG, "Reader repoted error", e1);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainYourLocationActivity.this);
                builder.setTitle("Achtung!");
                builder.setMessage("Oops! " + "The reader is missing or connection is wrong. "
                        + "Check the connection between phone and reader. "
                        + "Do you wanna try to communicate with reader again?");
                builder.setNegativeButton("No, thanks", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        getApplication().stopService(getIntent());
                    }
                });
                builder.setPositiveButton("Go ahead", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        startActivity(new Intent(getApplicationContext(),
                                MainYourLocationActivity.class));
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
                return;
            case THREAD_EVENT_READ_TAGS:
                reader.performRound();
                sendMessageDelayed(obtainMessage(THREAD_EVENT_READ_TAGS), READ_TAGS_INTERVAL);
                break;
            default:
                break;
            }
        };
    };

    // ** Called when the activity is first created. *//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_your_location);
        mapView = (MapView) findViewById(R.id.mapView);
        /*
         * mapView.setOnLongClickListener(new OnLongClickListener() { public
         * boolean onLongClick(View v) { Intent intent = new
         * Intent(MainYourLocationActivity.this, FindRoomActivity.class);
         * startActivityForResult(intent, REQUEST_ROOM); return false; } });
         */
        // Control elements for zooming
        zoomControls = (ZoomControls) findViewById(R.id.zoomcontrols);
        zoomControls.setOnZoomInClickListener(new OnClickListener() {
            public void onClick(View v) {
                mapView.setAreaPadding(mapView.getPadding() - 2.0f);
            }
        });

        zoomControls.setOnZoomOutClickListener(new OnClickListener() {
            public void onClick(View v) {
                mapView.setAreaPadding(mapView.getPadding() + 2.0f);
            }
        });

        try {
            mapBuilder = new MapBuilder("/sdcard/like/map.txt");
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(),
                    "Sorry, current file is not readable or not found", Toast.LENGTH_SHORT).show();
            mapBuilder = new MapBuilder("1,1,2,2;2,2,3,3;", true);
            Log.e("TAG", "oops", e);
        }

        ArrayList<Wall> walls = mapBuilder.getWalls();
        ArrayList<Door> doors = mapBuilder.getDoors();
        mapView.setWalls(walls);
        mapView.setDoors(doors);
        navigation = new Navigation(walls, doors);

        try {
            readerSerialPort = SerialPort.getSerialPort();
            readerSerialPort.setSharedPreferences(PreferenceManager
                    .getDefaultSharedPreferences(this));
        } catch (InvalidParameterException e) {
            Log.w(TAG, "SerialPort has to be configured first", e);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Achtung!");
            builder.setMessage("Serial port has to be configured first. After configuration go back to Your Location Activity");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(getApplicationContext(), SerialPortPreferences.class));
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
            e.printStackTrace();
        }
        reader = new Reader(readerSerialPort, handler);
        Log.d(TAG, "Reader and serial port were created succesfully");
        // Toast.makeText(getApplicationContext(),"Press Menu button",Toast.LENGTH_SHORT).show();
        dijkstraRouter = new DijkstraRouter();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        // MotionEvent object holds XY values
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            String text = "You clicked at x = " + event.getRawX() + " and y = " + event.getRawY();
            Log.d(TAG, text);
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        }
        return super.onTouchEvent(event);
    }

    /*
     * @Override public void onShowPress(MotionEvent e) { String text =
     * "You click at x = " + e.getX() + " and y = " + e.getY(); Log.d(TAG,
     * text); Toast.makeText(this, text, Toast.LENGTH_LONG).show();
     * 
     * }
     * 
     * @Override public boolean onScroll(MotionEvent e1, MotionEvent e2, float
     * distanceX, float distanceY) { endX = e2.getX(); endY = e2.getY(); }
     */

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() in MainYourLocationActivity called");
        handler.obtainMessage(THREAD_EVENT_READ_TAGS).sendToTarget();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() in MainYourLocationActivity called");
        handler.removeMessages(THREAD_EVENT_READ_TAGS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Comparison of two floats (checking if resultCode == RESULT_OK)
        if (Math.abs(resultCode) - Math.abs(Activity.RESULT_OK) < 0.00001f) {
            String roomName = (String) data.getExtras().get(FindRoomActivity.ROOM_NAME_EXTRA);
            RoomsDatabase roomsDatabase = RoomsDatabase.getRoomsDatabase();
            roomCoordinates = roomsDatabase.getRoomCoordinates(roomName);
            StringBuilder sb = new StringBuilder()
                    .append("Activity.RESULT_OK; room's name and coordinates: ");
            sb.append(roomName + ", " + "{" + roomCoordinates.x + ";" + roomCoordinates.y + "}");
            Log.d(TAG, sb.toString());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public boolean onDown(MotionEvent e) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        // TODO Auto-generated method stub
        return false;
    }

    public void onLongPress(MotionEvent e) {
        // TODO Auto-generated method stub

    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        // TODO Auto-generated method stub
        return false;
    }

    public void onShowPress(MotionEvent e) {
        // TODO Auto-generated method stub

    }

    public boolean onSingleTapUp(MotionEvent e) {
        // TODO Auto-generated method stub
        return false;
    }
}