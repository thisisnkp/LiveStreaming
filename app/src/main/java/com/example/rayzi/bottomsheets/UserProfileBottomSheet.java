package com.example.rayzi.bottomsheets;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.example.rayzi.MainApplication;
import com.example.rayzi.R;
import com.example.rayzi.SessionManager;
import com.example.rayzi.chat.ChatActivity;
import com.example.rayzi.databinding.BottomSheetUserProfileBinding;
import com.example.rayzi.modelclass.GuestProfileRoot;
import com.example.rayzi.modelclass.LiveUserRoot;
import com.example.rayzi.retrofit.Const;
import com.example.rayzi.retrofit.UserApiCall;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;

public class UserProfileBottomSheet {
    private final BottomSheetDialog bottomSheetDialog;
    private final BottomSheetUserProfileBinding sheetDilogBinding;
    private final UserApiCall userApiCall;
    OnUserTapListner onUserTapListner;
    SessionManager sessionManager;
    private Context context;

    public UserProfileBottomSheet(Context context) {
        bottomSheetDialog = new BottomSheetDialog(context, R.style.CustomBottomSheetDialogTheme);
        this.context = context;
        sessionManager = new SessionManager(context);
        bottomSheetDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        bottomSheetDialog.setOnShowListener(dialog -> {
            BottomSheetDialog d = (BottomSheetDialog) dialog;
            FrameLayout bottomSheet = (FrameLayout) d.findViewById(R.id.design_bottom_sheet);
            BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
        });
        userApiCall = new UserApiCall(context);

        sheetDilogBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.bottom_sheet_user_profile, null, false);
        bottomSheetDialog.setContentView(sheetDilogBinding.getRoot());


        //   sheetDilogBinding.pdFollow.setVisibility(View.VISIBLE);
        //  sheetDilogBinding.tvFollowStatus.setVisibility(View.INVISIBLE);

/*
            new Handler(Looper.myLooper()).postDelayed(() -> {
                if (sheetDilogBinding.tvFollowStatus.getText().toString().equalsIgnoreCase("follow")) {
                    sheetDilogBinding.tvFollowStatus.setText("UNFOLLOW");
                    sheetDilogBinding.lytFollowUnfollow.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.graylight));
                } else {
                    sheetDilogBinding.tvFollowStatus.setText("FOLLOW");
                    sheetDilogBinding.lytFollowUnfollow.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.pink));
                }
                sheetDilogBinding.pdFollow.setVisibility(View.GONE);
                sheetDilogBinding.tvFollowStatus.setVisibility(View.VISIBLE);
                sheetDilogBinding.lytFollowUnfollow.setEnabled(true);
            }, 1000);
*/


    }

    public OnUserTapListner getOnUserTapListner() {
        return onUserTapListner;
    }

    public void setOnUserTapListner(OnUserTapListner onUserTapListner) {
        this.onUserTapListner = onUserTapListner;
    }

    public void show(boolean isHost, GuestProfileRoot.User userDummy, String liveStreamingId) {
        Glide.with(context).load(R.drawable.dummy_user).apply(MainApplication.requestOptions).circleCrop().into(sheetDilogBinding.imgUser);
        sheetDilogBinding.btnClose.setOnClickListener(v -> bottomSheetDialog.dismiss());
        if (isHost) {
            sheetDilogBinding.btnMessage.setVisibility(View.GONE);
            sheetDilogBinding.lytFollowUnfollow.setVisibility(View.GONE);
            sheetDilogBinding.btnBlock.setVisibility(View.VISIBLE);
        } else {
            sheetDilogBinding.lytFollowUnfollow.setVisibility(View.VISIBLE);
            sheetDilogBinding.btnMessage.setVisibility(View.VISIBLE);
            sheetDilogBinding.btnBlock.setVisibility(View.GONE);
        }
        if (sessionManager.getUser().getId().equalsIgnoreCase(userDummy.getUserId())) {
            sheetDilogBinding.btnMessage.setVisibility(View.GONE);
            sheetDilogBinding.lytFollowUnfollow.setVisibility(View.GONE);
            sheetDilogBinding.btnBlock.setVisibility(View.GONE);
        } else {
            sheetDilogBinding.btnMessage.setVisibility(View.VISIBLE);
            sheetDilogBinding.lytFollowUnfollow.setVisibility(View.VISIBLE);
            sheetDilogBinding.btnBlock.setVisibility(View.VISIBLE);
        }
        if (userDummy != null) {
            Glide.with(context).load(userDummy.getImage()).apply(MainApplication.requestOptions).circleCrop().into(sheetDilogBinding.imgUser);
            sheetDilogBinding.tvUserId.setText(userDummy.getUsername());
            sheetDilogBinding.tvName.setText(userDummy.getName());
            sheetDilogBinding.tvAge.setText(String.valueOf(userDummy.getAge()));
            sheetDilogBinding.tvCountry.setText(userDummy.getCountry());
            sheetDilogBinding.tvLevel.setText(userDummy.getLevel().getName());
            sheetDilogBinding.tvPosts.setText(String.valueOf(userDummy.getPost()));
            sheetDilogBinding.tvFollowrs.setText(String.valueOf(userDummy.getFollowers()));
            sheetDilogBinding.tvVideos.setText(String.valueOf(userDummy.getVideo()));
        }

        sheetDilogBinding.folllowBox.setVisibility(View.VISIBLE);


        if (userDummy.isFollow()) {
            sheetDilogBinding.tvFollowStatus.setText("UNFOLLOW");
            sheetDilogBinding.lytFollowUnfollow.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.graylight));
        } else {
            sheetDilogBinding.tvFollowStatus.setText("FOLLOW");
            sheetDilogBinding.lytFollowUnfollow.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.pink));
        }
        sheetDilogBinding.pdFollow.setVisibility(View.GONE);
        sheetDilogBinding.lytFollowUnfollow.setOnClickListener(v -> {


            sheetDilogBinding.lytFollowUnfollow.setEnabled(false);
            sheetDilogBinding.pdFollow.setVisibility(View.VISIBLE);
            sheetDilogBinding.tvFollowStatus.setVisibility(View.INVISIBLE);
            userApiCall.followUnfollowUser(!userDummy.isFollow(), userDummy.getUserId(), liveStreamingId, new UserApiCall.OnFollowUnfollowListner() {
                @Override
                public void onFollowSuccess() {
                    sheetDilogBinding.pdFollow.setVisibility(View.GONE);
                    userDummy.setFollow(true);
                    sheetDilogBinding.tvFollowStatus.setText("UNFOLLOW");
                    sheetDilogBinding.tvFollowStatus.setVisibility(View.VISIBLE);
                    sheetDilogBinding.lytFollowUnfollow.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.graylight));
                    sheetDilogBinding.lytFollowUnfollow.setEnabled(true);
                }

                @Override
                public void onUnfollowSuccess() {
                    sheetDilogBinding.pdFollow.setVisibility(View.GONE);
                    userDummy.setFollow(false);
                    sheetDilogBinding.tvFollowStatus.setText("FOLLOW");
                    sheetDilogBinding.tvFollowStatus.setVisibility(View.VISIBLE);
                    sheetDilogBinding.lytFollowUnfollow.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.pink));
                    sheetDilogBinding.lytFollowUnfollow.setEnabled(true);
                }

                @Override
                public void onFail() {
                    sheetDilogBinding.pdFollow.setVisibility(View.GONE);
                    sheetDilogBinding.tvFollowStatus.setVisibility(View.VISIBLE);
                    sheetDilogBinding.lytFollowUnfollow.setEnabled(true);
                }
            });
        });

        sheetDilogBinding.btnMessage.setOnClickListener(view -> {
            context.startActivity(new Intent(context, ChatActivity.class).putExtra(Const.USER, new Gson().toJson(userDummy)));
        });

        sheetDilogBinding.btnBlock.setOnClickListener(v -> {
            onUserTapListner.onBlockClick(userDummy);
            Toast.makeText(context, "Blocked", Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();
        });
        bottomSheetDialog.show();
    }


    public void show(boolean isHost, LiveUserRoot.UsersItem userDummy, String liveStreamingId) {

        Glide.with(context).load(R.drawable.dummy_user).apply(MainApplication.requestOptions).circleCrop().into(sheetDilogBinding.imgUser);
        sheetDilogBinding.btnClose.setOnClickListener(v -> bottomSheetDialog.dismiss());
        if (isHost) {
            sheetDilogBinding.btnMessage.setVisibility(View.GONE);
            sheetDilogBinding.btnBlock.setVisibility(View.VISIBLE);
        } else {
            sheetDilogBinding.btnMessage.setVisibility(View.VISIBLE);
            sheetDilogBinding.btnBlock.setVisibility(View.GONE);
        }

        if (userDummy != null) {
            Glide.with(context).load(userDummy.getImage()).apply(MainApplication.requestOptions).circleCrop().into(sheetDilogBinding.imgUser);
            sheetDilogBinding.tvUserId.setText(userDummy.getUsername());
            sheetDilogBinding.tvName.setText(userDummy.getName());
            sheetDilogBinding.tvAge.setText(String.valueOf("18"));
            sheetDilogBinding.tvCountry.setText(userDummy.getCountry());
            sheetDilogBinding.tvLevel.setText("Lavel 1");
            sheetDilogBinding.tvPosts.setText(String.valueOf("6"));
            sheetDilogBinding.tvFollowrs.setText(String.valueOf("3"));
            sheetDilogBinding.tvVideos.setText(String.valueOf("7"));
        }

        sheetDilogBinding.folllowBox.setVisibility(View.GONE);
        bottomSheetDialog.show();

    }

    public interface OnUserTapListner {
        void onBlockClick(GuestProfileRoot.User userDummy);
    }
}
