package de.unierlangen.like.ui;

import android.os.Bundle;
import android.widget.Button;

public class ReaderRegistersListActivity extends OptionsMenuActivity {

    Button readerSettingsButton;
    Button menuButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.reader_registers);

    }
}
