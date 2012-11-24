package de.unierlangen.like.navigation;

import android.graphics.PointF;
import de.unierlangen.like.rfid.GenericTag;

/**
 * Class describes RFID tag used for navigation, extends generic tag by adding
 * coordinates
 * 
 * @author Kate Lyavinskova
 * 
 */
public class Tag extends GenericTag {
    /** X position of the tag */
    float x;
    /** Y position of the tag */
    float y;

    public static final float maxRSSI = -15;
    public static final float minRSSI = -90;

    /** Debug constructor */
    public Tag(String epc, int rssi, boolean isRead, float x, float y) {
        super(epc, rssi, isRead);
        this.x = x;
        this.y = y;
    }

    /**
     * Default constructor, takes generic Tag as one of parameters.
     * 
     * @param tag
     * @param x
     * @param y
     */
    public Tag(GenericTag tag, float x, float y) {
        super(tag.getEpc(), tag.getRssi(), tag.isRead());
        this.x = x;
        this.y = y;
    }

    // Getters and setters
    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    /**
     * @param PointF
     *            point
     * @return
     */
    public double getDistanceTo(PointF point) {
        return Math.hypot(this.x - point.x, this.y - point.y);
    }

}
