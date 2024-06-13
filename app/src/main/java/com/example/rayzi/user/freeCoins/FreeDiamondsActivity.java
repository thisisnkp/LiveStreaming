package com.example.rayzi.user.freeCoins;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.example.rayzi.BuildConfig;
import com.example.rayzi.R;
import com.example.rayzi.activity.BaseActivity;
import com.example.rayzi.ads.MyRewardAds;
import com.example.rayzi.databinding.ActivityFreeDiamondsBinding;
import com.example.rayzi.modelclass.UserRoot;
import com.example.rayzi.retrofit.RetrofitBuilder;
import com.google.gson.JsonObject;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FreeDiamondsActivity extends BaseActivity implements MyRewardAds.RewardAdListener {
    ActivityFreeDiamondsBinding binding;
    MyRewardAds myRewardAds;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_free_diamonds);
        getWindow().setStatusBarColor(Color.parseColor("#170D1F"));
        myRewardAds = new MyRewardAds(this, this);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        binding.tvCode.setText(sessionManager.getUser().getReferralCode());
        binding.lytreferCount.setText("You have " + sessionManager.getUser().getReferralCount() + " referrals");

        binding.lytAds.setOnClickListener(v -> myRewardAds.showAds(this));
        binding.etRefercode.requestFocus();
        if (isRTL(this)) {
            binding.backimg.setScaleX(isRTL(this) ? -1 : 1);
        }
    }

    public void onClickCopy(View view) {

        ClipboardManager manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("text", binding.tvCode.getText().toString());
        if (manager != null) {
            manager.setPrimaryClip(clipData);
            Toast.makeText(this, "Referral Code Copied!", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickShare(View view) {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            String shareMessage = "\nLet me recommend you this application\n\n";
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n\n";
            shareMessage = shareMessage + "Here is My Referral Code " + sessionManager.getUser().getReferralCode().toUpperCase(Locale.ROOT);
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "choose one"));
        } catch (Exception e) {
            //ll
        }
    }

    public void onClickSubmit(View view) {
        binding.pd.setVisibility(View.VISIBLE);
        String referCode = binding.etRefercode.getText().toString();
        if (referCode.isEmpty()) {
            Toast.makeText(this, "Enter Refer Code", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("userId", sessionManager.getUser().getId());
        jsonObject.addProperty("referralCode", referCode);
        Call<UserRoot> call = RetrofitBuilder.create().reedemReferalCode(jsonObject);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<UserRoot> call, Response<UserRoot> response) {
                if (response.code() == 200) {
                    if (response.body().isStatus()) {
                        Toast.makeText(FreeDiamondsActivity.this, "Refereed Successfully", Toast.LENGTH_SHORT).show();
                        sessionManager.saveUser(response.body().getUser());
                    } else {
                        if (response.body().getMessage() != null) {
                            Toast.makeText(FreeDiamondsActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                binding.pd.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<UserRoot> call, Throwable t) {
                Log.d("TAG", "onFailure: " + t.getMessage());
                binding.pd.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public void onAdClosed() {
        myRewardAds = new MyRewardAds(this, this);
    }

    @Override
    public void onEarned() {
        myRewardAds = new MyRewardAds(this, this);
        submitData();
    }

    private void submitData() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("userId", sessionManager.getUser().getId());
        Call<UserRoot> call = RetrofitBuilder.create().addDiamondFromAds(jsonObject);
        call.enqueue(new Callback<UserRoot>() {
            @Override
            public void onResponse(Call<UserRoot> call, Response<UserRoot> response) {
                if (response.code() == 200 && response.body().isStatus()) {
                    sessionManager.saveUser(response.body().getUser());
                    Toast.makeText(FreeDiamondsActivity.this, "Earned by User", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(FreeDiamondsActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserRoot> call, Throwable t) {

            }
        });
    }
}