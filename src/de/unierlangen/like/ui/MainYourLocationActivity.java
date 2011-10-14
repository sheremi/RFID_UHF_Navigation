package de.unierlangen.like.ui;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import android.widget.ZoomControls;
import de.unierlangen.like.customviews.MapView;
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
	public static final int THREAD_EVENT_READ_TAGS = 4;
	private static final int READ_TAGS_INTERVAL = 3000;
	private MapView mapView;
	private Navigation navigation;
	private MapBuilder mapBuilder;
	private ZoomControls zoomControls;
	private SerialPort readerSerialPort;
	private Reader reader;
	private TagsDatabase tagsDatabase = new TagsDatabase();
	private ArrayList<GenericTag> readTagsFromReader = new ArrayList<GenericTag>();
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
				readTagsFromReader = (ArrayList<GenericTag>) msg.obj;
				// FIXME replace fields with local variables
				ArrayList<Tag> arrayOfTags = new ArrayList<Tag>();
				arrayOfTags.addAll(tagsDatabase.getTags(readTagsFromReader));
				navigation = new Navigation(arrayOfTags);
				mapView.setRectFTags(navigation.getAreaWithTags());
				mapView.setTags(arrayOfTags);
				break;
			case Reader.RESPONSE_REGS:
				//TODO implement analysis of RESPONSE_REGS
				break;
			case Reader.ERROR:
				Toast.makeText(getApplicationContext(),"Reader MCU reported error: check the connection",Toast.LENGTH_LONG).show();
				break;
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
		
		//FIXME move that stuff to onResume
		try {
			readerSerialPort = SerialPort.getSerialPort(this);
				try {
					reader = new Reader(readerSerialPort, handler);
				} catch (Exception e1) {
					Log.d(TAG,"reader constructor failed", e1);
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle("Achtung!");
					builder.setMessage("Oops! Reader constructor failed. " +
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
				}
			
				
		} catch (InvalidParameterException e2) {
			Log.d(TAG,"Unable to get SerialPort. It has to be configured first", e2);
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
			e2.printStackTrace();
		} catch (SecurityException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (InterruptedException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} 
		
		try {
			mapBuilder = new MapBuilder("/sdcard/like/map.txt");
		} catch (IOException e) {
			Toast.makeText(getApplicationContext(),"Sorry, current file is not readable or not found",Toast.LENGTH_SHORT).show();
			mapBuilder = new MapBuilder("1,1,2,2;2,2,3,3;", true);
			Log.e("TAG", "oops",e);
		}

		ArrayList<Wall> walls = mapBuilder.getWalls();
		ArrayList<Door> doors = mapBuilder.getDoors();
		mapView.setWalls(walls);
		mapView.setDoors(doors);
		
				
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
		handler.removeMessages(THREAD_EVENT_READ_TAGS);
	}

}






	
	
        

    
 