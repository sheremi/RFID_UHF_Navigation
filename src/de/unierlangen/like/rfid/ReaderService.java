package de.unierlangen.like.rfid;

import java.util.ArrayList;

import de.unierlangen.like.navigation.Tag;
import de.unierlangen.like.rfid.Reader.ReaderException;
import de.unierlangen.like.ui.MainYourLocationActivity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class ReaderService extends Service {
    public static final int THREAD_EVENT_READ_TAGS = 4;
    private static final int READ_TAGS_INTERVAL = 3000;

    private Reader reader;

    private Handler handler = new Handler() {
        @SuppressWarnings("unchecked")
        @Override
        public void handleMessage(Message msg) {
            // Log.d (TAG, "handleMessage(" + msg.what + ")");
            switch (msg.what) {
            case Reader.RESPONSE_TAGS:
            case Reader.EVENT_TAGS:
                // TODO do something with tags we have got!
                ArrayList<GenericTag> readTagsFromReader = new ArrayList<GenericTag>();
                if (!readTagsFromReader.isEmpty()) {

                }
                break;
            case Reader.RESPONSE_REGS:
                // TODO implement analysis of RESPONSE_REGS
                break;
            case Reader.WARNING:
                ReaderException e = (ReaderException) msg.obj;
                // FIXME revert this commit later when do not send warnings all
                // the time.
                // Toast.makeText(getApplicationContext(),"Warning: " +
                // e.getMessage(), Toast.LENGTH_LONG).show();
                break;
            case Reader.ERROR:
                ReaderException e1 = (ReaderException) msg.obj;
                Log.d(TAG, "Reader repoted error", e1);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainYourLocationActivity.this);
                builder.setTitle("Achtung!");
                builder.setMessage("Oops! " + "The reader is missing or connection is wrong. "
                        + "Check the connection between phone and reader. "
                        + "Do you wanna try to communicate with reader again?");
                builder.setNegativeButton("No, thanks", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        getApplication().stopService(getIntent());
                    }
                });
                builder.setPositiveButton("Go ahead", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        startActivity(new Intent(getApplicationContext(),
                                MainYourLocationActivity.class));
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
                return;
            case THREAD_EVENT_READ_TAGS:
                reader.performRound();
                sendMessageDelayed(obtainMessage(THREAD_EVENT_READ_TAGS), READ_TAGS_INTERVAL);
                break;
            default:
                break;
            }
        };
    };

    @Override
    public void onCreate() {

        reader = new Reader(handler);
        Log.d(TAG, "Reader and serial port were created succesfully");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ReaderIntents.ACTION_READ_TAGS.equals(intent.getAction())) {
            // TODO here we read tags
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // it is not used, but we have to implement this method
        return null;
    }

}
