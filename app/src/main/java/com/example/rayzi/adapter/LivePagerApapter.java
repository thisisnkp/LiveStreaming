package com.example.rayzi.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.rayzi.fragment.LiveFragment;
import com.example.rayzi.fragment.PostFragment;
import com.example.rayzi.fragment.RecordReelFragment;

public class LivePagerApapter extends FragmentPagerAdapter {
    private final String[] categories;

    public LivePagerApapter(@NonNull FragmentManager fm, String[] categories) {
        super(fm);
        this.categories = categories;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new LiveFragment();
        } else if (position == 1) {
            return new RecordReelFragment();
        } else if (position == 2) {
            return new PostFragment();
        } else {
            return new PostFragment();
        }

    }

    @Override
    public int getCount() {
        return 3;
    }

}
