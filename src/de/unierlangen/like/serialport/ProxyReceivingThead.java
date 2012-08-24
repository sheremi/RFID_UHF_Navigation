package de.unierlangen.like.serialport;

import java.util.Map;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Handler;
import android.util.Log;
import de.unierlangen.like.serialport.CommunicationManager.IStringPublisher;

/**
 * ProxyReceivingThead is a representative for the receiving threads
 * (StringPublishers), which are used by current connection type: Serial port,
 * Bluetooth or Emulator. Nobody knows, what is current connection type and
 * current receiving thread. They use Proxy instead.
 * 
 * @author lyavinskova
 * 
 */
public class ProxyReceivingThead implements IStringPublisher, OnSharedPreferenceChangeListener {

    private static final String COMM_TYPE = "COMM_TYPE";
    private IStringPublisher activeStringPublisher;

    /**
     * 
     * @param publishers
     *            - publishers are mapped with their names
     * @param sharedPreferences
     *            TODO
     */
    public ProxyReceivingThead(Map<String, IStringPublisher> publishers, SharedPreferences sp) {
        String activePublisherName = sp.getString(COMM_TYPE, "emulation");
        activeStringPublisher = publishers.get(activePublisherName);
        sp.registerOnSharedPreferenceChangeListener(this);
    }

    public void register(Handler handler, int what) {
        activeStringPublisher.register(handler, what);

    }

    public void unregister(Handler handler) {
        activeStringPublisher.unregister(handler);

    }

    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
        if (key.equals(COMM_TYPE)) {
            String activePublisherName = sp.getString(COMM_TYPE, "emulation");
            Log.d("ProxyReceivingThead", "activePublisherName = " + activePublisherName);
        }
    }
}
