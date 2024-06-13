package com.example.rayzi.user.complain;

import static android.provider.MediaStore.MediaColumns.DATA;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.rayzi.R;
import com.example.rayzi.activity.BaseActivity;
import com.example.rayzi.databinding.ActivityCreateComplainBinding;
import com.example.rayzi.modelclass.RestResponse;
import com.example.rayzi.retrofit.RetrofitBuilder;

import java.io.File;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateComplainActivity extends BaseActivity {
    private static final int GALLERY_CODE = 1001;
    private static final String[] REQUESTED_PERMISSIONS = {Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final String[] REQUESTED_PERMISSIONS_13 = {Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO};
    private static final int PERMISSION_REQ_ID = 22;
    private static final int REQ_ID = 1;
    ActivityCreateComplainBinding binding;
    Uri selectedImage;
    String picturePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_complain);
        getWindow().setStatusBarColor(Color.parseColor("#292132"));

        initView();
        initListener();

    }

    private void initListener() {
        binding.btnOpenGallery.setOnClickListener(v -> {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                if (checkSelfPermission(REQUESTED_PERMISSIONS_13[0], REQ_ID) && checkSelfPermission(REQUESTED_PERMISSIONS_13[1], REQ_ID) && checkSelfPermission(REQUESTED_PERMISSIONS_13[2], REQ_ID) && checkSelfPermission(REQUESTED_PERMISSIONS_13[3], REQ_ID)) {
                    Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, GALLERY_CODE);
                } else {
                    ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS_13, REQ_ID);
                }

            } else {

                if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) && checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID) && checkSelfPermission(REQUESTED_PERMISSIONS[2], PERMISSION_REQ_ID) && checkSelfPermission(REQUESTED_PERMISSIONS[3], PERMISSION_REQ_ID)) {
                    Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, GALLERY_CODE);
                } else {
                    ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
                }
            }

        });

    }

    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            //  ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @androidx.annotation.NonNull String[] permissions, @androidx.annotation.NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e(TAG, "onRequestPermissionsResult: " + requestCode);

        if (requestCode == PERMISSION_REQ_ID) {
            Log.e(TAG, "onRequestPermissionsResult: " + PackageManager.PERMISSION_GRANTED + "  " + grantResults[0] + " " + grantResults[1]);
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED || grantResults[2] != PackageManager.PERMISSION_GRANTED || grantResults[3] != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "onRequestPermissionsResult: 11 ");

                showLongToast("Need permissions " + Manifest.permission.READ_EXTERNAL_STORAGE + "/" + Manifest.permission.WRITE_EXTERNAL_STORAGE);
                finish();
                return;
            }

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (requestCode == PERMISSION_REQ_ID) {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED || grantResults[2] != PackageManager.PERMISSION_GRANTED || grantResults[3] != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "onRequestPermissionsResult: 22 ");
                    showLongToast("Need permissions ");
                    finish();
                }
            }
        }
    }

    private void showLongToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK && null != data) {
            selectedImage = data.getData();
            RequestOptions requestOptions = new RequestOptions();
            requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(16));

            Glide.with(this).load(selectedImage).apply(requestOptions).into(binding.image);
            String[] filePathColumn = {DATA};
            Cursor cursor = this.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            picturePath = cursor.getString(columnIndex);
            cursor.close();
        }
    }

    public void onClickSubmit(View view) {
        binding.tvSubmit.setEnabled(false);
        String message = binding.etMessage.getText().toString().trim();
        String contact = binding.etContact.getText().toString().trim();
        if (message.equals("")) {
            Toast.makeText(this, "please Enter Message", Toast.LENGTH_SHORT).show();
            return;
        }
        RequestBody messagebody = RequestBody.create(MediaType.parse("text/plain"), message);
        RequestBody contactbody = RequestBody.create(MediaType.parse("text/plain"), contact);
        RequestBody userIdbody = RequestBody.create(MediaType.parse("text/plain"), sessionManager.getUser().getId());
        HashMap<String, RequestBody> map = new HashMap<>();
        MultipartBody.Part body = null;
        Call<RestResponse> call = null;
        if (picturePath != null) {
            File file = new File(picturePath);
            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);
        }

        call = RetrofitBuilder.create().addSupport(map, body);
        map.put("contact", contactbody);
        map.put("message", messagebody);
        map.put("userId", userIdbody);
        binding.animationView.setVisibility(View.VISIBLE);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<RestResponse> call, Response<RestResponse> response) {
                if (response.code() == 200) {
                    if (response.body().isStatus()) {
                        Toast.makeText(CreateComplainActivity.this, "Complain Send Successfully", Toast.LENGTH_SHORT).show();
                        try {
                            finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(CreateComplainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CreateComplainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }

                binding.tvSubmit.setEnabled(true);
                binding.animationView.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<RestResponse> call, Throwable t) {
                binding.tvSubmit.setEnabled(true);
                t.printStackTrace();
            }
        });
    }

    private void initView() {
        if (isRTL(this)) {
            binding.back.setScaleX(isRTL(this) ? -1 : 1);
        }
    }

}