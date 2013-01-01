package de.unierlangen.like.navigation;

import java.util.ArrayList;

import android.graphics.PointF;

import com.github.androidutils.logger.Logger;

public class Navigation {
    private final Logger log = Logger.getDefaultLogger();
    // Fields
    private final ArrayList<Wall> mWalls;
    private final ArrayList<Door> mDoors;
    // Tools for navigation
    private ArrayList<Tag> arrayOfTags;

    // Constructor
    public Navigation(ArrayList<Wall> walls, ArrayList<Door> doors) {
        super();
        mWalls = walls;
        mDoors = doors;
    }

    public void setTags(ArrayList<Tag> arrayOfTags) {
        this.arrayOfTags = arrayOfTags;

    }

    /**
     * 
     * @param radius
     * @param amountOfPoints
     * @return
     */
    public ArrayList<Zone> getZones(float radius) {
        ArrayList<Zone> zones = new ArrayList<Zone>();
        for (Tag tag : this.arrayOfTags) {
            Zone zone = new Zone(tag, radius);
            for (PointF point : zone.getPoints()) {
                for (Wall wall : mWalls) {
                    PointF intersection = wall.getIntersection(tag, point.x, point.y);
                    if (intersection != null) {
                        if (tag.getDistanceTo(intersection) < tag.getDistanceTo(point)) {
                            point.x = intersection.x;
                            point.y = intersection.y;
                        }
                    }
                }
            }
            zones.add(zone);
        }
        return zones;
    }

    // TODO modify it according to the found zones intersection
    public PointF getReaderPosition() {
        PointF readerPosition = new PointF();
        /* TODO modify */
        if (this.arrayOfTags.size() == 0) {
            log.d("There are no any tags in the reading area or they weren't found");
        }
        if (this.arrayOfTags.size() == 1) {
            readerPosition.x = arrayOfTags.get(0).x;
            readerPosition.y = arrayOfTags.get(0).y;
        } else {
            this.arrayOfTags.size();
            float sumX = 0;
            float sumY = 0;
            for (Tag tag : this.arrayOfTags) {
                sumX = sumX + tag.x;
                sumY = sumY + tag.y;
            }
            readerPosition.x = sumX / this.arrayOfTags.size();
            readerPosition.y = sumY / this.arrayOfTags.size();
        }
        return readerPosition;
    }
}
