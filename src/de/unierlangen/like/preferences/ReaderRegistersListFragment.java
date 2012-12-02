package de.unierlangen.like.preferences;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import de.unierlangen.like.R;

public class ReaderRegistersListFragment extends Fragment {

    Button readerSettingsButton;
    Button menuButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.reader_registers, container, false);
        return view;
    }
}
