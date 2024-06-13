package com.example.rayzi.viewModel;

import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.rayzi.chat.ChatAdapter;
import com.example.rayzi.modelclass.ChatItem;
import com.example.rayzi.modelclass.ChatListRoot;
import com.example.rayzi.modelclass.RestResponse;
import com.example.rayzi.retrofit.Const;
import com.example.rayzi.retrofit.RetrofitBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatViewModel extends ViewModel {
    public ChatAdapter chatAdapter = new ChatAdapter();
    public MutableLiveData<Boolean> sendBtnEnable = new MutableLiveData<>(false);

    public String chatTopic;
    public int start = 0;
    public ObservableBoolean isLoding = new ObservableBoolean(false);

    public void deleteChat(ChatItem chatDummy, int position) {
        Call<RestResponse> call = RetrofitBuilder.create().deleteChat(chatDummy.getId());
        call.enqueue(new Callback<RestResponse>() {
            @Override
            public void onResponse(Call<RestResponse> call, Response<RestResponse> response) {
                if (response.code() == 200) {
                    if (response.body().isStatus()) {
                        chatAdapter.removeSingleItem(position);
                    }
                }
            }

            @Override
            public void onFailure(Call<RestResponse> call, Throwable t) {

            }
        });
    }

    public void getOldChat(boolean isLoadMore) {

        isLoding.set(true);

        if (isLoadMore) {
            start = start + Const.LIMIT;

        } else {
            start = 0;
            chatAdapter.clear();
        }

        Call<ChatListRoot> call = RetrofitBuilder.create().getOldChats(chatTopic, start, Const.LIMIT);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ChatListRoot> call, Response<ChatListRoot> response) {
                if (response.code() == 200) {
                    if (response.body().isStatus() && !response.body().getChat().isEmpty()) {
                        chatAdapter.addData(response.body().getChat());
                    }
                }
                isLoding.set(false);
            }

            @Override
            public void onFailure(Call<ChatListRoot> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

}
