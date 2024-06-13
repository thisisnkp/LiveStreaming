package com.example.rayzi.user;

import static android.provider.MediaStore.MediaColumns.DATA;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.rayzi.BuildConfig;
import com.example.rayzi.MainApplication;
import com.example.rayzi.R;
import com.example.rayzi.activity.BaseActivity;
import com.example.rayzi.activity.MainActivity;
import com.example.rayzi.databinding.ActivityEditProfileBinding;
import com.example.rayzi.modelclass.RestResponse;
import com.example.rayzi.modelclass.UserRoot;
import com.example.rayzi.retrofit.Const;
import com.example.rayzi.retrofit.RetrofitBuilder;

import java.io.File;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends BaseActivity {
    private static final int GALLERY_CODE = 1001;
    private static final int GALLERY_CODE_1 = 1002;
    private static final int PERMISSION_REQUEST_CODE = 111;
    private static final String TAG = "Editprofileact";
    ActivityEditProfileBinding binding;
    boolean isValidUserName = false;
    String nameS, usernameS;
    UserRoot.User userDummy;
    private String gender = "";
    private String picturePath = "";
    private String coverPhotoPath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_profile);
        getWindow().setStatusBarColor(Color.parseColor("#292132"));

        binding.pd1.setVisibility(View.GONE);
        userDummy = sessionManager.getUser();

        Glide.with(this).load(userDummy.getImage())
                .apply(MainApplication.requestOptions)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .circleCrop().into(binding.imgUser);

        if (userDummy.getCoverImage() != null || !userDummy.getCoverImage().isEmpty()) {
            Glide.with(this).load(userDummy.getCoverImage())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .apply(MainApplication.requestOptions)
                    .centerCrop().into(binding.ivCoverPhoto);
        } else {
            Glide.with(this).load(R.drawable.default_cover)
                    .apply(MainApplication.requestOptions)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop().into(binding.ivCoverPhoto);
        }

//        String userName = android.os.Build.MODEL;
//        Log.d(TAG, "onCreate: " + android.os.Build.MODEL);
//        binding.etUserName.setText(userName);
        binding.etName.setText(userDummy.getName());
        binding.etBio.setText(userDummy.getBio());
        binding.etAge.setText(String.valueOf(userDummy.getAge()));
        if (String.valueOf(userDummy.getAge()).equals("0")) {
            binding.etAge.setText("18");
        }

        binding.etUserName.setText(userDummy.getUsername());

        binding.lytMale.setOnClickListener(v -> onMaleClick());
        binding.lytFemale.setOnClickListener(v -> onFeMaleClick());
        binding.radioFemale.setOnClickListener(v -> onFeMaleClick());
        binding.radioMale.setOnClickListener(v -> onMaleClick());

        if (userDummy.getGender().equalsIgnoreCase(Const.MALE)) {
            binding.lytMale.performClick();
        } else if (userDummy.getGender().equalsIgnoreCase(Const.FEMALE)) {
            binding.tvFemale.performClick();
        } else {
            binding.lytMale.performClick();
        }
        if (isRTL(this)) {
            binding.back.setScaleX(isRTL(this) ? -1 : 1);
        }

        gender = Const.MALE;

        isValidUserName = !userDummy.getUsername().isEmpty();
        Log.d(TAG, "checkDetails: " + isValidUserName + "  " + gender);
        if (userDummy != null && userDummy.getUsername() != null && !userDummy.getUsername().isEmpty()) {
            binding.etUserName.setText(userDummy.getUsername());
            isValidUserName = true;
            // binding.etUserName.setEnabled(false);
        }

        if (userDummy.getUsername() != null && !userDummy.getUsername().isEmpty()) {

            binding.etUserName.setText(userDummy.getUsername());
            isValidUserName = true;
            //  binding.etUserName.setEnabled(false);
        }
        binding.imgUser.setOnClickListener(v -> choosePhoto(GALLERY_CODE));
        binding.btnPencil.setOnClickListener(v -> choosePhoto(GALLERY_CODE));
        binding.ivCoverPhotoEdit.setOnClickListener(v -> choosePhoto(GALLERY_CODE_1));

        binding.etUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkValidation(s.toString());
                usernameS = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {
//                if (!usernameS.isEmpty()) {
//                  checkDetails();
//                }
            }
        });

        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                nameS = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.etAge.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) {
                    binding.etAge.setError("Enter Correct Age");
                    return;
                }
                int age = Integer.parseInt(s.toString());
                if (age < 18 || age > 105) {
                    binding.etAge.setError("Minimum age must be 18 years.");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.tvSubmit.setOnClickListener(v -> {
            binding.tvSubmit.setEnabled(false);
            String name = binding.etName.getText().toString().trim();
            String userName1 = binding.etUserName.getText().toString().trim();
            String bio = binding.etBio.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Enter your name first", Toast.LENGTH_SHORT).show();
                return;
            }

            if (userName1.isEmpty()) {
                Toast.makeText(this, "Enter Username first", Toast.LENGTH_SHORT).show();
                return;
            }
            if (gender.isEmpty()) {
                Toast.makeText(this, "Select your gender", Toast.LENGTH_SHORT).show();
                return;
            }

            int age = Integer.parseInt(binding.etAge.getText().toString());
            if (age < 18 || age > 105) {
                Toast.makeText(this, "Minimum age must be 18 years.", Toast.LENGTH_SHORT).show();
                return;
            }

            HashMap<String, RequestBody> map = new HashMap<>();

            MultipartBody.Part body = null;
            if (picturePath != null && !picturePath.isEmpty()) {
                File file = new File(picturePath);
                RequestBody requestFile =
                        RequestBody.create(MediaType.parse("multipart/form-data"), file);
                body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);
            }

            MultipartBody.Part body1 = null;
            if (coverPhotoPath != null && !coverPhotoPath.isEmpty()) {
                File file = new File(coverPhotoPath);
                RequestBody requestFile =
                        RequestBody.create(MediaType.parse("multipart/form-data"), file);
                body1 = MultipartBody.Part.createFormData("coverImage", file.getName(), requestFile);
            }

            RequestBody bodyUserid = RequestBody.create(MediaType.parse("text/plain"), sessionManager.getUser().getId());
            RequestBody bodyName = RequestBody.create(MediaType.parse("text/plain"), name);
            RequestBody bodyGender = RequestBody.create(MediaType.parse("text/plain"), gender);
            // RequestBody bodyEmail = RequestBody.create(MediaType.parse("text/plain"), userDummy.getEmail());
            RequestBody bodyUserName = RequestBody.create(MediaType.parse("text/plain"), userName1);
            RequestBody bodyBio = RequestBody.create(MediaType.parse("text/plain"), bio);

            RequestBody bodyAge = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(age));

            map.put("name", bodyName);
            map.put("username", bodyUserName);
            map.put("bio", bodyBio);
            map.put("userId", bodyUserid);
            map.put("gender", bodyGender);
            map.put("age", bodyAge);

            binding.loder.setVisibility(View.VISIBLE);
            Call<UserRoot> call = RetrofitBuilder.create().updateUser(map, body, body1);
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<UserRoot> call, Response<UserRoot> response) {
                    if (response.code() == 200) {
                        if (response.body().isStatus()) {
                            sessionManager.saveUser(response.body().getUser());
                            sessionManager.saveBooleanValue(Const.ISLOGIN, true);
                            startActivity(new Intent(EditProfileActivity.this, MainActivity.class));
                            finishAffinity();
                        } else {
                            Toast.makeText(EditProfileActivity.this, "" + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    binding.loder.setVisibility(View.GONE);
                    binding.tvSubmit.setEnabled(true);
                }

                @Override
                public void onFailure(Call<UserRoot> call, Throwable t) {
                    binding.tvSubmit.setEnabled(true);
                    t.printStackTrace();
                }
            });

        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private String getProfileUrl(String imageUrl, String gender) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            return imageUrl;
        }
        if (gender.equalsIgnoreCase(Const.FEMALE)) {
            imageUrl = BuildConfig.BASE_URL + "storage/female.png";
        } else if (gender.equalsIgnoreCase(Const.MALE)) {
            imageUrl = BuildConfig.BASE_URL + "storage/male.png";

        } else return "";
        return imageUrl;
    }


    private void checkValidation(String toString) {

        binding.pd1.setVisibility(View.VISIBLE);

        Call<RestResponse> call = RetrofitBuilder.create().checkUserName(toString, sessionManager.getUser().getId());
        call.enqueue(new Callback<RestResponse>() {
            @Override
            public void onResponse(Call<RestResponse> call, Response<RestResponse> response) {
                if (response.code() == 200) {
                    if (!response.body().isStatus()) {
                        binding.etUserName.setError("Username already taken");
                        isValidUserName = false;
                    } else {
                        isValidUserName = true;
                    }
                    Log.d(TAG, "checkDetails: " + isValidUserName + "  " + gender);
                }
                binding.pd1.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<RestResponse> call, Throwable t) {

            }
        });
    }


    private void choosePhoto(int galleryCode) {
        requestPermission();
        Intent i = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, galleryCode);

    }

    private void requestPermission() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(EditProfileActivity.this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(EditProfileActivity.this, new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        1);
            }
        } else {
            if (ContextCompat.checkSelfPermission(EditProfileActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(EditProfileActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, getResources().getString(R.string.per_deny), Toast.LENGTH_SHORT).show();
            }
            // Do something for lollipop and above versions
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, getResources().getString(R.string.per_deny), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK && null != data) {

            Uri selectedImage = data.getData();
            Log.d(TAG, "onActivityResult: " + selectedImage);

            Glide.with(this)
                    .load(selectedImage)
                    .circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.imgUser);
            String[] filePathColumn = {DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            picturePath = cursor.getString(columnIndex);
            cursor.close();

        } else if (requestCode == GALLERY_CODE_1 && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            Log.d(TAG, "onActivityResult: " + selectedImage);

            Glide.with(this)
                    .load(selectedImage)
                    .centerCrop()
                    .into(binding.ivCoverPhoto);
            String[] filePathColumn = {DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            coverPhotoPath = cursor.getString(columnIndex);
            cursor.close();
        }
    }

    private void onFeMaleClick() {
        gender = Const.FEMALE;
        binding.tvFemale.setTextColor(ContextCompat.getColor(this, R.color.pink));
        binding.tvMale.setTextColor(ContextCompat.getColor(this, R.color.white));
        binding.radioMale.setChecked(false);
        binding.radioFemale.setChecked(true);
    }

    private void onMaleClick() {
        gender = Const.MALE;
        binding.tvMale.setTextColor(ContextCompat.getColor(this, R.color.pink));
        binding.tvFemale.setTextColor(ContextCompat.getColor(this, R.color.white));
        binding.radioMale.setChecked(true);
        binding.radioFemale.setChecked(false);
    }
}