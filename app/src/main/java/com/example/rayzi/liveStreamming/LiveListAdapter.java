package com.example.rayzi.liveStreamming;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Picture;
import android.graphics.drawable.PictureDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import com.example.rayzi.MainApplication;
import com.example.rayzi.R;
import com.example.rayzi.activity.FakeWatchLiveActivity;
import com.example.rayzi.databinding.ItemVideoGrid1Binding;
import com.example.rayzi.modelclass.LiveUserRoot;
import com.example.rayzi.retrofit.Const;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class LiveListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String TAG = "LiveListAdapter";
    private Context context;
    private List<LiveUserRoot.UsersItem> userDummies = new ArrayList<>();
    private int adbanner_layout = 2;
    private int live_layout = 1;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        if (viewType == 1) {
            return new VideoListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_grid_1, parent, false));
        } else {
            return new AdViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ad, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof VideoListViewHolder) {
            ((VideoListViewHolder) holder).setData(position);
        }
    }

    @Override
    public int getItemCount() {
        return userDummies.size();
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemViewType(int i) {
        return this.live_layout;
    }

    public void addData(List<LiveUserRoot.UsersItem> userDummies) {
        this.userDummies.addAll(userDummies);
        notifyItemRangeInserted(this.userDummies.size(), userDummies.size());
    }

    public void clear() {
        userDummies.clear();
        notifyDataSetChanged();
    }

    public class VideoListViewHolder extends RecyclerView.ViewHolder {
        ItemVideoGrid1Binding binding;

        public VideoListViewHolder(View itemView) {
            super(itemView);
            binding = ItemVideoGrid1Binding.bind(itemView);
        }

        public void setData(int position) {
            LiveUserRoot.UsersItem userDummy = userDummies.get(position);
            binding.tvName.setText(userDummy.getName());
            binding.tvCountry.setText(userDummy.getCountry());

            Glide.with(context).load(userDummy.getImage())
                    .apply(MainApplication.requestOptionsLive)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop().into(binding.image);

            binding.imag1.setUserImage(userDummy.getImage(), userDummy.isIsVIP(), context, 12);

            MultiTransformation<Bitmap> transformations = new MultiTransformation<>(
                    new BlurTransformation(70),
                    new CenterCrop()
            );

            Glide.with(context).load(userDummy.getImage())
                    .circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .transform(transformations)
                    .into(binding.ivDetails);

            AsyncTask.execute(() -> {
                try {
                    String url = userDummy.getCountryFlagImage();
                    SVG svg = SVG.getFromInputStream(new URL(url).openStream());
                    Picture picture = svg.renderToPicture();
                    ((Activity) context).runOnUiThread(() -> {
                        binding.svgWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                        binding.svgWebView.setImageDrawable(new PictureDrawable(picture));
                    });

                } catch (SVGParseException | IOException e) {
                    e.printStackTrace();
                }
            });

            binding.tvViewCount.setText(String.valueOf(userDummy.getView()));
            binding.getRoot().setOnClickListener(v -> {
                if (userDummy.isFake()) {
                    Log.d(TAG, "setData: " + userDummy.toString());
                    context.startActivity(new Intent(context, FakeWatchLiveActivity.class).putExtra(Const.DATA, new Gson().toJson(userDummy)));
                } else {
                    Log.e("TAG", "setData: isfake not");
                    context.startActivity(new Intent(context, WatchLiveActivity.class).putExtra(Const.DATA, new Gson().toJson(userDummy)));
                }

            });
        }
    }

    private class AdViewHolder extends RecyclerView.ViewHolder {
        public AdViewHolder(View inflate) {
            super(inflate);
        }
    }
}
