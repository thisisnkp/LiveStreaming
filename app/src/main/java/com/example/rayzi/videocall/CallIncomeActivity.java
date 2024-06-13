package com.example.rayzi.videocall;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.example.rayzi.MainApplication;
import com.example.rayzi.R;
import com.example.rayzi.activity.BaseActivity;
import com.example.rayzi.databinding.ActivityCallIncomeBinding;
import com.example.rayzi.retrofit.Const;
import com.example.rayzi.socket.CallHandler;
import com.example.rayzi.socket.MySocketManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class CallIncomeActivity extends VideoCallBaseActivity {
    private static final String TAG = "callincome";
    ActivityCallIncomeBinding binding;
    CallHandler callHandler = new CallHandler() {
        @Override
        public void onCallRequest(Object[] args) {

        }

        @Override
        public void onCallReceive(Object[] args) {

        }

        @Override
        public void onCallConfirm(Object[] args) {

        }

        @Override
        public void onCallAnswer(Object[] args) {

        }

        @Override
        public void onCallCancel(Object[] args) {
            runOnUiThread(() -> {
                if (args != null) {
                    Log.d(TAG, "callcancelLister: " + args[0].toString());
                    try {
                        JSONObject jsonObject = new JSONObject(args[0].toString());
                        String userId1 = jsonObject.getString(Const.USERID1);
                        String userId2 = jsonObject.getString(Const.USERID2);

                        if (userId1.equals(sessionManager.getUser().getId())) {
                            BaseActivity.STATUS_VIDEO_CALL = false;
                            onBackPressed();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
        }

        @Override
        public void onCallDisconnect(Object[] args) {

        }
    };
    private Vibrator v;
    private MediaPlayer player2;
    private String onotherUserId;
    private String callRoomId;
    private String agoraToken = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_call_income);

        MySocketManager.getInstance().addCallListener(callHandler);

        BaseActivity.STATUS_VIDEO_CALL = true;
        Intent intent = getIntent();
        String dataStr = intent.getStringExtra(Const.DATA);
        boolean isAcceptClicked = intent.getBooleanExtra(Const.ISACCEPT_CLICK, false);

        if (dataStr != null && !dataStr.isEmpty()) {
            try {
                Log.d(TAG, "onCreate: call receive");
                JSONObject jsonObject = new JSONObject(dataStr);
                onotherUserId = jsonObject.getString(Const.USERID2);
                callRoomId = jsonObject.getString(Const.CALL_ROOM_ID);
                agoraToken = jsonObject.getString(Const.TOKEN);
                String otherUserName = jsonObject.getString(Const.USER2_NAME);
                String otherUserImage = jsonObject.getString(Const.USER2_IMAGE);
                Log.d(TAG, "onCreate: " + jsonObject);
                Glide.with(this).load(otherUserImage)
                        .apply(MainApplication.requestOptions)
                        .circleCrop().into(binding.imgUser);
                binding.tvUserName.setText(otherUserName);
                try {
                    jsonObject.put(Const.ISCONFIRM, true);
                    Log.d(TAG, "callISCONFIRM : " + jsonObject);
                    MySocketManager.getInstance().getSocket().emit(Const.EVENT_CALL_CONFIRMED, jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                initSound();
                initListener();

                if (isAcceptClicked) { //todo
                    // binding.btnAccept.performClick();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private void initListener() {

        binding.btnAccept.setOnClickListener(v1 -> {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(Const.USERID1, sessionManager.getUser().getId());
                jsonObject.put(Const.USERID2, onotherUserId);
                jsonObject.put(Const.TOKEN, agoraToken);
                jsonObject.put(Const.CALL_ROOM_ID, callRoomId);
                jsonObject.put(Const.CHANNEL, sessionManager.getUser().getId());
                jsonObject.put(Const.ISACCEPT, true);

                Log.d(TAG, "initListner: callAns " + jsonObject);
                MySocketManager.getInstance().getSocket().emit(Const.EVENT_CALL_ANSWER, jsonObject);

                Intent intent = new Intent(CallIncomeActivity.this, VideoCallActivity.class);
                intent.putExtra(Const.USERID, onotherUserId);
                intent.putExtra(Const.TOKEN, agoraToken);
                intent.putExtra(Const.CALL_ROOM_ID, callRoomId);
                intent.putExtra(Const.CHANNEL, sessionManager.getUser().getId());
                intent.putExtra(Const.CALL_BY_ME, false);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
            finish();

        });

        binding.btnDecline.setOnClickListener(v1 -> {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(Const.USERID1, sessionManager.getUser().getId());
                jsonObject.put(Const.USERID2, onotherUserId);
                jsonObject.put(Const.TOKEN, agoraToken);
                jsonObject.put(Const.CALL_ROOM_ID, callRoomId);
                jsonObject.put(Const.CHANNEL, sessionManager.getUser().getId());
                jsonObject.put(Const.ISACCEPT, false);
                MySocketManager.getInstance().getSocket().emit(Const.EVENT_CALL_ANSWER, jsonObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            BaseActivity.STATUS_VIDEO_CALL = false;
            onBackPressed();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MySocketManager.getInstance().removeCallListener(callHandler);
        //BaseActivity.STATUS_VIDEO_CALL = false;

    }

    @Override
    protected void onPause() {
        if (v != null) {
            v.cancel();
        }
        if (player2 != null) {
            player2.release();
        }

        super.onPause();

    }

    private void initSound() {
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
// Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            long[] pattern = {100, 200, 400, 500, 100, 200, 300, 400, 500, 100, 200, 300, 400, 500, 500, 200, 100, 500, 500, 500};
            v.vibrate(VibrationEffect.createWaveform(pattern, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            v.vibrate(500);
        }

        try {

            player2 = new MediaPlayer();
            try {
                AssetFileDescriptor afd2 = getAssets().openFd("call.mp3");
                player2.setDataSource(afd2.getFileDescriptor(), afd2.getStartOffset(), afd2.getLength());
                player2.prepare();

                player2.start();
            } catch (IOException e) {
                Log.d(TAG, "initUI: " + e.getMessage());
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "initUI: errrr " + e.getMessage());
        }
    }
}