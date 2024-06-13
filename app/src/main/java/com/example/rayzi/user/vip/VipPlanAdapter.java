package com.example.rayzi.user.vip;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.example.rayzi.R;
import com.example.rayzi.databinding.ItemVipPlanBinding;
import com.example.rayzi.modelclass.VipPlanRoot;

import java.util.ArrayList;
import java.util.List;

public class VipPlanAdapter extends RecyclerView.Adapter<VipPlanAdapter.VipPlanViewHolder> {

    Context context;
    OnPlanClickLisnter onPlanClickLisnter;
    List<VipPlanRoot.VipPlanItem> vipPlan = new ArrayList<>();
    int pos = 0;

    public void setOnPlanClickListener(OnPlanClickLisnter onPlanClickLisnter) {
        this.onPlanClickLisnter = onPlanClickLisnter;
    }

    @Override
    public VipPlanViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new VipPlanViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vip_plan, parent, false));
    }

    @Override
    public void onBindViewHolder(VipPlanViewHolder holder, int position) {
        if (pos == position) {
            holder.binding.mainRelativeLayout.setBackgroundResource(R.drawable.vipplanpinkbgbordergray);
            holder.binding.tvAmount.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E61755")));
        } else {
            holder.binding.mainRelativeLayout.setBackgroundResource(R.drawable.vipplanwhitebgbordergray);
            holder.binding.tvAmount.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#342F3A")));
        }
        holder.setData(position);
    }

    @Override
    public int getItemCount() {
        return vipPlan.size();
    }

    public void addData(List<VipPlanRoot.VipPlanItem> vipPlan) {
        this.vipPlan = vipPlan;
        notifyDataSetChanged();
    }

    public interface OnPlanClickLisnter {
        void onPlanClick(VipPlanRoot.VipPlanItem vipPlanItem);
    }

    public class VipPlanViewHolder extends RecyclerView.ViewHolder {
        ItemVipPlanBinding binding;

        public VipPlanViewHolder(View itemView) {
            super(itemView);
            binding = ItemVipPlanBinding.bind(itemView);
        }

        @SuppressLint("SetTextI18n")
        public void setData(int position) {

            VipPlanRoot.VipPlanItem plan = vipPlan.get(position);
            binding.tvDays.setText(String.valueOf(plan.getValidity()));
            binding.planTime.setText(plan.getValidityType());
            binding.tvAmount.setText("$ " + plan.getDollar());
            binding.tvAmountPerMonth.setText(plan.getDollar() / plan.getValidity() + "/M");

            if (plan.isTop()) {
                binding.tvOfferTag1.setVisibility(View.VISIBLE);
                binding.tvOfferTag1.setText("Hot");
            } else {
                binding.tvOfferTag1.setVisibility(View.GONE);
            }

            binding.getRoot().setOnClickListener(v -> {
                pos = position;
                notifyDataSetChanged();
                onPlanClickLisnter.onPlanClick(vipPlan.get(position));
            });
        }
    }
}
