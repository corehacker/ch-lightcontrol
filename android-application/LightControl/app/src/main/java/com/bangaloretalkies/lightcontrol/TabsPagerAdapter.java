package com.bangaloretalkies.lightcontrol;

import com.bangaloretalkies.lightcontrol.DiscoverFragment;
import com.bangaloretalkies.lightcontrol.OperateFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TabsPagerAdapter extends FragmentPagerAdapter {

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int index) {

        switch (index) {
            case 0:
                // Top Rated fragment activity
                return new DiscoverFragment();
            case 1:
                // Games fragment activity
                return new OperateFragment();
        }

        return null;
    }

    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 2;
    }

}