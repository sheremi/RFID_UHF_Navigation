package de.unierlangen.like.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

public class MainYourLocationActivity extends OptionsMenuActivity /*implements OnClickListener, OnLongClickListener */{
	private static final String TAG = "MainYourLocationActivity";
	private MapView mapView;
	private Navigation navigation;
	private MapBuilder mapBuilder;
	private ZoomControls zoomControls;
				
	//TODO make something nice with long click on the view
	/*public boolean onLongClick(View v) {
		return false;
	}*/
	
	//** Called when the activity is first created. *//
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_your_location);
		mapView = (MapView)findViewById(R.id.mapView);
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
		
		ArrayList<Tag> arrayOfTags = new ArrayList<Tag>();
		
		try {
			Reader reader = new Reader(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
			//arrayOfTags.add(new Tag(genericTag,(float)Math.random()*20f, (float)Math.random()*20f));
			TagsDatabase tagsDatabase = new TagsDatabase();
			HashMap<String, Float[]> tagsHashMap = tagsDatabase.createTagsHashMap();
			for (GenericTag genericTag: reader.performRound()){
				if (tagsHashMap.containsKey(genericTag.getEpc())){
					Float[] coordinates = tagsHashMap.get(genericTag.getEpc());
					arrayOfTags.add(new Tag(genericTag, coordinates[0], coordinates[1]));
				}
			}
		} catch (Exception e1) {
			Log.d(TAG,"reader constructor failed", e1);
			// Build the dialog
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			// Set title of the dialog
			builder.setTitle("Achtung!");
			// The message that is displayed in the dialog
			builder.setMessage("Serial port has to be configured first. After configuration go back to Your Location Activity");
			// Set behavior of positive button
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					startActivity(new Intent(getApplicationContext(), SerialPortPreferences.class));
				}
			});
			// Create and assign the dialog
			AlertDialog alert = builder.create();
			// Show the dialog
			alert.show();
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
		navigation = new Navigation(arrayOfTags);
		mapView.setRectFTags(navigation.getAreaWithTags());
		mapView.setTags(arrayOfTags);
		mapView.setWalls(walls);
		mapView.setDoors(doors);
				
        //Toast.makeText(getApplicationContext(),"Press Menu button",Toast.LENGTH_SHORT).show();
	}

}






	
	
        

    
 