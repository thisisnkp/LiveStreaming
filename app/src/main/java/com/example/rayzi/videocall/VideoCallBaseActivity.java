package com.example.rayzi.videocall;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.example.rayzi.activity.BaseActivity;

public class VideoCallBaseActivity extends BaseActivity {


    private static final String TAG = "callBaseact";

    @Override
    protected void onStart() {
        super.onStart();
        BaseActivity.STATUS_VIDEO_CALL = true;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseActivity.STATUS_VIDEO_CALL = true;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
