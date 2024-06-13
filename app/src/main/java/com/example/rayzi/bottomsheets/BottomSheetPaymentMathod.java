package com.example.rayzi.bottomsheets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.databinding.DataBindingUtil;

import com.example.rayzi.R;
import com.example.rayzi.SessionManager;
import com.example.rayzi.databinding.BottomSheetPaymentBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class BottomSheetPaymentMathod {
    private final BottomSheetDialog bottomSheetDialog;
    private final Context context;
    private final BottomSheetPaymentBinding bottomSheetPaymentBinding;
    SessionManager sessionManager;
    private OnPaymentOptionListner onPaymentOptionListner;

    public BottomSheetPaymentMathod(Context context, OnPaymentOptionListner onPaymentOptionListner) {
        bottomSheetDialog = new BottomSheetDialog(context, R.style.CustomBottomSheetDialogTheme);
        this.context = context;
        sessionManager = new SessionManager(context);
        this.onPaymentOptionListner = onPaymentOptionListner;
        bottomSheetDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        bottomSheetDialog.setOnShowListener(dialog -> {
            BottomSheetDialog d = (BottomSheetDialog) dialog;
            FrameLayout bottomSheet = (FrameLayout) d.findViewById(R.id.design_bottom_sheet);
            BottomSheetBehavior.from(bottomSheet)
                    .setState(BottomSheetBehavior.STATE_EXPANDED);
        });
        bottomSheetPaymentBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.bottom_sheet_payment, null, false);
        bottomSheetDialog.setContentView(bottomSheetPaymentBinding.getRoot());


        if (sessionManager.getSetting().isGooglePlaySwitch()) {
            bottomSheetPaymentBinding.lytgooglepay.setVisibility(View.VISIBLE);
        } else {
            bottomSheetPaymentBinding.lytgooglepay.setVisibility(View.GONE);
        }
        if (sessionManager.getSetting().isStripeSwitch()) {
            bottomSheetPaymentBinding.lytstripe.setVisibility(View.VISIBLE);
        } else {
            bottomSheetPaymentBinding.lytstripe.setVisibility(View.GONE);
        }

        bottomSheetPaymentBinding.btnclose.setOnClickListener(v -> bottomSheetDialog.dismiss());
        bottomSheetPaymentBinding.lytgooglepay.setOnClickListener(v -> {
            onPaymentOptionListner.onPaymentOptionSelected(PaymentMethodType.GOOGLE);
            bottomSheetDialog.dismiss();
        });
        bottomSheetPaymentBinding.lytstripe.setOnClickListener(v -> {
            onPaymentOptionListner.onPaymentOptionSelected(PaymentMethodType.STRIPE);
            bottomSheetDialog.dismiss();
        });
        bottomSheetDialog.show();

    }

    public enum PaymentMethodType {
        GOOGLE, STRIPE;
    }

    public interface OnPaymentOptionListner {
        void onPaymentOptionSelected(PaymentMethodType google);

    }
}
