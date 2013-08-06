package de.unierlangen.like.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import de.unierlangen.like.DynamicThemeHandler;
import de.unierlangen.like.R;

public class HelpActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(DynamicThemeHandler.getInstance().getIdForName(this.getClass().getName()));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help);

        Button ok = (Button) findViewById(R.id.ok_button);
        ok.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                HelpActivity.this.finish();
            }
        });
    }
}
