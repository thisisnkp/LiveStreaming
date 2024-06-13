package com.example.rayzi.user.wallet;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;

import com.example.rayzi.R;
import com.example.rayzi.activity.BaseFragment;
import com.example.rayzi.databinding.FragmentRcoinBinding;
import com.example.rayzi.modelclass.UserRoot;
import com.example.rayzi.popups.PopupBuilder;
import com.example.rayzi.retrofit.Const;
import com.example.rayzi.retrofit.RetrofitBuilder;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RcoinFragment extends BaseFragment {

    FragmentRcoinBinding binding;
    private PopupBuilder popupBuilder;

    public RcoinFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_rcoin, container, false);
        popupBuilder = new PopupBuilder(getActivity());

        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    private void initMain() {
        binding.tvSettingRcoin.setText(sessionManager.getSetting().getRCoinForDiamond() + Const.CoinName);
        binding.tvRcoin.setText(String.valueOf(sessionManager.getUser().getRCoin()));
        binding.tvWithdrawingRcoin.setText(String.valueOf(sessionManager.getUser().getWithdrawalRcoin()));
        binding.btnConvert.setOnClickListener(v -> {
            popupBuilder.showRcoinConvertPopup(false, sessionManager.getUser().getRCoin(), rcoin -> convertRcoinToDiamond(rcoin));
        });

        binding.btnCashout.setOnClickListener(v -> {
            if (!sessionManager.getUser().isIsVIP()) {
                if (!sessionManager.getUser().getLevel().getAccessibleFunction().isCashOut()) {
                    new PopupBuilder(getActivity()).showSimplePopup("You are not able to cashout at your level", "Dismiss", () -> {
                    });
                    return;
                }
            }
            startActivity(new Intent(getActivity(), CashOutActivity.class));
           /* PopupBuilder popupBuilder = new PopupBuilder(getActivity());
            popupBuilder.showRcoinConvertPopup(true, myRcoin, rcoin -> {
                double cash = rcoin / 100;

            });
*/
        });

    }

    private void convertRcoinToDiamond(int rcoin) {
        binding.loder.setVisibility(View.VISIBLE);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("userId", sessionManager.getUser().getId());
        jsonObject.addProperty("rCoin", rcoin);
        Call<UserRoot> call = RetrofitBuilder.create().convertRcoinToDiamond(jsonObject);
        call.enqueue(new Callback<UserRoot>() {
            @Override
            public void onResponse(Call<UserRoot> call, Response<UserRoot> response) {
                if (response.code() == 200) {
                    if (response.body().isStatus()) {
                        sessionManager.saveUser(response.body().getUser());
                        double dimonds = rcoin / sessionManager.getSetting().getRCoinForDiamond();
                        String s = "Your " + rcoin + Const.CoinName + "Successfully Converted into " + dimonds + " Diamonds";
                        popupBuilder.showSimplePopup(s, "Continue", () -> initMain());
                    } else {
                        popupBuilder.showSimplePopup(response.body().getMessage(), "Continue", () -> initMain());
                    }
                }
                binding.loder.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<UserRoot> call, Throwable t) {
                binding.loder.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        initMain();
    }
}