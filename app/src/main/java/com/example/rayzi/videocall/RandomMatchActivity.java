package com.example.rayzi.videocall;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.example.rayzi.R;
import com.example.rayzi.activity.BaseActivity;
import com.example.rayzi.databinding.ActivityRandomMatchBinding;
import com.example.rayzi.modelclass.GuestProfileRoot;
import com.example.rayzi.retrofit.Const;
import com.example.rayzi.retrofit.RetrofitBuilder;
import com.google.gson.Gson;

import jp.wasabeef.glide.transformations.BlurTransformation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RandomMatchActivity extends BaseActivity {
    ActivityRandomMatchBinding binding;

    private Animation animZoomin;
    private GuestProfileRoot.User guestUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_random_match);

        if (!isFinishing()) {
            binding.ivUser.setUserImage(sessionManager.getUser().getImage(), sessionManager.getUser().isIsVIP(), RandomMatchActivity.this, 10);
        }
        animZoomin = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoomin);

        matchAgain();

        binding.btnMatch.setOnClickListener(v -> matchAgain());
        binding.btnCall.setOnClickListener(v -> makeACall());
    }

    private void makeACall() {
        onBackPressed();
        startActivity(new Intent(this, CallRequestActivity.class).putExtra(Const.USER, new Gson().toJson(guestUser)).putExtra("type", getIntent().getStringExtra("type")));
    }

    @SuppressLint("SetTextI18n")
    private void matchAgain() {
        binding.lytStatus.setText("Searching for new Friends...");

        binding.ivBg.setVisibility(View.GONE);
        binding.ivUser2.setVisibility(View.GONE);
        binding.btnCall.setVisibility(View.GONE);
        binding.btnMatch.setVisibility(View.GONE);
        binding.ivUser.startAnimation(animZoomin);
        binding.ivMatch.setVisibility(View.VISIBLE);

        Call<GuestProfileRoot> call = RetrofitBuilder.create().getRandomUser(sessionManager.getUser().getId(), getIntent().getStringExtra("type"));
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<GuestProfileRoot> call, Response<GuestProfileRoot> response) {
                if (response.code() == 200) {
                    if (response.body().isStatus() && response.body().getUser() != null) {
                        guestUser = response.body().getUser();
                        setGuestUser();
                    } else {
                        Toast.makeText(RandomMatchActivity.this, "No One Found Online", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }
                }
            }

            @Override
            public void onFailure(Call<GuestProfileRoot> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void setGuestUser() {


        binding.lytStatus.setText("Matched with " + guestUser.getName());

        binding.ivUser.clearAnimation();
        binding.ivUser2.setVisibility(View.VISIBLE);
        binding.btnMatch.setVisibility(View.VISIBLE);
        binding.ivBg.setVisibility(View.VISIBLE);
        binding.btnCall.setVisibility(View.VISIBLE);
        binding.ivMatch.setVisibility(View.GONE);

        if (!isFinishing()) {
            MultiTransformation<Bitmap> transformations = new MultiTransformation<>(
                    new BlurTransformation(50),
                    new CenterCrop()
            );
            Glide.with(RandomMatchActivity.this)
                    .load(guestUser.getImage())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .transform(transformations)
                    .into(binding.ivBg);

            binding.ivUser2.setUserImage(guestUser.getImage(), guestUser.isVIP(), RandomMatchActivity.this, 10);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
}