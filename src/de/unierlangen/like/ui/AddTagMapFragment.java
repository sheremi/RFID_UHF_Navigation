package de.unierlangen.like.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.unierlangen.like.R;
import de.unierlangen.like.customviews.MapView;

public class AddTagMapFragment extends Fragment {
    MapView mapView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_tag_map_fragment, container, false);
        mapView = (MapView) view.findViewById(R.id.add_tag_details_mapview);
        mapView.setMode(MapView.MARKER);
        return view;
    }
}
