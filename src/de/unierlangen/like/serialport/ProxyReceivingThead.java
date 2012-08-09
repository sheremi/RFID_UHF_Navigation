package de.unierlangen.like.serialport;

import java.util.Map;

import android.os.Handler;
import de.unierlangen.like.serialport.CommunicationManager.IStringPublisher;

/**
 * ProxyReceivingThead is a representative for the receiving threads (StringPublishers), 
 * which are used by current connection type: Serial port, Bluetooth or Emulator.
 * Nobody knows, what is current connection type and current receiving thread. 
 * They use Proxy instead.
 * 
 * @author lyavinskova
 * 
 */
public class ProxyReceivingThead implements IStringPublisher {

    IStringPublisher activeStringPublisher;

    /**
     * 
     * @param publishers - publishers are mapped with their names
     */
    public ProxyReceivingThead(Map<String, IStringPublisher> publishers) {
        // TODO get activePublisherName from preferences. For now it's only SerialPort thread
        String activePublisherName = "serial";
        activeStringPublisher = publishers.get(activePublisherName);
    }

    public void register(Handler handler, int what) {
        activeStringPublisher.register(handler, what);

    }

    public void unregister(Handler handler) {
        activeStringPublisher.unregister(handler);

    }

}
