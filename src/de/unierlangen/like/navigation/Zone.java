package de.unierlangen.like.navigation;

import java.lang.reflect.Array;
import java.util.ArrayList;

import android.graphics.PointF;
import android.os.Parcel;
import android.os.Parcelable;

import com.github.androidutils.logger.Logger;

public class Zone implements Parcelable {
    private Logger log = Logger.getDefaultLogger();

    private static final int AMOUNT_OF_POINTS_PER_ZONE = 72;
    private ArrayList<PointF> points;
    private static Zone instance;

    // Constructors
    public Zone(Tag tag, float radius) {
        if (instance == null) {
            // if there is no static instance yet, create it
            instance = new Zone();
        }
        points = new ArrayList<PointF>(AMOUNT_OF_POINTS_PER_ZONE);
        // for each point set radius by multiplying point.x and point.y
        // and make a coordinate translation by tag.x and tag.y
        for (PointF point : instance.getPoints()) {
            float x = point.x * radius + tag.x;
            float y = point.y * radius + tag.y;
            points.add(new PointF(x, y));
        }
    }

    /**
     * Creates a zone with center in (0f, 0f) and radius of 1f
     */
    private Zone() {
        log.d("Creating a zone with center in (0f, 0f) and radius of 1f");
        points = new ArrayList<PointF>(AMOUNT_OF_POINTS_PER_ZONE);
        for (int i = 0; i < AMOUNT_OF_POINTS_PER_ZONE; i++) {
            float x = (float) Math.cos(i * 6.283 / AMOUNT_OF_POINTS_PER_ZONE);
            float y = (float) Math.sin(i * 6.283 / AMOUNT_OF_POINTS_PER_ZONE);
            points.add(new PointF(x, y));
        }
    }

    public Zone(Array x[], Array y[], float radius, int amountOfPoints) {
        super();
    }

    public static Zone obtainTestZone(Array x[], Array y[]) {
        if (instance == null) {
            Tag tag = new Tag("dummy", 12, true, 0.0f, 0.0f);
            instance = new Zone(tag, 2.0f);
        }
        return instance;
    }

    public ArrayList<PointF> getPoints() {
        return points;
    }

    // ----------------- Parcelable API ------------------

    public static final Parcelable.Creator<Zone> CREATOR = new Parcelable.Creator<Zone>() {
        @Override
        public Zone createFromParcel(Parcel in) {
            return new Zone(in);
        }

        @Override
        public Zone[] newArray(int size) {
            return new Zone[size];
        }
    };

    public Zone(Parcel in) {
        log = Logger.getDefaultLogger();
        points = new ArrayList<PointF>(AMOUNT_OF_POINTS_PER_ZONE);
        in.readTypedList(points, PointF.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(points);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // ----------------- END of Parcelable API ------------------

}
