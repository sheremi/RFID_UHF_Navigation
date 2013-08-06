/*
 * Copyright 2011 Yuriy Kulikov
 * FAU LIKE
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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.github.androidutils.logger.Logger;

import de.unierlangen.like.DynamicThemeHandler;
import de.unierlangen.like.R;
import de.unierlangen.like.preferences.PreferenceWithHeadersActivity;
import de.unierlangen.like.serialport.CommunicationManager;
import de.unierlangen.like.serialport.IStringPublisher;
import de.unierlangen.like.serialport.ITxChannel;

/**
 * 
 * @author Ekaterina Lyavinskova and Yuriy Kulikov
 * 
 */
public class ConsoleActivity extends Activity implements OnEditorActionListener,
        OnLongClickListener {
    private final Logger log = Logger.getDefaultLogger();

    private static final String TAG = "ConsoleActivity";
    private static final int EVENT_STRING_RECEIVED = 1;

    /** TxChannel used by console */
    private ITxChannel txChannel;
    /** Displays symbols received */
    private TextView textViewReception;
    /** EditText to send custom strings */
    private EditText editTextEmission;
    /* Concurrency - AsyncTasks, Threads and Handlers */
    /** activePublisher (ReadingThread), used by current txChannel */
    private IStringPublisher stringPublisher;
    /** UI thread handler, use it to post runnables on UI thread */
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            log.d("handleMessage(" + msg.what + ")");
            switch (msg.what) {
            case EVENT_STRING_RECEIVED:
                textViewReception.append((String) msg.obj);
                break;

            default:
                log.d("unknown data");
                break;
            }
        }
    };

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        txChannel.sendString(v.getText().toString());// +"\n");
        return false;
    }

    @Override
    public boolean onLongClick(View v) {
        textViewReception.setText("Reception cleaned");
        return false;

    }

    /* Override lifecycle methods */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(DynamicThemeHandler.getInstance().getIdForName(this.getClass().getName()));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.console);

        textViewReception = (TextView) findViewById(R.id.textViewConsoleReception);
        editTextEmission = (EditText) findViewById(R.id.editTextConsoleEmission);
        editTextEmission.setOnEditorActionListener(this);
        textViewReception.setOnLongClickListener(this);

        txChannel = CommunicationManager.getTxChannel();
        stringPublisher = CommunicationManager.getStringPublisher();
    }

    @Override
    protected void onResume() {
        super.onResume();
        stringPublisher.register(handler, EVENT_STRING_RECEIVED);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stringPublisher.unregister(handler);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        menu.removeItem(R.id.find_room);
        return true;
    }

    /**
     * override onOptions ItemSelected here
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.about:
            startActivity(new Intent(this, AboutActivity.class));
            break;
        case R.id.help:
            startActivity(new Intent(this, HelpActivity.class));
            break;
        case R.id.prefs:
            startActivity(new Intent(this, PreferenceWithHeadersActivity.class));
            break;
        default:
            log.w("unexpected item " + item.getTitle());
        }
        // return super.onOptionsItemSelected(item);
        return false;
    }
}
