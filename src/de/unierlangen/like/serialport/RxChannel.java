package de.unierlangen.like.serialport;

import java.io.IOException;

public interface RxChannel {
    /**
     * 
     * @return
     * @throws IOException 
     */
    String readString() throws IOException;
}
