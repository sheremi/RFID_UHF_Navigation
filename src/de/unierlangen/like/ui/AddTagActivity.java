package de.unierlangen.like.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import de.unierlangen.like.DynamicThemeHandler;
import de.unierlangen.like.R;

public class AddTagActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(DynamicThemeHandler.getInstance().getIdForName(this.getClass().getName()));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_tag_activity);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_tag_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        return true;
    }
}