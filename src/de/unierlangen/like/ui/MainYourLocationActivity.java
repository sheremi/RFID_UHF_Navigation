package de.unierlangen.like.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

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
			
	/*public boolean onLongClick(View v) {
		return false;
	}*/
	
	//** Called when the activity is first created. *//
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_your_location);
		mapView = (MapView)findViewById(R.id.mapView);
		
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
		/*arrayOfTags.add(new Tag(-15, true, 42.63f,	6.35f));
		arrayOfTags.add(new Tag(-30, true, 47.85f,	6.12f));
		arrayOfTags.add(new Tag(-60, true, 51.71f,	6.80f));
		arrayOfTags.add(new Tag(-20, true, 42.86f,	0.45f));
		arrayOfTags.add(new Tag(-22, true, 46.94f,	0.45f));
		arrayOfTags.add(new Tag(-90, true, 56.70f,	7.03f));
		arrayOfTags.add(new Tag(-200, true, 18.37f, 5.89f));*/
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
			arrayOfTags.add(new Tag("HJHVJH7865", -20, true, 18.37f, 5.89f));
			arrayOfTags.add(new Tag("DSTRUJ6789", -28, true, 14.74f, 0.45f));
			arrayOfTags.add(new Tag("EFJKHG5790", -25, true, 14.74f, 7.03f));
			arrayOfTags.add(new Tag("GFGHGN0458", -200, true, 22.00f, 5.89f));
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
		/*doors.add(new Door(18.15f, 5.82f, 21.29f, 8.38f, 270));
		
		WallWithDoor wallWithDoor1 = new WallWithDoor(18.15f, 5.82f, 21.29f, 8.38f);
		wallWithDoor1.addDoor(2, 2, 90);
		arrayOfObstacles.add(wallWithDoor1);*/
		navigation = new Navigation(arrayOfTags);
		mapView.setRectFTags(navigation.getAreaWithTags());
		mapView.setTags(arrayOfTags);
		mapView.setWalls(walls);
		mapView.setDoors(doors);
				
        Toast.makeText(getApplicationContext(),"Press Menu button",Toast.LENGTH_SHORT).show();
	}
	// TODO make a slider for zooming
	/*public void onClick(View view) {
		switch (view.getId()) {
		case R.id.plus:
			mapView.setAreaPadding(mapView.getPadding() - 2.0f);
			break;
		case R.id.minus:
			mapView.setAreaPadding(mapView.getPadding() + 2.0f);
			break;	
		default:
			break;
		}*/

	}






	
	
        

    
 