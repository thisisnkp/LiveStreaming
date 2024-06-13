package com.example.rayzi.reels;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.example.rayzi.MyLoader;
import com.example.rayzi.R;
import com.example.rayzi.SessionManager;
import com.example.rayzi.activity.BaseActivity;
import com.example.rayzi.databinding.ActivityVideoListGridBinding;
import com.example.rayzi.dilog.CustomDialogClass;
import com.example.rayzi.modelclass.ReliteRoot;
import com.example.rayzi.modelclass.RestResponse;
import com.example.rayzi.retrofit.Const;
import com.example.rayzi.retrofit.RetrofitBuilder;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VideoListGridActivity extends BaseActivity {
    ActivityVideoListGridBinding binding;
    ProfileVideoGridAdapter profileVideoGridAdapter = new ProfileVideoGridAdapter();
    SessionManager sessionManager;
    MyLoader myLoader = new MyLoader();
    private CustomDialogClass customDialogClass;
    private String userId;
    private int start = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.parseColor("#11101B"));
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_list_grid);
        binding.setLoader(myLoader);

        customDialogClass = new CustomDialogClass(this, R.style.customStyle);
        customDialogClass.setCancelable(false);
        customDialogClass.setCanceledOnTouchOutside(false);

        sessionManager = new SessionManager(this);
        Intent intent = getIntent();
        userId = intent.getStringExtra(Const.DATA);
        binding.rvFeed.setAdapter(profileVideoGridAdapter);

        if (userId != null && !userId.isEmpty()) {
            getData(false);
        }
        if (isRTL(this)) {
            binding.backimg.setScaleX(isRTL(this) ? -1 : 1);
        }
        binding.swipeRefresh.setOnRefreshListener((refreshLayout) -> getData(false));
        binding.swipeRefresh.setOnLoadMoreListener(refreshLayout -> getData(true));


        profileVideoGridAdapter.setOnVideoGridClickListner(new ProfileVideoGridAdapter.OnVideoGridClickListner() {
            @Override
            public void onVideoClick(int position) {
                startActivity(new Intent(VideoListGridActivity.this, ReelsActivity.class)
                        .putExtra(Const.POSITION, position)
                        .putExtra(Const.DATA, new Gson().toJson(profileVideoGridAdapter.getList())));
            }

            @Override
            public void onDeleteClick(ReliteRoot.VideoItem postItem, int position) {
                Dialog dialog = new Dialog(VideoListGridActivity.this);
                dialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.color.transparent));
                dialog.setContentView(R.layout.delete_popup);

                Button yes = dialog.findViewById(R.id.yes);
                Button no = dialog.findViewById(R.id.no);

                yes.setOnClickListener(v -> {
                    DeleteVideo(postItem, position);

                    dialog.dismiss();
                });

                no.setOnClickListener(v -> dialog.dismiss());
                dialog.show();
            }
        });

    }


    public void DeleteVideo(ReliteRoot.VideoItem postItem, int pos) {
        customDialogClass.show();
        Call<RestResponse> call = RetrofitBuilder.create().deleteRelite(postItem.getId());

        call.enqueue(new Callback<RestResponse>() {
            @Override
            public void onResponse(Call<RestResponse> call, Response<RestResponse> response) {
                if (response.body().isStatus() && response.isSuccessful()) {
                    Toast.makeText(VideoListGridActivity.this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                    customDialogClass.dismiss();
                    profileVideoGridAdapter.getList().remove(pos);
                    profileVideoGridAdapter.notifyItemRemoved(pos);
                }
            }

            @Override
            public void onFailure(Call<RestResponse> call, Throwable t) {

            }
        });
    }

    private void getData(boolean isLoadMore) {
        if (isLoadMore) {
            start = start + Const.LIMIT;
        } else {
            myLoader.isFristTimeLoading.set(true);
            profileVideoGridAdapter.clear();
            start = 0;
        }
        myLoader.noData.set(false);
        Call<ReliteRoot> call = RetrofitBuilder.create().getRelites(SessionManager.getUserId(this), "User", start, Const.LIMIT);
        call.enqueue(new Callback<ReliteRoot>() {
            @Override
            public void onResponse(Call<ReliteRoot> call, Response<ReliteRoot> response) {
                if (response.code() == 200) {
                    if (response.body().isStatus() && !response.body().getVideo().isEmpty()) {
                        profileVideoGridAdapter.addData(response.body().getVideo());
                    } else if (start == 0) {
                        myLoader.noData.set(true);
                    }
                }

                myLoader.isFristTimeLoading.set(false);
                binding.swipeRefresh.finishRefresh();
                binding.swipeRefresh.finishLoadMore();
            }

            @Override
            public void onFailure(Call<ReliteRoot> call, Throwable t) {

            }
        });
    }
}