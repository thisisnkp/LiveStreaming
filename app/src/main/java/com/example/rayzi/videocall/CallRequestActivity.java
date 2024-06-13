package com.example.rayzi.videocall;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.example.rayzi.MainApplication;
import com.example.rayzi.R;
import com.example.rayzi.activity.BaseActivity;
import com.example.rayzi.databinding.ActivityCallRequestBinding;
import com.example.rayzi.modelclass.CallRequestRoot;
import com.example.rayzi.modelclass.GuestProfileRoot;
import com.example.rayzi.retrofit.Const;
import com.example.rayzi.retrofit.RetrofitBuilder;
import com.example.rayzi.socket.CallHandler;
import com.example.rayzi.socket.MySocketManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CallRequestActivity extends VideoCallBaseActivity {

    private static final String TAG = "callReqAct";
    ActivityCallRequestBinding binding;
    Handler handler = new Handler();
    private GuestProfileRoot.User guestUser;
    Runnable runnable = () -> {
        Toast.makeText(CallRequestActivity.this, guestUser.getName() + " is busy with someone else.", Toast.LENGTH_SHORT).show();
        finish();
    };

    CallHandler callHandler = new CallHandler() {
        @Override
        public void onCallRequest(Object[] args) {

        }

        @Override
        public void onCallReceive(Object[] args) {

        }

        @Override
        public void onCallConfirm(Object[] args) {
            runOnUiThread(() -> {
                if (args != null) {
                    Log.d(TAG, "callConfirmLister: " + args[0].toString());
                    try {
                        JSONObject jsonObject = new JSONObject(args[0].toString());
                        String userId1 = jsonObject.getString(Const.USERID1);
                        String userId2 = jsonObject.getString(Const.USERID2);
                        boolean isConfirm = jsonObject.getBoolean(Const.ISCONFIRM);
                        if (userId1.equals(guestUser.getUserId())) {
                            if (userId2.equals(sessionManager.getUser().getId())) {
                                if (isConfirm) {
                                    binding.tvStatus.setText("Ringing....");
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
        }

        @Override
        public void onCallAnswer(Object[] args) {
            runOnUiThread(() -> {
                if (args != null) {
                    Log.d(TAG, "callAnswerLister: " + args[0].toString());
                    try {
                        JSONObject jsonObject = new JSONObject(args[0].toString());  // required feild  token channel
                        String userId1 = jsonObject.getString(Const.USERID1);
                        String userId2 = jsonObject.getString(Const.USERID2);
                        String token = jsonObject.getString(Const.TOKEN);
                        String callRoomId = jsonObject.getString(Const.CALL_ROOM_ID);
                        String channel = jsonObject.getString(Const.CHANNEL);
                        Log.d(TAG, "guest id : " + guestUser.getUserId());
                        Log.d(TAG, "local  id : " + sessionManager.getUser().getId());
                        boolean isAccept = jsonObject.getBoolean(Const.ISACCEPT);
                        if (userId1.equals(guestUser.getUserId())) {
                            if (userId2.equals(sessionManager.getUser().getId())) {
                                if (isAccept) {
                                    Intent intent = new Intent(CallRequestActivity.this, VideoCallActivity.class);
                                    intent.putExtra(Const.USERID, userId1);
                                    intent.putExtra(Const.TOKEN, token);
                                    intent.putExtra(Const.CHANNEL, channel);
                                    intent.putExtra(Const.CALL_ROOM_ID, callRoomId);
                                    intent.putExtra(Const.CALL_BY_ME, true);
                                    intent.putExtra("type", getIntent().getStringExtra("type"));
                                    startActivity(intent);

                                } else {
                                    Toast.makeText(CallRequestActivity.this, "Call Declined", Toast.LENGTH_SHORT).show();
                                }
                                finish();
                                BaseActivity.STATUS_VIDEO_CALL = true;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
        }

        @Override
        public void onCallCancel(Object[] args) {

        }

        @Override
        public void onCallDisconnect(Object[] args) {

        }
    };
    private String callRoomId;
    //    private Socket callRoomSocket;
    private String agoraToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_call_request);
        BaseActivity.STATUS_VIDEO_CALL = true;

        MySocketManager.getInstance().addCallListener(callHandler);

        Intent intent = getIntent();
        String userData = intent.getStringExtra(Const.USER);
        if (userData != null && !userData.isEmpty()) {
            guestUser = new Gson().fromJson(userData, GuestProfileRoot.User.class);
            Log.d(TAG, "onCreate: guest user  " + guestUser.toString());

            Glide.with(this).load(guestUser.getImage())
                    .apply(MainApplication.requestOptions)
                    .circleCrop().into(binding.imgUser);
            binding.tvName.setText(guestUser.getName());

            makeCallRequest();
            handler.postDelayed(runnable, 30000);
        }
        binding.btnDecline.setOnClickListener(v -> onBackPressed());
    }

    private void makeCallRequest() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("callerUserId", sessionManager.getUser().getId());
        jsonObject.addProperty("receiverUserId", guestUser.getUserId());
        jsonObject.addProperty("channel", guestUser.getUserId());
        jsonObject.addProperty("callType", getIntent().getStringExtra("type"));
        Call<CallRequestRoot> call = RetrofitBuilder.create().makeCallRequest(jsonObject);
        call.enqueue(new Callback<CallRequestRoot>() {
            @Override
            public void onResponse(Call<CallRequestRoot> call, Response<CallRequestRoot> response) {
                if (response.code() == 200) {
                    if (response.body().isStatus()) {
                        callRoomId = response.body().getCallId();
                        agoraToken = response.body().getToken();
                        Log.d(TAG, "onResponse: " + callRoomId);
                        initMain();
                    }
                }
            }

            @Override
            public void onFailure(Call<CallRequestRoot> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void initMain() {
        try {
            JSONObject callReqObject = new JSONObject();
            callReqObject.put(Const.USERID1, guestUser.getUserId());
            callReqObject.put(Const.USERID2, sessionManager.getUser().getId());
            callReqObject.put(Const.USER2_NAME, sessionManager.getUser().getName());
            callReqObject.put(Const.USER2_IMAGE, sessionManager.getUser().getImage());
            callReqObject.put(Const.CALL_ROOM_ID, callRoomId);
            callReqObject.put(Const.TOKEN, agoraToken);

            Log.d(TAG, "initMain:call req send  " + callReqObject);

            MySocketManager.getInstance().getSocket().emit(Const.EVENT_CALL_REQUEST, callReqObject);
            binding.tvStatus.setText("Calling...");

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        MySocketManager.getInstance().removeCallListener(callHandler);
        handler.removeCallbacks(runnable);
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        try {
            JSONObject callReqObject = new JSONObject();
            callReqObject.put(Const.USERID1, guestUser.getUserId());
            callReqObject.put(Const.USERID2, sessionManager.getUser().getId());
            callReqObject.put(Const.USER2_NAME, sessionManager.getUser().getName());
            callReqObject.put(Const.USER2_IMAGE, sessionManager.getUser().getImage());
            callReqObject.put(Const.CALL_ROOM_ID, callRoomId);

            Log.d(TAG, "initMain:call req send  " + callReqObject);

            MySocketManager.getInstance().getSocket().emit(Const.EVENT_CALL_CANCEL, callReqObject);
            handler.removeCallbacks(runnable);
            handler.removeCallbacksAndMessages(null);

            //  todo ios ma pending

        } catch (JSONException e) {
            e.printStackTrace();
        }

        BaseActivity.STATUS_VIDEO_CALL = false;
        finish();
    }
}