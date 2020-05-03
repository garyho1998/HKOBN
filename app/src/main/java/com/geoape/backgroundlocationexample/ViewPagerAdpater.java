package com.geoape.backgroundlocationexample;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdpater extends FragmentPagerAdapter {
    private final List<Fragment> fragmentList = new ArrayList<>();
    private final List<String> FragmentListTitle = new ArrayList<>();
    public ViewPagerAdpater(FragmentManager fm){
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        return fragmentList.get(i);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    public CharSequence getPageTitle(int i){
        return FragmentListTitle.get(i);
    }

    public void AddFragement(Fragment fragment, String Title){
        fragmentList.add(fragment);
        FragmentListTitle.add(Title);
    }
}
