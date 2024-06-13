package com.example.rayzi.user.wallet;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class WalletViewPagerAdapter extends FragmentPagerAdapter {

    Fragment[] fragments = new Fragment[]{new RechargeFragment(), new RcoinFragment()};

    public WalletViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return fragments[position];
    }

    @Override
    public int getCount() {
        return fragments.length;
    }

}
