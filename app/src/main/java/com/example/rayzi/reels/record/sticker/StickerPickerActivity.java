package com.example.rayzi.reels.record.sticker;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.example.rayzi.R;
import com.example.rayzi.RayziUtils;
import com.example.rayzi.activity.BaseActivity;
import com.example.rayzi.databinding.ActivityStickerPickerBinding;
import com.example.rayzi.modelclass.StickerRoot;

public class StickerPickerActivity extends BaseActivity {

    public static final String EXTRA_STICKER = "sticker";

    private static final String TAG = "StickerPickerActivity";

    ActivityStickerPickerBinding binding;
    StickerAdapter stickerAdapter = new StickerAdapter();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sticker_picker);

        binding.rvSongs.setAdapter(stickerAdapter);
        stickerAdapter.addData(RayziUtils.getSticker());
        stickerAdapter.setOnSongClickListner(sticker -> closeWithSelection(sticker));


    }


    public void closeWithSelection(StickerRoot.StickerItem stickerDummy) {
        Intent data = new Intent();
        data.putExtra(EXTRA_STICKER, stickerDummy);
        setResult(RESULT_OK, data);
        finish();
    }


}
