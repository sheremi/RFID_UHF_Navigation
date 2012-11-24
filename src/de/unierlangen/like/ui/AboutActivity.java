package de.unierlangen.like.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import de.unierlangen.like.R;

public class AboutActivity extends Activity {

    Button ok;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        Button ok = (Button) findViewById(R.id.ok_button);
        ok.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                AboutActivity.this.finish();
            }
        });
    }
}
