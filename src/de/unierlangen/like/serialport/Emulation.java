package de.unierlangen.like.serialport;

import java.util.ArrayList;
import java.util.Iterator;

import android.os.Handler;
import android.os.Message;

import com.github.androidutils.logger.Logger;

public class Emulation implements ITxChannel, IStringPublisher {
    private final Logger log = Logger.getDefaultLogger();

    private Handler recipientHandler;
    private int msgWhat;
    private final boolean isSimplified;
    private final ArrayList<String> responses;
    private Iterator<String> iterator;

    public Emulation(boolean isSimplified) {
        responses = new ArrayList<String>();
        responses.add(0, "resp,tags,1,BE1,30,\n");
        responses.add(1, "resp,tags,2,BE1,14,BE2,30,\n");
        responses.add(2, "resp,tags,3,BE2,10,BE3,5,BE4,15,\n");
        responses.add(3, "resp,tags,2,BE3,30,BE5,2,\n");
        responses.add(4, "resp,tags,3,BE5,14,BE6,2,BE7,30,\n");
        responses.add(5, "resp,tags,1,BE6,10,\n");
        responses.add(6, "resp,tags,3,BE5,14,BE6,2,BE7,30,\n");
        responses.add(7, "resp,tags,2,BE3,30,BE5,2,\n");
        responses.add(8, "resp,tags,3,BE2,10,BE3,5,BE4,15,\n");
        responses.add(9, "resp,tags,2,BE1,14,BE2,30,\n");
        responses.add(10, "resp,tags,1,BE1,30,\n");
        iterator = responses.iterator();
        this.isSimplified = isSimplified;
        mHandler.sendEmptyMessageDelayed(1, 1700);
        response = iterator.next();
    }

    private String response;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (!iterator.hasNext()) {
                iterator = responses.iterator();
            }
            response = iterator.next();
            sendEmptyMessageDelayed(1, 1700);
        };
    };

    @Override
    public void register(Handler handler, int what) {
        this.recipientHandler = handler;
        this.msgWhat = what;
    }

    @Override
    public void unregister(Handler handler) {
        recipientHandler = null;
    }

    @Override
    public void sendString(String stringToSend) {
        if (recipientHandler != null) {
            Message msg;
            msg = recipientHandler.obtainMessage();
            msg.what = msgWhat;
            String receivedString = "";
            if (stringToSend.contains("preved")) {
                receivedString = "medved";
            } else if (stringToSend.contains("rdr get tags")) {
                if (isSimplified == true) {
                    log.d("isSimplified == true");
                    receivedString = responses.get(0);
                } else {
                    log.d("isSimplified == false");
                    receivedString = response;
                }
            } else if (stringToSend.contains("rdr get regs")) {
                // TODO insert correct strings!
                receivedString = "reg,reg,reg";
            } else {
                receivedString = "error";
            }
            msg.obj = receivedString;
            recipientHandler.sendMessage(msg);
        }
    }
}
