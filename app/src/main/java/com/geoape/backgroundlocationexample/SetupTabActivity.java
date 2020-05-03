package com.geoape.backgroundlocationexample;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.FrameLayout;

public class SetupTabActivity extends AppCompatActivity {

    private AppBarLayout appBarLayout;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);
        viewPager = (ViewPager) findViewById(R.id.viewpager_id);
        tabLayout = (TabLayout) findViewById(R.id.tablayout_id);
        ViewPagerAdpater adpater = new ViewPagerAdpater(getSupportFragmentManager());

        adpater.AddFragement(new FirstFragment(), "CONTACTS");
        adpater.AddFragement(new SecondFragment(), "TARGETS");
        adpater.AddFragement(new ThirdFragment(), "SENSITIVITY");

        viewPager.setAdapter(adpater);
        tabLayout.setupWithViewPager(viewPager);


    }

    public void selectTab(int position) {
        viewPager.setCurrentItem(position);
    }

}