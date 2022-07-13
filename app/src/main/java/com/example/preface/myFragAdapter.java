package com.example.preface;

import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

public class myFragAdapter extends FragmentPagerAdapter {
    private FragmentManager fragmentManager;
    private List<Fragment> list;

    private ViewGroup container;
    private Object object;

    public myFragAdapter(FragmentManager fm, List<Fragment> list) {
        super(fm);
        this.fragmentManager = fm;
        this.list = list;
    }

    @Override
    public Fragment getItem(int position) {
        return list.get(position);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }
}
