package de.unierlangen.like.serialport;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import de.unierlangen.like.navigation.RoomsDatabase;
import de.unierlangen.like.serialport.CommunicationManager.IStringPublisher;
import de.unierlangen.like.ui.FindRoomActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

public class Bluetooth implements TxChannel, IStringPublisher
 {

    //private static final String REQUEST_ENABLE_BT = 1;
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
    private List<String> mArrayAdapter;
    BluetoothServerSocket mBluetoothServerSocket;


    public void sendString(String stringToSend) {
/*        if (mBluetoothAdapter.isEnabled()) {
            boolean foundPairedDevice = false;
            // If we are enabled, maybe one of devices is already paired
            for (BluetoothDevice device : mBluetoothAdapter.getBondedDevices()) {
                if (isDeviceSuitable(device)) {
                    connectSink(device);
                    foundPairedDevice = true;
                    break;
                }
            }
            if (!foundPairedDevice) {
                // lets start discovery, maybe something comes up
                mBluetoothAdapter.startDiscovery();
            }
        } else {
            // turn adapter on and wait for the intent
            mBluetoothAdapter.enable();
        }
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                if (device.getName().contains("RFID reader")){
                    
                }
            }
        }*/
    }

/*    public String readString() throws IOException {
        // TODO Auto-generated method stub
        return null;
        //mBluetoothAdapter.listenUsingRfcommWithServiceRecord(name, uuid);
    }*/

    public void register(Handler handler, int what) {
        
    }

    public void unregister(Handler handler) {
        
    }

}
