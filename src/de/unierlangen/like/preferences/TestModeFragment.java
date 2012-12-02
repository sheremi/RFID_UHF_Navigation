package de.unierlangen.like.preferences;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import de.unierlangen.like.R;
import de.unierlangen.like.customviews.AnalogGauge;

public class TestModeFragment extends Fragment implements OnClickListener {

    /*
     * ReaderDriver MyReader; //protected int a=0; //protected int y=0; TextView
     * tagAmount;
     */
    AnalogGauge analogGauge1;
    AnalogGauge analogGauge2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.testmodeview, container, false);

        analogGauge1 = (AnalogGauge) view.findViewById(R.id.analogGauge1);
        analogGauge1.setHandTarget(20);
        analogGauge2 = (AnalogGauge) view.findViewById(R.id.analogGauge2);
        analogGauge2.setHandTarget(60);
        analogGauge1.setOnClickListener(this);
        analogGauge2.setOnClickListener(this);
        /*
         * MyReader = new ReaderDriver(); MyReader.makeInventoryRound();
         * 
         * tagAmount = (TextView)findViewById(R.id.tag_amount);
         * tagAmount.setText
         * (Integer.toString(MyReader.getAmountOfTagsInRange()));
         */

        // y = MyReader.getTagEPC(0);
        // MyReader.getTagRSSI(0);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.analogGauge1:
            analogGauge1.setHandTarget((int) (Math.random() * 140 - 20));
            break;
        case R.id.analogGauge2:
            analogGauge2.setHandTarget((int) (Math.random() * 140 - 20));
            break;
        default:
            break;
        }
    }
}
