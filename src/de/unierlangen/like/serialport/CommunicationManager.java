package de.unierlangen.like.serialport;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class CommunicationManager {

    static SerialPort serialPort;

    private CommunicationManager() {
    };

    public static RxChannel getRxChannel() {
        return serialPort;

    }

    public static TxChannel getTxChannel() {
        return serialPort;

    }

    public static void init(Context context) {
        serialPort = SerialPort.getSerialPort();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SerialPort.getSerialPort().setSharedPreferences(sp);
    }
}
