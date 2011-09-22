/*
 * Copyright 2011 Yuriy Kulikov
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package de.unierlangen.like.serialport;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.InvalidParameterException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;

/** Describes serial port */
public class SerialPort implements OnSharedPreferenceChangeListener {

	private static final String TAG = "SerialPort";

	/** mFd is used in native method close() as a reference. */
	private FileDescriptor mFd;
	ByteBuffer buffer;
	FileChannel serialInputChannel;
	FileChannel serialOutputChannel;
	
	private ReceivingThread receivingThread;
	private OnStringReceivedListener onStringReceivedListener;

	private static Context mContext;

	private static SerialPort instance;
	
	/** Set interface to handle symbols received by serial port */
	public void setOnStringReceivedListener(OnStringReceivedListener onStringReceivedListener) {
		this.onStringReceivedListener = onStringReceivedListener;
		if (receivingThread==null){
			receivingThread = new ReceivingThread();
			receivingThread.start();
		}
	}
	
	/** Thread reads data from port and calls onStringReceived from 
	 * given {@link de.unierlangen.like.serialport.OnStringReceivedListener OnStringReceivedListener}
	 */
	private class ReceivingThread extends Thread {
		private static final String TAG = "ReceivingThread";
		@Override
		public void run() {
			try {
				while (!isInterrupted()){
					String readString = readString();
					onStringReceivedListener.onStringReceived(readString);
					Thread.yield();
				}
			} catch (IOException e) {
				Log.e (TAG, "IOException in SendingThread",e);
			} /*catch (InterruptedException e) {Log.e (TAG, "InterruptedException in SendingThread",e);
			}*/
			super.run();
		}
	}
	/**
	 * Serial port constructor. To receive data use {@link de.unierlangen.like.serialport.SerialPort#setOnStringReceivedListener setOnStringReceivedListener}
	 * @param context
	 * @throws InvalidParameterException
	 * @throws SecurityException
	 * @throws IOException
	 */
	private SerialPort(Context context) throws InvalidParameterException, SecurityException, IOException {
		
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		String path = sharedPreferences.getString("DEVICE", "");
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
		int baudrate = Integer.decode(sharedPreferences.getString("BAUDRATE", "-1"));
		
		/** Check parameters */
		if ( (path.length() == 0) || (baudrate == -1)) {
			throw new InvalidParameterException();
		}
	
		/** Use native opener which has some specific flags, see C code */
		mFd = open(path, baudrate);
		if (mFd == null) {
			Log.e(TAG, "native open returns null");
			throw new IOException();
		}
		
		buffer = ByteBuffer.allocate(64);
		Log.d(TAG,"Allocated buffer of " + buffer.capacity()+ " bytes" );
		
		/** Create input channel to read from Java */
		serialInputChannel = new FileInputStream(mFd).getChannel();
		/** Create output channel to write from Java */
		FileOutputStream serialOutputStream = new FileOutputStream(mFd);
		serialOutputChannel = serialOutputStream.getChannel();
	}
	
	public static SerialPort getSerialPort(Context context) throws InvalidParameterException, SecurityException, IOException, InterruptedException{
		if (instance==null){
			instance = new SerialPort(context);
		}
		mContext=context;
		return instance;
	}
	
	/** Writes single string from serial port */
	public void writeString (String stringToWrite) throws IOException {
		serialOutputChannel.write(ByteBuffer.wrap(stringToWrite.getBytes()));
	}
	/** Private, because can never return. Use {@link setOnStringReceivedListener} 
	 * to receive */
	private String readString () throws IOException{
		/** Here exception could be generated */
		int size = serialInputChannel.read(buffer);
	
		buffer.flip();
		return new String(buffer.array(),0,size);
	}

	/** Configures and opens serial port */
	private native static FileDescriptor open(String path, int baudrate);
	/** Closes serial port */
	private native void close();
	/** Load library with open() and close() */
	static {
		System.loadLibrary("serial_port");
	}
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals("baudrate")||key.equals("path")){
			try {
				instance = new SerialPort(mContext);
			} catch (IOException e) {
				// TODO make a toast
				Log.d(TAG, "Settings are not correct");
			}
		}
	}
}

