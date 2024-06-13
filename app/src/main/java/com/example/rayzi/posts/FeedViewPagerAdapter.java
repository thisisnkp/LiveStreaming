package com.example.rayzi.posts;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.rayzi.reels.VideoListFragment;

public class FeedViewPagerAdapter extends FragmentStateAdapter {

    public FeedViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new FeedListFragment("Popular");
        } else {
            return new VideoListFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
