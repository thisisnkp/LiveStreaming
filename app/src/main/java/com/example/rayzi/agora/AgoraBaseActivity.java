package com.example.rayzi.agora;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;

import androidx.annotation.Nullable;

import com.example.rayzi.MainApplication;
import com.example.rayzi.SessionManager;
import com.example.rayzi.activity.BaseActivity;
import com.example.rayzi.agora.rtc.Constants;
import com.example.rayzi.agora.rtc.EngineConfig;
import com.example.rayzi.agora.rtc.EventHandler;
import com.example.rayzi.agora.stats.StatsManager;

import java.io.IOException;

import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;

public abstract class AgoraBaseActivity extends BaseActivity implements EventHandler {
    private static final String TAG = "agorabaseactivity";
    SessionManager sessionManager;

    private MediaPlayer player2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        registerRtcEventHandler(this);
    }

    public void makeSound() {
        if (player2 != null) {
            player2.release();
            player2 = null;
        }
        try {
            player2 = new MediaPlayer();
            try {
                AssetFileDescriptor afd2 = getAssets().openFd("pop.mp3");
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


    protected MainApplication application() {
        return (MainApplication) getApplication();
    }

    protected StatsManager statsManager() {
        return application().statsManager();
    }

    protected RtcEngine rtcEngine() {
        return application().rtcEngine();
    }


    public void configVideo() {
        VideoEncoderConfiguration configuration = new VideoEncoderConfiguration(
                Constants.VIDEO_DIMENSIONS[config().getVideoDimenIndex()],
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
        );
        configuration.mirrorMode = Constants.VIDEO_MIRROR_MODES[config().getMirrorEncodeIndex()];
        rtcEngine().setVideoEncoderConfiguration(configuration);
    }

    protected SurfaceView prepareRtcVideo(int uid, boolean local) {
        // Render local/remote video on a SurfaceView

        SurfaceView surface = RtcEngine.CreateRendererView(getApplicationContext());
        if (local) {
            rtcEngine().setupLocalVideo(
                    new VideoCanvas(
                            surface,
                            VideoCanvas.RENDER_MODE_HIDDEN,
                            0,
                            Constants.VIDEO_MIRROR_MODES[config().getMirrorLocalIndex()]
                    )
            );
        } else {
            rtcEngine().setupRemoteVideo(
                    new VideoCanvas(
                            surface,
                            VideoCanvas.RENDER_MODE_HIDDEN,
                            uid,
                            Constants.VIDEO_MIRROR_MODES[config().getMirrorRemoteIndex()]
                    )
            );
        }
        return surface;
    }

    protected EngineConfig config() {
        return application().engineConfig();
    }

    protected void removeRtcVideo(int uid, boolean local) {
        if (rtcEngine() != null) {
            if (local) {
                rtcEngine().setupLocalVideo(null);
            } else {
                rtcEngine().setupRemoteVideo(new VideoCanvas(null, VideoCanvas.RENDER_MODE_HIDDEN, uid));
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (rtcEngine() != null) {
            rtcEngine().leaveChannel();
        }
        removeRtcEventHandler(this);
//        getSocket().disconnect();
    }

}
