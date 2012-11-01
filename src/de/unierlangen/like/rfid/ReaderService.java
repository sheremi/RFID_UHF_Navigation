package de.unierlangen.like.rfid;

import java.util.ArrayList;
import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import de.unierlangen.like.rfid.Reader.ReaderException;

public class ReaderService extends IntentService implements Handler.Callback {

    private static final String TAG = "ReaderService";
    public static final int THREAD_EVENT_READ_TAGS = 4;
    public static final int EVENT_TAGS = 1;
    private static final int READ_TAGS_INTERVAL = 3000;

    private Reader reader;

    private Handler handler;

    public ReaderService() {
        super("ReaderService");
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate() {
        handler = new Handler(this);
        reader = new Reader(handler);
        Log.d(TAG, "Reader and serial port were created succesfully");

        handler.sendEmptyMessage(THREAD_EVENT_READ_TAGS);

        super.onCreate();
    }

    // Receives the intent for each start request, so we can do the background
    // work
    @Override
    protected void onHandleIntent(Intent intent) {
        if (ReaderIntents.ACTION_READ_TAGS.equals(intent.getAction())) {
            // TODO
        }
    }

    @SuppressWarnings("unchecked")
    public boolean handleMessage(Message msg) {
     // Log.d (TAG, "handleMessage(" + msg.what + ")");
        switch (msg.what) {
        case Reader.RESPONSE_TAGS:
        case Reader.EVENT_TAGS:
            ArrayList<GenericTag> readTagsFromReader = new ArrayList<GenericTag>();
            readTagsFromReader = (ArrayList<GenericTag>) msg.obj;
            if (!readTagsFromReader.isEmpty()) {
                Intent intent =  new Intent(ReaderIntents.ACTION_TAGS);
                intent.putParcelableArrayListExtra(ReaderIntents.EXTRA_TAGS, readTagsFromReader);
                sendBroadcast(intent);
            }
            break;
        case Reader.RESPONSE_REGS:
            // TODO implement analysis of RESPONSE_REGS
            break;
        case Reader.WARNING:
            ReaderException e = (ReaderException) msg.obj;
            Log.d(TAG, e.getString());
            break;
        case THREAD_EVENT_READ_TAGS:
            reader.performRound();
            handler.sendEmptyMessageDelayed(THREAD_EVENT_READ_TAGS, READ_TAGS_INTERVAL);
            break;
        default:
            break;
        }
        return true;
    }

}
