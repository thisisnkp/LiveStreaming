package com.example.rayzi.liveStreamming;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.rayzi.BuildConfig;
import com.example.rayzi.R;
import com.example.rayzi.RayziUtils;
import com.example.rayzi.SessionManager;
import com.example.rayzi.activity.BaseActivity;
import com.example.rayzi.activity.FakeWatchLiveActivity;
import com.example.rayzi.agora.AgoraBaseActivity;
import com.example.rayzi.agora.stats.LocalStatsData;
import com.example.rayzi.agora.stats.RemoteStatsData;
import com.example.rayzi.agora.stats.StatsData;
import com.example.rayzi.agora.ui.VideoGridContainer;
import com.example.rayzi.bottomsheets.UserProfileBottomSheet;
import com.example.rayzi.databinding.ActivityHostLiveBinding;
import com.example.rayzi.emoji.EmojiBottomSheetFragment;
import com.example.rayzi.modelclass.GiftRoot;
import com.example.rayzi.modelclass.GuestProfileRoot;
import com.example.rayzi.modelclass.LiveStramComment;
import com.example.rayzi.modelclass.LiveStreamRoot;
import com.example.rayzi.modelclass.UserRoot;
import com.example.rayzi.popups.PopupBuilder;
import com.example.rayzi.retrofit.Const;
import com.example.rayzi.retrofit.RetrofitBuilder;
import com.example.rayzi.socket.LiveHandler;
import com.example.rayzi.socket.MySocketManager;
import com.example.rayzi.socket.SocketConnectHandler;
import com.example.rayzi.utils.Filters.FilterRoot;
import com.example.rayzi.utils.Filters.FilterUtils;
import com.example.rayzi.viewModel.EmojiSheetViewModel;
import com.example.rayzi.viewModel.HostLiveViewModel;
import com.example.rayzi.viewModel.ViewModelFactory;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.video.VideoEncoderConfiguration;
import io.agora.rtc2.IMediaExtensionObserver;
import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.util.ContentMetadata;
import io.branch.referral.util.LinkProperties;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HostLiveActivity extends AgoraBaseActivity implements IMediaExtensionObserver {

    static {
        System.loadLibrary("AgoraFaceUnityExtension");
    }
    public static final String TAG = "hostliveactivity";
    ActivityHostLiveBinding binding;
    SessionManager sessionManager;
    EmojiBottomSheetFragment emojiBottomsheetFragment;
    UserProfileBottomSheet userProfileBottomSheet;
    JSONArray blockedUsersList = new JSONArray();
    int seconds = 0;
    Handler timerHandler;
    Runnable timerRunnable;
    private HostLiveViewModel viewModel;
    private VideoGridContainer mVideoGridContainer;
    private EmojiSheetViewModel giftViewModel;
    private LiveStreamRoot.LiveUser liveUser;
    LiveHandler liveHandler = new LiveHandler() {
        @Override
        public void onSimpleFilter(Object[] args) {
            if (args[0] != null) {
                runOnUiThread(() -> {

                    String filtertype = null;

                    filtertype = args[0].toString();
                    FilterRoot filterRoot = new Gson().fromJson(filtertype, FilterRoot.class);
                    if (filterRoot != null) {
                        if (filterRoot.getTitle().equalsIgnoreCase("None")) {
                            Log.d(TAG, "initLister: null");
                            binding.imgFilter.setImageDrawable(null);
                        } else {
                            Log.d(TAG, "initLister: ffff");
                            Glide.with(binding.imgFilter).load(FilterUtils.getDraw(filterRoot.getTitle())).into(binding.imgFilter);
//                              Glide.with(this).asGif().load(selectedFilter.getFilter()).into(binding.imgFilter);
                        }

                    }

                });

            }
        }

        @Override
        public void onAnimatedFilter(Object[] args) {
            if (args[0] != null) {
                runOnUiThread(() -> {

                    String filtertype = null;

                    filtertype = args[0].toString();
                    FilterRoot filterRoot = new Gson().fromJson(filtertype, FilterRoot.class);
                    if (filterRoot != null) {
                        if (filterRoot.getTitle().equalsIgnoreCase("None")) {
                            binding.imgFilter.setImageDrawable(null);
                        } else {
                            Glide.with(binding.imgFilter).load(FilterUtils.getDraw(filterRoot.getTitle()))
                                    .placeholder(R.drawable.placeholder)
                                    .error(R.drawable.placeholder)
                                    .into(binding.imgFilter);
                        }
                    }

                });

            }
        }

        @Override
        public void onGif(Object[] args) {

        }

        @Override
        public void onComment(Object[] args) {
            if (args[0] != null) {
                runOnUiThread(() -> {

                    Log.d(TAG, "commentlister : " + args[0]);

                    String data = args[0].toString();
                    if (!data.isEmpty()) {
                        try {
                            JSONObject jsonObject = new JSONObject(data.toString());

                            LiveStramComment liveStramComment = new Gson().fromJson(jsonObject.toString(), LiveStramComment.class);

                            if (liveStramComment != null) {
                                viewModel.liveStramCommentAdapter.addSingleComment(liveStramComment);
                                binding.rvComments.smoothScrollToPosition(viewModel.liveStramCommentAdapter.getItemCount() - 1);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onGift(Object[] args) {
            runOnUiThread(() -> {
                if (args[0] != null) {

                    Log.d(TAG, "giftloister : " + args.toString());
                    String data = args[0].toString();
                    try {
                        JSONObject jsonObject = new JSONObject(data.toString());
                        if (jsonObject.get("gift") != null) {
                            Log.d(TAG, "json gift : " + jsonObject.toString());
                            GiftRoot.GiftItem giftData = new Gson().fromJson(jsonObject.get("gift").toString(), GiftRoot.GiftItem.class);
                            if (giftData != null) {

                                String finalGiftLink = null;
                                List<GiftRoot.GiftItem> giftItemList = sessionManager.getGiftsList(giftData.getCategory());
                                for (int i = 0; i < giftItemList.size(); i++) {
                                    if (giftData.getId().equals(giftItemList.get(i).getId())) {
                                        finalGiftLink = BuildConfig.BASE_URL + giftItemList.get(i).getImage();
                                    }
                                }


//                                Log.d(TAG, "onGift11111: "+giftData.getType());
//                                Log.d(TAG, "onGift11111: "+giftData.getCategory());


                                if (giftData.getType() == 2) {

                                    Glide.with(binding.imgGiftCount).load(RayziUtils.getImageFromNumber(giftData.getCount()))
                                            .into(binding.imgGiftCount);
                                    binding.svgaImage.setVisibility(View.VISIBLE);
                                    Log.d(TAG, "giftReciveListnear: ma jay che ++++++++++++++++++++++++++++++++" + finalGiftLink);
                                    Animation zoomin = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoomin);
                                    binding.svgaImage.startAnimation(zoomin);
//                                binding.svgaImage.startAnimation(slide_up);

                                    SVGAImageView imageView = binding.svgaImage;
                                    SVGAParser parser = new SVGAParser(HostLiveActivity.this);
                                    try {
                                        parser.decodeFromURL(new URL(finalGiftLink), new SVGAParser.ParseCompletion() {
                                            @Override
                                            public void onComplete(@NonNull SVGAVideoEntity svgaVideoEntity) {
                                                SVGADrawable drawable = new SVGADrawable(svgaVideoEntity);
                                                imageView.setImageDrawable(drawable);
                                                imageView.startAnimation();
                                                Log.d("TAG", "setData: " + giftData.getImage());
                                                new Handler().postDelayed(() -> {
                                                    binding.svgaImage.setVisibility(View.GONE);
                                                    binding.svgaImage.clear();
                                                }, 5000);
                                            }

                                            @Override
                                            public void onError() {

                                            }
                                        }, list -> {

                                        });
                                    } catch (MalformedURLException e) {
                                        e.printStackTrace();
                                    }

                                    Glide.with(HostLiveActivity.this).load(finalGiftLink).into(binding.svgaImage);

                                } else {
                                    String name = jsonObject.getString("userName").toString();
                                    binding.tvGiftUserName.setText(name + " Sent a gift");
                                    binding.lytGift.setVisibility(View.VISIBLE);
                                    binding.tvGiftUserName.setVisibility(View.VISIBLE);
                                    Glide.with(binding.imgGift).load(finalGiftLink).into(binding.imgGift);
                                    Log.d(TAG, "giftReciveListnear: ma jay che ++++++++++++++++++++++++++++++++" + finalGiftLink);

                                    Glide.with(binding.imgGiftCount).load(RayziUtils.getImageFromNumber(giftData.getCount()))
                                            .into(binding.imgGiftCount);

                                    new Handler().postDelayed(() -> {
                                        binding.lytGift.setVisibility(View.GONE);
                                        binding.tvGiftUserName.setVisibility(View.GONE);
                                        binding.tvGiftUserName.setText("");
                                        binding.imgGift.setImageDrawable(null);
                                        binding.imgGiftCount.setImageDrawable(null);
                                    }, 4000);
                                    makeSound();
                                }

                            }

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }


                if (args[2] != null) {   // host
                    Log.d(TAG, "host string   : " + args[2].toString());
                    try {
                        JSONObject jsonObject = new JSONObject(args[2].toString());
                        UserRoot.User host = new Gson().fromJson(jsonObject.toString(), UserRoot.User.class);
                        if (host != null) {
                            if (sessionManager.getUser().getId().equals(host.getId())) {
                                sessionManager.saveUser(host);
                                binding.tvRcoins.setText(String.valueOf(host.getRCoin()));
                                giftViewModel.localUserCoin.setValue(host.getDiamond());
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

            });
        }

        @Override
        public void onView(Object[] args1) {
            runOnUiThread(() -> {
                Object args = args1[0];
                Log.d(TAG, "viewListner : " + args.toString());

                try {

                    JSONArray jsonArray = new JSONArray(args.toString());
                    viewModel.liveViewUserAdapter.addData(jsonArray);
                    binding.tvViewUserCount.setText(String.valueOf(jsonArray.length()));
                    Log.d(TAG, "views2 : " + jsonArray);
                    binding.tvNoOneJoined.setVisibility(viewModel.liveViewUserAdapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);

                } catch (JSONException e) {
                    Log.d(TAG, "207: ");
                    e.printStackTrace();
                }

                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("blocked", blockedUsersList);
                    jsonObject.put("liveStreamingId", liveUser.getLiveStreamingId());
                    MySocketManager.getInstance().getSocket().emit(Const.EVENT_BLOCK, jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        }

        @Override
        public void onGetUser(Object[] args) {
            runOnUiThread(() -> {
                if (args[0] != null) {
                    String data = args[0].toString();
                    Log.d(TAG, "initLister: usr sty1 " + data);
                    JsonParser parser = new JsonParser();
                    JsonElement mJson = parser.parse(data);
                    Log.d(TAG, "initLister: usr sty2 " + mJson);
                    Gson gson = new Gson();
                    GuestProfileRoot.User userData = gson.fromJson(mJson, GuestProfileRoot.User.class);
                    Log.d(TAG, "initLister: user  " + userData.toString());
                    if (userData != null) {
                        userProfileBottomSheet.show(true, userData, "");
                    }
                }
            });
        }

        @Override
        public void onBlock(Object[] args1) {

        }

        @Override
        public void onLiveRejoin(Object[] args1) {

            runOnUiThread(() -> {
                boolean isEnd = (boolean) args1[0];
                if (!isEnd) {
                    Toast.makeText(HostLiveActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                    endLive();
                }
            });

        }
    };

    SocketConnectHandler socketConnectHandler = new SocketConnectHandler() {
        @Override
        public void onConnect() {

        }

        @Override
        public void onDisconnect() {

        }

        @Override
        public void onReconnecting() {

        }

        @Override
        public void onReconnected(Object[] args) {
            Log.d(TAG, "onReconnected: " + args[0].toString());
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("liveStreamingId", liveUser.getLiveStreamingId());
                jsonObject.put("userId", sessionManager.getUser().getId());
                jsonObject.put("isLive", true);
                MySocketManager.getInstance().getSocket().emit(Const.LIVE_REJOIN, jsonObject);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_host_live);

        MySocketManager.getInstance().addLiveListener(liveHandler);
        MySocketManager.getInstance().addSocketConnectHandler(socketConnectHandler);

        sessionManager = new SessionManager(this);
        Intent intent = getIntent();
        if (intent != null) {
            String data = intent.getStringExtra(Const.DATA);
            String privacy = intent.getStringExtra(Const.PRIVACY);
            binding.tvPrivacy.setText(privacy);
            if (privacy.equalsIgnoreCase("Private")) {
                binding.imgPrivacyk.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.lock));
            }
            if (data != null && !data.isEmpty()) {
                liveUser = new Gson().fromJson(data, LiveStreamRoot.LiveUser.class);
                Log.d(TAG, "onCreate: live room id " + liveUser.getLiveStreamingId());
            }
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("liveStreamingId", liveUser.getLiveStreamingId());
            jsonObject.put("liveUserId", sessionManager.getUser().getId());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        MySocketManager.getInstance().getSocket().emit("liveRoomConnect", jsonObject);

        giftViewModel = ViewModelProviders.of(this, new ViewModelFactory(new EmojiSheetViewModel()).createFor()).get(EmojiSheetViewModel.class);
        viewModel = ViewModelProviders.of(this, new ViewModelFactory(new HostLiveViewModel()).createFor()).get(HostLiveViewModel.class);
        binding.setViewModel(viewModel);
        BaseActivity.STATUS_LIVE = true;
        giftViewModel.getGiftCategory();

        viewModel.initLister();
        initView();
        joinChannel();
        startBroadcast();
        initLister();
        startTime();

    }


    public void startTime() {
        timerHandler = new Handler(Objects.requireNonNull(Looper.myLooper()));
        timerRunnable = new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                seconds++;
                int p1 = seconds % 60;
                int p2 = seconds / 60;
                int p3 = p2 % 60;
                p2 = p2 / 60;

                String sec;
                String hour;
                String min;
                if (p1 < 10) {
                    sec = "0" + p1;
                } else {
                    sec = String.valueOf(p1);
                }
                if (p2 < 10) {
                    hour = "0" + p2;
                } else {
                    hour = String.valueOf(p2);
                }
                if (p3 < 10) {
                    min = "0" + p3;
                } else {
                    min = String.valueOf(p3);
                }
                binding.tvTime.setText(hour + ":" + min + ":" + sec);

                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private void showEndLivePopup() {
        new PopupBuilder(this).showLiveEndPopup(new PopupBuilder.OnCloseLiveClickListner() {
            @Override
            public void onEndClick() {
                endLive();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        endLive();
    }

    private void endLive() {
        timerHandler.removeCallbacks(timerRunnable);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("liveStreamingId", liveUser.getLiveStreamingId());
            jsonObject.put("liveUserId", sessionManager.getUser().getId());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        MySocketManager.getInstance().getSocket().emit("liveHostEnd", jsonObject);

        removeRtcVideo(0, true);
        mVideoGridContainer.removeUserVideo(0, true);
        BaseActivity.STATUS_LIVE = false;
        startActivity(new Intent(this, LiveSummaryActivity.class).putExtra(Const.DATA, liveUser.getLiveStreamingId()));
        finish();

    }

    private void joinChannel() {
        try {
            rtcEngine().setChannelProfile(io.agora.rtc.Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
            rtcEngine().enableVideo();

            configVideo();
            Log.d("TAG", "joinChannel:tkn " + liveUser.getToken());
            Log.d("TAG", "joinChannel:chanel " + liveUser.getChannel());
            rtcEngine().joinChannel(liveUser.getToken(), liveUser.getChannel(), "", 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startBroadcast() {
        Log.d(TAG, "startBroadcast: ");
        try {
            rtcEngine().setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
            rtcEngine().enableAudio();
            SurfaceView surface = prepareRtcVideo(0, true);
            mVideoGridContainer.addUserVideoSurface(0, surface, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        binding.tvLiveStremingId.setText("Id: " + sessionManager.getUser().getUniqueId());
        binding.tvRcoins.setText(String.valueOf(sessionManager.getUser().getRCoin()));
        Glide.with(binding.imgFilter).load(FilterUtils.getDraw(liveUser.getFilter()))
                .into(binding.imgFilter);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("liveStreamingId", liveUser.getLiveStreamingId());
            jsonObject.put("title", liveUser.getFilter());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        MySocketManager.getInstance().getSocket().emit(Const.EVENT_SIMPLEFILTER, jsonObject);
        mVideoGridContainer = binding.liveVideoGridLayout;
        mVideoGridContainer.setStatsManager(statsManager());
        emojiBottomsheetFragment = new EmojiBottomSheetFragment();
        userProfileBottomSheet = new UserProfileBottomSheet(this);

    }

    private void initLister() {
        viewModel.isShowFilterSheet.observe(this, aBoolean -> {
            Log.d(TAG, "initLister:filter sheet  " + aBoolean);
            if (aBoolean) {
                binding.lytFilters.setVisibility(View.VISIBLE);
            } else {
                binding.lytFilters.setVisibility(View.GONE);
            }
        });
        viewModel.selectedFilter.observe(this, selectedFilter -> {
            if (selectedFilter.getTitle().equalsIgnoreCase("None")) {
                Log.d(TAG, "initLister: null");
                binding.imgFilter.setImageDrawable(null);
            } else {
                Log.d(TAG, "initLister: ffff");
//                  Glide.with(this).asGif().load(FilterUtils.getDraw(selectedFilter.getTitle())).into(binding.imgFilter);
            }
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("liveStreamingId", liveUser.getLiveStreamingId());
                jsonObject.put("title", selectedFilter.getTitle());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            MySocketManager.getInstance().getSocket().emit(Const.EVENT_ANIMFILTER, jsonObject);
            Log.d(HostLiveActivity.TAG + " ", "onBindViewHolder: 11===========" + selectedFilter.getTitle());
        });

        viewModel.selectedFilter2.observe(this, selectedFilter -> {
            if (selectedFilter.getTitle().equalsIgnoreCase("None")) {
                Log.d(TAG, "initLister: null");
                binding.imgFilter.setImageDrawable(null);
            } else {
                Log.d(TAG, "initLister: ffff");

                //  Glide.with(this).asGif().load(selectedFilter.getFilter()).into(binding.imgFilter);
            }

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("liveStreamingId", liveUser.getLiveStreamingId());
                jsonObject.put("title", selectedFilter.getTitle());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            MySocketManager.getInstance().getSocket().emit(Const.EVENT_SIMPLEFILTER, jsonObject);
            Log.d(HostLiveActivity.TAG + " ", "onBindViewHolder: 11===========" + selectedFilter.getTitle());
        });
        viewModel.selectedSticker.observe(this, selectedSticker -> {
            binding.imgSticker.setImageURI(selectedSticker.getSticker());

            binding.imgSticker.setVisibility(View.VISIBLE);
            new Handler(Looper.myLooper()).postDelayed(() -> binding.imgSticker.setVisibility(View.GONE), 2000);
            MySocketManager.getInstance().getSocket().emit(Const.EVENT_GIF, new Gson().toJson(selectedSticker));

        });
        viewModel.clickedComment.observe(this, user -> {
            getUser(user.getId());

        });
        viewModel.clickedUser.observe(this, user -> {
            try {
                getUser(user.get("userId").toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        binding.btnClose.setOnClickListener(v -> showEndLivePopup());


        binding.btnSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rtcEngine().switchCamera();
            }
        });


        giftViewModel.finelGift.observe(this, giftItem -> {


            if (giftItem != null) {
                int totalCoin = giftItem.getCoin() * giftItem.getCount();
                if (sessionManager.getUser().getDiamond() < totalCoin) {
                    Toast.makeText(HostLiveActivity.this, "You not have enough diamonds to send gift", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("userId", sessionManager.getUser().getId());
                    jsonObject.put("coin", giftItem.getCoin() * giftItem.getCount());
                    jsonObject.put("gift", new Gson().toJson(giftItem));
                    jsonObject.put("userName", sessionManager.getUser().getName());
                    jsonObject.put("liveStreamingId", liveUser.getLiveStreamingId());
                    MySocketManager.getInstance().getSocket().emit(Const.EVENT_LIVEUSER_GIFT, jsonObject);
                    emojiBottomsheetFragment.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


        userProfileBottomSheet.setOnUserTapListner(user -> {  // for block user
            blockedUsersList.put(user.getUserId());
            Log.d(TAG, "initLister: blocked " + blockedUsersList.toString());

            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("blocked", blockedUsersList);
                MySocketManager.getInstance().getSocket().emit(Const.EVENT_BLOCK, jsonObject);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        });


    }


    private void getUser(String userId) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("fromUserId", sessionManager.getUser().getId());
            jsonObject.put("toUserId", userId);
            Log.d(TAG, "getUser:request  " + jsonObject);
            MySocketManager.getInstance().getSocket().emit(Const.EVENT_GET_USER, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

    }

    public void onClickFilter(View view) {
        viewModel.isShowFilterSheet.setValue(true);
        binding.rvFilters.setAdapter(viewModel.filterAdapter_tt);

    }


    @Override
    public void onErr(int err) {
        Log.d(TAG, "onErr: " + err);
    }

    @Override
    public void onConnectionLost() {
        Log.d(TAG, "onConnectionLost: ");
    }

    @Override
    public void onVideoStopped() {
        Log.d(TAG, "onVideoStopped: ");
    }

    public void onClickGifIcon(View view) {
        viewModel.isShowFilterSheet.setValue(true);
        binding.rvFilters.setAdapter(viewModel.filterAdapter2);
    }

    public void onClickStickerIcon(View view) {
        viewModel.isShowFilterSheet.setValue(true);
        binding.rvFilters.setAdapter(viewModel.stickerAdapter);
    }

    public void onClickEmojiIcon(View view) {
    }

    public void onLocalAudioMute(View view) {
        viewModel.isMuted = !viewModel.isMuted;
        rtcEngine().muteLocalAudioStream(viewModel.isMuted);
        if (viewModel.isMuted) {
            binding.btnMute.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.mute));

            Toast.makeText(this, "Muted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Unmuted", Toast.LENGTH_SHORT).show();
            binding.btnMute.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.unmute));
        }
    }

    public void onclickGiftIcon(View view) {
        emojiBottomsheetFragment.show(getSupportFragmentManager(), "emojifragfmetn");
    }

    @Override
    protected void onPause() {
        super.onPause();
//        endLive();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MySocketManager.getInstance().removeLiveListener(liveHandler);
        MySocketManager.getInstance().removeSocketConnectHandler(socketConnectHandler);
        BaseActivity.STATUS_LIVE = false;
        statsManager().clearAllData();
    }

    public void onClickSendComment(View view) {
        String comment = binding.etComment.getText().toString();
        if (!comment.isEmpty()) {
            binding.etComment.setText("");
            LiveStramComment liveStramComment = new LiveStramComment(liveUser.getLiveStreamingId(), comment, sessionManager.getUser(), false);
            MySocketManager.getInstance().getSocket().emit(Const.EVENT_COMMENT, new Gson().toJson(liveStramComment));
            Log.d(TAG, "onClickSendComment: " + liveStramComment.toString());
        }
    }

    public void onclickShare(View view) {
        BranchUniversalObject buo = new BranchUniversalObject()
                .setCanonicalIdentifier("content/12345")
                .setTitle("Watch My Live Video")
                .setContentDescription("By : " + sessionManager.getUser().getName())
                .setContentImageUrl(sessionManager.getUser().getImage())
                .setContentMetadata(new ContentMetadata().addCustomMetadata("type", "LIVE").addCustomMetadata(Const.DATA, new Gson().toJson(liveUser)));

        LinkProperties lp = new LinkProperties()
                .setChannel("facebook")
                .setFeature("sharing")
                .setCampaign("content 123 launch")
                .setStage("new user")

                .addControlParameter("", "")
                .addControlParameter("", Long.toString(Calendar.getInstance().getTimeInMillis()));

        buo.generateShortUrl(this, lp, (url, error) -> {
            Log.d(TAG, "initListnear: branch url" + url);
            try {
                Log.d(TAG, "initListnear: share");
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                String shareMessage = url;
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                startActivity(Intent.createChooser(shareIntent, "choose one"));
            } catch (Exception e) {
                Log.d(TAG, "initListnear: " + e.getMessage());
                //e.toString();
            }
        });
    }

    @Override
    public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                renderRemoteUser(uid);
            }
        });
    }

    private void renderRemoteUser(int uid) {
        // SurfaceView surface = prepareRtcVideo(uid, false);
        // mVideoGridContainer.addUserVideoSurface(uid, surface, false);
    }

    private void removeRemoteUser(int uid) {
        // removeRtcVideo(uid, false);
        //  mVideoGridContainer.removeUserVideo(uid, false);
    }

    @Override
    public void onLeaveChannel(IRtcEngineEventHandler.RtcStats stats) {
        Log.d(TAG, "onLeaveChannel: stts " + stats);
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        Log.d(TAG, "onJoinChannelSuccess: chanel " + channel + " uid" + uid + "  elapsed " + elapsed);
    }

    @Override
    public void onUserOffline(int uid, int reason) {
        Log.d(TAG, "onUserOffline: " + uid + " reason" + reason);

    }

    @Override
    public void onUserJoined(int uid, int elapsed) {
        Log.d(TAG, "onUserJoined: " + uid + "  elapsed" + elapsed);
    }

    @Override
    public void onLastmileQuality(int quality) {

    }

    @Override
    public void onLastmileProbeResult(IRtcEngineEventHandler.LastmileProbeResult result) {

    }

    @Override
    public void onLocalVideoStats(IRtcEngineEventHandler.LocalVideoStats stats) {
        if (!statsManager().isEnabled()) return;

        LocalStatsData data = (LocalStatsData) statsManager().getStatsData(0);
        if (data == null) return;

        VideoEncoderConfiguration.VideoDimensions mVideoDimension = VideoEncoderConfiguration.VD_960x720;
        data.setWidth(mVideoDimension.width);
        data.setHeight(mVideoDimension.height);
        data.setFramerate(stats.sentFrameRate);
    }

    @Override
    public void onRtcStats(IRtcEngineEventHandler.RtcStats stats) {
        if (!statsManager().isEnabled()) return;

        LocalStatsData data = (LocalStatsData) statsManager().getStatsData(0);
        if (data == null) return;

        data.setLastMileDelay(stats.lastmileDelay);
        data.setVideoSendBitrate(stats.txVideoKBitRate);
        data.setVideoRecvBitrate(stats.rxVideoKBitRate);
        data.setAudioSendBitrate(stats.txAudioKBitRate);
        data.setAudioRecvBitrate(stats.rxAudioKBitRate);
        data.setCpuApp(stats.cpuAppUsage);
        data.setCpuTotal(stats.cpuAppUsage);
        data.setSendLoss(stats.txPacketLossRate);
        data.setRecvLoss(stats.rxPacketLossRate);
    }

    @Override
    public void onNetworkQuality(int uid, int txQuality, int rxQuality) {
        if (!statsManager().isEnabled()) return;

        StatsData data = statsManager().getStatsData(uid);
        if (data == null) return;

        data.setSendQuality(statsManager().qualityToString(txQuality));
        data.setRecvQuality(statsManager().qualityToString(rxQuality));
    }

    @Override
    public void onRemoteVideoStats(IRtcEngineEventHandler.RemoteVideoStats stats) {
        if (!statsManager().isEnabled()) return;

        RemoteStatsData data = (RemoteStatsData) statsManager().getStatsData(stats.uid);
        if (data == null) return;

        data.setWidth(stats.width);
        data.setHeight(stats.height);
        data.setFramerate(stats.rendererOutputFrameRate);
        data.setVideoDelay(stats.delay);
    }

    @Override
    public void onRemoteAudioStats(IRtcEngineEventHandler.RemoteAudioStats stats) {
        if (!statsManager().isEnabled()) return;

        RemoteStatsData data = (RemoteStatsData) statsManager().getStatsData(stats.uid);
        if (data == null) return;

        data.setAudioNetDelay(stats.networkTransportDelay);
        data.setAudioNetJitter(stats.jitterBufferDelay);
        data.setAudioLoss(stats.audioLossRate);
        data.setAudioQuality(statsManager().qualityToString(stats.quality));
    }

    @Override
    public void finish() {
        super.finish();
        statsManager().clearAllData();
    }

    @Override
    public void onEvent(String s, String s1, String s2, String s3) {

    }

    ///   filter  gift sticker emoji
}