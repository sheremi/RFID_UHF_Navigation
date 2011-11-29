package de.unierlangen.like.ui;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Path;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import android.widget.ZoomControls;
import de.unierlangen.like.customviews.MapView;
import de.unierlangen.like.navigation.DijkstraRouter;
import de.unierlangen.like.navigation.Door;
import de.unierlangen.like.navigation.MapBuilder;
import de.unierlangen.like.navigation.Navigation;
import de.unierlangen.like.navigation.Tag;
import de.unierlangen.like.navigation.TagsDatabase;
import de.unierlangen.like.navigation.Wall;
import de.unierlangen.like.rfid.GenericTag;
import de.unierlangen.like.rfid.Reader;
import de.unierlangen.like.rfid.Reader.ReaderException;
import de.unierlangen.like.serialport.SerialPort;

public class MainYourLocationActivity extends OptionsMenuActivity /*implements OnClickListener, OnLongClickListener */{
	private static final String TAG = "MainYourLocationActivity";
	private static final float ZONE_RADIUS = 4.0f;
	private static final int AMOUNT_OF_POINTS_PER_ZONE = 72;
	public static final int THREAD_EVENT_READ_TAGS = 4;
	private static final int READ_TAGS_INTERVAL = 3000;
	private MapView mapView;
	private Navigation navigation;
	private MapBuilder mapBuilder;
	private ZoomControls zoomControls;
	private SerialPort readerSerialPort;
	private Reader reader;
	private TagsDatabase tagsDatabase = new TagsDatabase();
	//TODO make something nice with long click on the view
	/*public boolean onLongClick(View v) {
		return false;
	}*/

	private Handler handler = new Handler() {
		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			Log.d (TAG, "handleMessage(" + msg.what + ")");
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
				mapView.setZones(navigation.getZones(ZONE_RADIUS, AMOUNT_OF_POINTS_PER_ZONE));
				break;
			case Reader.RESPONSE_REGS:
				//TODO implement analysis of RESPONSE_REGS
				break;
			case Reader.WARNING:
				ReaderException e = (ReaderException) msg.obj;
				//FIXME revert this commit later whan do not send warnings all the time.
				//Toast.makeText(getApplicationContext(),"Warning: " + e.getMessage(), Toast.LENGTH_LONG).show();
				break;
			case Reader.ERROR:
				ReaderException e1 = (ReaderException) msg.obj;
				Log.d(TAG,"Reader repoted error", e1);
				AlertDialog.Builder builder = new AlertDialog.Builder(MainYourLocationActivity.this);
				builder.setTitle("Achtung!");
				builder.setMessage("Oops! " +
						"The reader is missing or connected is wrong. " +
						"Check the connection between phone and reader. " +
						"Do you wanna try to communicate with reader again?");
				builder.setNegativeButton("No, thanks", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						getApplication().stopService(getIntent()); 
					}
				});
				builder.setPositiveButton("Go ahead", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						startActivity(new Intent(getApplicationContext(), MainYourLocationActivity.class));
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

	//** Called when the activity is first created. *//
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_your_location);
		mapView = (MapView)findViewById(R.id.mapView);
		// Control elements for zooming
		zoomControls = (ZoomControls)findViewById(R.id.zoomcontrols);
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
			Toast.makeText(getApplicationContext(),"Sorry, current file is not readable or not found",Toast.LENGTH_SHORT).show();
			mapBuilder = new MapBuilder("1,1,2,2;2,2,3,3;", true);
			Log.e("TAG", "oops",e);
		}

		ArrayList<Wall> walls = mapBuilder.getWalls();
		ArrayList<Door> doors = mapBuilder.getDoors();
		DijkstraRouter dijkstraRouter = new DijkstraRouter();
		Path routingPath = dijkstraRouter.findRoute(null, null);
		mapView.setWalls(walls);
		mapView.setDoors(doors);
		mapView.setRoute(routingPath);
		navigation = new Navigation(walls, doors);
		
		try {
			readerSerialPort = SerialPort.getSerialPort();
			readerSerialPort.setSharedPreferences(PreferenceManager.getDefaultSharedPreferences(this));
		} catch (InvalidParameterException e) {
			Log.w(TAG,"SerialPort has to be configured first", e);
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
		Log.d(TAG,"Reader and serial port were created succesfully");
        //Toast.makeText(getApplicationContext(),"Press Menu button",Toast.LENGTH_SHORT).show();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG,"onResume() in MainYourLocationActivity called");
		handler.obtainMessage(THREAD_EVENT_READ_TAGS).sendToTarget();
	}
	
	@Override
    protected void onPause(){
		super.onPause();
		Log.d(TAG,"onPause() in MainYourLocationActivity called");
		handler.removeMessages(THREAD_EVENT_READ_TAGS);
	}

}






	
	
        

    
 