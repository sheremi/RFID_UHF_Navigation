package de.unierlangen.like.ui;

import de.unierlangen.like.R;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class ReaderPreferences extends PreferenceActivity /*
                                                           * implements
                                                           * OnSharedPreferenceChangeListener
                                                           */{

    // public static final String KEY_LIST_PREFERENCE = "listPref";

    // private ListPreference mListPreference;
    /*
     * boolean CheckboxPreference; String ListPreference; String
     * editTextPreference; String ringtonePreference; String
     * secondEditTextPreference; String customPref;
     * 
     * private void getPrefs() { // Get the xml/preferences.xml preferences
     * SharedPreferences prefs = PreferenceManager
     * .getDefaultSharedPreferences(getBaseContext()); CheckboxPreference =
     * prefs.getBoolean("checkboxPref", true); ListPreference =
     * prefs.getString("listPref", "nr1"); editTextPreference =
     * prefs.getString("editTextPref", "Nothing has been entered");
     * ringtonePreference = prefs.getString("ringtonePref",
     * "DEFAULT_RINGTONE_URI"); secondEditTextPreference =
     * prefs.getString("SecondEditTextPref", "Nothing has been entered"); // Get
     * the custom preference SharedPreferences mySharedPreferences =
     * getSharedPreferences( "myCustomSharedPrefs", Activity.MODE_PRIVATE);
     * customPref = mySharedPreferences.getString("myCusomPref", ""); }
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.reader_preferences);

        // getListView().setBackgroundResource(R.drawable.background7_2);

        /*
         * // Get a reference to the preferences mListPreference =
         * (ListPreference
         * )getPreferenceScreen().findPreference(KEY_LIST_PREFERENCE);
         * 
         * // Get the custom preference Preference customPref =
         * (Preference)findPreference("customPref");
         * customPref.setOnPreferenceClickListener(new
         * OnPreferenceClickListener() {
         * 
         * public boolean onPreferenceClick(Preference preference) {
         * 
         * Toast.makeText(getBaseContext(),
         * "The custom preference has been clicked",Toast.LENGTH_LONG).show();
         * 
         * SharedPreferences customSharedPreference = getSharedPreferences(
         * "myCustomSharedPrefs", Activity.MODE_PRIVATE);
         * 
         * SharedPreferences.Editor editor = customSharedPreference.edit();
         * editor.putString("myCustomPref","The preference has been clicked");
         * editor.commit();
         * 
         * return true; } });
         * 
         * }
         * 
         * /* @Override protected void onStart() { super.onStart(); getPrefs();
         * 
         * }
         * 
         * @Override protected void onResume() { super.onResume();
         * 
         * // Setup the initial values
         * mListPreference.setSummary("Current value is " +
         * mListPreference.getEntry().toString());
         * 
         * // Set up a listener whenever a key changes
         * getPreferenceScreen().getSharedPreferences
         * ().registerOnSharedPreferenceChangeListener(this); }
         * 
         * @Override protected void onPause() { super.onPause();
         * 
         * // Unregister the listener whenever a key changes
         * getPreferenceScreen(
         * ).getSharedPreferences().unregisterOnSharedPreferenceChangeListener
         * (this); }
         * 
         * public void onSharedPreferenceChanged(SharedPreferences
         * sharedPreferences, String key) { // Set new summary, when a
         * preference value changes if (key.equals(KEY_LIST_PREFERENCE)) {
         * mListPreference.setSummary("Current value is " +
         * mListPreference.getEntry().toString()); } }
         */

        // Old ReaderSettings

        /*
         * ReaderDriver MyReader; CheckBox checkRX; CheckBox checkRF; Button
         * readerRegistersButton; Button menuButton;
         * 
         * @Override protected void onCreate(Bundle savedInstanceState) {
         * 
         * super.onCreate(savedInstanceState);
         * setContentView(R.layout.reader_settings);
         * 
         * MyReader = new ReaderDriver();
         * 
         * checkRX = (CheckBox)findViewById(R.id.RXonoff_check);
         * checkRX.setChecked(MyReader.getRX());
         * 
         * checkRF = (CheckBox)findViewById(R.id.RFonoff_check);
         * checkRF.setChecked(MyReader.getRF());
         * 
         * readerRegistersButton = (Button)findViewById(R.id.reader_button);
         * readerRegistersButton.setOnClickListener(new OnClickListener() {
         * 
         * @Override public void onClick(View v){
         * readerRegistersButtonClickHandler(v); } });
         * 
         * menuButton = (Button)findViewById(R.id.menubutton);
         * menuButton.setOnClickListener(new OnClickListener(){
         * 
         * @Override public void onClick(View v){ menuButtonClickHandler(v); }
         * });
         * 
         * checkRX.setOnCheckedChangeListener(new OnCheckedChangeListener(){
         * 
         * @Override public void onCheckedChanged(CompoundButton buttonView,
         * boolean isChecked){ RXcheckBoxHandler(buttonView, isChecked); } });
         * 
         * checkRF.setOnCheckedChangeListener(new OnCheckedChangeListener(){
         * 
         * @Override public void onCheckedChanged(CompoundButton buttonView,
         * boolean isChecked){ RFcheckBoxHandler(buttonView, isChecked); } }); }
         * 
         * public void RXcheckBoxHandler(View v, boolean isChecked){
         * 
         * MyReader.setRX(isChecked);
         * 
         * // Create an intent object to store the data // we want to send back
         * to the first activity Intent answerRX = new Intent();
         * answerRX.putExtra("label", R.id.RXonoff_check);
         * answerRX.putExtra("ischecked", MyReader.getRX());
         * 
         * // Signal that we have indeed a valid return object // by setting the
         * activity result to RESULT_OK setResult(RESULT_OK, answerRX);
         * 
         * // Finish this activity and return control to the // calling activity
         * (Episode11 in this case) //finish();
         * 
         * }
         * 
         * public void RFcheckBoxHandler(View v, boolean isChecked) {
         * 
         * MyReader.setRF(isChecked);
         * 
         * // Create an intent object to store the data // we want to send back
         * to the first activity Intent answerRF = new Intent(); // Depending on
         * which radio button was chosen // we put in different data into the
         * intent object answerRF.putExtra("label", R.id.RFonoff_check);
         * answerRF.putExtra("ischecked", MyReader.getRF());
         * 
         * // Signal that we have indeed a valid return object // by setting the
         * activity result to RESULT_OK setResult(RESULT_OK, answerRF);
         * 
         * // Finish this activity and return control to the // calling activity
         * (Episode11 in this case) //finish();
         * 
         * }
         * 
         * public void readerRegistersButtonClickHandler(View v) { // Create new
         * intent object and tell it to call the ReaderRegistersList class
         * Intent i = new Intent(this,
         * de.unierlangen.like.rfid.uhf.ReaderRegistersList.class); // Start
         * ReaderRegistersList as a new activity startActivity(i);
         * 
         * }
         * 
         * // This code below will be execute after clicking on the Menu button
         * // In our case see above public void menuButtonClickHandler(View v){
         * // Create new intent object and tell it to call the Menu class Intent
         * i = new Intent(this, de.unierlangen.like.rfid.uhf.Menu.class); //
         * Start Menu as a new activity startActivity(i); }
         */

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_reader_prefs, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
        case R.id.reader_registers:
            startActivity(new Intent(this, ReaderRegistersListActivity.class));
            break;
        /*
         * TODO case R.id.help: startActivity(new Intent(this, .class)); break;
         */
        case R.id.your_location:
            startActivity(new Intent(this, MainYourLocationActivity.class));
            break;
        default:
            this.showMsg((String) item.getTitle());

        }
        // return super.onOptionsItemSelected(item);
        return false;
    }

    /**
     * Show toast message
     * 
     * @param String
     *            message
     */
    private void showMsg(String message) {
        Toast msg = Toast.makeText(this, message, Toast.LENGTH_LONG);
        msg.setGravity(Gravity.CENTER, msg.getXOffset() / 2, msg.getYOffset() / 2);
        msg.show();
    }

}
