package com.example.rayzi.home.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.rayzi.BuildConfig;
import com.example.rayzi.R;
import com.example.rayzi.activity.WebActivity;
import com.example.rayzi.databinding.ItemBannerBinding;
import com.example.rayzi.modelclass.BannerRoot;

import java.util.ArrayList;
import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private Context context;
    private List<BannerRoot.BannerItem> banner = new ArrayList<>();

    @Override
    public BannerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new BannerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner, parent, false));
    }

    @Override
    public void onBindViewHolder(BannerAdapter.BannerViewHolder holder, int position) {
        Glide.with(context).load(BuildConfig.BASE_URL + banner.get(position).getImage())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop().into(holder.bannerBinding.imageview);
        holder.bannerBinding.imageview.setOnClickListener(v -> WebActivity.open(context, "", banner.get(position).getURL()));
    }

    @Override
    public int getItemCount() {
        return banner.size();
    }

    public void addData(List<BannerRoot.BannerItem> banner) {

        this.banner = banner;
        notifyDataSetChanged();
    }

    public class BannerViewHolder extends RecyclerView.ViewHolder {
        ItemBannerBinding bannerBinding;

        public BannerViewHolder(View itemView) {
            super(itemView);
            bannerBinding = ItemBannerBinding.bind(itemView);

        }
    }
}
