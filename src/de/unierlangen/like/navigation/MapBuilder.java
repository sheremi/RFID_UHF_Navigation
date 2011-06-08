package de.unierlangen.like.navigation;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.regex.Pattern;

import android.util.Log;

public class MapBuilder {
	private static final String TAG = MapBuilder.class.getSimpleName();
	private ArrayList<Wall> walls;
	private ArrayList<Door> doors;
	private float wallX1;
	private float wallY1;
	private float wallX2;
	private float wallY2;
	private float distanceToAxle;
	private float length;
	private float sweepAngle;
	private float startAngle;
	private float doorAxleX;
	private float doorAxleY;
	private double alpha;
	
	public MapBuilder(String path) throws IOException {
		/** Create input channel to read from Java */
		FileChannel inputChannel = new FileInputStream(path).getChannel();
		ByteBuffer buffer = ByteBuffer.allocate((int)(inputChannel.size()*2));
		int size = inputChannel.read(buffer);
		
		buffer.flip();
		String content = new String(buffer.array(),0,size);
		
		inputChannel.close();
		recognizeString(content, false);
	}

	/**
	 * Debug constructor, use only for debug
	 * @param content
	 * @param debug
	 */
	public MapBuilder(String content, boolean debug){
		recognizeString(content, debug);
	}
	
	private void recognizeString(String content, boolean debug){
		walls = new ArrayList<Wall>();
		doors = new ArrayList<Door>();
		// Create a pattern to match breaks
		Pattern oneRow = Pattern.compile(";\r\n");
		Pattern oneNumber = Pattern.compile(",");
		//Split input with the pattern
		String[] allRows = oneRow.split(content);
		
	    for (String entry: allRows)
	    {
	    	String[] singlenumber = oneNumber.split(entry);
	    	if (singlenumber[0].equals("w")){
	    		wallX1 = Float.parseFloat(singlenumber[1]);
	    		wallY1 = Float.parseFloat(singlenumber[2]);
	    		wallX2 = Float.parseFloat(singlenumber[3]);
	    		wallY2 = Float.parseFloat(singlenumber[4]);
	    		Wall wall = new Wall(wallX1, wallY1, wallX2, wallY2);
	    		alpha = wall.getAlpha();
	    		Log.d(TAG, "Created "+wall.toString());
	    		walls.add(wall);
	    	}
	    	else if (singlenumber[0].equals("d")){
	    		Door door = new Door(Float.parseFloat(singlenumber[1]), Float.parseFloat(singlenumber[2]), Float.parseFloat(singlenumber[3]), Float.parseFloat(singlenumber[4]), Float.parseFloat(singlenumber[5]));
	    		Log.d(TAG, "Created "+door.toString());
	    		doors.add(door);
	    	} else {
	    		distanceToAxle = Float.parseFloat(singlenumber[0]);
	    		length = Float.parseFloat(singlenumber[1]);
	    		sweepAngle = Float.parseFloat(singlenumber[2]);
	    		this.addDoor(distanceToAxle, length, sweepAngle);
	    		/* Door door = new Door(distanceToAxle, length, sweepAngle);
	    		 * Log.d(TAG, "Created "+door.toString());
	    		 * doors.add(door);
	    		 */
	    	}
	    }
	}
	// Methods
	public ArrayList<Wall> getWalls() {
		return walls;
	}
	public ArrayList<Door> getDoors() {
		return doors;
	}
	//@Deprecated
	public void addDoor(float distanceToAxle, float length, float sweepAngle){
		doorAxleX = (float)(Math.cos(alpha)) * distanceToAxle + wallX1;
		doorAxleY = (float)(Math.sin(alpha)) * distanceToAxle + wallY1;
		startAngle = (float)(alpha) * 57.2974f;
		Door door = new Door(doorAxleX, doorAxleY, length, startAngle, sweepAngle);
		Log.d(TAG, "Created "+door.toString());
		doors.add(door);
	}
}

