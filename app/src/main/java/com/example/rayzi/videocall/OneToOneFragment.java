package com.example.rayzi.videocall;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LifecycleOwner;

import com.example.rayzi.R;
import com.example.rayzi.activity.BaseFragment;
import com.example.rayzi.activity.MainActivity;
import com.example.rayzi.databinding.FragmentOneToOneBinding;
import com.example.rayzi.user.vip.VipPlanActivity;
import com.example.rayzi.user.wallet.MyWalletActivity;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

public class OneToOneFragment extends BaseFragment {

    FragmentOneToOneBinding binding;
    String type = "Female";
    private ProcessCameraProvider cameraProvider;
    private CameraSelector cameraSelector;
    private Preview preview;

    public OneToOneFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Window window = getActivity().getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_one_to_one, container, false);

        initView();
        initListener();
        return binding.getRoot();
    }

    private void initListener() {

        binding.layMale.setOnClickListener(view -> {
            type = "Male";
            binding.rbMale.setChecked(true);
            binding.rbFemale.setChecked(false);
            binding.rbRandom.setChecked(false);
        });

        binding.layFemale.setOnClickListener(view -> {
            type = "Female";
            binding.rbMale.setChecked(false);
            binding.rbFemale.setChecked(true);
            binding.rbRandom.setChecked(false);
        });

        binding.layRandom.setOnClickListener(view -> {
            type = "Both";
            binding.rbMale.setChecked(false);
            binding.rbFemale.setChecked(false);
            binding.rbRandom.setChecked(true);
        });

        binding.rbMale.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                type = "Male";
                binding.rbFemale.setChecked(false);
                binding.rbRandom.setChecked(false);
            }
        });

        binding.rbFemale.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                type = "Female";
                binding.rbMale.setChecked(false);
                binding.rbRandom.setChecked(false);
            }
        });

        binding.rbRandom.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                type = "Both";
                binding.rbMale.setChecked(false);
                binding.rbFemale.setChecked(false);
            }
        });

        binding.layStore.setOnClickListener(view -> {
            startActivity(new Intent(getActivity(), MyWalletActivity.class));
        });

        binding.ivFlip.setOnClickListener(view -> {
            if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
            } else {
                cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
            }
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, cameraSelector, preview);
        });

        binding.layFeed.setOnClickListener(view -> {
            ((MainActivity) getActivity()).changeFragment(2, R.id.miFeed);
        });

        binding.ivVip.setOnClickListener(view -> {
            startActivity(new Intent(getActivity(), VipPlanActivity.class));
        });

        binding.layGender.setOnClickListener(view -> {
            if (binding.laySelectGender.getVisibility() == View.VISIBLE) {
                binding.laySelectGender.setVisibility(View.GONE);
            } else {
                binding.laySelectGender.setVisibility(View.VISIBLE);
            }
        });

        binding.previewView.setOnClickListener(view -> startActivity(new Intent(getActivity(), RandomMatchActivity.class).putExtra("type", type)));

    }

    @SuppressLint({"SetTextI18n"})
    private void initView() {

        binding.tvMale.setText("Male (" + sessionManager.getSetting().getMaleCallCharge() + " diamond)");
        binding.tvFemale.setText("Female (" + sessionManager.getSetting().getFemaleCallCharge() + " diamond)");
        binding.tvBoth.setText("Both (FREE)");

        binding.tvDone.setOnClickListener(v -> {
            binding.laySelectGender.setVisibility(View.GONE);
            binding.tvGender.setText(type);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        new Handler(Looper.getMainLooper()).postDelayed(this::initCamera, 1000);
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.laySelectGender.setVisibility(View.GONE);
    }

    @SuppressLint("RestrictedApi")
    private void initCamera() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, 1);
        } else {
            try {
                final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireActivity());
                cameraProviderFuture.addListener(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            cameraProvider = cameraProviderFuture.get();
                            cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
                            preview = new Preview.Builder().build();
                            preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());
                            cameraProvider.unbindAll();
                            cameraProvider.bindToLifecycle((LifecycleOwner) requireActivity(), cameraSelector, preview);
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }, ContextCompat.getMainExecutor(requireActivity()));

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        initCamera();
    }

}