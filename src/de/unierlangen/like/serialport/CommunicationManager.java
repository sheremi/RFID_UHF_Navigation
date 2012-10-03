package de.unierlangen.like.serialport;

import java.util.HashMap;
import java.util.Map;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;

/**
 * CommunicationManager is a Factory, which produces TxChannel and
 * StringPublisher, depending on current connection between phone and reader -
 * Serial port, Bluetooth or Emulator
 * 
 * @author lyavinskova
 * 
 */
public class CommunicationManager {

    /**
     * This interface is implemented by classes, which are reading strings from
     * some source (socket, uart, textview, etc.). Such class first concatenates
     * a string, then put it to a message to be held by handler, which is passed
     * as a parameter to the method register(Handler handler, int what).
     * 
     * @author Kate
     * 
     */
    public interface IStringPublisher {
        /**
         * Register a {@link Handler} to receive messages containing published
         * strings when this strings are read by {@link IStringPublisher}.
         * 
         * @param handler
         *            - a client {@link Handler} which will receive messages
         * @param what
         *            - desired message code. Message will have this code as
         *            {@link Message#what}
         */
        public void register(Handler handler, int what);

        /**
         * 
         * @param handler
         */
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
     * Creates hashmaps of txChannels (connection types) and reading threads
     * (stringPublishers). Initializes mTxChannel and mStringPublisher.
     * 
     * @param context
     */
    public static void init(Context context) {
        SharedPreferences defaultSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        // Creating HashMaps
        Map<String, TxChannel> txChannels = new HashMap<String, TxChannel>();
        Map<String, IStringPublisher> publishers = new HashMap<String, IStringPublisher>();

        /*
         * SerialPort serialPort; try { serialPort = SerialPort.getSerialPort();
         * txChannels.put("serial", serialPort); IStringPublisher
         * serialPortPublisher = new ReceivingThread(serialPort);
         * publishers.put("serial", serialPortPublisher); } catch
         * (SerialPortException e) { e.printStackTrace();
         * Log.e("CommunicationManager", "SerialPort is not created"); }
         */

        Emulation emulation = new Emulation(false);
        txChannels.put("emulation", emulation);
        publishers.put("emulation", emulation);

        Emulation emulationSimple = new Emulation(true);
        txChannels.put("emulationSimple", emulationSimple);
        publishers.put("emulationSimple", emulationSimple);

        // Here will be created and added one more txChannel to the map - BT

        // Assigns mTxChannel to the current connection type (it's Proxy, nobody
        // has
        // to know exactly, what the current connection type is).
        ProxyTxChannel proxyTx = new ProxyTxChannel(txChannels, defaultSharedPreferences);
        mTxChannel = proxyTx;

        // Here will be created and added one more publisher to the map - BT

        // Assigns mStringPublisher to the current reading thread (it's Proxy,
        // nobody has
        // to know exactly, what the current connection type and its reading
        // thread are).
        ProxyReceivingThead proxyStringPublisher = new ProxyReceivingThead(publishers);
        mStringPublisher = proxyStringPublisher;
    }
}