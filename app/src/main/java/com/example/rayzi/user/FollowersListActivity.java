package com.example.rayzi.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.databinding.DataBindingUtil;

import com.example.rayzi.MyLoader;
import com.example.rayzi.R;
import com.example.rayzi.activity.BaseActivity;
import com.example.rayzi.databinding.ActivityFollowrsListBinding;
import com.example.rayzi.modelclass.GuestUsersListRoot;
import com.example.rayzi.retrofit.Const;
import com.example.rayzi.retrofit.RetrofitBuilder;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FollowersListActivity extends BaseActivity {
    ActivityFollowrsListBinding binding;
    FollowrsUsersAdapter followrsUsersAdapter = new FollowrsUsersAdapter();
    MyLoader myLoader = new MyLoader();
    private int type;
    private String userId;
    private int start = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_followrs_list);
        binding.setMyLoder(myLoader);
        Intent intent = getIntent();
        userId = intent.getStringExtra(Const.USERID);

        type = intent.getIntExtra(Const.TYPE, 0);

        Log.d(TAG, "onCreate112233: " + userId);
        Log.d(TAG, "onCreate112233: type == " + type);


        if (userId != null && !userId.isEmpty()) {

            if (type == 1) {

                Log.d(TAG, "onCreate112233: call followers == ");
                getFollowers(false);
            } else if (type == 2) {
                getFollowing(false);
            }
        }

        binding.rvFeed.setAdapter(followrsUsersAdapter);

        binding.swipeRefresh.setOnRefreshListener((refreshLayout) -> {
            if (type == 1) {
                getFollowers(false);
            } else if (type == 2) {
                getFollowing(false);
            }
        });
        binding.swipeRefresh.setOnLoadMoreListener(refreshLayout -> {
            if (type == 1) {
                getFollowers(true);
            } else if (type == 2) {
                getFollowing(true);
            }
        });
    }

    private void getFollowing(boolean isLoadMore) {

        myLoader.noData.set(false);
        if (isLoadMore) {
            start = start + Const.LIMIT;
        } else {
            start = 0;
            followrsUsersAdapter.clear();
            myLoader.isFristTimeLoading.set(true);
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("userId", userId);
        jsonObject.addProperty("start", start);
        jsonObject.addProperty("limit", Const.LIMIT);
        Call<GuestUsersListRoot> call = RetrofitBuilder.create().getFollowrsList(userId, start, Const.LIMIT);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<GuestUsersListRoot> call, Response<GuestUsersListRoot> response) {
                if (response.code() == 200) {
                    if (response.body().isStatus() && !response.body().getUser().isEmpty()) {
                        followrsUsersAdapter.addData(response.body().getUser());
                    } else {
                        myLoader.noData.set(true);
                    }

                }

                myLoader.isFristTimeLoading.set(false);
                binding.swipeRefresh.finishRefresh();
                binding.swipeRefresh.finishLoadMore();

            }

            @Override
            public void onFailure(Call<GuestUsersListRoot> call, Throwable t) {
                myLoader.noData.set(false);
                t.printStackTrace();
            }
        });
    }

    private void getFollowers(boolean isLoadMore) {


        Log.d(TAG, "onCreate112233: call done");

        myLoader.noData.set(false);

        if (isLoadMore) {
            start = start + Const.LIMIT;

        } else {
            start = 0;
            followrsUsersAdapter.clear();
            myLoader.isFristTimeLoading.set(true);
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("userId", userId);
        jsonObject.addProperty("start", start);
        jsonObject.addProperty("limit", Const.LIMIT);

        Call<GuestUsersListRoot> call = RetrofitBuilder.create().getFollowingList(userId, start, Const.LIMIT);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<GuestUsersListRoot> call, Response<GuestUsersListRoot> response) {

                if (response.code() == 200) {
                    if (response.body().isStatus() && !response.body().getUser().isEmpty()) {
                        followrsUsersAdapter.addData(response.body().getUser());
                    } else {
                        myLoader.noData.set(true);
                    }
                }

                myLoader.isFristTimeLoading.set(false);
                binding.swipeRefresh.finishRefresh();
                binding.swipeRefresh.finishLoadMore();

            }

            @Override
            public void onFailure(Call<GuestUsersListRoot> call, Throwable t) {
                myLoader.noData.set(true);
                t.printStackTrace();
            }

        });

    }

}