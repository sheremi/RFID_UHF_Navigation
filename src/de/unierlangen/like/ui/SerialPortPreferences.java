/*
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

package de.unierlangen.like.ui;

import java.io.IOException;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import de.unierlangen.like.serialport.SerialPortFinder;
import de.unierlangen.like.usb.FreerunnerUSB;

public class SerialPortPreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
	public static final String KEY_DEVICES_PREFERENCE = "DEVICE";
	public static final String KEY_BAUDRATES_PREFERENCE = "BAUDRATE";
    public static final String KEY_USBHOST_PREFERENCE = "USBhost";
	
	private ListPreference devices;
	private ListPreference baudrates;
	private CheckBoxPreference usbHost;
	//private SharedPreferences sharedPreferences;
	
	/**
	 * Interface
	 */
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		preference.setSummary((String)newValue);
		return true;
	}
	
	
//	private Application mApplication;
	private SerialPortFinder mSerialPortFinder;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//mApplication = (Application) getApplication();
		//mSerialPortFinder = mApplication.mSerialPortFinder;

		addPreferencesFromResource(R.xml.serial_port_preferences);

		
		// Devices
		devices = (ListPreference)findPreference("DEVICE");
        
		
		mSerialPortFinder = new SerialPortFinder();
		String[] entries = mSerialPortFinder.getAllDevices();
        String[] entryValues = mSerialPortFinder.getAllDevicesPath();
		devices.setEntries(entries);
		devices.setEntryValues(entryValues);
		
		
		// Baud rates
		baudrates = (ListPreference)findPreference("BAUDRATE");
		
		//USB
		usbHost = (CheckBoxPreference)findPreference("USBhost");
				
	}
	
	 @Override
	    protected void onResume() {
	        super.onResume();

	        // Setup the initial values
	        devices.setSummary("Current value is " + devices.getValue());
	        baudrates.setSummary("Current value is " + baudrates.getValue());
	        //usbHost.setSummary(sharedPreferences.getBoolean(key, false) ? "Disable this setting" : "Enable this setting"); 
	        
	        // Set up a listener whenever a key changes            
	        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_serial_prefs, menu);
		return true; 
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
			
		switch (item.getItemId()) {
		case R.id.your_location:
			startActivity(new Intent(SerialPortPreferences.this, MainYourLocationActivity.class));
			break;
		case R.id.console:
			startActivity(new Intent(SerialPortPreferences.this, ConsoleActivity.class));
			break;
		
		default: UserMessages.showMsg((String)item.getTitle(), this);
			
		}
		//return super.onOptionsItemSelected(item);
		return false;
	}
	
	
	
	@Override
	protected void onPause() {
		
		super.onPause();

        // Unregister the listener whenever a key changes            
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);    
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Let's do something a preference value changes
        if (key.equals(KEY_DEVICES_PREFERENCE)) {
          devices.setSummary("Current value is " + devices.getValue());
        }
        else if (key.equals(KEY_BAUDRATES_PREFERENCE)) {
          baudrates.setSummary("Current value is " + baudrates.getValue()); 
        }
        else if (key.equals(KEY_USBHOST_PREFERENCE)) {
            usbHost.setSummary(sharedPreferences.getBoolean(key, false) ? "Disable this setting" : "Enable this setting"); 
          }
   		
		try {
			FreerunnerUSB.setUSBHostState(false);
		} catch (IOException e) {
			//showMsg("Access to driver denied");					
			e.printStackTrace();
		}
		
		super.onPause();
	}

	
}




		
		

	

