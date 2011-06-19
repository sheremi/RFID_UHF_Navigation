/*
 * Copyright 2011 Yuriy Kulikov
 * FAU LIKE
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

package de.unierlangen.like.ui;

import java.io.IOException;
import java.security.InvalidParameterException;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import de.unierlangen.like.serialport.OnStringReceivedListener;
import de.unierlangen.like.serialport.SerialPort;
/**
 * 
 * @author Ekaterina Lyavinskova and Yuriy Kulikov
 *
 */
public class ConsoleActivity extends OptionsMenuActivity
	implements OnClickListener, OnEditorActionListener, OnCheckedChangeListener, OnStringReceivedListener, OnLongClickListener {
	
	/** Fields */
	private static final String TAG = "ConsoleActivity";
	private SerialPort serialPort;
	/** Symbol counters */
	private Integer incoming;
	private Integer outgoing;
	/** Views */	
	private TextView textViewOutgoing;
	private TextView textViewIncoming;
	private TextView textViewReception;
	private Button buttonSend;
	private EditText editTextEmission;
	private CheckBox checkBoxSendingThread;
	/** Concurrency - AsyncTasks, Threads and Handlers*/
	Handler handler;
	private SendingThread sendingThread;
	/** Interfaces */
	
	/** This method could be called from another thread.
	* Use the handler to make sure the code runs in the UI thread*/
	public void handleSymbolsSent(String sentString) {
		outgoing+=sentString.length();
		handler.post(new Runnable() {
			public void run() {
				textViewOutgoing.setText(outgoing.toString());
			}
		});
	}

	public void onClick(View v) {

		// XXX((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(100);
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		String messageString = sp.getString("GREETING", "");
		try {
			serialPort.writeString(messageString);
		} catch (IOException e) {
			Log.d(TAG,"Opps",e);
		}

	}
	
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		try {
			serialPort.writeString(v.getText().toString());//+"\n");
			outgoing+=v.getText().length();
			textViewIncoming.setText(outgoing.toString());
		} catch (IOException e) {
			Log.e(TAG, "Catched IOException from writeString() in onEditorAction()",e);
		}			
		return false;
	}
	
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		Log.d(TAG, "isChecked ="+isChecked);
		if (isChecked)
		{
			sendingThread = new SendingThread();
			sendingThread.start();
		} else {
			sendingThread.interrupt();
		}
	}
	
	public boolean onLongClick(View v) {
		textViewReception.setText("Reception cleaned");
		return false;

	}
	
	public void onStringReceived(final String string) {
		incoming+=string.length();
		handler.post(new Runnable() {
			public void run() {
				textViewIncoming.setText(incoming.toString());
				textViewReception.append(string);
			}
		});
	}
	
	private class SendingThread extends Thread {
		private static final String TAG = "SendingThread";
		@Override
		public void run() {
		
			String loopbackString = "Hi!";
			try {
				while (!isInterrupted()){
					serialPort.writeString(loopbackString);
					handleSymbolsSent(loopbackString);
					Thread.sleep(1000);
				}
			} catch (IOException e) {Log.e (TAG, "IOException in SendingThread",e);
			} catch (InterruptedException e) {Log.e (TAG, "InterruptedException in SendingThread",e);
			}
			super.run();
		}
		
		
	}
	
	/** Override lifecycle methods */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.console);
		this.setTitle("Serial port test");
		
		textViewReception = (TextView)findViewById(R.id.textViewConsoleReception);
		textViewOutgoing = (TextView)findViewById(R.id.textViewOutgoingValue);
		textViewIncoming = (TextView)findViewById(R.id.textViewIncomingValue);
		editTextEmission = (EditText)findViewById(R.id.editTextConsoleEmission);
		buttonSend = (Button)findViewById(R.id.buttonSend);
		checkBoxSendingThread = (CheckBox) findViewById(R.id.checkBoxSendingThread);
		
		editTextEmission.setOnEditorActionListener(this);
		buttonSend.setOnClickListener(this);
		checkBoxSendingThread.setOnCheckedChangeListener(this);
		textViewReception.setOnLongClickListener(this);
				
		outgoing=0;
		incoming=0;
        /** Create the Handler. It will implicitly bind to the Looper that is
         * internally created for this thread (since it is the UI thread)
         */
        handler = new Handler();
	}
	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG,"onResume() called");
		try {
			/** Read serial port parameters and open it */
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			String path = sp.getString("DEVICE", "");
			int baudrate = Integer.decode(sp.getString("BAUDRATE", "-1"));
			serialPort = new SerialPort(path, baudrate);
			serialPort.setOnStringReceivedListener(this);
			/** Create threads */
			//sendingThread = new SendingThread(this, serialPort);//started in onCheckedChangeListener
			
			
		} catch (SecurityException e) {
			UserMessages.displayAlertDialog(R.string.error_security,this);
		} catch (IOException e) {
			UserMessages.displayAlertDialog(R.string.error_unknown,this);
		} catch (InvalidParameterException e) {
			UserMessages.displayAlertDialog(R.string.error_configuration,this);
		} catch (InterruptedException e) {
			Log.e (TAG, "Catched InterruptedException in onResume()",e);
		}
	}
		
	@Override
	protected void onPause() {
		sendingThread.interrupt();
		try {
			serialPort.closePort();
		} catch (IOException e) {Log.e(TAG, "IOException in onPause()",e);}
		super.onPause();
	}
}
