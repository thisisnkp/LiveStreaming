package com.example.rayzi.FakeChat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.rayzi.FakeChat.fakemodelclass.ChatRootFake;
import com.example.rayzi.R;
import com.example.rayzi.databinding.FakeItemChatBinding;

import java.util.ArrayList;
import java.util.List;

public class FakeChatAdapter extends RecyclerView.Adapter<FakeChatAdapter.ChatTextViewHolder> {
    Context context;
    List<ChatRootFake> list = new ArrayList<>();
    OnClickListener onClickListener;

    @Override
    public FakeChatAdapter.ChatTextViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new ChatTextViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fake_item_chat, parent, false));
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Override
    public void onBindViewHolder(ChatTextViewHolder holder, int position) {
        holder.binding.tvuser.setVisibility(View.GONE);
        holder.binding.lytimagerobot.setVisibility(View.GONE);
        holder.binding.lytimageuser.setVisibility(View.GONE);
        ChatRootFake messageRoot = list.get(position);
        if (messageRoot.getFlag() == 1) {
            if (!messageRoot.getMessage().equals("")) {
                holder.binding.tvuser.setText(messageRoot.getMessage());
                Glide.with(holder.binding.imgUser2).load(messageRoot.getImage()).circleCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL).into(holder.binding.imgUser2);
                holder.binding.tvuser.setVisibility(View.VISIBLE);
            }
        } else if (messageRoot.getFlag() == 2) {
            holder.binding.tvuser.setVisibility(View.GONE);
            holder.binding.lytimagerobot.setVisibility(View.VISIBLE);
            Glide.with(holder.binding.imagerobot).load(messageRoot.getMessage())
                    .diskCacheStrategy(DiskCacheStrategy.ALL).into(holder.binding.imagerobot);
            Glide.with(holder.binding.imgUser2).load(messageRoot.getImage())
                    .diskCacheStrategy(DiskCacheStrategy.ALL).circleCrop().into(holder.binding.imgUser2);

            holder.binding.imagerobot.setOnClickListener(view -> {
                onClickListener.onImageClick(position, messageRoot.getMessage());
            });

        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void addSingleMessage(ChatRootFake chatRootFake) {
        list.add(chatRootFake);
        notifyItemInserted(list.size() - 1);
    }

    public interface OnClickListener {

        void onImageClick(int position, String imageUrl);

    }

    public class ChatTextViewHolder extends RecyclerView.ViewHolder {
        FakeItemChatBinding binding;

        public ChatTextViewHolder(View itemView) {
            super(itemView);
            binding = FakeItemChatBinding.bind(itemView);
        }

        public void setData(int position) {

        }
    }

}
