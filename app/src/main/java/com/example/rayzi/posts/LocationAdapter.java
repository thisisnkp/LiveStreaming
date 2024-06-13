package com.example.rayzi.posts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rayzi.R;
import com.example.rayzi.databinding.ItemLocationBinding;
import com.example.rayzi.modelclass.SearchLocationRoot;

import java.util.ArrayList;
import java.util.List;


public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {
    OnLocationClickLisnter onLocationClickLisnter;
    private Context context;
    private List<SearchLocationRoot.DataItem> locations = new ArrayList<>();

    public OnLocationClickLisnter getOnLocationClickLisnter() {
        return onLocationClickLisnter;
    }

    public void setOnLocationClickLisnter(OnLocationClickLisnter onLocationClickLisnter) {
        this.onLocationClickLisnter = onLocationClickLisnter;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new LocationViewHolder(LayoutInflater.from(context).inflate(R.layout.item_location, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        holder.setData(position);
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    public void addData(List<SearchLocationRoot.DataItem> locations) {

        this.locations.addAll(locations);
        notifyItemRangeInserted(this.locations.size(), locations.size());
    }

    public void clear() {
        this.locations.clear();
        notifyDataSetChanged();
    }

    public interface OnLocationClickLisnter {
        void onLocationclick(SearchLocationRoot.DataItem hashtag);
    }

    public class LocationViewHolder extends RecyclerView.ViewHolder {
        ItemLocationBinding binding;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemLocationBinding.bind(itemView);
        }

        public void setData(int position) {
            SearchLocationRoot.DataItem location = locations.get(position);

            binding.tvLocation.setText(location.getLabel());
            binding.getRoot().setOnClickListener(v -> onLocationClickLisnter.onLocationclick(locations.get(position)));
        }
    }
}
