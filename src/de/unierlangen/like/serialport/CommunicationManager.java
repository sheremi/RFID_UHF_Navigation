package de.unierlangen.like.serialport;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;

/**
 * CommunicationManager is a Factory, which produces TxChannel and StringPublisher,
 * depending on current connection between phone and reader - Serial port, Bluetooth or Emulator
 * 
 * @author lyavinskova
 * 
 */
public class CommunicationManager {

    public interface IStringPublisher {
        public void register(Handler handler, int what);

        public void unregister(Handler handler);
    }

    static TxChannel mTxChannel;
    static IStringPublisher mStringPublisher;

    private CommunicationManager() {
    };

    public static TxChannel getTxChannel() {
        return mTxChannel;

    }

    public static IStringPublisher getStringPublisher() {
        return mStringPublisher;

    }

    /**
     * Creates hashmaps of txChannels (connection types) and reading threads.
     * Initializes mTxChannel and mStringPublisher.
     * 
     * @param context
     */
    public static void init(Context context) {
        //Creating
        Map<String, TxChannel> txChannels = new HashMap<String, TxChannel>();
        SerialPort serialPort = SerialPort.getSerialPort();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        serialPort.setSharedPreferences(sp);
        // Here will be created and added to the map 2 more objects - BT and Emulator
        txChannels.put("serial", serialPort);

        //Assigns mTxChannel to the current connection type (it's Proxy, nobody has 
        // to know exactly, what is the current connection type).
        ProxyTxChannel proxyTx = new ProxyTxChannel(txChannels);
        mTxChannel = proxyTx;

        Map<String, IStringPublisher> publishers = new HashMap<String, IStringPublisher>();
        IStringPublisher serialPortPublisher = new ReceivingThread(serialPort);
        publishers.put("serial", serialPortPublisher);
        // Here will be created and added to the map 2 more publishers - BT and Emulator threads

        //Assigns mStringPublisher to the current reading thread (it's Proxy, nobody has 
        // to know exactly, what is the current connection type and its reading thread).
        ProxyReceivingThead proxyStringPublisher = new ProxyReceivingThead(publishers);
        mStringPublisher = proxyStringPublisher;

    }
}
