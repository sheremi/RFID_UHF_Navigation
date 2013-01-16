package de.unierlangen.like.navigation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.PointF;

import com.github.androidutils.logger.Logger;

public class RoomsDatabase {
    private final Logger log = Logger.getDefaultLogger();
    private static RoomsDatabase instance;
    HashMap<String, PointF> hashMap = new HashMap<String, PointF>();

    public static RoomsDatabase getRoomsDatabase(Context context) {
        if (instance == null) {
            instance = new RoomsDatabase(context);
        }
        return instance;
    }

    private RoomsDatabase(Context context) {
        try {
            /** Create input channel and read from the file */
            ArrayList<String> lines = FileReader.getStringsFromAsset(context, "rooms.txt");
            Pattern oneElement = Pattern.compile(",");
            for (String entry : lines) {
                String[] singleElement = oneElement.split(entry);
                String roomName = singleElement[0];
                float x = Float.parseFloat(singleElement[1]);
                float y = Float.parseFloat(singleElement[2]);
                hashMap.put(roomName, new PointF(x, y));
            }
        } catch (IOException e) {
            log.e("file with rooms is not found", e);
        }

    }

    public Set<String> getRoomsNamesSet() {
        return hashMap.keySet();
    }

    public String[] getRoomsNamesArray() {
        Set<String> keySet = getRoomsNamesSet();
        return keySet.toArray(new String[keySet.size()]);
    }

    public PointF getRoomCoordinates(String roomName) {
        return hashMap.get(roomName);
    }
}
