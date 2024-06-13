package com.example.rayzi.activity;

import static com.google.android.exoplayer2.Player.STATE_BUFFERING;
import static com.google.android.exoplayer2.Player.STATE_ENDED;
import static com.google.android.exoplayer2.Player.STATE_IDLE;
import static com.google.android.exoplayer2.Player.STATE_READY;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProviders;

import com.example.rayzi.R;
import com.example.rayzi.SessionManager;
import com.example.rayzi.databinding.ActivityVideoCallFakeBinding;
import com.example.rayzi.modelclass.ChatUserListRoot;
import com.example.rayzi.modelclass.UserRoot;
import com.example.rayzi.retrofit.Const;
import com.example.rayzi.retrofit.RetrofitBuilder;
import com.example.rayzi.viewModel.FakeVideoCallViewModel;
import com.example.rayzi.viewModel.ViewModelFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.concurrent.ExecutionException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FakeVideoCallActivity extends AppCompatActivity {
    private static final String TAG = "fakevideocallact";
    ActivityVideoCallFakeBinding binding;
    SessionManager sessionManager;
    FakeVideoCallViewModel viewModel;
    private SimpleExoPlayer player;
    private String videoURL;
    ChatUserListRoot.ChatUserItem user;
    private ProcessCameraProvider cameraProvider;
    private CameraSelector cameraSelector;
    private Preview preview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_call_fake);
        viewModel = ViewModelProviders.of(this, new ViewModelFactory(new FakeVideoCallViewModel()).createFor()).get(FakeVideoCallViewModel.class);
        sessionManager = new SessionManager(this);
        Intent intent = getIntent();
        String userStr = intent.getStringExtra(Const.CHATROOM);
        Log.e(TAG, "onCreate: >>>>>>>>>>>> " + userStr);
        if (userStr != null && !userStr.isEmpty()) {
            user = new Gson().fromJson(userStr, ChatUserListRoot.ChatUserItem.class);
            videoURL = user.getLink();
        }
        initCamera();
        setPlayer();
        getCoin();

        binding.btnDecline.setOnClickListener(v -> {
            onBackPressed();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        initCamera();
    }

    public MediaSource buildMediaSource(Uri uri) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, "exoplayer-codelab");
        return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
    }

    private void setPlayer() {
        player = new SimpleExoPlayer.Builder(this).build();
        binding.playerview.setPlayer(player);
//        binding.playerview.setShowBuffering(1);
        Log.d(TAG, "setvideoURL: " + videoURL);
        Uri uri = Uri.parse(videoURL);
        MediaSource mediaSource = buildMediaSource(uri);
        Log.d(TAG, "initializePlayer: " + uri);
        player.setPlayWhenReady(true);
        player.seekTo(0, 0);
        player.prepare(mediaSource, false, false);
        player.setRepeatMode(Player.REPEAT_MODE_ALL);
        player.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                switch (playbackState) {
                    case STATE_BUFFERING -> Log.d(TAG, "buffer: " + uri);
                    case STATE_ENDED -> {
                        Toast.makeText(FakeVideoCallActivity.this, "Call Ended!", Toast.LENGTH_SHORT).show();
                        new Handler().postDelayed(() -> finish(), 2000);
                        Log.d(TAG, "end: " + uri);
                    }
                    case STATE_IDLE -> Log.d(TAG, "idle: " + uri);
                    case STATE_READY ->
//                        binding.animationView.setVisibility(View.GONE);
//                        if (!timestarted) {
//                            timestarted = true;
//                            timerHandler.postDelayed(timerRunnable, 1000);
//                        }

                            Log.d(TAG, "ready: " + uri);
                    default -> {
                    }
                }
            }
        });

    }


    @SuppressLint("RestrictedApi")
    private void initCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        } else {
            final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

            cameraProviderFuture.addListener(new Runnable() {
                @Override
                public void run() {
                    try {
                        cameraProvider = cameraProviderFuture.get();

                        cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

                        preview = new Preview.Builder().build();

                        preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

                        cameraProvider.unbindAll();

                        cameraProvider.bindToLifecycle((LifecycleOwner) FakeVideoCallActivity.this, cameraSelector, preview);

                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, ContextCompat.getMainExecutor(this));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.setPlayWhenReady(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.setPlayWhenReady(true);
        }
    }

    @Override
    protected void onDestroy() {
        if (player != null) {
            player.release();
        }
        super.onDestroy();
    }

    private void switchCamera() {
        if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
        } else {
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
        }
        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(this, cameraSelector, preview);
    }

    public void onSwitchCameraClicked(View view) {
        switchCamera();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    private void getCoin() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("senderUserId", sessionManager.getUser().getId());
        jsonObject.addProperty("coin", sessionManager.getSetting().getCallCharge());
        jsonObject.addProperty("receiverUserId", "");
        jsonObject.addProperty("type", Const.CAll);
        Call<UserRoot> call;
        call = RetrofitBuilder.create().getCoin(jsonObject);
        call.enqueue(new Callback<UserRoot>() {
            @Override
            public void onResponse(Call<UserRoot> call, Response<UserRoot> response) {
                if (response.code() == 200) {
                    if (response.body().isStatus()) {
                        Log.e("TAG", "onResponse: >>>>>>>>>>>> " + response.body().getUser());
//                        binding.tvCoin.setText(String.valueOf(response.body().getSenderUser().getDiamond()));

                    }
                }
            }

            @Override
            public void onFailure(Call<UserRoot> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}