package com.example.rayzi.user.guestUser;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.rayzi.MainApplication;
import com.example.rayzi.MyLoader;
import com.example.rayzi.R;
import com.example.rayzi.RayziUtils;
import com.example.rayzi.activity.BaseActivity;
import com.example.rayzi.bottomsheets.BottomSheetReport_g;
import com.example.rayzi.bottomsheets.BottomSheetReport_option;
import com.example.rayzi.chat.ChatActivity;
import com.example.rayzi.databinding.ActivityGuestBinding;
import com.example.rayzi.modelclass.GuestProfileRoot;
import com.example.rayzi.retrofit.Const;
import com.example.rayzi.retrofit.UserApiCall;
import com.example.rayzi.user.FollowersListActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import java.util.Calendar;

import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.util.ContentMetadata;
import io.branch.referral.util.LinkProperties;

public class GuestActivity extends BaseActivity {
    ActivityGuestBinding binding;
    MyLoader myLoader = new MyLoader();
    private GuestProfileRoot.User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_guest);
        binding.ivback.setOnClickListener(view -> onBackPressed());
        binding.setLoader(myLoader);

        Intent intent = getIntent();
        String userStr = intent.getStringExtra(Const.USER_STR);
        String userID = intent.getStringExtra(Const.USERID);
        String username = intent.getStringExtra(Const.USERNAME);

        binding.shimmer.setVisibility(View.VISIBLE);
        binding.layMain.setVisibility(View.GONE);

        if (userID != null && !userID.isEmpty()) {
            if (userID.equalsIgnoreCase(sessionManager.getUser().getId())) {
                binding.lytFollowUnfollow.setVisibility(View.GONE);
                binding.tvMessages.setVisibility(View.GONE);
            } else {
                binding.lytFollowUnfollow.setVisibility(View.VISIBLE);
                binding.tvMessages.setVisibility(View.VISIBLE);
            }
        }

        if (userStr != null && !userStr.isEmpty()) {
            user = new Gson().fromJson(userStr, GuestProfileRoot.User.class);
            initView();
            initListener();
        } else if (userID != null && !userID.isEmpty()) {
            myLoader.isFristTimeLoading.set(true);
            userApiCall.getGuestProfile(userID, new UserApiCall.OnGuestUserApiListner() {
                @Override
                public void onUserGetted(GuestProfileRoot.User user1) {
                    user = user1;
                    initView();
                    initListener();
                    myLoader.isFristTimeLoading.set(false);
                }

                @Override
                public void onFailure() {
                    onBackPressed();
                }
            });
        } else if (username != null && !username.isEmpty()) {
            myLoader.isFristTimeLoading.set(true);
            userApiCall.getGuestProfileByUserName(username, new UserApiCall.OnGuestUserApiListner() {
                @Override
                public void onUserGetted(GuestProfileRoot.User user1) {
                    user = user1;
                    initView();
                    initListener();
                    myLoader.isFristTimeLoading.set(false);
                }

                @Override
                public void onFailure() {
                    onBackPressed();
                }
            });

        }

        if (isRTL(this)) {
            binding.ivback.setScaleX(isRTL(this) ? -1 : 1);
        }
    }

    private void initListener() {
        binding.lytFollowing.setOnClickListener(v -> startActivity(new Intent(this, FollowersListActivity.class).putExtra(Const.TYPE, 1).putExtra(Const.USERID, user.getUserId())));
        binding.lytFollowrs.setOnClickListener(v -> startActivity(new Intent(this, FollowersListActivity.class).putExtra(Const.TYPE, 2).putExtra(Const.USERID, user.getUserId())));
        binding.ivback.setOnClickListener(v -> finish());
        binding.pdFollow.setVisibility(View.GONE);
        binding.lytFollowUnfollow.setOnClickListener(v -> followUnfollowLogic());
        binding.tvMessages.setOnClickListener(v -> {
            startActivity(new Intent(this, ChatActivity.class).putExtra(Const.USER, new Gson().toJson(user)));
        });


    }

    private void followUnfollowLogic() {
        binding.lytFollowUnfollow.setEnabled(false);
        binding.pdFollow.setVisibility(View.VISIBLE);
        binding.tvFollowStatus.setVisibility(View.INVISIBLE);

        boolean followNow = binding.tvFollowStatus.getText().toString().equalsIgnoreCase("follow");
        userApiCall.followUnfollowUser(followNow, user.getUserId(), "", new UserApiCall.OnFollowUnfollowListner() {
            @Override
            public void onFollowSuccess() {

                binding.tvFollowStatus.setText("Unfollow");
                binding.lytFollowUnfollow.setBackgroundTintList(ContextCompat.getColorStateList(GuestActivity.this, R.color.graylight));
                user.setFollow(false);
                RayziUtils.incriseDecriseCount(binding.tvFollowrs, true);
                binding.pdFollow.setVisibility(View.GONE);
                binding.tvFollowStatus.setVisibility(View.VISIBLE);
                binding.lytFollowUnfollow.setEnabled(true);

            }

            @Override
            public void onUnfollowSuccess() {

                binding.lytFollowUnfollow.setBackgroundTintList(ContextCompat.getColorStateList(GuestActivity.this, R.color.pink));
                binding.tvFollowStatus.setText("Follow");
                user.setFollow(true);
                RayziUtils.incriseDecriseCount(binding.tvFollowrs, false);
                binding.pdFollow.setVisibility(View.GONE);
                binding.tvFollowStatus.setVisibility(View.VISIBLE);
                binding.lytFollowUnfollow.setEnabled(true);

            }

            @Override
            public void onFail() {

            }
        });

    }

    private void initView() {

        binding.shimmer.setVisibility(View.GONE);
        binding.layMain.setVisibility(View.VISIBLE);

        if (user != null) {

            if (!isFinishing()) {
                if (!user.getCoverImage().isEmpty()) {
                    Glide.with(this).load(user.getCoverImage())
                            .placeholder(R.drawable.default_cover)
                            .apply(MainApplication.requestOptions)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop().into(binding.imgUser1);
                } else {
                    binding.imgUser1.setImageResource(R.drawable.default_cover);
                }
                binding.imgUser.setUserImage(user.getImage(), user.isIsVIP(), GuestActivity.this, 40);
            }

            binding.tvName.setText(user.getName());
            binding.tvAge.setText(String.valueOf(user.getAge()));


            if (user.getBio().isEmpty()) {
                binding.tvBio.setVisibility(View.GONE);
            } else {
                binding.tvBio.setText(user.getBio());
            }
            binding.tvCountry.setText(user.getCountry());
            binding.tvFollowrs.setText(String.valueOf(user.getFollowers()));
            binding.tvLevel.setText(user.getLevel().getName());
            binding.tvFollowing.setText(String.valueOf(user.getFollowing()));

            binding.tvUserName.setText(user.getName());

            if (user.isFollow()) {
                binding.tvFollowStatus.setText("Unfollow");
                binding.lytFollowUnfollow.setBackgroundTintList(ContextCompat.getColorStateList(GuestActivity.this, R.color.graylight));
            } else {
                binding.lytFollowUnfollow.setBackgroundTintList(ContextCompat.getColorStateList(GuestActivity.this, R.color.pink));
                binding.tvFollowStatus.setText("Follow");
            }
            try {
                if (user.getGender().equalsIgnoreCase(Const.MALE)) {
                    binding.imgGender.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.male));
                } else {
                    binding.imgGender.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.female));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        initTabLayout();
    }

    private void initTabLayout() {
        binding.viewPager.setAdapter(new GuestUserProfileViewPagerAdapter(getSupportFragmentManager(), user));
        binding.tablayout1.setupWithViewPager(binding.viewPager);
        binding.tablayout1.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //ll

                View v = tab.getCustomView();
                if (v != null) {
                    TextView tv = (TextView) v.findViewById(R.id.tvTab);
                    tv.setTextColor(ContextCompat.getColor(GuestActivity.this, R.color.white));

                    View indicator = (View) v.findViewById(R.id.indicator);
                    indicator.setVisibility(View.VISIBLE);

                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                //ll
                View v = tab.getCustomView();
                if (v != null) {
                    TextView tv = (TextView) v.findViewById(R.id.tvTab);
                    tv.setTextColor(ContextCompat.getColor(GuestActivity.this, R.color.graylight));

                    View indicator = (View) v.findViewById(R.id.indicator);
                    indicator.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
//ll
            }
        });
        settab(new String[]{"Posts", "Relites"});
    }

    private void settab(String[] contry) {
        binding.tablayout1.setTabGravity(TabLayout.GRAVITY_FILL);
        binding.tablayout1.removeAllTabs();
        for (int i = 0; i < contry.length; i++) {
            binding.tablayout1.addTab(binding.tablayout1.newTab().setCustomView(createCustomView(i, contry[i])));
        }

    }


    private View createCustomView(int i, String s) {

        View v = LayoutInflater.from(GuestActivity.this).inflate(R.layout.custom_tabhorizontol2, null);
        TextView tv = (TextView) v.findViewById(R.id.tvTab);
        tv.setText(s);
        tv.setTextColor(ContextCompat.getColor(GuestActivity.this, R.color.white));
        View indicator = (View) v.findViewById(R.id.indicator);
        if (i == 0) {
            indicator.setVisibility(View.VISIBLE);
        } else {
            indicator.setVisibility(View.GONE);
        }
        return v;

    }


    public void onClickShare(View view) {
        BranchUniversalObject buo = new BranchUniversalObject()
                .setCanonicalIdentifier("content/12345")
                .setTitle(user.getName())
                .setContentDescription(user.getBio())
                .setContentImageUrl(user.getImage())
                .setContentMetadata(new ContentMetadata().addCustomMetadata("type", "PROFILE").addCustomMetadata(Const.DATA, user.getUserId()));

        LinkProperties lp = new LinkProperties()
                .setChannel("facebook")
                .setFeature("sharing")
                .setCampaign("content 123 launch")
                .setStage("new user")

                .addControlParameter("", "")
                .addControlParameter("", Long.toString(Calendar.getInstance().getTimeInMillis()));

        buo.generateShortUrl(this, lp, (url, error) -> {

            try {

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                String shareMessage = url;
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                startActivity(Intent.createChooser(shareIntent, "choose one"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void onClickReport(View view) {
        if (user == null) return;

        new BottomSheetReport_option(GuestActivity.this, new BottomSheetReport_option.OnReportedListener() {
            @Override
            public void onReported() {
                new BottomSheetReport_g(GuestActivity.this, user.getUserId(), () -> {
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.toast_layout, (ViewGroup) findViewById(R.id.customtoastlyt));

                    Toast toast = new Toast(getApplicationContext());
                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();

                });
            }

            @Override
            public void onBlocked() {
                finish();
            }
        });

    }
}