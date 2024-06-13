package com.example.rayzi.posts;


import static android.provider.MediaStore.MediaColumns.DATA;
import static com.example.rayzi.posts.LocationChooseActivity.REQ_CODE_LOCATION;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.rayzi.R;
import com.example.rayzi.RayziUtils;
import com.example.rayzi.activity.BaseActivity;
import com.example.rayzi.databinding.ActivityUploadPostBinding;
import com.example.rayzi.databinding.BottomSheetPrivacyBinding;
import com.example.rayzi.modelclass.RestResponse;
import com.example.rayzi.modelclass.SearchLocationRoot;
import com.example.rayzi.retrofit.Const;
import com.example.rayzi.retrofit.RetrofitBuilder;
import com.example.rayzi.utils.SocialSpanUtil;
import com.example.rayzi.utils.autoComplete.AutocompleteUtil;
import com.example.rayzi.utils.socialView.SocialEditText;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.jakewharton.rxbinding4.widget.RxTextView;
import com.yalantis.ucrop.UCrop;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.disposables.Disposable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadPostActivity extends BaseActivity {
    public static final int REQ_CODE_HASHTAG = 122;
    public static final int RESULT_LOAD_IMAGE = 201;
    private static final int GALLERY_CODE = 101;
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final String TAG = "uploadpost";
    private static final String SAMPLE_CROPPED_IMAGE_NAME = "cropimage";
    private final List<Disposable> mDisposables = new ArrayList<>();
    ActivityUploadPostBinding binding;
    String destinationUri = SAMPLE_CROPPED_IMAGE_NAME + ".png";
    private RayziUtils.Privacy privacy = RayziUtils.Privacy.PUBLIC;
    private Uri selectedImage;
    private String picturePath;
    private int hashTagIsComing = 0;
    private SearchLocationRoot.DataItem selectedLocation;
    private boolean allowComments = true;
    private UploadActivityViewModel mModel;
    private boolean isCaptured = false;
    private String capturedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.parseColor("#150B1F"));

        binding = DataBindingUtil.setContentView(this, R.layout.activity_upload_post);
        mModel = new ViewModelProvider(this).get(UploadActivityViewModel.class);
        Intent intent = getIntent();
        capturedImage = intent.getStringExtra(Const.CAPTURED_POST_IMAGE);
        isCaptured = intent.getBooleanExtra(Const.IS_CAPTURED, false);
        if (capturedImage != null && !capturedImage.isEmpty()) {
            Glide.with(this).load(capturedImage).into(binding.imageview);
        } else {
            picturePath = intent.getStringExtra(Const.GALLERY_PHOTO_PATH);
            Glide.with(this).load(picturePath).into(binding.imageview);
        }
        setPrivacy(privacy);
        initListner();
        if (!sessionManager.getStringValue(Const.CURRENT_CITY).isEmpty()) {
            binding.tvLocation.setText(sessionManager.getStringValue(Const.CURRENT_CITY));
        } else {
            binding.tvLocation.setText(sessionManager.getStringValue(Const.COUNTRY));
        }

        SocialEditText description = findViewById(R.id.decriptionView);
        description.setText(mModel.description);
        @NonNull Disposable disposable = RxTextView.afterTextChangeEvents(binding.decriptionView)
                .skipInitialValue()
                .subscribe(e -> {
                    Editable editable = e.getEditable();
                    mModel.description = editable != null ? editable.toString() : null;
                });
        mDisposables.add(disposable);


        SocialSpanUtil.apply(binding.decriptionView, mModel.description, null);
        AutocompleteUtil.setupForHashtags(this, binding.decriptionView);
        AutocompleteUtil.setupForUsers(this, binding.decriptionView);
    }


    private void initListner() {
        binding.lytPrivacy.setOnClickListener(v -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.customStyle);
            BottomSheetPrivacyBinding sheetPrivacyBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.bottom_sheet_privacy, null, false);
            bottomSheetDialog.setContentView(sheetPrivacyBinding.getRoot());
            bottomSheetDialog.show();
            sheetPrivacyBinding.tvPublic.setOnClickListener(v1 -> {
                setPrivacy(RayziUtils.Privacy.PUBLIC);
                bottomSheetDialog.dismiss();
            });
            sheetPrivacyBinding.tvOnlyFollowr.setOnClickListener(v1 -> {
                setPrivacy(RayziUtils.Privacy.FOLLOWRS);
                bottomSheetDialog.dismiss();
            });


        });

        binding.imgback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        binding.switchComments.setOnCheckedChangeListener((buttonView, isChecked) -> allowComments = isChecked);
        binding.btnAdd.setOnClickListener(v -> choosePhoto());
        binding.btnDelete.setOnClickListener(v -> {
            binding.imageview.setImageDrawable(null);
            selectedImage = null;
            picturePath = "";
            binding.btnDelete.setVisibility(View.GONE);
            binding.btnAdd.setVisibility(View.VISIBLE);
        });

      /*  binding.lytHashtag.setOnClickListener(v -> {
            startActivityForResult(new Intent(this, HashtagsActivity.class).putExtra(Const.DATA, binding.tvHashtag.getText().toString()), REQ_CODE_HASHTAG);
        });*/
        binding.lytLocation.setOnClickListener(v -> startActivityForResult(new Intent(this, LocationChooseActivity.class).putExtra(Const.DATA, binding.tvLocation.getText().toString()), REQ_CODE_LOCATION));

    }

    private void choosePhoto() {
        /*if (checkPermission()) {
            Intent i = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, GALLERY_CODE);
        } else {
            requestPermission();
        }*/

        if (checkPermission()) {
            openGallery(this);
        } else {
            requestPermission();
        }


    }

    public void openGallery(Context context) {
        try {
            startActivityForResult(new Intent("android.intent.action.PICK", MediaStore.Images.Media.EXTERNAL_CONTENT_URI), RESULT_LOAD_IMAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void requestPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES)) {
                Toast.makeText(this, "Write External Storage permission allows us to save files. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_REQUEST_CODE);
            }
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "Write External Storage permission allows us to save files. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    private boolean checkPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            return checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e("value", "Permission Granted, Now you can use local drive .");
                choosePhoto();
            } else {
                Log.e("value", "Permission Denied, You cannot use local drive .");
            }
        }
    }

    public void onClickPost(View view) {
        Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show();

        String s = binding.decriptionView.getText().toString();

        Log.d(TAG, "onClickPost: des " + s);
        Log.d(TAG, "onClickPost: hesh " + binding.decriptionView.getHashtags());
        Log.d(TAG, "onClickPost: men " + binding.decriptionView.getMentions());


        MultipartBody.Part body = null;
        if (isCaptured) {
            if (capturedImage != null && !capturedImage.isEmpty()) {
                File file = new File(capturedImage);
                RequestBody requestFile =
                        RequestBody.create(MediaType.parse("multipart/form-data"), file);
                body = MultipartBody.Part.createFormData("post", file.getName(), requestFile);

            }
        } else {
            if (picturePath != null && !picturePath.isEmpty()) {
                File file = new File(picturePath);
                RequestBody requestFile =
                        RequestBody.create(MediaType.parse("multipart/form-data"), file);
                body = MultipartBody.Part.createFormData("post", file.getName(), requestFile);

            } else {
                Toast.makeText(this, "Select Image First", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        HashMap<String, RequestBody> hashMap = new HashMap<>();
        hashMap.put("userId", toRequestBody(sessionManager.getUser().getId()));
        if (selectedLocation != null) {
            hashMap.put("location", toRequestBody(binding.tvLocation.getText().toString()));
        }


        String heshtag = "", mentionPeoples = "";
        for (int i = 0; i < binding.decriptionView.getHashtags().size(); i++) {
            Log.d(TAG, "onClickPost: hash  " + binding.decriptionView.getHashtags().get(i));
            heshtag = heshtag + binding.decriptionView.getHashtags().get(i) + ",";
        }
        for (int i = 0; i < binding.decriptionView.getMentions().size(); i++) {
            Log.d(TAG, "onClickPost: mens  " + binding.decriptionView.getMentions().get(i));
            mentionPeoples = mentionPeoples + binding.decriptionView.getMentions().get(i) + ",";
            Log.d(TAG, "onClickPost: mens2  " + mentionPeoples);
        }


        hashMap.put("caption", toRequestBody(s));
        hashMap.put("hashtag", toRequestBody(heshtag));
        hashMap.put("mentionPeople", toRequestBody(mentionPeoples));
        hashMap.put("showPost", toRequestBody(String.valueOf(getPrivacy())));
        hashMap.put("allowComment", toRequestBody(String.valueOf(allowComments)));


        binding.setIsLoading(true);
        Call<RestResponse> call = RetrofitBuilder.create().uploadPost(hashMap, body);
        call.enqueue(new Callback<RestResponse>() {
            @Override
            public void onResponse(Call<RestResponse> call, Response<RestResponse> response) {
                if (response.code() == 200 && response.body().isStatus()) {
                    Toast.makeText(UploadPostActivity.this,
                            "Upload Successfully", Toast.LENGTH_SHORT).show();
                }
                Log.d(TAG, "onResponse: success");
                binding.setIsLoading(false);
                onBackPressed();
            }

            @Override
            public void onFailure(Call<RestResponse> call, Throwable t) {
                Log.d(TAG, "onFailure: err  " + t.getMessage());
                binding.setIsLoading(false);
            }
        });


    }

    private int getPrivacy() {
        if (privacy == RayziUtils.Privacy.FOLLOWRS) {
            return 1;
        } else {
            return 0;
        }
    }

    private void setPrivacy(RayziUtils.Privacy privacy) {
        this.privacy = privacy;
        if (privacy == RayziUtils.Privacy.FOLLOWRS) {
            binding.tvPrivacy.setText("My Followers");
        } else {
            binding.tvPrivacy.setText("Public");
        }
    }

    public RequestBody toRequestBody(String value) {
        return RequestBody.create(MediaType.parse("text/plain"), value);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {


            selectedImage = data.getData();


            startCropActivity(data.getData());

            Glide.with(this)
                    .load(selectedImage)
                    .placeholder(R.drawable.ic_user_place).error(R.drawable.ic_user_place)
                    .into(binding.imageview);
            binding.imageview.setAdjustViewBounds(true);
            String[] filePathColumn = {DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            picturePath = cursor.getString(columnIndex);
            cursor.close();
            binding.btnDelete.setVisibility(View.VISIBLE);

        } else if (requestCode == 69 && resultCode == -1) {
            handleCropResult(data);
        }
        if (resultCode == 96) {
            handleCropError(data);
        }


        if (requestCode == REQ_CODE_HASHTAG && resultCode == RESULT_OK && data != null) {
            String hashtag = data.getStringExtra(Const.DATA);
            //  binding.tvHashtag.setText(hashtag);
        }
        if (requestCode == REQ_CODE_LOCATION && resultCode == RESULT_OK && data != null) {
            String locationData = data.getStringExtra(Const.DATA);
            SearchLocationRoot.DataItem location = new Gson().fromJson(locationData, SearchLocationRoot.DataItem.class);
            if (location != null) {
                selectedLocation = location;
                binding.tvLocation.setText(location.getLabel());
            }
        }

    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void handleCropResult(@androidx.annotation.NonNull Intent result) {
        Uri resultUri = UCrop.getOutput(result);
        if (resultUri != null) {

            selectedImage = resultUri;

            Glide.with(this)
                    .load(selectedImage)
                    .placeholder(R.drawable.ic_user_place).error(R.drawable.ic_user_place)
                    .into(binding.imageview);
            binding.imageview.setAdjustViewBounds(true);
            picturePath = getRealPathFromURI(selectedImage);
            binding.btnDelete.setVisibility(View.VISIBLE);

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


    public static class UploadActivityViewModel extends ViewModel {

        public String description = null;
    }
}