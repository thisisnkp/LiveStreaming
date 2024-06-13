package com.example.rayzi.posts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.rayzi.BuildConfig;
import com.example.rayzi.MainApplication;
import com.example.rayzi.R;
import com.example.rayzi.SessionManager;
import com.example.rayzi.activity.BaseActivity;
import com.example.rayzi.bottomsheets.BottomSheetReport_g;
import com.example.rayzi.bottomsheets.BottomSheetReport_option;
import com.example.rayzi.databinding.ItemFeed1Binding;
import com.example.rayzi.modelclass.PostRoot;
import com.example.rayzi.retrofit.Const;
import com.example.rayzi.user.guestUser.GuestActivity;
import com.like.LikeButton;
import com.like.OnLikeListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {

    OnPostClickListner onPostClickListner;
    SessionManager sessionManager;
    private Context context;
    private List<PostRoot.PostItem> postDummies = new ArrayList<>();

    public OnPostClickListner getOnPostClickListner() {
        return onPostClickListner;
    }

    public void setOnPostClickListner(OnPostClickListner onPostClickListner) {
        this.onPostClickListner = onPostClickListner;
    }

    @Override
    public FeedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        sessionManager = new SessionManager(context);
        return new FeedViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feed_1, parent, false));
    }

    @Override
    public void onBindViewHolder(FeedAdapter.FeedViewHolder holder, int position) {
        holder.setData(position);
    }

    @Override
    public int getItemCount() {
        return postDummies.size();
    }

    public void addData(List<PostRoot.PostItem> postDummies) {

        this.postDummies.addAll(postDummies);
        notifyItemRangeInserted(this.postDummies.size(), postDummies.size());
    }

    public void clear() {
        postDummies.clear();
        notifyDataSetChanged();
    }

    public void onClickReport(int position) {
        if (postDummies.get(position) == null) return;
        new BottomSheetReport_option(context, new BottomSheetReport_option.OnReportedListener() {
            @Override
            public void onReported() {
                new BottomSheetReport_g(context, postDummies.get(position).getUserId(), () -> {
                    Toast.makeText(context, "We will take immediately action for this user \n Thank You", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onBlocked() {
                notifyDataSetChanged();
            }
        });

    }

    public interface OnPostClickListner {
        void onLikeClick(PostRoot.PostItem postDummy, int position, ItemFeed1Binding binding);

        void onCommentListClick(PostRoot.PostItem postDummy);

        void onLikeListClick(PostRoot.PostItem postDummy);

        void onShareClick(PostRoot.PostItem postDummy);

        void onMentionClick(String userName);

    }

    public class FeedViewHolder extends RecyclerView.ViewHolder {
        ItemFeed1Binding binding;

        public FeedViewHolder(View itemView) {
            super(itemView);
            binding = ItemFeed1Binding.bind(itemView);
        }

        @SuppressLint("SetTextI18n")
        public void setData(int position) {
            PostRoot.PostItem postDummy = postDummies.get(position);
            Glide.with(context).load(BuildConfig.BASE_URL + postDummy.getPost())
                    .apply(MainApplication.requestOptionsFeed)
                    .into(binding.imagepost);
            binding.btnShare.setScaleX(BaseActivity.isRTL(context) ? -1 : 1);
            binding.imgMessage.setScaleX(BaseActivity.isRTL(context) ? -1 : 1);
            binding.imagepost.setAdjustViewBounds(true);
            binding.imgUser.setUserImage(postDummy.getUserImage(), postDummy.isIsVIP(), context, 10);

            if (postDummy.getLocation().isEmpty()) {
                binding.svgWebView.setVisibility(View.GONE);
                binding.tvLocation.setVisibility(View.GONE);
            } else {
                binding.svgWebView.setVisibility(View.VISIBLE);
                binding.tvLocation.setVisibility(View.VISIBLE);
            }

            if (postDummy.getCaption().isEmpty()) {
                binding.tvCaption1.setVisibility(View.GONE);
            } else {
                binding.tvCaption1.setVisibility(View.VISIBLE);
            }

            binding.tvCaption1.setText(postDummy.getCaption().trim());
            binding.tvCaption1.setHashtagEnabled(true);
            binding.tvCaption1.setMentionEnabled(true);
            binding.tvCaption1.setHashtagColor(ContextCompat.getColor(context, R.color.text_gray));
            binding.tvCaption1.setMentionColors(Objects.requireNonNull(ContextCompat.getColorStateList(context, R.color.pink)));
            binding.tvComments.setText(postDummy.getComment() + " Comments");
            binding.tvLikes.setText(postDummy.getLike() + " Likes");
            binding.tvusername.setText(postDummy.getName());
            binding.tvtime.setText(postDummy.getTime());
            binding.tvLocation.setText(postDummy.getLocation());
            binding.imgUser.setOnClickListener(v -> context.startActivity(new Intent(context, GuestActivity.class).putExtra(Const.USERID, postDummy.getUserId())));

            binding.likeButton.setLiked(postDummy.isIsLike());
            if (postDummy.isAllowComment()) {
                binding.lytComments.setVisibility(View.VISIBLE);
            } else {
                binding.lytComments.setVisibility(View.GONE);
            }
            binding.lytComments.setOnClickListener(v -> onPostClickListner.onCommentListClick(postDummy));
            binding.tvLikes.setOnClickListener(v -> onPostClickListner.onLikeListClick(postDummy));
            binding.btnShare.setOnClickListener(v -> onPostClickListner.onShareClick(postDummy));

            binding.likeButton.setOnLikeListener(new OnLikeListener() {
                @Override
                public void liked(LikeButton likeButton) {
                    onPostClickListner.onLikeClick(postDummy, position, binding);
                }

                @Override
                public void unLiked(LikeButton likeButton) {
                    onPostClickListner.onLikeClick(postDummy, position, binding);
                }
            });

            binding.btnRepot.setOnClickListener(view -> {
                onClickReport(position);
            });

            binding.tvCaption1.setOnMentionClickListener((view, text) -> onPostClickListner.onMentionClick(text.toString()));
/*
            binding.likeButton.setOnLikeListener(new OnLikeListener() {
                @Override
                public void liked(LikeButton likeButton) {
                    int like = Integer.parseInt(binding.tvLikes.getText().toString()) + 1;
                    binding.tvLikes.setText(String.valueOf(like));
                    onPostClickListner.onLikeClick(postDummy,position, binding);
                }

                @Override
                public void unLiked(LikeButton likeButton) {
                    int like = Integer.parseInt(binding.tvLikes.getText().toString()) - 1;
                    binding.tvLikes.setText(String.valueOf(like));
                    onPostClickListner.onLikeClick(postDummy, position, binding);
                }
            });
*/


        }
    }

}
