package com.example.rayzi.viewModel;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.rayzi.SessionManager;
import com.example.rayzi.comments.CommentAdapter;
import com.example.rayzi.modelclass.PostCommentRoot;
import com.example.rayzi.modelclass.RestResponse;
import com.example.rayzi.retrofit.Const;
import com.example.rayzi.retrofit.RetrofitBuilder;
import com.example.rayzi.user.SearchUserAdapter;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentLikeListViewModel extends ViewModel {
    public static final int POST = 0;
    public static final int RELITE = 1;
    public CommentAdapter commentAdapter = new CommentAdapter();
    public SearchUserAdapter userAdapter = new SearchUserAdapter();
    public int start = 0;
    public int commentCount = 0;
    public MutableLiveData<Integer> listCountFinel = new MutableLiveData<>();
    public ObservableBoolean noData = new ObservableBoolean(false);
    public ObservableBoolean isLoading = new ObservableBoolean(false);
    SessionManager sessionManager;
    private Context context;

    public void getCommentList(String postId, int type, boolean b) {
        Log.d("TAG", "getCommentList: type " + type);
        Call<PostCommentRoot> call;
        if (type == POST) {
            call = RetrofitBuilder.create().getPostCommentList(SessionManager.getUserId(context), postId, start, Const.LIMIT);
        } else {
            call = RetrofitBuilder.create().getReliteCommentList(SessionManager.getUserId(context), postId, start, Const.LIMIT);
        }

        noData.set(false);
        isLoading.set(true);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<PostCommentRoot> call, Response<PostCommentRoot> response) {
                if (response.code() == 200) {
                    if (response.body().isStatus() && !response.body().getData().isEmpty()) {
                        commentAdapter.addData(response.body().getData());
                        commentCount = commentCount + response.body().getData().size();
                        listCountFinel.postValue(commentCount);
                    } else if (start == 0) {
                        noData.set(true);
                    }
                }
                isLoading.set(false);
            }

            @Override
            public void onFailure(Call<PostCommentRoot> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void init(Context context) {
        this.context = context;
        sessionManager = new SessionManager(context);
    }

    public void getLikeList(String id, int type, boolean isLoadMore) {
        Call<PostCommentRoot> call;
        if (type == POST) {
            call = RetrofitBuilder.create().getPostLikeList(SessionManager.getUserId(context), id, start, Const.LIMIT);
        } else {
            call = RetrofitBuilder.create().getReliteLikeList(SessionManager.getUserId(context), id, start, Const.LIMIT);
        }

        noData.set(false);
        isLoading.set(true);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<PostCommentRoot> call, Response<PostCommentRoot> response) {
                if (response.code() == 200) {
                    if (response.body().isStatus() && !response.body().getData().isEmpty()) {
                        commentAdapter.addData(response.body().getData());
                        commentCount = commentCount + response.body().getData().size();
                        listCountFinel.postValue(commentCount);
                    } else if (start == 0) {
                        noData.set(true);
                    }
                }
                isLoading.set(false);
            }

            @Override
            public void onFailure(Call<PostCommentRoot> call, Throwable t) {
                Log.d("TAG", "onFailure: " + t.getMessage());
            }
        });
    }

    public void deleteComment(PostCommentRoot.CommentsItem commentDummy, int position) {
        isLoading.set(true);
        Call<RestResponse> call = RetrofitBuilder.create().deleteComment(commentDummy.getId());
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<RestResponse> call, @NonNull Response<RestResponse> response) {
                if (response.code() == 200) {
                    if (response.body().isStatus()) {
                        commentAdapter.removeSingleItem(position);
                    }
                }
                isLoading.set(false);
            }

            @Override
            public void onFailure(Call<RestResponse> call, Throwable t) {

            }
        });
    }
}
