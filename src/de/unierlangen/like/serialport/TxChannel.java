package de.unierlangen.like.serialport;

public interface TxChannel {
    /**
     * 
     * @param string
     */
    void sendString(String stringToSend);
}
