package de.unierlangen.like.navigation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import android.content.Context;

import com.github.androidutils.logger.Logger;

public class MapBuilder {
    private static MapBuilder sInstance;
    private final ArrayList<Wall> walls;
    private final ArrayList<Door> doors;
    private float wallX1;
    private float wallY1;
    private float wallX2;
    private float wallY2;
    private double alpha;

    private final Logger log = Logger.getDefaultLogger();

    /**
     * TODO Make loading not lazy
     * 
     * @return
     */
    public synchronized static MapBuilder getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new MapBuilder(context);
        }
        return sInstance;
    }

    private MapBuilder(Context context) {
        /** Create input channel and read from the file */
        ArrayList<String> dataFromFile;
        try {
            dataFromFile = FileReader.getStringsFromAsset(context, "map.txt");
        } catch (IOException e) {
            dataFromFile = new ArrayList<String>();
            log.e("Something wrong " + e.getMessage());
        }

        walls = new ArrayList<Wall>();
        doors = new ArrayList<Door>();
        Pattern oneNumber = Pattern.compile(",");
        for (String entry : dataFromFile) {
            String[] singlenumber = oneNumber.split(entry);
            if (singlenumber[0].equals("w")) {
                wallX1 = Float.parseFloat(singlenumber[1]);
                wallY1 = Float.parseFloat(singlenumber[2]);
                wallX2 = Float.parseFloat(singlenumber[3]);
                wallY2 = Float.parseFloat(singlenumber[4]);
                Wall wall = new Wall(wallX1, wallY1, wallX2, wallY2);
                alpha = wall.getAlpha();
                log.d("Created " + wall.toString());
                walls.add(wall);
            } else if (singlenumber[0].equals("d")) {
                Door door = new Door(Float.parseFloat(singlenumber[1]),
                        Float.parseFloat(singlenumber[2]), Float.parseFloat(singlenumber[3]),
                        Float.parseFloat(singlenumber[4]), Float.parseFloat(singlenumber[5]));
                log.d("Created " + door.toString());
                doors.add(door);
            } else {
                Door door = new Door(Float.parseFloat(singlenumber[0]),
                        Float.parseFloat(singlenumber[1]), Float.parseFloat(singlenumber[2]),
                        wallX1, wallY1, alpha);
                log.d("Created " + door.toString());
                doors.add(door);

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

}
