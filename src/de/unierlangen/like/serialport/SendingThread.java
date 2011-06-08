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
public class SendingThread extends Thread {
	private static final String TAG = "SendingThread";
	private ConsoleThreadListener listener;
	SerialPort serialPort;
	/**
	 * COnstructor
	 * @param consoleThreadListener
	 * @param serialPortToUse
	 */
	public SendingThread(ConsoleThreadListener consoleThreadListener, SerialPort serialPortToUse) {
		listener = consoleThreadListener;
		serialPort = serialPortToUse;
	}
	@Override
	public void run() {
	
		String loopbackString = "Hi!";
		try {
			while (!isInterrupted()){
				serialPort.writeString(loopbackString);
				listener.handleSymbolsSent(loopbackString);
				Thread.sleep(1000);
			}
		} catch (IOException e) {Log.e (TAG, "IOException in SendingThread",e);
		} catch (InterruptedException e) {Log.e (TAG, "InterruptedException in SendingThread",e);
		}
		super.run();
	}
	
	
}
