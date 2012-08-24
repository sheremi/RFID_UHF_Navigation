/*
 * Copyright 2011 Yuriy Kulikov
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package de.unierlangen.like.serialport;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.InvalidParameterException;
import android.util.Log;

/** Describes serial port */
public class SerialPort implements RxChannel, TxChannel {

    private static final String TAG = "SerialPort";
    // Serial port parameters
    private static final int baudrate = 9600;
    private static final String path = "/dev/s3c2410_serial2";

    /** mFd is used in native method close() as a reference. */
    private FileDescriptor mFd;
    private ByteBuffer buffer;
    private FileChannel serialInputChannel;
    private FileChannel serialOutputChannel;

    /** Singleton instance */
    private static SerialPort instance;

    /**
     * 
     * @param context
     * @return
     * @throws SecurityException
     * @throws IOException
     * @throws InterruptedException
     */
    public static SerialPort getSerialPort() {
        if (instance == null) {
            instance = new SerialPort(path, baudrate);
        }
        return instance;
    }

    /**
     * Serial port constructor. To receive data use
     * {@link de.unierlangen.like.serialport.SerialPort#setOnStringReceivedListener
     * setOnStringReceivedListener}
     * 
     * @param context
     * @throws InvalidParameterException
     * @throws SecurityException
     * @throws IOException
     */
    private SerialPort(String path, int baudrate) {
        Log.d(TAG, "SerialPort(" + path + ", " + baudrate + ")");
        buffer = ByteBuffer.allocate(64);
        openPort(path, baudrate);
    }

    private void openPort(String path, int baudrate) {
        if (mFd != null) {
            close();
        }
        mFd = open(path, baudrate);
        serialInputChannel = new FileInputStream(mFd).getChannel();
        serialOutputChannel = new FileOutputStream(mFd).getChannel();
    }

    /**
     * Read string from the port. Will block the thread if port is empty.
     * 
     * @return String read from the port
     * @throws IOException
     */
    public String readString() throws IOException {
        /** Here exception could be generated */
        int size = serialInputChannel.read(buffer);
        buffer.flip();
        return new String(buffer.array(), 0, size);
    }
    
    /** Configures and opens serial port */
    private native static FileDescriptor open(String path, int baudrate);

    /** Closes serial port */
    private native void close();

    /** Load library with open() and close() */
    static {
        System.loadLibrary("serial_port");
    }

    /** Writes single string to the serial port */
    public void sendString(String stringToSend) {
        try {
            serialOutputChannel.write(ByteBuffer.wrap(stringToSend.getBytes()));
        } catch (IOException e) {
            Log.d(TAG, "Was not able to write", e);
        }
    }
}
