package com.example.rayzi.user.wallet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.example.rayzi.R;
import com.example.rayzi.databinding.ItemPurchaseCoinBinding;
import com.example.rayzi.modelclass.DiamondPlanRoot;
import com.example.rayzi.retrofit.Const;

import java.util.List;

public class CoinPurchaseAdapter extends RecyclerView.Adapter<CoinPurchaseAdapter.CoinViewHolder> {

    OnCoinPlanClickListner onCoinPlanClickListner;
    private Context context;
    private OnBuyCoinClickListnear onBuyCoinClickListnear;
    private List<DiamondPlanRoot.DiamondPlanItem> coinList;

    public CoinPurchaseAdapter(List<DiamondPlanRoot.DiamondPlanItem> coinList, OnBuyCoinClickListnear onBuyCoinClickListnear) {

        this.coinList = coinList;
        this.onBuyCoinClickListnear = onBuyCoinClickListnear;
    }

    public OnCoinPlanClickListner getOnCoinPlanClickListner() {
        return onCoinPlanClickListner;
    }


    @Override
    public CoinViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new CoinViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_purchase_coin, parent, false));
    }

    @Override
    public void onBindViewHolder(CoinViewHolder holder, int position) {
        holder.setData(position);
    }

    @Override
    public int getItemCount() {
        return coinList.size();
    }

    public void addData(List<DiamondPlanRoot.DiamondPlanItem> coinList) {

        this.coinList.addAll(coinList);
        notifyItemRangeInserted(this.coinList.size(), coinList.size());
    }

    public interface OnCoinPlanClickListner {
        void onPlanClick(DiamondPlanRoot.DiamondPlanItem coinPlan);
    }

    public interface OnBuyCoinClickListnear {
        void onButClick(DiamondPlanRoot.DiamondPlanItem dataItem);
    }

    public class CoinViewHolder extends RecyclerView.ViewHolder {
        ItemPurchaseCoinBinding binding;

        public CoinViewHolder(View itemView) {
            super(itemView);
            binding = ItemPurchaseCoinBinding.bind(itemView);
        }

        @SuppressLint("SetTextI18n")
        public void setData(int position) {
            DiamondPlanRoot.DiamondPlanItem coinPlan = coinList.get(position);
            binding.tvCoin.setText(String.valueOf(coinPlan.getDiamonds()));
            binding.layTag.setVisibility(coinPlan.getTag().isEmpty() ? View.GONE : View.VISIBLE);
            binding.tvTag.setText(coinPlan.getTag());
            binding.tvAmount.setText(Const.getCurrency() + coinPlan.getDollar());
            binding.getRoot().setOnClickListener(v -> onBuyCoinClickListnear.onButClick(coinList.get(position)));
        }
    }
}
