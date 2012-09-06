package de.unierlangen.like.serialport;

import java.util.HashMap;
import java.util.Map;

import de.unierlangen.like.serialport.SerialPort.SerialPortException;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

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
     * Creates hashmaps of txChannels (connection types) and reading threads (stringPublishers).
     * Initializes mTxChannel and mStringPublisher.
     * 
     * @param context
     */
    public static void init(Context context) {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        //Creating HashMaps
        Map<String, TxChannel> txChannels = new HashMap<String, TxChannel>();
        Map<String, IStringPublisher> publishers = new HashMap<String, IStringPublisher>();

/*        SerialPort serialPort;
        try {
            serialPort = SerialPort.getSerialPort();
            txChannels.put("serial", serialPort);
            IStringPublisher serialPortPublisher = new ReceivingThread(serialPort);
            publishers.put("serial", serialPortPublisher);
        } catch (SerialPortException e) {
            e.printStackTrace();
            Log.e("CommunicationManager", "SerialPort is not created");
        }*/
        

        Emulation emulation = new Emulation(false);
        txChannels.put("emulation", emulation);
        publishers.put("emulation", emulation);

        Emulation emulationSimple = new Emulation(true);
        txChannels.put("emulationSimple", emulationSimple);
        publishers.put("emulationSimple", emulationSimple);
        

        // Here will be created and added one more txChannel to the map - BT

        //Assigns mTxChannel to the current connection type (it's Proxy, nobody has 
        // to know exactly, what the current connection type is).
        ProxyTxChannel proxyTx = new ProxyTxChannel(txChannels, defaultSharedPreferences);
        mTxChannel = proxyTx;

        // Here will be created and added one more publisher to the map - BT

        //Assigns mStringPublisher to the current reading thread (it's Proxy, nobody has 
        // to know exactly, what the current connection type and its reading thread are).
        ProxyReceivingThead proxyStringPublisher = new ProxyReceivingThead(publishers);
        mStringPublisher = proxyStringPublisher;
    }
}