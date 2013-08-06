package de.unierlangen.like.ui;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import com.github.androidutils.logger.Logger;

import de.unierlangen.like.R;
import de.unierlangen.like.preferences.PreferenceWithHeadersActivity;
import de.unierlangen.like.preferences.SettingsActivity;

public class ActionBarHandler {
    private static final int REQUEST_ROOM = 1;
    private final Activity activity;
    private final Logger log;

    public ActionBarHandler(Activity activity) {
        this.activity = activity;
        log = Logger.getDefaultLogger();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        activity.getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * override onOptions ItemSelected here
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.find_room:
            activity.startActivityForResult(new Intent(activity, FindRoomActivity.class),
                    REQUEST_ROOM);
            break;
        case R.id.about:
            activity.startActivity(new Intent(activity, AboutActivity.class));
            break;
        case R.id.help:
            activity.startActivity(new Intent(activity, HelpActivity.class));
            break;
        case R.id.advanced:
            activity.startActivity(new Intent(activity, PreferenceWithHeadersActivity.class));
            break;
        case R.id.prefs:
            activity.startActivity(new Intent(activity, SettingsActivity.class));
            break;
        case R.id.main_add_tag:
            activity.startActivity(new Intent(activity, AddTagActivity.class));
            break;
        default:
            log.w("unexpected item " + item.getTitle());
        }
        // return super.onOptionsItemSelected(item);
        return false;
    }
}
