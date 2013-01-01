package de.unierlangen.like.rfid;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class describes generic RFID tag. Objects supposed to be created by reader or
 * its simulation.
 * 
 * @author Kate Lyavinskova
 * 
 */
public class GenericTag extends Object implements Parcelable {

    protected final String epc;
    protected final int rssi;
    /** Describes whether tag was or was not read(in range) */
    protected final boolean isRead;

    /**
     * Debug constructor used to create tags with certain RSSI value and
     * (random) EPC
     * 
     * @param epc
     * @param rssi
     * @param isRead
     */
    public GenericTag(String epc, int rssi, boolean isRead) {
        super();
        this.rssi = rssi;
        this.isRead = isRead;
        this.epc = epc;
        // epc = (int)(Math.random()*100);
    }

    // Getters and setters
    public String getEpc() {
        return epc;
    }

    public int getRssi() {
        return rssi;
    }

    public boolean isRead() {
        return isRead;
    }

    // ----------------- Parcelable API ------------------

    public static final Parcelable.Creator<GenericTag> CREATOR = new Parcelable.Creator<GenericTag>() {
        @Override
        public GenericTag createFromParcel(Parcel in) {
            return new GenericTag(in);
        }

        @Override
        public GenericTag[] newArray(int size) {
            return new GenericTag[size];
        }
    };

    public GenericTag(Parcel in) {
        epc = in.readString();
        rssi = in.readInt();
        isRead = in.readInt() == 1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(epc);
        dest.writeInt(rssi);
        dest.writeInt(isRead ? 1 : 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // ----------------- END of Parcelable API ------------------

}
