package de.unierlangen.like.rfid;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.os.Handler;
import android.os.Message;

import com.better.wakelock.Logger;

import de.unierlangen.like.serialport.CommunicationManager;
import de.unierlangen.like.serialport.IStringPublisher;
import de.unierlangen.like.serialport.ITxChannel;

public class Reader implements Handler.Callback {

    public interface ReaderClient {
        public void onTagsReceived(ArrayList<GenericTag> readTagsFromReader);

        public void onRegsReceived(Map<String, String> registers);

        public void onReaderStatus(ReaderStatus status);
    }

    private static final String TAG = "Reader";

    public static final int EVENT_TAGS = 1;
    public static final int RESPONSE_TAGS = 2;
    public static final int RESPONSE_REGS = 3;
    public static final int ERROR = -1;
    public static final int WARNING = -2;
    private static final int EVENT_STRING_RECEIVED = 1;

    private final ITxChannel txChannel;
    private final IStringPublisher stringPublisher;
    private int amountOfTags;

    private final ReaderClient readerClient;

    /**
     * This handler is the internal Handler of the reader. It uses
     * {@link ReaderService#handleMessage(Message)} as a message handling
     * strategy.
     */
    private final Handler mHandler = new Handler(this);

    @Override
    public boolean handleMessage(Message msg) {
        Logger.d("handleMessage(" + msg.what + ")");
        switch (msg.what) {
        case EVENT_STRING_RECEIVED:
            analyzeResponse((String) msg.obj);
            return true;

        default:
            Logger.d("unknown data");
            return false;
        }
    }

    public static enum Configuration {
        DEFAULT, LOW_POWER, HIGH_POWER
    };

    public static enum ReaderStatus {
        PLL_FAIL, OSC_FAIL, ALL_ZEROS, ALL_FFS, SOMETHING_ELSE, UNINTIALZED, ALL_GOOD
    };

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
    public Reader(ReaderClient readerClient) {
        this.readerClient = readerClient;
        txChannel = CommunicationManager.getTxChannel();
        stringPublisher = CommunicationManager.getStringPublisher();
        stringPublisher.register(mHandler, EVENT_STRING_RECEIVED);
        shakeHands();
    }

    public void shakeHands() {
        txChannel.sendString("preved\n");
    }

    public void initialize(Configuration configuration) {
        // TODO choose configuration from enum
        switch (configuration) {
        case LOW_POWER:
        case HIGH_POWER:
        case DEFAULT:
        default:
            // FIXME what's "a"?
            txChannel.sendString("rdr init\n");
        }

    }

    public void performRound() {
        // Tell reader MCU to start inventory round
        txChannel.sendString("rdr get tags\n");
    }

    public void displayRegisters() {
        txChannel.sendString("rdr get regs\n");
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
    private void analyzeResponse(String response) {
        Logger.d("response = " + response);
        try {

            List<String> strings = breakDown(response, ",");

            if (strings.size() > 1 && strings.get(1).contains("tags")) {
                ArrayList<GenericTag> tags = analyzeTagsString(strings);
                readerClient.onTagsReceived(tags);

            } else if (strings.size() > 1 && strings.get(1).contains("regs")) {
                Map<String, String> regs = analyzeRegsString(strings);
                readerClient.onRegsReceived(regs);

            } else if (strings.size() > 0 && strings.get(0).contains("error")) {
                readerClient.onReaderStatus(ReaderStatus.SOMETHING_ELSE);

            }
        } catch (IndexOutOfBoundsException e) {
            readerClient.onReaderStatus(ReaderStatus.SOMETHING_ELSE);
        }

    }

    private List<String> breakDown(String what, String withWhat) {
        List<String> strings = Arrays.asList(what.split(withWhat));
        if (strings.size() > 1) {
            strings = strings.subList(0, strings.size() - 1);
        }
        return strings;
    }

    /**
     * Analyzes the data about tags, which were read by the reader
     * 
     * @return tags - ArrayList of GenericTags
     */
    private ArrayList<GenericTag> analyzeTagsString(List<String> strings) {
        Iterator<String> iterator = strings.iterator();
        // skip two first words
        iterator.next();
        iterator.next();
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
    private Map<String, String> analyzeRegsString(List<String> strings) {
        // string has a format like this:
        // resp,regs,0:4, 1:6, 2:70, 3:60, 4:35, 5:5, 6:0, 7:7, 8:7, 9:41, a:5,
        // b:2,
        // c:0, d:37, e:1, f:0, 10:0, 11:0,
        // 0,0,0,38,4fd840,46181,f39f0,03f20,000,
        // and we get a list of comma separated values
        // we have to build a map
        Map<String, String> regs = new HashMap<String, String>();
        // first fill in first 17 regs
        List<String> first17registers = strings.subList(2, 19);
        for (String string : first17registers) {
            List<String> keyValue = Arrays.asList(string.split(":"));
            regs.put(keyValue.get(0).trim(), keyValue.get(1).trim());
        }

        for (Entry<String, String> entry : regs.entrySet()) {
            Logger.d(entry.getKey() + " - " + entry.getValue());
        }

        if ("6".equals(regs.get("1"))) {
            readerClient.onReaderStatus(ReaderStatus.ALL_GOOD);
        } else if ("0".equals(regs.get("1"))) {
            readerClient.onReaderStatus(ReaderStatus.ALL_ZEROS);
        } else if ("ff".equals(regs.get("1"))) {
            readerClient.onReaderStatus(ReaderStatus.ALL_FFS);
        }

        return regs;
    }
}
