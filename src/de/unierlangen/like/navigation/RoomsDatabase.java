package de.unierlangen.like.navigation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Pattern;

import android.graphics.PointF;
import android.util.Log;

public class RoomsDatabase {

    private static RoomsDatabase instance;
    HashMap<String, PointF> hashMap = new HashMap<String, PointF>();

    public static RoomsDatabase getRoomsDatabase() {
        if (instance == null) {
            instance = new RoomsDatabase();
        }
        return instance;
    }

    private RoomsDatabase() {
        try {
            /** Create input channel and read from the file */
            FileReader fileReader = new FileReader();
            String content = fileReader.getDataFromFile("/sdcard/like/rooms.txt");
            Pattern oneElement = Pattern.compile(",");
            for (String entry : fileReader.splitStringContent(content)) {
                String[] singleElement = oneElement.split(entry);
                String roomName = singleElement[0];
                float x = Float.parseFloat(singleElement[1]);
                float y = Float.parseFloat(singleElement[2]);
                hashMap.put(roomName, new PointF(x, y));
            }
        } catch (IOException e) {
            Log.e("RoomsDatabase", "file with rooms is not found", e);
        }

    }

    public Set<String> getRoomsNamesSet() {
        return hashMap.keySet();
    }

    public String[] getRoomsNamesArray() {
        Set<String> keySet = getRoomsNamesSet();
        return (String[]) keySet.toArray(new String[keySet.size()]);
    }

    public PointF getRoomCoordinates(String roomName) {
        return hashMap.get(roomName);
    }
}
