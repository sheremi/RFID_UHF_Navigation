package de.unierlangen.like.ui;

import android.app.ActionBar;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import de.unierlangen.like.R;

public class HelpActivity extends Activity implements TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help);

        // Create the adapter that will return a fragment for each of the three
        // primary sections
        // of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter();

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab.
        // We can also use ActionBar.Tab#select() to do this if we have a
        // reference to the
        // Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter.
            // Also specify this Activity object, which implements the
            // TabListener interface, as the
            // listener for when this tab is selected.
            actionBar.addTab(actionBar.newTab().setText(mSectionsPagerAdapter.getPageTitle(i))
                    .setTabListener(this));
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page
        // in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /*
     * @Override public boolean onCreateOptionsMenu( Menu menu ) {
     * getMenuInflater().inflate( R.menu.main, menu ); return true; }
     * 
     * @Override public boolean onOptionsItemSelected( MenuItem item ) { boolean
     * ret; if (item.getItemId() == R.id.menu_settings) { // Handle Settings ret
     * = true; } else { ret = super.onOptionsItemSelected( item ); } return ret;
     * }
     */

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the primary sections of the app.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter() {
            super(getFragmentManager());
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
            case 0:
                Fragment fragment1 = Fragment.instantiate(getApplicationContext(),
                        "de.unierlangen.like.ui.Fragment1");
                return fragment1;

            case 1:
                Fragment fragment2 = Fragment.instantiate(getApplicationContext(),
                        "de.unierlangen.like.ui.Fragment2");
                // TODO that is how we can give fragment some data
                Bundle args = new Bundle();
                args.putParcelable("intent", getIntent());
                fragment2.setArguments(args);
                return fragment2;

            default:
                return null;

            }

        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
            case 0:
                return "Adin";
            case 1:
                return "Dva";
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
