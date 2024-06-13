package com.example.rayzi.posts;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.example.rayzi.R;
import com.example.rayzi.activity.BaseActivity;
import com.example.rayzi.databinding.ActivityLocationChooseBinding;
import com.example.rayzi.modelclass.SearchLocationRoot;
import com.example.rayzi.retrofit.Const;
import com.example.rayzi.retrofit.RetrofitBuilder;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationChooseActivity extends BaseActivity {
    public static final int REQ_CODE_LOCATION = 123;
    ActivityLocationChooseBinding binding;
    LocationAdapter locationAdapter = new LocationAdapter();
    private String keyword = "Surat";
    private Call<SearchLocationRoot> call;
    private int start = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_location_choose);
        Intent intent = getIntent();
        String location = intent.getStringExtra(Const.DATA);
        if (location != null && !location.isEmpty()) {
            binding.etLocation.setText(location);
            searchLocation();
        } else {
            if (!sessionManager.getStringValue(Const.CURRENT_CITY).isEmpty()) {
                binding.etLocation.setText(sessionManager.getStringValue(Const.CURRENT_CITY));
            } else {
                binding.etLocation.setText(sessionManager.getStringValue(Const.COUNTRY));
            }
            searchLocation();
        }

        binding.btnDone.setOnClickListener(v -> {
            Intent i = getIntent();
            i.putExtra(Const.DATA, binding.etLocation.getText().toString().trim());
            setResult(RESULT_OK, i);
            finish();
        });
        binding.rvLocation.setAdapter(locationAdapter);


        locationAdapter.setOnLocationClickLisnter(selectedLocation -> {
            Intent i = getIntent();

            i.putExtra(Const.DATA, new Gson().toJson(selectedLocation));
            setResult(RESULT_OK, i);
            finish();
        });

        binding.etLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                keyword = s.toString();
                searchLocation();

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.backimg.setScaleX(isRTL(this) ? -1 : 1);
    }

    private void searchLocation() {
        if (call != null) {
            call.cancel();
        }
        binding.loder.setVisibility(View.VISIBLE);
        binding.noData.setVisibility(View.GONE);
        call = RetrofitBuilder.getLocation().searchLocation("60498b173fb991ed89c2f10f6a187dfd", keyword);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<SearchLocationRoot> call, Response<SearchLocationRoot> response) {
                if (response.code() == 200) {
                    if (!response.body().getData().isEmpty()) {
                        locationAdapter.clear();
                        locationAdapter.addData(response.body().getData());
                    } else if (start == 0) {
                        binding.noData.setVisibility(View.VISIBLE);
                    }
                }
                binding.loder.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<SearchLocationRoot> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}