package com.example.rayzi.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.example.rayzi.R;
import com.example.rayzi.databinding.LiveFragmentBinding;


public class PostFragment extends Fragment {
    private CameraSelector cameraSelector;
    private ProcessCameraProvider cameraProvider;
    private Preview preview;
    private LiveFragmentBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        binding = DataBindingUtil.inflate(inflater, R.layout.live_fragment, container, false);
        return binding.getRoot();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

}
