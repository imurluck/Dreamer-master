package com.example.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by Zhangzongxiang on 2017/5/7.
 */

public class FragmentAdapter extends FragmentPagerAdapter {

    private ArrayList<String> titles = new ArrayList<>();

    ArrayList<Fragment> fragmentList = new ArrayList<>();

    public FragmentAdapter(FragmentManager fm, ArrayList<Fragment> fragmentList,
                           ArrayList<String> titles) {
        super(fm);
        this.fragmentList = fragmentList;
        this.titles = titles;
    }

    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    public int getCount() {
        return fragmentList.size();
    }

    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }

    @Override
    public int getItemPosition(Object object) {
        return super.getItemPosition(object);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {}
}
