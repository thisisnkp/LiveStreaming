package com.example.rayzi.activity;

import static com.google.android.exoplayer2.Player.STATE_BUFFERING;
import static com.google.android.exoplayer2.Player.STATE_ENDED;
import static com.google.android.exoplayer2.Player.STATE_IDLE;
import static com.google.android.exoplayer2.Player.STATE_READY;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.rayzi.BuildConfig;
import com.example.rayzi.R;
import com.example.rayzi.SessionManager;
import com.example.rayzi.bottomsheets.BottomSheetReport_g;
import com.example.rayzi.bottomsheets.BottomSheetReport_option;
import com.example.rayzi.bottomsheets.UserProfileBottomSheet;
import com.example.rayzi.databinding.ActivityFakeWatchLiveBinding;
import com.example.rayzi.emoji.EmojiBottomSheetFragment;
import com.example.rayzi.modelclass.GiftRoot;
import com.example.rayzi.modelclass.LiveStramComment;
import com.example.rayzi.modelclass.LiveUserRoot;
import com.example.rayzi.modelclass.UserRoot;
import com.example.rayzi.retrofit.Const;
import com.example.rayzi.retrofit.RetrofitBuilder;
import com.example.rayzi.viewModel.EmojiSheetViewModel;
import com.example.rayzi.viewModel.ViewModelFactory;
import com.example.rayzi.viewModel.WatchLiveViewModel;
import com.example.rayzi.z_demo.Demo_contents;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.List;

import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.util.ContentMetadata;
import io.branch.referral.util.LinkProperties;
import jp.wasabeef.glide.transformations.BlurTransformation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FakeWatchLiveActivity extends AppCompatActivity {

    private static final String TAG = "fakewatch";
    ActivityFakeWatchLiveBinding binding;
    LiveUserRoot.UsersItem fakeHost;
    String videoURL;
    SessionManager sessionManager;

    UserProfileBottomSheet userProfileBottomSheet;

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            viewModel.liveStramCommentAdapter.addSingleComment(Demo_contents.getLiveStreamComment().get(0));
            binding.rvComments.scrollToPosition(viewModel.liveStramCommentAdapter.getItemCount() - 1);
            handler.postDelayed(this, 2000);
        }
    };
    Handler handler = new Handler();
    EmojiBottomSheetFragment emojiBottomsheetFragment;
    private EmojiSheetViewModel giftViewModel;
    private SimpleExoPlayer player;
    private WatchLiveViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_fake_watch_live);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        viewModel = ViewModelProviders.of(this, new ViewModelFactory(new WatchLiveViewModel()).createFor()).get(WatchLiveViewModel.class);
        giftViewModel = ViewModelProviders.of(this, new ViewModelFactory(new EmojiSheetViewModel()).createFor()).get(EmojiSheetViewModel.class);
        sessionManager = new SessionManager(this);
        binding.setViewModel(viewModel);
        viewModel.initLister();
        giftViewModel.getGiftCategory();

        emojiBottomsheetFragment = new EmojiBottomSheetFragment();
        Intent intent = getIntent();
        String userStr = intent.getStringExtra(Const.DATA);
        sessionManager = new SessionManager(this);
        Log.e(TAG, "onCreate: >>>>>>>>>>>> " + sessionManager.getUser().isFake());
        if (userStr != null && !userStr.isEmpty()) {
            fakeHost = new Gson().fromJson(userStr, LiveUserRoot.UsersItem.class);
            Glide.with(this).load(fakeHost.getImage()).circleCrop().diskCacheStrategy(DiskCacheStrategy.ALL).into(binding.imgProfile);
            Log.d(TAG, "onCreate: fakeho==========" + fakeHost);
            videoURL = fakeHost.getLink();
            Log.d(TAG, "onCreate: link==================== " + fakeHost.getLink());
            Log.e(TAG, "onCreate: " + fakeHost.getLiveStreamingId());
            initView();
        }
        viewModel.liveStramCommentAdapter.addSingleComment(new LiveStramComment("", "", Demo_contents.getUsers(true).get(0), true));
        handler.postDelayed(runnable, 2000);
        initListener();
    }

    private void initListener() {
        binding.imggift2.setOnClickListener(v -> {

            if (!emojiBottomsheetFragment.isAdded()) {

                emojiBottomsheetFragment.show(getSupportFragmentManager(), "emojifragfmetn");
            }
        });

        binding.profileBox.setOnClickListener(view -> showProfileSheet());

        giftViewModel.finelGift.observe(this, giftItem -> {
            if (giftItem != null) {
                int totalCoin = giftItem.getCoin() * giftItem.getCount();
                if (sessionManager.getUser().getDiamond() < totalCoin) {
                    Toast.makeText(FakeWatchLiveActivity.this, "You not have enough diamonds to send gift", Toast.LENGTH_SHORT).show();
                    return;
                }

                getCoin(giftItem);

                String finalGiftLink = null;
                List<GiftRoot.GiftItem> giftItemList = sessionManager.getGiftsList(giftItem.getCategory());
                for (int i = 0; i < giftItemList.size(); i++) {
                    if (giftItem.getId().equals(giftItemList.get(i).getId())) {
                        finalGiftLink = BuildConfig.BASE_URL + giftItemList.get(i).getImage();
                    }
                }

                if (giftItem.getType() == 2) {
                    binding.svgaImage.setVisibility(View.VISIBLE);
                    SVGAImageView imageView = binding.svgaImage;
                    SVGAParser parser = new SVGAParser(FakeWatchLiveActivity.this);
                    try {
                        parser.decodeFromURL(new URL(finalGiftLink), new SVGAParser.ParseCompletion() {
                            @Override
                            public void onComplete(@NonNull SVGAVideoEntity svgaVideoEntity) {
                                SVGADrawable drawable = new SVGADrawable(svgaVideoEntity);
                                imageView.setImageDrawable(drawable);
                                imageView.startAnimation();
                                Log.d("TAG", "setData: " + giftItem.getImage());
                                new Handler(Looper.myLooper()).postDelayed(() -> {
                                    binding.svgaImage.setVisibility(View.GONE);
                                    binding.svgaImage.clear();
                                }, 5000);
                            }

                            @Override
                            public void onError() {

                            }
                        }, new SVGAParser.PlayCallback() {
                            @Override
                            public void onPlay(@NonNull List<? extends File> list) {
                            }
                        });

                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }

                } else {
                    binding.imgGift.setVisibility(View.VISIBLE);
                    Glide.with(this).load(finalGiftLink).diskCacheStrategy(DiskCacheStrategy.ALL).into(binding.imgGift);
                    new Handler(Looper.getMainLooper()).postDelayed(() -> binding.imgGift.setImageDrawable(null), 4000);
                }

                emojiBottomsheetFragment.dismiss();

            }
        });

    }

    private void getCoin(GiftRoot.GiftItem selectedGift) {
        Log.e("TAG", "getCoin: >>>>>>>>>>>>>>> " + selectedGift.getCoin());
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("senderUserId", sessionManager.getUser().getId());
        jsonObject.addProperty("coin", selectedGift.getCoin());
        jsonObject.addProperty("receiverUserId", "");
        jsonObject.addProperty("type", Const.LIVE);
        Call<UserRoot> call = RetrofitBuilder.create().getCoin(jsonObject);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<UserRoot> call, Response<UserRoot> response) {
                if (response.code() == 200) {
                    if (response.body().isStatus() && response.body().getUser() != null) {
                        Log.e("TAG", "onResponse: >>>>>>>>>>>> " + response.body().getUser());
                        emojiBottomsheetFragment.setCoin(response.body().getUser().getDiamond());
                    }
                }
            }

            @Override
            public void onFailure(Call<UserRoot> call, Throwable t) {

            }
        });
    }


    private void showProfileSheet() {

        userProfileBottomSheet = new UserProfileBottomSheet(this);
        userProfileBottomSheet.show(false, fakeHost, "");

    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        Log.e(TAG, "onCreate: " + fakeHost.getRCoin());
        setPlayer();
        binding.tvName.setText(fakeHost.getName());
        binding.tvCoins.setText(String.valueOf(fakeHost.getRCoin()));
        binding.tvGifts.setText(String.valueOf(Demo_contents.getRandomPostCoint()));

        LiveStramComment liveStramComment = new LiveStramComment("", "", sessionManager.getUser(), true);
        viewModel.liveStramCommentAdapter.addSingleComment(liveStramComment);
        binding.rvComments.scrollToPosition(viewModel.liveStramCommentAdapter.getItemCount() - 1);

        MultiTransformation<Bitmap> transformations = new MultiTransformation<>(
                new BlurTransformation(30),
                new CenterCrop()
        );

        MultiTransformation<Bitmap> transformations1 = new MultiTransformation<>(
                new BlurTransformation(30),
                new CenterCrop(),
                new CircleCrop()
        );
        Glide.with(FakeWatchLiveActivity.this)
                .load(fakeHost.getImage())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transform(transformations)
                .into(binding.backImageBlur);

        Glide.with(FakeWatchLiveActivity.this)
                .load(fakeHost.getImage())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transform(transformations1)
                .into(binding.imgUser);

    }

    private void setPlayer() {
        player = new SimpleExoPlayer.Builder(this).build();
        binding.playerview.setPlayer(player);
        //  binding.playerview.setShowBuffering(true);
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
                    case STATE_BUFFERING -> {
                        binding.backImageBlur.setVisibility(View.VISIBLE);
                        binding.pd.setVisibility(View.VISIBLE);
                        Log.d(TAG, "buffer: " + uri);
                    }
                    case STATE_ENDED -> {
                        Toast.makeText(FakeWatchLiveActivity.this, "Live Ended!", Toast.LENGTH_SHORT).show();
                        new Handler().postDelayed(() -> finish(), 2000);
                        Log.d(TAG, "end: " + uri);
                    }
                    case STATE_IDLE -> Log.d(TAG, "idle: " + uri);
                    case STATE_READY -> {
                        binding.backImageBlur.setVisibility(View.GONE);
                        binding.pd.setVisibility(View.GONE);
                        Log.d(TAG, "ready: " + uri);
                    }
                    default -> {
                    }
                }
            }
        });
    }

    private MediaSource buildMediaSource(Uri uri) {
        DataSource.Factory dataSourceFactory =
                new DefaultDataSourceFactory(this, "exoplayer-codelab");
        return new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri);
    }


    public void onClickBack(View view) {
        onBackPressed();
    }

    public void onClickSendComment(View view) {
        String comment = binding.etComment.getText().toString();
        if (!comment.isEmpty()) {
            LiveStramComment liveStramComment = new LiveStramComment(fakeHost.getLiveStreamingId(), comment, sessionManager.getUser(), false);
            viewModel.liveStramCommentAdapter.addSingleComment(liveStramComment);
            binding.rvComments.scrollToPosition(viewModel.liveStramCommentAdapter.getItemCount() - 1);
            binding.etComment.setText("");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
        if (player != null) {
            player.release();
        }
    }


    public void onclickShare(View view) {

        BranchUniversalObject buo = new BranchUniversalObject()
                .setCanonicalIdentifier("content/12345")
                .setTitle("Watch Live Video")
                .setContentDescription("By : " + fakeHost.getName())
                .setContentImageUrl(fakeHost.getImage())
                .setContentMetadata(new ContentMetadata().addCustomMetadata("type", "LIVE").addCustomMetadata(Const.DATA, new Gson().toJson(fakeHost)));

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
        if (fakeHost == null) return;
        new BottomSheetReport_option(FakeWatchLiveActivity.this, new BottomSheetReport_option.OnReportedListener() {
            @Override
            public void onReported() {
                new BottomSheetReport_g(FakeWatchLiveActivity.this, fakeHost.getId(), () -> {
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