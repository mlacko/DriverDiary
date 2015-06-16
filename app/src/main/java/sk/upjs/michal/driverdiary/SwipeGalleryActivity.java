package sk.upjs.michal.driverdiary;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.Locale;


public class SwipeGalleryActivity extends ActionBarActivity {
    private ArrayList<Vehicle> vehicles;
    private int position;
    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    private ArrayList<CarDetailFragment> fragments;
    private CarDetailFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipe_gallery);
        vehicles = (ArrayList<Vehicle>) getIntent().getSerializableExtra("vehicles");
        //int count = bundle.getInt("count");
        position = getIntent().getIntExtra("position", 0);
        /*for (int i = 0; i< count; i++) {
            Vehicle vehicle =(Vehicle) bundle.getSerializable("position"+i);
            Log.e("vehicle present",vehicle.getSpz());
            vehicles.add(vehicle);
        }*/
        fragments = new ArrayList<>();
        for (Vehicle vehicle : vehicles) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("vehicle", vehicle);
            CarDetailFragment fragment = new CarDetailFragment();
            fragment.setArguments(bundle);
            fragments.add(fragment);
        }
        PageListener pageListener = new PageListener();
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setPageTransformer(true, new DepthPageTransformer());
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(pageListener);
        mViewPager.setCurrentItem(position);
        fragment = fragments.get(position);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_swipe_gallery, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_addRecord) {
            fragment.addRecord();
            return true;
        }

        if (id == R.id.action_share) {
            fragment.share();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            /*Vehicle vehicle = vehicles.get(position);
            Bundle bundle = new Bundle();
            bundle.putSerializable("vehicle", vehicle);
            fragment = new CarDetailFragment();
            /*if (vehicle != null) {
                Log.e("brand",vehicle.getBrand());
                fragment.setDetails(vehicle.getBrand(), vehicle.getModel(), vehicle.getSpz(), vehicle.getPath());
                fragment.getDrives();
            }*/
            //fragment.setArguments(bundle);
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            return vehicles.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();

            return null;
        }
    }

    public class DepthPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.8f;

        public void transformPage(View view, float position) {
            float translationX;
            float scale;
            float alpha;

            if (position >= 1 || position <= -1) {
                // Fix for https://code.google.com/p/android/issues/detail?id=58918
                translationX = 0;
                scale = 1;
                alpha = 1;
            } else if (position >= 0) {
                translationX = -view.getWidth() * position;
                scale = -0.1f * position + 1;
                alpha = Math.max(1 - position, 0);
            } else {
                translationX = 0.5f * view.getWidth() * position;
                scale = 1.0f;
                alpha = Math.max(0.1f * position + 1, 0);
            }

            view.setTranslationX(translationX);
            view.setScaleX(scale);
            view.setScaleY(scale);
            view.setAlpha(alpha);
        }
    }

    public class PageListener extends ViewPager.SimpleOnPageChangeListener {
        private int currentPage;

        @Override
        public void onPageSelected(int position) {
            fragment = fragments.get(position);
        }

        public final int getCurrentPage() {
            return currentPage;
        }
    }
}
