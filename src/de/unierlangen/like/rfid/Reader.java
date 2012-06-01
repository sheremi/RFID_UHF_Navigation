package de.unierlangen.like.rfid;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import de.unierlangen.like.serialport.ReceivingThread;
import de.unierlangen.like.serialport.SerialPort;

public class Reader /*extends Service*/ {

    private static final String TAG = "Reader";
    private static final boolean DBG = true;
    public static final int EVENT_TAGS = 1;
    public static final int RESPONSE_TAGS = 2;
    public static final int RESPONSE_REGS = 3;
    public static final int ERROR = -1;
    public static final int WARNING = -2;
    private static final int EVENT_STRING_RECEIVED = 1;

    private SerialPort readerSerialPort;
    private int amountOfTags;

    private Handler registrantHandler;
    private ReceivingThread mReceivingThread;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (DBG) Log.d(TAG, "handleMessage(" + msg.what + ")");
            switch (msg.what) {
            case EVENT_STRING_RECEIVED:
                Message message = analyzeResponse((String) msg.obj);
                if (registrantHandler != null) {
                    registrantHandler.sendMessage(message);
                }
                break;

            default:
                Log.d(TAG, "unknown data");
                break;
            }

        }
    };

    public static enum Configuration {
        DEFAULT, LOW_POWER, HIGH_POWER
    };

    public class ReaderException extends Exception {
        public ReaderException(String string) {
            super(string);
        }

        private static final long serialVersionUID = 1L;
    }

    /**
     * Constructor opens serial port and performs handshake
     * 
     * @param sp
     * @throws InvalidParameterException
     * @throws SecurityException
     * @throws IOException
     * @throws InterruptedException
     * @throws ReaderException
     */
    public Reader(SerialPort serialPort, Handler handler) {
        readerSerialPort = serialPort;
        this.registrantHandler = handler;
        mReceivingThread = new ReceivingThread(serialPort, mHandler, EVENT_STRING_RECEIVED);
        mReceivingThread.start();
        readerSerialPort.sendString("preved");
    }

    public void initialize(Configuration configuration) throws IOException {
        // FIXME choose configuration from enum
        switch (configuration) {
        case LOW_POWER:
            break;
        case HIGH_POWER:
            break;
        case DEFAULT:
        default:
            readerSerialPort.sendString("a");
        }

    }

    public void performRound() {
        // Tell reader MCU to start inventory round
        readerSerialPort.sendString("rdr get tags");
    }

    public void displayRegisters() throws IOException {
        readerSerialPort.sendString("rdr get regs");
    }

    public int getAmountOfTags() {
        return amountOfTags;
    }

    /**
     * Recognizes the string (answer), which was received from the reader after
     * performing an inventory round. Then packs the recognized data to the
     * message and assigns a topic to it.
     * 
     * @param response the answer (string) from reader.
     * @return message to be handled by a Handler.
     * @throws ReaderException
     */
    private Message analyzeResponse(String response) {
        Message msg = Message.obtain();
        if (DBG) Log.d(TAG, "response = " + response);
        List<String> strings = Arrays.asList(response.split(","));
        if (strings.size() > 1) {
            strings = strings.subList(0, strings.size() - 1);
        }

        Iterator<String> iterator = strings.iterator();
        if (strings.get(0).contains("resp")) {
            if (strings.get(1).contains("tags")) {
                msg.what = RESPONSE_TAGS;
                iterator.next();
                iterator.next();
                msg.obj = analyzeTagsString(iterator);
            } else if (strings.get(1).contains("regs")) {
                msg.what = RESPONSE_REGS;
                iterator.next();
                iterator.next();
                msg.obj = analyzeRegsString(iterator);
            }
        } else if (strings.get(0).contains("evnt")) {
            if (strings.get(1).contains("tags")) {
                msg.what = EVENT_TAGS;
                iterator.next();
                iterator.next();
                msg.obj = analyzeTagsString(iterator);
            }
        } else if (strings.get(0).contains("error")) {
            msg.what = WARNING;
            msg.obj = new ReaderException("Reader MCU reported error:" + response);
        } else {
            msg.what = WARNING;
            msg.obj = new ReaderException("Unexpected data: " + response);
        }
        return msg;
    }

    /**
     * Analyzes the data about tags, which were read by the reader
     * 
     * @return tags - ArrayList of GenericTags
     */
    private ArrayList<GenericTag> analyzeTagsString(Iterator<String> iterator) {
        // TODO optimize arraylist creation with
        // ArrayList<GenericTag>(howmanytags)
        ArrayList<GenericTag> tags = new ArrayList<GenericTag>();
        // Skip third member - amount of tags
        iterator.next();
        while (iterator.hasNext()) {
            tags.add(new GenericTag(iterator.next(), Integer.parseInt(iterator.next()), true));
        }
        // TODO use amountOfTags where it should be used
        amountOfTags = tags.size();
        if (DBG) Log.d(TAG, "amountOfTags = " + amountOfTags);
        return tags;
    }

    /**
     * Analyzes the data, which is contained in the reader's registers
     * 
     * @return
     */
    private ArrayList<String> analyzeRegsString(Iterator<String> iterator) {
        // TODO optimize arraylist creation with
        // ArrayList<GenericTag>(howmanytags)
        // Skip third member - amount of regs
        iterator.next();
        ArrayList<String> regs = new ArrayList<String>();
        // TODO implement analyzeRegsString
        while (iterator.hasNext()) {
            regs.add(iterator.next());
        }
        return regs;
    }
}
