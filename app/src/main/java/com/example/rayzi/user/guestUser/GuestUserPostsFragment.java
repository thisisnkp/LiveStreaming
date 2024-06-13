package com.example.rayzi.user.guestUser;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.example.rayzi.MyLoader;
import com.example.rayzi.R;
import com.example.rayzi.activity.BaseFragment;
import com.example.rayzi.databinding.FragmentGuestUserPostsBinding;
import com.example.rayzi.modelclass.GuestProfileRoot;
import com.example.rayzi.modelclass.PostRoot;
import com.example.rayzi.modelclass.RestResponse;
import com.example.rayzi.posts.FeedGridAdapter;
import com.example.rayzi.posts.FeedListActivity;
import com.example.rayzi.retrofit.Const;
import com.example.rayzi.retrofit.RetrofitBuilder;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class GuestUserPostsFragment extends BaseFragment {


    FragmentGuestUserPostsBinding binding;
    FeedGridAdapter feedGridAdapter = new FeedGridAdapter();
    MyLoader myLoader = new MyLoader();
    private GuestProfileRoot.User user;
    private int start = 0;

    public GuestUserPostsFragment(GuestProfileRoot.User user) {
        // Required empty public constructor
        this.user = user;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_guest_user_posts, container, false);
        binding.setLoader(myLoader);
        initMain();
        getData(false);
        return binding.getRoot();
    }

    //todo pagiation
    private void getData(boolean isLoadMore) {
        if (isLoadMore) {
            start = start + Const.LIMIT;
        } else {
            myLoader.isFristTimeLoading.set(true);
            feedGridAdapter.clear();
            start = 0;
        }

        myLoader.noData.set(false);
        Call<PostRoot> call = RetrofitBuilder.create().getUserPostList(user.getUserId(), start, Const.LIMIT);
        call.enqueue(new Callback<PostRoot>() {
            @Override
            public void onResponse(Call<PostRoot> call, Response<PostRoot> response) {
                if (response.code() == 200) {
                    if (response.body().isStatus() && !response.body().getPost().isEmpty()) {
                        feedGridAdapter.addData(response.body().getPost());
                    } else if (start == 0) {
                        myLoader.noData.set(true);
                    }
                }
                myLoader.isFristTimeLoading.set(false);
                binding.swipeRefresh.finishRefresh();
                binding.swipeRefresh.finishLoadMore();
            }

            @Override
            public void onFailure(Call<PostRoot> call, Throwable t) {
                Log.d("TAG", "onFailure: " + t.getMessage());
            }
        });
    }

    private void initMain() {


        binding.rvFeed.setAdapter(feedGridAdapter);

        binding.swipeRefresh.setOnRefreshListener((refreshLayout) -> getData(false));
        binding.swipeRefresh.setOnLoadMoreListener(refreshLayout -> getData(true));


        feedGridAdapter.setOnFeedGridAdapterClickLisnter(new FeedGridAdapter.OnFeedGridAdapterClickLisnter() {
            @Override
            public void onFeedClick(int position) {
                startActivity(new Intent(getActivity(), FeedListActivity.class)
                        .putExtra(Const.USERID, user.getId())
                        .putExtra(Const.POSITION, position).putExtra(Const.DATA, new Gson().toJson(feedGridAdapter.getList())));
            }

            @Override
            public void onDeleteClick(PostRoot.PostItem postItem, int position) {
                if (postItem.getUserId().equalsIgnoreCase(sessionManager.getUser().getId())) {
                    Dialog dialog = new Dialog(getActivity());
                    dialog.setContentView(R.layout.delete_popup);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));


                    Button yes = dialog.findViewById(R.id.yes);
                    Button no = dialog.findViewById(R.id.no);

                    yes.setOnClickListener(v -> {
                        DeletePost(postItem, position);
                        dialog.dismiss();
                    });

                    no.setOnClickListener(v -> dialog.dismiss());

                    dialog.show();
                }
            }
        });

    }


    public void DeletePost(PostRoot.PostItem postItem, int pos) {
        Call<RestResponse> call = RetrofitBuilder.create().deletePost(postItem.getId());

        call.enqueue(new Callback<RestResponse>() {
            @Override
            public void onResponse(Call<RestResponse> call, Response<RestResponse> response) {
                if (response.body().isStatus() && response.isSuccessful()) {
                    Toast.makeText(getActivity(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                    feedGridAdapter.getList().remove(pos);
                    feedGridAdapter.notifyItemRemoved(pos);
                    //  feedGridAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<RestResponse> call, Throwable t) {

            }

        });
    }
}