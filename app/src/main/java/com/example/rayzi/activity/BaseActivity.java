package com.example.rayzi.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.example.rayzi.MainApplication;
import com.example.rayzi.NetWorkChangeReceiver;
import com.example.rayzi.R;
import com.example.rayzi.RayziUtils;
import com.example.rayzi.SessionManager;
import com.example.rayzi.agora.rtc.EventHandler;
import com.example.rayzi.modelclass.AdsRoot;
import com.example.rayzi.modelclass.RestResponse;
import com.example.rayzi.modelclass.StickerRoot;
import com.example.rayzi.retrofit.Const;
import com.example.rayzi.retrofit.RetrofitBuilder;
import com.example.rayzi.retrofit.UserApiCall;
import com.example.rayzi.socket.CallHandler;
import com.example.rayzi.socket.MySocketManager;
import com.example.rayzi.videocall.CallIncomeActivity;
import com.google.gson.JsonObject;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;
import com.yalantis.ucrop.UCrop;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class BaseActivity extends AppCompatActivity {
    public static final String TAG = "baseactivirty";
    public static boolean STATUS_LIVE = false;
    public static boolean STATUS_VIDEO_CALL = false;

    protected SessionManager sessionManager;
    protected UserApiCall userApiCall;
    private static final String SAMPLE_CROPPED_IMAGE_NAME = "cropimage";
    private NetWorkChangeReceiver netWorkChangeReceiver;

    CallHandler callHandler = new CallHandler() {
        @Override
        public void onCallRequest(Object[] args1) {
            if (args1 != null) {
                Log.d(TAG, "EVENT_CALL_REQUEST  : " + args1.toString());
                try {

                    JSONObject jsonObject = new JSONObject(args1[0].toString());
                    String userId1 = jsonObject.getString(Const.USERID1);
                    String userId2 = jsonObject.getString(Const.USERID2);
                    String user2Name = jsonObject.getString(Const.USER2_NAME);
                    String user2Image = jsonObject.getString(Const.USER2_IMAGE);
                    String callRoomId = jsonObject.getString(Const.CALL_ROOM_ID);
                    if (userId1.equals(sessionManager.getUser().getId())) {
                        Log.d(TAG, "getGlobalSocket: is In CALl   " + BaseActivity.STATUS_VIDEO_CALL);
                        Log.d(TAG, "getGlobalSocket: is In CALl   " + BaseActivity.STATUS_LIVE);
                        if (!BaseActivity.STATUS_VIDEO_CALL && !BaseActivity.STATUS_LIVE) {
                            BaseActivity.STATUS_VIDEO_CALL = true;
                            Log.d(TAG, "getGlobalSocket:call Object " + jsonObject);
                            startActivity(new Intent(BaseActivity.this, CallIncomeActivity.class).putExtra(Const.DATA, jsonObject.toString()));
                        } else {
                            // logic here   he is in other call
                        }
                    }
                } catch (JSONException e) {
                    Log.d(TAG, "getGlobalSocket: err " + e.toString());
                    e.printStackTrace();
                }


            }
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

        }

        @Override
        public void onCallDisconnect(Object[] args) {

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();

        MySocketManager.getInstance().addCallListener(callHandler);

        sessionManager = new SessionManager(this);
        userApiCall = new UserApiCall(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        decorView.setOnApplyWindowInsetsListener((v, insets) -> {
            WindowInsets defaultInsets = v.onApplyWindowInsets(insets);
            return defaultInsets.replaceSystemWindowInsets(
                    defaultInsets.getSystemWindowInsetLeft(),
                    0,
                    defaultInsets.getSystemWindowInsetRight(),
                    defaultInsets.getSystemWindowInsetBottom());
        });

        ViewCompat.requestApplyInsets(decorView);


        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.layout_no_internet, null);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.gravity = Gravity.BOTTOM;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        getWindow().addContentView(layout, params);
    }

    public static boolean isRTL(Context context) {

        Configuration config = context.getResources().getConfiguration();
        if (config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            return true;
        } else {
            return false;
        }
    }

    public void doTransition(int type) {
        if (type == Const.BOTTOM_TO_UP) {
            overridePendingTransition(R.anim.enter_from_bottom, R.anim.exit_none);
        } else if (type == Const.UP_TO_BOTTOM) {
            overridePendingTransition(R.anim.exit_none, R.anim.enter_from_up);
        }
    }

    public void getAdsKeys() {
        Call<AdsRoot> call = RetrofitBuilder.create().getAds();
        call.enqueue(new Callback<AdsRoot>() {
            @Override
            public void onResponse(Call<AdsRoot> call, Response<AdsRoot> response) {
                if (response.code() == 200 && response.body().isStatus()) {
                    if (response.body().getAdvertisement() != null) {
                        sessionManager.saveAds(response.body().getAdvertisement());
                    }
                }
            }

            @Override
            public void onFailure(Call<AdsRoot> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    public void getStrickers() {
        Call<StickerRoot> call = RetrofitBuilder.create().getStickers();
        call.enqueue(new Callback<StickerRoot>() {
            @Override
            public void onResponse(Call<StickerRoot> call, Response<StickerRoot> response) {
                if (response.code() == 200 && response.body().isStatus()) {
                    if (!response.body().getSticker().isEmpty()) {
                        RayziUtils.setstickers(response.body().getSticker());
                    }
                }
            }

            @Override
            public void onFailure(Call<StickerRoot> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    public MainApplication getApp() {
        return ((MainApplication) getApplication());
    }

    public void makeOnlineUser() {
        if (sessionManager.getBooleanValue(Const.ISLOGIN)) {
            try {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("userId", sessionManager.getUser().getId());
                Call<RestResponse> call = RetrofitBuilder.create().makeOnlineUser(jsonObject);
                call.enqueue(new Callback<RestResponse>() {
                    @Override
                    public void onResponse(Call<RestResponse> call, Response<RestResponse> response) {
                        if (response.code() == 200) {
                            if (response.body().isStatus()) {
                                Log.d(TAG, "onResponse: user online now");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<RestResponse> call, Throwable t) {
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    protected MainApplication application() {
        return (MainApplication) getApplication();
    }

    protected void registerRtcEventHandler(EventHandler handler) {
        application().registerEventHandler(handler);
    }

    protected void removeRtcEventHandler(EventHandler handler) {
        application().removeEventHandler(handler);
    }

    public void onClickBack(View view) {
        onBackPressed();
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
        Log.d(TAG, "showHideInternet: " + isOnline);
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

    public void requestPermissionIfNeeded(List<String> permissions, RequestCallback requestCallback) {
        boolean allGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
            }
        }
        if (allGranted) {
            requestCallback.onResult(true, permissions, new ArrayList<>());
            return;
        }

        PermissionX.init(this).permissions(permissions).onExplainRequestReason((scope, deniedList) -> {
            String message = "";
            if (permissions.size() == 1) {
                if (deniedList.contains(Manifest.permission.CAMERA)) {
                    message = this.getString(R.string.permission_explain_camera);
                } else if (deniedList.contains(Manifest.permission.READ_MEDIA_IMAGES)) {
                    message = this.getString(R.string.permission_explain_gallery);
                }
            } else {
                if (deniedList.size() == 1) {
                    if (deniedList.contains(Manifest.permission.CAMERA)) {
                        message = this.getString(R.string.permission_explain_camera);
                    } else if (deniedList.contains(Manifest.permission.READ_MEDIA_IMAGES)) {
                        message = this.getString(R.string.permission_explain_gallery);
                    } else if (deniedList.contains(Manifest.permission.READ_MEDIA_IMAGES)) {
                        message = this.getString(R.string.permission_explain_gallery);
                    }
                } else {
                    message = this.getString(R.string.permission_explain_camera_mic);
                }
            }
            scope.showRequestReasonDialog(deniedList, message, getString(R.string.ok));
        }).onForwardToSettings((scope, deniedList) -> {
            String message = "";
            if (permissions.size() == 1) {
                if (deniedList.contains(Manifest.permission.CAMERA)) {
                    message = this.getString(R.string.settings_camera);
                } else if (deniedList.contains(Manifest.permission.READ_MEDIA_IMAGES)) {
                    message = this.getString(R.string.settings_images);
                }
            } else {
                if (deniedList.size() == 1) {
                    if (deniedList.contains(Manifest.permission.CAMERA)) {
                        message = this.getString(R.string.settings_camera);
                    } else if (deniedList.contains(Manifest.permission.READ_MEDIA_IMAGES)) {
                        message = this.getString(R.string.settings_images);
                    }
                } else {
                    message = this.getString(R.string.settings_camera_mic);
                }
            }
            scope.showForwardToSettingsDialog(deniedList, message, getString(R.string.settings),
                    getString(R.string.cancel));
        }).request((allGranted1, grantedList, deniedList) -> {
            if (requestCallback != null) {
                requestCallback.onResult(allGranted1, grantedList, deniedList);
            }
        });
    }

    public void startCropActivity(@NonNull Uri uri) {
        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(getCacheDir(), SAMPLE_CROPPED_IMAGE_NAME + System.currentTimeMillis() + ".png"))).useSourceImageAspectRatio();
        UCrop.Options options = new UCrop.Options();
        options.setActiveControlsWidgetColor(ContextCompat.getColor(this, R.color.pink));
        options.setToolbarWidgetColor(ContextCompat.getColor(this, R.color.pink));
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.blacklight));
        options.setToolbarColor(ContextCompat.getColor(this, R.color.blacklight));
        options.setCropFrameColor(ContextCompat.getColor(this, R.color.blackpure));
        options.setDimmedLayerColor(ContextCompat.getColor(this, R.color.blackpure));
        options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        uCrop.withOptions(options);
        uCrop.start(this);
    }

    public String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        MySocketManager.getInstance().removeCallListener(callHandler);

    }
}
