package de.unierlangen.like.rfid;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import android.os.Handler;
import android.os.Message;
import com.better.wakelock.Logger;
import de.unierlangen.like.serialport.CommunicationManager;
import de.unierlangen.like.serialport.IStringPublisher;
import de.unierlangen.like.serialport.ITxChannel;

public class Reader /* extends Service */{

    private static final String TAG = "Reader";
    
    public static final int EVENT_TAGS = 1;
    public static final int RESPONSE_TAGS = 2;
    public static final int RESPONSE_REGS = 3;
    public static final int ERROR = -1;
    public static final int WARNING = -2;
    private static final int EVENT_STRING_RECEIVED = 1;

    private ITxChannel txChannel;
    private IStringPublisher stringPublisher;
    private int amountOfTags;

    /**
     * FIXME write comment!!!
     */
    private Handler registrantHandler;

    /**
     * FIXME write comment!!!
     */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Logger.d("handleMessage(" + msg.what + ")");
            switch (msg.what) {
            case EVENT_STRING_RECEIVED:
                Message message = analyzeResponse((String) msg.obj);
                if (registrantHandler != null) {
                    registrantHandler.sendMessage(message);
                }
                break;

            default:
                Logger.d("unknown data");
                break;
            }

        }
    };

    public static enum Configuration {
        DEFAULT, LOW_POWER, HIGH_POWER
    };

    public class ReaderException extends Exception {
        private String string;
        public ReaderException(String string) {
            super(string);
            this.string = string;
        }
        
        public String getString() {
            return string;
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
    public Reader(Handler handler) {
        this.registrantHandler = handler;
        txChannel = CommunicationManager.getTxChannel();
        stringPublisher = CommunicationManager.getStringPublisher();
        stringPublisher.register(mHandler, EVENT_STRING_RECEIVED);
        txChannel.sendString("preved");
    }

    public void initialize(Configuration configuration) throws IOException {
        // TODO choose configuration from enum
        switch (configuration) {
        case LOW_POWER:
            break;
        case HIGH_POWER:
            break;
        case DEFAULT:
        default:
            // FIXME what's "a"?
            txChannel.sendString("a");
        }

    }

    public void performRound() {
        // Tell reader MCU to start inventory round
        txChannel.sendString("rdr get tags");
    }

    public void displayRegisters() throws IOException {
        txChannel.sendString("rdr get regs");
    }

    public int getAmountOfTags() {
        return amountOfTags;
    }

    /**
     * Recognizes the string (answer), which was received from the reader after
     * performing an inventory round. Then packs the recognized data to the
     * message and assigns a topic to it.
     * 
     * @param response
     *            the answer (string) from reader.
     * @return message to be handled by a Handler.
     * @throws ReaderException
     */
    private Message analyzeResponse(String response) {
        Message msg = Message.obtain();
        Logger.d("response = " + response);
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
            tags.add(new GenericTag(iterator.next(), /*
                                                      * Integer.parseInt(iterator
                                                      * .next())
                                                      */0, true));
            iterator.next();
        }
        // TODO use amountOfTags where it should be used
        amountOfTags = tags.size();
        Logger.d("amountOfTags = " + amountOfTags);
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
