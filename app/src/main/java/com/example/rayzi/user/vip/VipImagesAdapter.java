package com.example.rayzi.user.vip;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.rayzi.MainApplication;
import com.example.rayzi.R;
import com.example.rayzi.databinding.ItemVipSliderBinding;
import com.example.rayzi.modelclass.BannerRoot;

import java.util.List;

public class VipImagesAdapter extends RecyclerView.Adapter<VipImagesAdapter.VipImagesViewHolder> {

    int[] images = new int[]{R.drawable.crown_royalty, R.drawable.extra_coin, R.drawable.all_level_access};
    String[] textList = new String[]{"GET PREMIUM CROWN", "GET EXTRA COINS", "GET ALL LEVEL ACCESS"};
    //    private List<BannerRoot.BannerItem> banner = new ArrayList<>();
    private Context context;

    @Override
    public VipImagesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new VipImagesViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vip_slider, parent, false));
    }

    @Override
    public void onBindViewHolder(VipImagesViewHolder holder, int position) {

        Glide.with(context).load(images[position])
                .apply(MainApplication.requestOptions)
                .into(holder.bannerBinding.image);

        holder.bannerBinding.tvText.setText(textList[position]);

    }

    @Override
    public int getItemCount() {
        return images.length;
    }

    public void addData(List<BannerRoot.BannerItem> banner) {
//        this.banner = banner;
        notifyDataSetChanged();
    }


    public class VipImagesViewHolder extends RecyclerView.ViewHolder {
        ItemVipSliderBinding bannerBinding;

        public VipImagesViewHolder(View itemView) {
            super(itemView);
            bannerBinding = ItemVipSliderBinding.bind(itemView);
        }
    }
}
