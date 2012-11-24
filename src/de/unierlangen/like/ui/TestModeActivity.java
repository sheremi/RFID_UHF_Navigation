package de.unierlangen.like.ui;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import de.unierlangen.like.R;
import de.unierlangen.like.customviews.AnalogGauge;

public class TestModeActivity extends OptionsMenuActivity implements OnClickListener {

    /*
     * ReaderDriver MyReader; //protected int a=0; //protected int y=0; TextView
     * tagAmount;
     */
    AnalogGauge analogGauge1;
    AnalogGauge analogGauge2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.testmodeview);

        analogGauge1 = (AnalogGauge) findViewById(R.id.analogGauge1);
        analogGauge1.setHandTarget(20);
        analogGauge2 = (AnalogGauge) findViewById(R.id.analogGauge2);
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
