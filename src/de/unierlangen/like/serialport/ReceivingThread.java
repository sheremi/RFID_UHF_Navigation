/**
 * 
 */
package de.unierlangen.like.serialport;

import java.io.IOException;

import android.util.Log;

/**
 * @author Yuriy
 *
 */
public class ReceivingThread extends Thread {
	private static final String TAG = "ReceivingThread";
	private ConsoleThreadListener listener;
	SerialPort serialPort;
	/**
	 * COnstructor
	 * @param sendingThreadListener
	 * @param serialPortToUse
	 */
	public ReceivingThread(ConsoleThreadListener consoleThreadListener, SerialPort serialPortToUse) {
		listener = consoleThreadListener;
		serialPort = serialPortToUse;
	}
	@Override
	public void run() {
		try {
			while (!isInterrupted()){
				String readString = serialPort.readString();
				listener.handleSymbolsReceived(readString);
				Thread.yield();
			}
		} catch (IOException e) {Log.e (TAG, "IOException in SendingThread",e);
		} /*catch (InterruptedException e) {Log.e (TAG, "InterruptedException in SendingThread",e);
		}*/
		super.run();
	}
	
}
