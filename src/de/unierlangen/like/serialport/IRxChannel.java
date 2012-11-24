package de.unierlangen.like.serialport;

import java.io.IOException;

public interface IRxChannel {
    /**
     * 
     * @return
     * @throws IOException
     */
    String readString() throws IOException;
}
