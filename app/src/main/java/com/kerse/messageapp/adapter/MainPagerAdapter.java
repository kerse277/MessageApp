package com.kerse.messageapp.adapter;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.kerse.messageapp.fragment.MessageFragment;
import com.kerse.messageapp.fragment.MessageFragment_;
import com.kerse.messageapp.fragment.ProfileFragment;
import com.kerse.messageapp.fragment.ProfileFragment_;
import com.kerse.messageapp.fragment.UsersFragment;
import com.kerse.messageapp.fragment.UsersFragment_;

public class MainPagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public MainPagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
               MessageFragment tab1 = new MessageFragment_();
                return tab1;
            case 1:
               UsersFragment tab2 = new UsersFragment_();
                return tab2;
            case 2:
                ProfileFragment tab3 = new ProfileFragment_();
                return tab3;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}