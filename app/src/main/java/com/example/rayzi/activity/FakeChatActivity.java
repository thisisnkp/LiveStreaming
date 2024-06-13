package com.example.rayzi.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.rayzi.FakeChat.FakeChatAdapter;
import com.example.rayzi.FakeChat.fakemodelclass.ChatRootFake;
import com.example.rayzi.R;
import com.example.rayzi.SessionManager;
import com.example.rayzi.bottomsheets.BottomSheetReport_g;
import com.example.rayzi.bottomsheets.BottomSheetReport_option;
import com.example.rayzi.databinding.ActivityFakeChatBinding;
import com.example.rayzi.modelclass.ChatUserListRoot;
import com.example.rayzi.retrofit.Const;
import com.example.rayzi.user.guestUser.GuestActivity;
import com.google.gson.Gson;
import com.yalantis.ucrop.UCrop;

public class FakeChatActivity extends BaseActivity {
    ActivityFakeChatBinding binding;
    FakeChatAdapter chatAdapter = new FakeChatAdapter();
    SessionManager sessionManager;
    private ChatUserListRoot.ChatUserItem chatUser;
    boolean isSend = false;
    boolean isShowFullImage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_fake_chat);
        getWindow().setStatusBarColor(Color.parseColor("#292132"));

        sessionManager = new SessionManager(this);
        Intent intent = getIntent();
        String userStr = intent.getStringExtra(Const.CHATROOM);
        Log.e(TAG, "onCreate: >>>>>>>>>>>> " + userStr);
        if (userStr != null && !userStr.isEmpty()) {
            chatUser = new Gson().fromJson(userStr, ChatUserListRoot.ChatUserItem.class);
            initView();
        }
        initListener();
    }

    private void initListener() {
        binding.btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messgae = binding.etChat.getText().toString();
                if (messgae.equals("")) {
                    Toast.makeText(FakeChatActivity.this, "Type message first", Toast.LENGTH_SHORT).show();
                    return;
                }
                binding.etChat.setText("");
                chatAdapter.addSingleMessage(new ChatRootFake(1, messgae, chatUser.getImage()));
                binding.rvChat.scrollToPosition(chatAdapter.getItemCount() - 1);
            }
        });
    }

    private void initView() {

        Glide.with(this).load(chatUser.getImage()).circleCrop().into(binding.imgUser);
        binding.tvUserNamew.setText(chatUser.getName());
        binding.rvChat.setAdapter(chatAdapter);

        chatAdapter.setOnClickListener(new FakeChatAdapter.OnClickListener() {
            @Override
            public void onImageClick(int position, String imageUrl) {

                isShowFullImage = true;

                binding.layFullImage.setVisibility(View.VISIBLE);

                Glide.with(FakeChatActivity.this)
                        .load(imageUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(binding.ivFullImage);

                binding.ivClose.setOnClickListener(view -> {
                    isShowFullImage = false;
                    binding.layFullImage.setVisibility(View.GONE);
                });
            }
        });

    }

    public void onClickVideoCall(View view) {
        startActivity(new Intent(this, FakeCallRequestActivity.class).putExtra(Const.CHATROOM, new Gson().toJson(chatUser)));
    }

    public void onClickUser(View view) {
        if (chatUser != null) {
            startActivity(new Intent(this, GuestActivity.class).putExtra(Const.USER_STR, new Gson().toJson(chatUser)));
        }
    }

    public void onClickCamara(View view) {
        choosePhoto();
    }

    private boolean checkPermission() {
        int result;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES);
        } else {
            result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void choosePhoto() {

        if (checkPermission()) {
            openGallery(this);
        } else {
            requestPermission();
        }

    }

    private void requestPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 10001);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1001);
        }

    }

    private String picturePath;
    private Uri selectedImage;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 201 && resultCode == RESULT_OK && null != data) {
            selectedImage = data.getData();
            startCropActivity(data.getData());

            Glide.with(this)
                    .load(selectedImage)
                    .placeholder(R.drawable.ic_user_place).error(R.drawable.ic_user_place)
                    .into(binding.imageview);

            picturePath = getRealPathFromURI(selectedImage);

            isSend = false;
        } else if (requestCode == 69 && resultCode == -1) {
            handleCropResult(data);
        }
        if (resultCode == 96) {
            handleCropError(data);
        }

    }

    private void handleCropResult(@androidx.annotation.NonNull Intent result) {
        Uri resultUri = UCrop.getOutput(result);
        if (resultUri != null) {

            selectedImage = resultUri;

            Glide.with(this)
                    .load(selectedImage)
                    .placeholder(R.drawable.ic_user_place).error(R.drawable.ic_user_place)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.imageview);
            binding.imageview.setAdjustViewBounds(true);
            picturePath = getRealPathFromURI(selectedImage);

            chatAdapter.addSingleMessage(new ChatRootFake(2, picturePath, chatUser.getImage()));
            binding.rvChat.scrollToPosition(chatAdapter.getItemCount() - 1);

        } else {
            Toast.makeText(this, "toast_cannot_retrieve_cropped_image", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleCropError(@androidx.annotation.NonNull Intent result) {
        Throwable cropError = UCrop.getError(result);
        if (cropError != null) {
            Log.e("TAG", "handleCropError: ", cropError);
            Toast.makeText(this, cropError.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(this, "toast_unexpected_error", Toast.LENGTH_SHORT).show();
    }

    public void openGallery(Context context) {
        try {
            startActivityForResult(new Intent("android.intent.action.PICK", MediaStore.Images.Media.EXTERNAL_CONTENT_URI), 201);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onClickReport(View view) {
        if (chatUser == null) return;
        new BottomSheetReport_option(FakeChatActivity.this, new BottomSheetReport_option.OnReportedListener() {
            @Override
            public void onReported() {
                new BottomSheetReport_g(FakeChatActivity.this, chatUser.getUserId(), () -> {
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.toast_layout, (ViewGroup) findViewById(R.id.customtoastlyt));
                    Toast toast = new Toast(getApplicationContext());
                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();

                });
            }

            @Override
            public void onBlocked() {
                finish();
            }
        });

    }

    @Override
    public void onBackPressed() {
        if (isShowFullImage) {
            binding.layFullImage.setVisibility(View.GONE);
            isShowFullImage = false;
        } else {
            super.onBackPressed();
        }

    }
}