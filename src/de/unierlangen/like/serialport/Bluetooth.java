package de.unierlangen.like.serialport;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.UUID;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

public class Bluetooth implements ITxChannel, IStringPublisher {
    private static final String TAG = "Bluetooth";
    private BluetoothSocket mSocket;
    private BluetoothAdapter mBluetoothAdapter;
    private Context mContext;
    private Handler mHandler;
    private int mWhat;

    private ReadingThread mReadingThread;

    private class BtRxChannel implements IRxChannel {
        private final BluetoothSocket mSocket;

        public BtRxChannel(BluetoothSocket socket) {
            mSocket = socket;
        }

        public String readString() throws IOException {
            String receivedString = "";
            Integer receivedByte = mSocket.getInputStream().read();
            if (receivedByte != -1) {
                byte[] bytes = ByteBuffer.allocate(4).putInt(receivedByte).array();
                receivedString = bytes.toString();
            } else {
                throw new RuntimeException(TAG + " - end of the InputStream has been reached");
            }
            return receivedString;
        }
    };

    // Create a BroadcastReceiver for ACTION_FOUND to handle "paired" events
    // this is required for connecting to devices which were paired after the
    // application started.
    private final BroadcastReceiver deviceFoundIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            mContext = context;
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getName().contains("RFID")) {
                    new OpenSocketTask().execute(device);
                }
            }
        }
    };

    /**
     * Opens an output stream using a given BluetoothDevice.
     */
    private final class OpenSocketTask extends AsyncTask<BluetoothDevice, Void, BluetoothSocket> {
        private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        private BluetoothDevice mDevice;

        /**
         * This is executed on a separate thread
         */
        @Override
        protected BluetoothSocket doInBackground(BluetoothDevice... device) {
            // Store the reference to the device for future use (e.g. for
            // logging)
            mDevice = device[0];

            Log.d(TAG, "Opening socket from " + mDevice.getName() + " - " + mDevice.getBondState());

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server
                // code
                mSocket = mDevice.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {

            }

            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mSocket.close();
                } catch (IOException closeException) {
                    Log.d(TAG, "was not able to close", closeException);
                }
            }

            return mSocket;
        }

        /**
         * This is executed on the main thread when the task is finished
         */
        @Override
        protected void onPostExecute(BluetoothSocket result) {
            mSocket = result;
            // if socket was successfully opened start reading thread
            // if we already have registered Handler
            // register it in the new thread
            if (mSocket != null) {
                IRxChannel rxChannel = new BtRxChannel(mSocket);
                mReadingThread = new ReadingThread(rxChannel);
                mReadingThread.start();
                if (mHandler != null) {
                    mReadingThread.register(mHandler, mWhat);
                }
            }
        }
    }

    public Bluetooth(Context context) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mContext = context;

        if (mBluetoothAdapter == null) {
            // TODO Device does not support Bluetooth
        }

        // If there are paired devices
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().contains("RFID")) {
                    new OpenSocketTask().execute(device);
                }
            }
        }

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        mContext.registerReceiver(deviceFoundIntentReceiver, filter);
    }

    public void sendString(String stringToSend) {
        if (mSocket != null) {
            try {
                mSocket.getOutputStream().write(stringToSend.getBytes());
            } catch (IOException e) {
                Log.d(TAG, "Was not able to write!", e);
                mSocket = null;
            }
        } else {
            Log.d(TAG, "No socket is present!");
        }
    }

    public void register(Handler handler, int what) {
        // Register handler in our active reading thread.
        // Store the Handler for future use in case we have to start a new
        // reading thread, e.g. when BT is disconnected and connected again
        mHandler = handler;
        mWhat = what;
        if (mReadingThread != null) {
            mReadingThread.register(handler, what);
        }

    }

    public void unregister(Handler handler) {
        if (mReadingThread != null) {
            mReadingThread.unregister(handler);
        }
        mHandler = null;
    }

}
