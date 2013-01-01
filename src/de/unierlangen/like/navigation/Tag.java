package de.unierlangen.like.navigation;

import android.graphics.PointF;
import android.os.Parcel;
import android.os.Parcelable;
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

    // ----------------- Parcelable API ------------------
    public static final Parcelable.Creator<Tag> CREATOR = new Parcelable.Creator<Tag>() {
        @Override
        public Tag createFromParcel(Parcel in) {
            return new Tag(in);
        }

        @Override
        public Tag[] newArray(int size) {
            return new Tag[size];
        }
    };

    public Tag(Parcel in) {
        super(in.readString(), in.readInt(), in.readInt() == 1);
        this.x = in.readFloat();
        this.y = in.readFloat();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(epc);
        dest.writeInt(rssi);
        dest.writeInt(isRead ? 1 : 0);
        dest.writeFloat(x);
        dest.writeFloat(y);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // ----------------- END of Parcelable API ------------------

}
