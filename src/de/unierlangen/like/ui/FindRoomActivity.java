package de.unierlangen.like.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import de.unierlangen.like.R;
import de.unierlangen.like.navigation.RoomsDatabase;

public class FindRoomActivity extends Activity {

    public static final String ROOM_NAME_EXTRA = "ROOM_NAME";

    private RoomsDatabase roomsDatabase;
    private Button buttonClear;
    private Button buttonOK;
    private AutoCompleteTextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_room);
        roomsDatabase = RoomsDatabase.getRoomsDatabase(this);
        buttonClear = (Button) findViewById(R.id.buttonClear);
        buttonOK = (Button) findViewById(R.id.buttonOK);
        textView = (AutoCompleteTextView) findViewById(R.id.listOfDestinations);
        String[] strings = roomsDatabase.getRoomsNamesArray();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, strings);
        textView.setAdapter(adapter);
        buttonClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText("");
            }
        });
        buttonOK.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String chosenRoomName = textView.getText().toString();
                if (roomsDatabase.getRoomsNamesSet().contains(chosenRoomName)) {
                    Intent result = new Intent();
                    result.putExtra(ROOM_NAME_EXTRA, chosenRoomName);
                    FindRoomActivity.this.setResult(Activity.RESULT_OK, result);
                    FindRoomActivity.this.finish();
                }
            }
        });
    }
}
