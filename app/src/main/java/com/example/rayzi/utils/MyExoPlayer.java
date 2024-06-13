package com.example.rayzi.utils;

import android.content.Context;
import android.net.Uri;

import com.example.rayzi.MainApplication;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;

public class MyExoPlayer {

    private SimpleExoPlayer player;

    public static MyExoPlayer getInstance() {
        return MyExoPlayer.Holder.INSTANCE;
    }

    public SimpleExoPlayer getPlayer() {

        return player;
    }

    public void createPlayer(Context activity, String videoUrl) {
        player = new SimpleExoPlayer.Builder(activity).build();
        SimpleCache simpleCache = MainApplication.simpleCache;
        CacheDataSourceFactory cacheDataSourceFactory = new CacheDataSourceFactory(simpleCache, new DefaultHttpDataSourceFactory(Util.getUserAgent(activity, "TejTok")), CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);
        ProgressiveMediaSource progressiveMediaSource = new ProgressiveMediaSource.Factory(cacheDataSourceFactory).createMediaSource(Uri.parse(videoUrl));
        player.setPlayWhenReady(true);
        player.seekTo(0, 0);
        player.setRepeatMode(Player.REPEAT_MODE_ALL);
        player.prepare(progressiveMediaSource, true, false);
    }

    private static final class Holder {

        private static final MyExoPlayer INSTANCE = new MyExoPlayer();
        private Context context;

        public Holder(Context context) {

            this.context = context;
        }
    }
}
