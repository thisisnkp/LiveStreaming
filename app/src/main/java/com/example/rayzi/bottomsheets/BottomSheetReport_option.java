package com.example.rayzi.bottomsheets;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.example.rayzi.R;
import com.example.rayzi.databinding.BottomSheetMessagedetailBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class BottomSheetReport_option {

    private final BottomSheetDialog bottomSheetDialog;
    private boolean submitButtonEnable = false;

    public BottomSheetReport_option(Context context, OnReportedListener onReportedListner) {
        bottomSheetDialog = new BottomSheetDialog(context, R.style.CustomBottomSheetDialogTheme);
        bottomSheetDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        bottomSheetDialog.setOnShowListener(dialog -> {
            BottomSheetDialog d = (BottomSheetDialog) dialog;
            FrameLayout bottomSheet = (FrameLayout) d.findViewById(R.id.design_bottom_sheet);
            BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
        });

        BottomSheetMessagedetailBinding sheetDilogBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.bottom_sheet_messagedetail, null, false);
        bottomSheetDialog.setContentView(sheetDilogBinding.getRoot());
        bottomSheetDialog.show();

        sheetDilogBinding.tvTime.setOnClickListener(view -> {
            bottomSheetDialog.dismiss();
            onReportedListner.onReported();
        });

        sheetDilogBinding.tvCopy.setOnClickListener(view -> {
            bottomSheetDialog.dismiss();
            onReportedListner.onBlocked();
            Toast.makeText(context, "Blocked Successfully", Toast.LENGTH_SHORT).show();
        });

    }

    public interface OnReportedListener {
        void onReported();

        void onBlocked();

    }
}
