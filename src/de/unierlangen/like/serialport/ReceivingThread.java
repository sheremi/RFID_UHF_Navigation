package de.unierlangen.like.serialport;

import java.io.IOException;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ReceivingThread extends Thread {
    private static final String TAG = "ReceivingThread";
    private static final boolean DBG = true;

    private RxChannel rxChannel;
    private Handler recipientHandler;
    private int msgWhat;

    /**
     * 
     * @param rxChannel
     * @param handler handler to which thread will send messages
     * @param msgWhat user-defined message code so that the recipient can identify what this message is about
     */
    public ReceivingThread(RxChannel rxChannel, Handler handler, int msgWhat) {
        super();
        this.msgWhat = msgWhat;
        this.rxChannel = rxChannel;
        this.recipientHandler = handler;
    }

    @Override
    public void run() {
        try {
            String receivedString = "";
            while (!isInterrupted()) {
                String receivedStringSymbol = rxChannel.readString();
                receivedString = receivedString.concat(receivedStringSymbol);
                if (receivedString.contains("\n")) {
                    if (DBG) Log.d(TAG, "Received string: " + receivedString);
                    Message msg = recipientHandler.obtainMessage();
                    msg.what = msgWhat;
                    msg.obj = receivedString;
                    recipientHandler.sendMessage(msg);
                    receivedString = "";
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException in SendingThread", e);
        }
        super.run();
    }
}
