package com.example.rayzi.emoji;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.rayzi.BuildConfig;
import com.example.rayzi.MainApplication;
import com.example.rayzi.R;
import com.example.rayzi.databinding.ItemEmojiGridBinding;
import com.example.rayzi.modelclass.GiftRoot;

import java.util.ArrayList;
import java.util.List;

public class EmojiGridAdapter extends RecyclerView.Adapter<EmojiGridAdapter.EmojiViewHolder> {

    OnEmojiSelectLister onEmojiSelectLister;
    List<GiftRoot.GiftItem> giftRootDummies = new ArrayList<>();
    private Context context;

    public OnEmojiSelectLister getOnEmojiSelectLister() {
        return onEmojiSelectLister;
    }

    public void setOnEmojiSelectLister(OnEmojiSelectLister onEmojiSelectLister) {
        this.onEmojiSelectLister = onEmojiSelectLister;
    }

    @Override
    public EmojiViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new EmojiViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_emoji_grid, parent, false));
    }

    @Override
    public void onBindViewHolder(EmojiViewHolder holder, int position) {

        holder.setData(position);
    }

    @Override
    public int getItemCount() {
        return giftRootDummies.size();
    }

    public void addData(List<GiftRoot.GiftItem> giftRootDummy) {
        this.giftRootDummies.addAll(giftRootDummy);
        notifyItemRangeInserted(this.giftRootDummies.size(), giftRootDummy.size());
    }

    public void clear() {
        this.giftRootDummies.clear();
    }

    public class EmojiViewHolder extends RecyclerView.ViewHolder {
        ItemEmojiGridBinding binding;

        public EmojiViewHolder(View itemView) {
            super(itemView);
            binding = ItemEmojiGridBinding.bind(itemView);
        }

        public void setData(int position) {
            GiftRoot.GiftItem gift = giftRootDummies.get(position);

            if (gift.getType() == 2) {
                Glide.with(binding.getRoot()).load(BuildConfig.BASE_URL + gift.getSvgaImage())
                        .apply(MainApplication.requestOptions)
                        .thumbnail(Glide.with(context).load(R.drawable.loadergif))
                        .into(binding.imgEmoji);
            } else {
                Glide.with(binding.getRoot()).load(BuildConfig.BASE_URL + gift.getImage())
                        .apply(MainApplication.requestOptions)
                        .thumbnail(Glide.with(context).load(R.drawable.loadergif))
                        .into(binding.imgEmoji);
            }

            binding.tvCoin.setText(String.valueOf(giftRootDummies.get(position).getCoin()));

            binding.getRoot().setOnClickListener(v -> {
                binding.itememoji.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_selected_5dp));
                onEmojiSelectLister.onEmojiSelect(binding, giftRootDummies.get(position));
            });
        }
    }
}
