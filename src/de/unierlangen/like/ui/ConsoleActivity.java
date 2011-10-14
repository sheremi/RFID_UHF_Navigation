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
	

	private static final String TAG = "ConsoleActivity";
	/** Serial port used by console */
	private SerialPort serialPort;
	/** Symbol counters */
	private Integer incoming;
	/** Amount of symbols sent*/
	private Integer outgoing;
	/** Displays outgoing count */	
	private TextView textViewOutgoing;
	/** Displays incoming count */	
	private TextView textViewIncoming;
	/** Displays symbols received */	
	private TextView textViewReception;
	/** Button to send string from settings */	
	private Button buttonSend;
	/** EditText to send custom strings */
	private EditText editTextEmission;
	/** Enables or disables sending thread */	
	private CheckBox checkBoxSendingThread;
	/* Concurrency - AsyncTasks, Threads and Handlers*/
	/** UI thread handler, use it to post runnables on UI thread */
	Handler handler;
	/** Thread which continuously sends symbols */
	private SendingThread sendingThread;
	
	/** Add length of the string to the outgoing counter
	 * and text view */
	public void onStringSent(String sentString) {
		/** This method could be called from another thread.
		* Use the handler to make sure the code runs in the UI thread*/
		outgoing+=sentString.length();
		handler.post(new Runnable() {
			public void run() {
				textViewOutgoing.setText(outgoing.toString());
			}
		});
	}
	/** Add length of the string to the incoming counter
	 * and text view of this counter and add received string 
	 * on reception*/
	public void onStringReceived(final String string) {
		incoming+=string.length();
		handler.post(new Runnable() {
			public void run() {
				textViewIncoming.setText(incoming.toString());
				textViewReception.append(string);
			}
		});
	}
	/** Describe what to do when button "Send" is pressed */
	public void onClick(View v) {
		// XXX((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(100);
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		String messageString = sp.getString("GREETING", "");
		serialPort.writeString(messageString);
		onStringSent(messageString);
	}
	
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		serialPort.writeString(v.getText().toString());//+"\n");
		onStringSent(v.getText().toString());
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
	
	
	
	/** Thread send symbols to serial port */
	private class SendingThread extends Thread {
		private static final String TAG = "SendingThread";
		@Override
		public void run() {
		
			String loopbackString = "Hi!";
			try {
				while (!isInterrupted()){
					serialPort.writeString(loopbackString);
					onStringSent(loopbackString);
					Thread.sleep(1000);
				}
			} catch (InterruptedException e) {Log.e (TAG, "InterruptedException in SendingThread",e);
			}
			super.run();
		}
		
		
	}
	
	/* Override lifecycle methods */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.console);
		
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
			serialPort = SerialPort.getSerialPort();
			serialPort.setSharedPreferences(PreferenceManager.getDefaultSharedPreferences(this));
			serialPort.setOnStringReceivedListener(this);
			/** Create threads */
			//sendingThread = new SendingThread(this, serialPort);//started in onCheckedChangeListener
			
			
		} catch (InvalidParameterException e) {
			UserMessages.displayAlertDialog(R.string.error_configuration,this);
		}
	}
		
	@Override
	protected void onPause() {
		sendingThread.interrupt();
		super.onPause();
	}
}
