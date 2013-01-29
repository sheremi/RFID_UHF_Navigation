package de.unierlangen.like.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import de.unierlangen.like.R;

public class Fragment2 extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.help_frag2, container, false);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageView1);
        imageView.setImageDrawable(getResources().getDrawable(getArguments().getInt("picture_id")));
        return view;
    }
}
