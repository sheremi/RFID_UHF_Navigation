package de.unierlangen.like.ui;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


public class OptionsMenuActivity extends Activity {
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true; 
	}
	
	/**
	 * override onOptions ItemSelected here
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		case R.id.your_location:
			startActivity(new Intent(OptionsMenuActivity.this, MainYourLocationActivity.class));
			break;
		case R.id.find_room:
			startActivity(new Intent(OptionsMenuActivity.this, FindRoomActivity.class));
			break;
		case R.id.test_mode:
			startActivity(new Intent(OptionsMenuActivity.this, TestModeActivity.class));
			break;
		case R.id.about_submenu:
			startActivity(new Intent(OptionsMenuActivity.this, AboutActivity.class));
			break;
		case R.id.help_submenu:
			startActivity(new Intent(OptionsMenuActivity.this, HelpActivity.class));
			break;
		case R.id.prefs:
			break;
		case R.id.info:
			break;
		//case R.id.user_prefs:
		//TODO implement user preferences
			//break;
		case R.id.serial_submenu:
			startActivity(new Intent(this, SerialPortPreferences.class));
			break;
		case R.id.readerset_submenu:
			startActivity(new Intent(this, ReaderPreferences.class));
			break;	
		default: UserMessages.showMsg((String)item.getTitle(), this);

		}
		//return super.onOptionsItemSelected(item);
		return false;
	}
	
	
	
}


