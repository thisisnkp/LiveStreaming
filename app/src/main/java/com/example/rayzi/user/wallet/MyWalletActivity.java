package com.example.rayzi.user.wallet;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.example.rayzi.R;
import com.example.rayzi.activity.BaseActivity;
import com.example.rayzi.databinding.ActivityMyWalletBinding;
import com.google.android.material.tabs.TabLayout;

public class MyWalletActivity extends BaseActivity {
    ActivityMyWalletBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_my_wallet);

        if (isRTL(this)) {
            binding.backimg.setScaleX(isRTL(this) ? -1 : 1);
        }

        binding.viewPager.setAdapter(new WalletViewPagerAdapter(getSupportFragmentManager()));
        binding.viewPager.setOffscreenPageLimit(2);
        binding.tablayout1.setupWithViewPager(binding.viewPager);

        binding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    binding.lytRoot.setBackground(ContextCompat.getDrawable(MyWalletActivity.this, R.color.black_back));
                } else if (position == 1) {
                    binding.lytRoot.setBackground(ContextCompat.getDrawable(MyWalletActivity.this, R.color.black_back));
                } else {
                    binding.lytRoot.setBackground(ContextCompat.getDrawable(MyWalletActivity.this, R.color.black_back));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        binding.tablayout1.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View v = tab.getCustomView();
                if (v != null) {
                    TextView tv = (TextView) v.findViewById(R.id.tvTab);
                    tv.setTextColor(ContextCompat.getColor(MyWalletActivity.this, R.color.pink));
                    tv.setTypeface(null, Typeface.BOLD);
                    tv.setTextSize(18);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View v = tab.getCustomView();
                if (v != null) {
                    TextView tv = (TextView) v.findViewById(R.id.tvTab);
                    tv.setTextColor(ContextCompat.getColor(MyWalletActivity.this, R.color.white));
                    tv.setTypeface(null, Typeface.NORMAL);
                    tv.setTextSize(18);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        setTab(new String[]{"Recharge", "Income"});

    }

    private void setTab(String[] country) {
        binding.tablayout1.setTabGravity(TabLayout.GRAVITY_FILL);
        binding.tablayout1.removeAllTabs();
        for (int i = 0; i < country.length; i++) {
            binding.tablayout1.addTab(binding.tablayout1.newTab().setCustomView(createCustomView(i, country[i])));
        }

    }

    private View createCustomView(int i, String s) {

        View v = LayoutInflater.from(this).inflate(R.layout.custom_tabhorizontol_plan, null);

        TextView tv = (TextView) v.findViewById(R.id.tvTab);

        tv.setText(s);
        if (i == 0) {
            tv.setTextColor(ContextCompat.getColor(this, R.color.white));
            tv.setTypeface(null, Typeface.BOLD);
            tv.setTextSize(18);
        } else {
            tv.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
            tv.setTypeface(null, Typeface.NORMAL);
            tv.setTextSize(18);
        }
        return v;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

}