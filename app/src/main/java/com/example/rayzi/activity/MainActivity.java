package com.example.rayzi.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.example.rayzi.FaceBeautyFilter.BeautyFilterActivity;
import com.example.rayzi.MainApplication;
import com.example.rayzi.NetWorkChangeReceiver;
import com.example.rayzi.R;
import com.example.rayzi.adapter.ScreenSlidePagerAdapter;
import com.example.rayzi.databinding.ActivityMainBinding;
import com.example.rayzi.liveStreamming.WatchLiveActivity;
import com.example.rayzi.modelclass.LiveUserRoot;
import com.example.rayzi.modelclass.PostRoot;
import com.example.rayzi.modelclass.ReliteRoot;
import com.example.rayzi.popups.PrivacyPopup_g;
import com.example.rayzi.posts.FeedListActivity;
import com.example.rayzi.reels.ReelsActivity;
import com.example.rayzi.retrofit.Const;
import com.example.rayzi.socket.MySocketManager;
import com.example.rayzi.user.guestUser.GuestActivity;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {
    ActivityMainBinding binding;
    private NetWorkChangeReceiver netWorkChangeReceiver;
    ScreenSlidePagerAdapter screenSlidePagerAdapter;
    private int position = 0;

    @Override
    protected void onStart() {
        super.onStart();
        MainApplication.isAppOpen = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
//        getWindow().setStatusBarColor(Color.parseColor("#150B1F"));

        if (!MySocketManager.getInstance().globalConnecting || !MySocketManager.getInstance().globalConnected) {
            getApp().initGlobalSocket();
        }

        initRequest();
        initMain();

    }

    private void initRequest() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if ((ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        } else {
            if ((ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }

        if (!sessionManager.getBooleanValue(Const.POLICY_ACCEPTED)) {
            new PrivacyPopup_g(this, new PrivacyPopup_g.OnSubmitClickListnear() {
                @Override
                public void onAccept() {
                    sessionManager.saveBooleanValue(Const.POLICY_ACCEPTED, true);
                }

                @Override
                public void onDeny() {
                    finishAffinity();
                }

            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, getResources().getString(R.string.per_deny), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } else {
            if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, getResources().getString(R.string.per_deny), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }
        initMain();
    }

    private void initMain() {
        getStrickers();
        getAdsKeys();
        startReceiver();
        handleBranchData();
        makeOnlineUser();
        initBottomBar();
        screenSlidePagerAdapter = new ScreenSlidePagerAdapter(MainActivity.this);
        binding.viewpagerMain.setAdapter(screenSlidePagerAdapter);
        binding.viewpagerMain.setUserInputEnabled(false);

        binding.ivLive.setOnClickListener(view -> {
//            startActivity(new Intent(this, GotoLiveActivityNew.class));
            startActivity(new Intent(this, BeautyFilterActivity.class));
        });

    }

    private void handleBranchData() {
        Intent intent = getIntent();
        String branchData = intent.getStringExtra(Const.DATA);
        String type = intent.getStringExtra(Const.TYPE);
        if (branchData != null && !branchData.isEmpty()) {
            switch (type) {
                case "POST" -> {
                    PostRoot.PostItem post = new Gson().fromJson(branchData, PostRoot.PostItem.class);
                    List<PostRoot.PostItem> list = new ArrayList<>();
                    list.add(post);
                    startActivity(new Intent(this, FeedListActivity.class).putExtra(Const.POSITION, 0).putExtra(Const.DATA, new Gson().toJson(list)));

                }
                case "RELITE" -> {
                    ReliteRoot.VideoItem post = new Gson().fromJson(branchData, ReliteRoot.VideoItem.class);
                    List<ReliteRoot.VideoItem> list = new ArrayList<>();
                    list.add(post);
                    startActivity(new Intent(this, ReelsActivity.class).putExtra(Const.POSITION, 0).putExtra(Const.DATA, new Gson().toJson(list)));
                }
                case "PROFILE" -> {
                    startActivity(new Intent(this, GuestActivity.class).putExtra(Const.USERID, branchData));
                }
                case "LIVE" -> {
                    LiveUserRoot.UsersItem usersItem = new Gson().fromJson(branchData, LiveUserRoot.UsersItem.class);
                    Log.d("TAG", "handleBranchData: live  " + usersItem.toString());
                    startActivity(new Intent(this, WatchLiveActivity.class).putExtra(Const.DATA, new Gson().toJson(usersItem)));
                }
            }
        }
    }

    private void setUpFragment(int position) {
        binding.viewpagerMain.setCurrentItem(position);
    }

    @SuppressLint("NonConstantResourceId")
    private void initBottomBar() {
        binding.bottomNavigationView.setItemIconTintList(null);
        binding.bottomNavigationView.setSelectedItemId(R.id.miHome);

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.miHome -> {
                    position = 0;
                    setUpFragment(0);
                    return true;
                }
                case R.id.miRandomCall -> {
                    position = 1;
                    setUpFragment(1);
                    return true;
                }
                case R.id.miFeed -> {
                    position = 2;
                    setUpFragment(2);
                    return true;
                }
                case R.id.miMessage -> {
                    position = 3;
                    setUpFragment(3);
                    return true;
                }
                default -> {
                    return false;
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        unregisterNetworkChanges();
        MainApplication.isAppOpen = false;
        super.onDestroy();
    }

    protected void startReceiver() {
        netWorkChangeReceiver = new NetWorkChangeReceiver(this::showHideInternet);
        registerNetworkBroadcastForNougat();
    }

    private void registerNetworkBroadcastForNougat() {
        registerReceiver(netWorkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    protected void unregisterNetworkChanges() {
        try {
            unregisterReceiver(netWorkChangeReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

    }

    private void showHideInternet(Boolean isOnline) {
        Log.d("TAG", "showHideInternet: " + isOnline);
        final TextView tvInternetStatus = findViewById(R.id.tv_internet_status);

        if (isOnline) {
            if (tvInternetStatus != null && tvInternetStatus.getVisibility() == View.VISIBLE && tvInternetStatus.getText().toString().equalsIgnoreCase(getString(R.string.no_internet_connection))) {
                tvInternetStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
                tvInternetStatus.setText(R.string.back_online);
                new Handler().postDelayed(() -> slideToTop(tvInternetStatus), 200);
            }
        } else {
            if (tvInternetStatus != null) {
                tvInternetStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.red));
                tvInternetStatus.setText(R.string.no_internet_connection);
                if (tvInternetStatus.getVisibility() == View.GONE) {
                    slideToBottom(tvInternetStatus);
                }
            }
        }
    }

    private void slideToTop(View view) {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.enter_up);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

        });
        view.startAnimation(animation);
    }

    private void slideToBottom(final View view) {

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.enter_down);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

        });

        view.startAnimation(animation);
    }

    public void changeFragment(int position, int id) {
        this.position = position;
        binding.bottomNavigationView.setSelectedItemId(id);
        setUpFragment(position);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpFragment(position);
    }

    @Override
    public void onBackPressed() {

        if (binding.viewpagerMain.getCurrentItem() == 0) {
            Dialog dialog = new Dialog(MainActivity.this);
            dialog.setContentView(R.layout.exitdialog);
            dialog.setCanceledOnTouchOutside(false);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            TextView yes = dialog.findViewById(R.id.tv_exit);
            TextView no = dialog.findViewById(R.id.tvno);

            yes.setOnClickListener(v -> {
                super.onBackPressed();
                dialog.dismiss();
                finishAffinity();
            });

            no.setOnClickListener(v -> {
                dialog.dismiss();

            });

            dialog.show();
        } else {
            changeFragment(0, R.id.miHome);
        }

    }
}