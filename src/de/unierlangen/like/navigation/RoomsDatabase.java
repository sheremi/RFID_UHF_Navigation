package de.unierlangen.like.navigation;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Pattern;

import android.graphics.PointF;
import android.util.Log;

public class RoomsDatabase {

	private static RoomsDatabase instance;
	HashMap<String, PointF> hashMap = new HashMap<String, PointF>();

	public static RoomsDatabase getRoomsDatabase() {
		if (instance==null){
			instance = new RoomsDatabase();
		}
		return instance;
	}

	private RoomsDatabase(){
		try {
			//"/sdcard/like/rooms.txt"
			/** Create input channel to read from Java */
			FileChannel inputChannel = new FileInputStream("/sdcard/like/rooms.txt").getChannel();
			ByteBuffer buffer = ByteBuffer.allocate((int)(inputChannel.size()*2));
			int size = inputChannel.read(buffer);
			buffer.flip();
			String content = new String(buffer.array(),0,size);
			inputChannel.close();
			// Create a pattern to match breaks
			Pattern oneRow = Pattern.compile(";\r\n");
			Pattern oneElement = Pattern.compile(",");
			//Split input with the pattern
			String[] allRows = oneRow.split(content);
			for (String entry: allRows){
				String[] singleElement = oneElement.split(entry);
				String roomName = singleElement[0];
				float x = Float.parseFloat(singleElement[1]);
				float y = Float.parseFloat(singleElement[2]);
				hashMap.put(roomName, new PointF(x, y));
			}
		} catch (IOException e) {
			Log.e("RoomsDatabase", "file with rooms is not found",e);
		}

	}

	public Set<String> getRoomsNamesSet(){
		return hashMap.keySet();
	}

	public String[] getRoomsNamesArray(){
		Set<String> keySet = getRoomsNamesSet();
		return (String[]) keySet.toArray(new String[keySet.size()]);
	}
	
	public PointF getRoomCoordinates(String roomName){
		return hashMap.get(roomName);
	}
}
