package com.example.rayzi.liveStreamming;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rayzi.R;
import com.example.rayzi.RayziUtils;
import com.example.rayzi.databinding.ItemStickerBinding;
import com.example.rayzi.modelclass.StickerRoot;

import java.util.List;

public class StickerAdapter extends RecyclerView.Adapter<StickerAdapter.StickerViewHolder> {
    List<StickerRoot.StickerItem> stickerDummies = RayziUtils.getSticker();
    private Context context;
    private OnStickerClickListner onStickerClickListner;


    public StickerAdapter() {

    }

    public OnStickerClickListner getOns() {
        return onStickerClickListner;
    }

    public void setOnStickerClickListner(OnStickerClickListner ons) {
        this.onStickerClickListner = ons;
    }

    @NonNull
    @Override
    public StickerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new StickerViewHolder(LayoutInflater.from(context).inflate(R.layout.item_sticker, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull StickerViewHolder holder, int position) {

        StickerRoot.StickerItem stickerDummyRoot = stickerDummies.get(position);
        holder.binding.image.setImageURI(stickerDummyRoot.getSticker());
        holder.binding.image.setOnClickListener(v -> {
            Log.d("TAG", "onBindViewHolder: ===========" + stickerDummyRoot.getSticker());
            onStickerClickListner.onStickerClick(stickerDummyRoot);
        });

    }


    @Override
    public int getItemCount() {
        return stickerDummies.size();
    }

    public interface OnStickerClickListner {
        void onStickerClick(StickerRoot.StickerItem filterRoot);
    }

    public class StickerViewHolder extends RecyclerView.ViewHolder {
        ItemStickerBinding binding;

        public StickerViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemStickerBinding.bind(itemView);
        }
    }
}
