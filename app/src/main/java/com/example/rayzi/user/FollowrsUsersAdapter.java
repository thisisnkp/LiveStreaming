package com.example.rayzi.user;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.example.rayzi.R;
import com.example.rayzi.databinding.ItemFollowrsBinding;
import com.example.rayzi.modelclass.GuestProfileRoot;
import com.example.rayzi.retrofit.Const;
import com.example.rayzi.user.guestUser.GuestActivity;

import java.util.ArrayList;
import java.util.List;

public class FollowrsUsersAdapter extends RecyclerView.Adapter<FollowrsUsersAdapter.FollowrsUserViewHolder> {

    private Context context;
    private List<GuestProfileRoot.User> users = new ArrayList<>();

    @Override
    public FollowrsUserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new FollowrsUserViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_followrs, parent, false));
    }

    @Override
    public void onBindViewHolder(FollowrsUserViewHolder holder, int position) {
        holder.setData(position);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void addData(List<GuestProfileRoot.User> userDummies) {

        this.users.addAll(userDummies);
        notifyItemRangeInserted(this.users.size(), userDummies.size());
    }

    public void clear() {
        users.clear();
        notifyDataSetChanged();
    }

    public class FollowrsUserViewHolder extends RecyclerView.ViewHolder {
        ItemFollowrsBinding binding;

        public FollowrsUserViewHolder(View itemView) {
            super(itemView);
            binding = ItemFollowrsBinding.bind(itemView);
        }

        public void setData(int position) {


            GuestProfileRoot.User userDummy = users.get(position);
            binding.imguser.setUserImage(userDummy.getImage(), userDummy.isVIP(), context, 10);
            binding.tvusername.setText(userDummy.getName());
            binding.tvBio.setText(userDummy.getBio());
            if (userDummy.getBio() != null && !userDummy.getBio().isEmpty()) {

                binding.tvBio.setVisibility(View.VISIBLE);
            } else {
                binding.tvBio.setVisibility(View.GONE);
            }
            binding.tvcountry.setText(userDummy.getCountry());
            binding.getRoot().setOnClickListener(v -> context.startActivity(new Intent(context, GuestActivity.class).putExtra(Const.USERID, userDummy.getUserId())));
        }
    }
}
