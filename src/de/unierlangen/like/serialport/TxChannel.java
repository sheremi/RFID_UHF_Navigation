package de.unierlangen.like.serialport;

/**
 * 
 * @author Kate
 *
 */
public interface TxChannel {
    /**
     * 
     * @param string
     */
    void sendString(String stringToSend);
}
