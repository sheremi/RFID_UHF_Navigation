package de.unierlangen.like.ui;

import android.os.Bundle;
import android.widget.Button;
import de.unierlangen.like.R;

public class ReaderRegistersListActivity extends OptionsMenuActivity {

    Button readerSettingsButton;
    Button menuButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.reader_registers);

    }
}
