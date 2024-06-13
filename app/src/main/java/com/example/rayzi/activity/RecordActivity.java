package com.example.rayzi.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.example.rayzi.R;
import com.example.rayzi.databinding.ActivityRecordBinding;
import com.example.rayzi.modelclass.CoinRecordRoot;
import com.example.rayzi.modelclass.CustomDate;
import com.example.rayzi.retrofit.Const;
import com.example.rayzi.retrofit.RetrofitBuilder;
import com.example.rayzi.user.wallet.HistoryActivity;
import com.example.rayzi.utils.datepicker.utils.DateUtils;
import com.example.rayzi.utils.datepicker.view.datePicker.DatePicker;
import com.google.gson.Gson;

import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecordActivity extends BaseActivity {

    ActivityRecordBinding binding;
    DatePicker datePicker;
    int selectedType = 1;
    CustomDate selectedDate;
    CustomDate startDate;
    CustomDate endDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_record);

        initMain();
        initDatePiker();
        initListener();

        int day = DateUtils.getDay(DateUtils.getCurrentTime());
        int month = DateUtils.getMonth(DateUtils.getCurrentTime()) + 1;
        int year = DateUtils.getYear(DateUtils.getCurrentTime());

        selectedDate = new CustomDate(day, month, year);
        startDate = selectedDate;
        endDate = selectedDate;
        binding.tvDate1.setText(selectedDate.getDateForHuman());
        binding.tvDate2.setText(selectedDate.getDateForHuman());
        getRecordData();
        binding.lytDiamonds.setOnClickListener(v -> startActivity(new Intent(RecordActivity.this, HistoryActivity.class)
                .putExtra(Const.TYPE, Const.DIAMOND).putExtra(Const.STARTDATE, new Gson().toJson(startDate)).putExtra(Const.ENDDATE, new Gson().toJson(endDate))));
        binding.lytRcoins.setOnClickListener(v -> startActivity(new Intent(RecordActivity.this, HistoryActivity.class)
                .putExtra(Const.TYPE, Const.RCOINS).putExtra(Const.STARTDATE, new Gson().toJson(startDate)).putExtra(Const.ENDDATE, new Gson().toJson(endDate))));

        binding.backimg.setOnClickListener(view -> {
            onBackPressed();
        });

    }

    private void initDatePiker() {
        binding.lytDatePicker.lytDatePicker.setVisibility(View.GONE);
        datePicker = binding.lytDatePicker.datepicker;
        datePicker.setOffset(3);
        datePicker.setTextSize(19);
        datePicker.setPickerMode(DatePicker.DAY_ON_FIRST);
        datePicker.setDarkModeEnabled(true);
        datePicker.setMaxDate(DateUtils.getTimeMiles(2099, 12, 25));
        datePicker.setDate(DateUtils.getCurrentTime());
        datePicker.setMinDate(DateUtils.getTimeMiles(2022, 0, 1));
        datePicker.setDataSelectListener((date, day, month, year) -> {
            selectedDate = new CustomDate(day, month + 1, year);
        });
        binding.lytDatePicker.tvCancel.setOnClickListener(v -> binding.lytDatePicker.lytDatePicker.setVisibility(View.GONE));
        binding.lytDatePicker.tvConfirm.setOnClickListener(v -> {
            binding.lytDatePicker.lytDatePicker.setVisibility(View.GONE);
            if (startDate.getDateForHuman() != null) {
                if (selectedType == 1) {
                    startDate = selectedDate;
                    binding.tvDate1.setText(startDate.getDateForHuman());
                } else {
                    endDate = selectedDate;
                    binding.tvDate2.setText(endDate.getDateForHuman());

                }
            }
            getRecordData();
        });

    }

    private void getRecordData() {
        Call<CoinRecordRoot> call = RetrofitBuilder.create().getCoinRecord(sessionManager.getUser().getId(), startDate.getDateForServer(), endDate.getDateForServer());
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<CoinRecordRoot> call, Response<CoinRecordRoot> response) {
                if (response.code() == 200) {
                    if (response.body().isStatus()) {
                        if (response.body().getDiamond() != null) {
                            CoinRecordRoot.Diamond diamonds = response.body().getDiamond();
                            binding.tvDimondsIncome.setText(String.valueOf(diamonds.getIncome()));
                            binding.tvDiamondsOutcome.setText(String.valueOf(diamonds.getOutgoing()));
                        }
                        if (response.body().getRCoin() != null) {
                            CoinRecordRoot.RCoin rCoins = response.body().getRCoin();
                            binding.tvRcoinIncome.setText(String.valueOf(rCoins.getIncome()));
                            binding.tvRcoinOutcome.setText(String.valueOf(rCoins.getOutgoing()));
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<CoinRecordRoot> call, Throwable t) {
                t.printStackTrace();
            }
        });

    }

    private void initListener() {
        binding.lytDate1.setOnClickListener(v -> {
            selectedType = 1;
            binding.lytDatePicker.lytDatePicker.setVisibility(View.VISIBLE);
        });
        binding.lytDate2.setOnClickListener(v -> {
            selectedType = 2;
            binding.lytDatePicker.lytDatePicker.setVisibility(View.VISIBLE);
        });

    }

    private void initMain() {

        Random random = new Random();
        binding.tvRcoinIncome.setText(String.valueOf(random.nextInt(999)));
        binding.tvRcoinOutcome.setText(String.valueOf(random.nextInt(999)));
        binding.tvDimondsIncome.setText(String.valueOf(random.nextInt(999)));
        binding.tvDiamondsOutcome.setText(String.valueOf(random.nextInt(999)));
    }
}