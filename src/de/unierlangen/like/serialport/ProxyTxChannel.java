package de.unierlangen.like.serialport;

import java.util.Map;

/**
 * ProxyTxChannel is a representative for the connection type Tx 
 * (Serial port, Bluetooth or Emulator). 
 * Nobody knows, what is current connection type. 
 * They use Proxy instead.
 * 
 * @author lyavinskova
 * 
 */
public class ProxyTxChannel implements TxChannel {

    TxChannel activeTxChannel;

    /**
     * User can choose a connection type in preferences.
     * Proxy becomes chosen connection type (TxChannel).
     * @param txChannels
     *            txChannels are mapped to their names
     */
    public ProxyTxChannel(Map<String, TxChannel> txChannels) {
        // TODO get activeRxChannelName from preferences. For now it's only SerialPort
        String activeRxChannelName = "serial";
        activeTxChannel = txChannels.get(activeRxChannelName);
    }

    public void sendString(String stringToSend) {
        activeTxChannel.sendString(stringToSend);
    }
}