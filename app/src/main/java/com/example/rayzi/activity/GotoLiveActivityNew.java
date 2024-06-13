package com.example.rayzi.activity;

import static com.comman.gallerypicker.PickerMainActivity.IMAGE_URI_LIST;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.comman.gallerypicker.PickerMainActivity;
import com.comman.gallerypicker.models.MediaItemObj;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.example.rayzi.R;
import com.example.rayzi.SharedConstants;
import com.example.rayzi.adapter.LivePagerApapter;
import com.example.rayzi.databinding.ActivityGoLiveNewBinding;
import com.example.rayzi.dilog.CustomDialogClass;
import com.example.rayzi.liveStreamming.HostLiveActivity;
import com.example.rayzi.modelclass.LiveStreamRoot;
import com.example.rayzi.modelclass.SongRoot;
import com.example.rayzi.modelclass.StickerRoot;
import com.example.rayzi.popups.PopupBuilder;
import com.example.rayzi.posts.UploadPostActivity;
import com.example.rayzi.reels.record.FilterActivity;
import com.example.rayzi.reels.record.FilterAdapter;
import com.example.rayzi.reels.record.RecorderActivity;
import com.example.rayzi.reels.record.TrimmerActivity;
import com.example.rayzi.reels.record.VideoFilter;
import com.example.rayzi.reels.record.VolumeActivity;
import com.example.rayzi.reels.record.filters.ExposureFilter;
import com.example.rayzi.reels.record.filters.HazeFilter;
import com.example.rayzi.reels.record.filters.MonochromeFilter;
import com.example.rayzi.reels.record.filters.PixelatedFilter;
import com.example.rayzi.reels.record.filters.SolarizeFilter;
import com.example.rayzi.reels.record.songPicker.SongPickerActivity;
import com.example.rayzi.reels.record.sticker.StickerPickerActivity;
import com.example.rayzi.reels.record.workers.FileDownloadWorker;
import com.example.rayzi.reels.record.workers.MergeVideosWorker2;
import com.example.rayzi.reels.record.workers.VideoSpeedWorker2;
import com.example.rayzi.retrofit.Const;
import com.example.rayzi.retrofit.RetrofitBuilder;
import com.example.rayzi.utils.AnimationUtil;
import com.example.rayzi.utils.BitmapUtil;
import com.example.rayzi.utils.Filters.FilterRoot;
import com.example.rayzi.utils.Filters.FilterUtils;
import com.example.rayzi.utils.IntentUtil;
import com.example.rayzi.utils.NoAnimationViewPagerTransformer;
import com.example.rayzi.utils.TempUtil;
import com.example.rayzi.utils.TextFormatUtil;
import com.example.rayzi.utils.VideoUtil;
import com.example.rayzi.viewModel.PKLiveViewModel;
import com.example.rayzi.viewModel.ViewModelFactory;
import com.example.segmentedprogressbar.SegmentedProgressBar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.slider.Slider;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.gowtham.library.utils.LogMessage;
import com.gowtham.library.utils.TrimVideo;
import com.munon.turboimageview.TurboImageView;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.controls.Facing;
import com.otaliastudios.cameraview.controls.Flash;
import com.otaliastudios.cameraview.controls.Mode;
import com.otaliastudios.cameraview.filter.Filters;
import com.otaliastudios.cameraview.filters.BrightnessFilter;
import com.otaliastudios.cameraview.filters.GammaFilter;
import com.otaliastudios.cameraview.filters.SharpnessFilter;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import info.hoang8f.android.segmented.SegmentedGroup;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GotoLiveActivityNew extends BaseActivity {
    private static final String TAG = "gotoLive";
    ActivityGoLiveNewBinding binding;
    private YoYo.YoYoString mPulse;
    public static final String EXTRA_AUDIO = "audio";
    public static final String EXTRA_SONG = "song";
    public static final int RESULT_LOAD_IMAGE = 201;
    private CameraView mCamera;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private MediaPlayer mMediaPlayer;
    private RecorderActivity.RecorderActivityViewModel mModel;
    private final Runnable mStopper = this::stopRecording;
    int timeInSeconds = 0;
    boolean isPrivate = false;
    private PKLiveViewModel viewModel;
    private CustomDialogClass customDialogClass;
    LivePagerApapter livePagerApapter;
    String[] categories = new String[]{"Live", "Reels", "Post"};
    private Uri selectedImage;
    String picturePath;
    private MediaPlayer player2;
    private String resultFilter = "";
    FilterRoot filter = null;
    private int selectedTabPosition = 0;
    boolean isCameraFacingBack = false;
    String picturePatch;
    ActivityResultLauncher<Intent> profileActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        ArrayList<MediaItemObj> list = result.getData().getExtras().getParcelableArrayList(IMAGE_URI_LIST);
                        final Uri imageUri = list.get(0).getUri();
                        picturePatch = getRealPathFromURI(imageUri);
                        startCropActivity(imageUri);
                    }
                }
            });

    private void viewModelListener() {
        viewModel.isShowFilterSheet.observe(this, aBoolean -> {
            Log.d(TAG, "initListner:i sShowFilterSheet aBoolean ========    " + aBoolean);
            if (aBoolean) {
                binding.rlGolive.setVisibility(View.GONE);
            } else {
                binding.rlGolive.setVisibility(View.VISIBLE);
            }
        });
    }
    ActivityResultLauncher<Intent> startForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {

        Log.d(TAG, "sdd: ");
        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
            Uri uri = Uri.parse(TrimVideo.getTrimmedVideoPath(result.getData()));

            Intent intent = new Intent(this, FilterActivity.class);
            intent.putExtra(FilterActivity.EXTRA_VIDEO, TrimVideo.getTrimmedVideoPath(result.getData()));
            startActivity(intent);
            finish();

        } else LogMessage.v("videoTrimResultLauncher data is null");
    });

    @AfterPermissionGranted(SharedConstants.REQUEST_CODE_PERMISSIONS_UPLOAD)
    private void chooseVideoForUpload() {
        IntentUtil.startChooser(this, SharedConstants.REQUEST_CODE_PICK_VIDEO, "video/mp4");
    }

    private void checkPermission(int caseScenario) {    // 0 == capturingPicture, 1==Gallery Picture, 2== Gallery video
        List<String> stringList = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            stringList = Arrays.asList(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO);
        } else {
            stringList = Arrays.asList(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        requestPermissionIfNeeded(stringList, (allGranted, grantedList, deniedList) -> {
            if (allGranted) {
                if (caseScenario == 0) takePicture();
                else if (caseScenario == 1) {
                    profileActivityResultLauncher.launch(new Intent(GotoLiveActivityNew.this, PickerMainActivity.class).putExtra("isSingleSelection", true));
                } else chooseVideoForUpload();
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_go_live_new);

        viewModel = ViewModelProviders.of(this, new ViewModelFactory(new PKLiveViewModel()).createFor()).get(PKLiveViewModel.class);

        initView();

    }

    private void initView() {
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);
        viewModel.initLister();
        customDialogClass = new CustomDialogClass(this, R.style.customStyle);
        customDialogClass.setCancelable(false);
        customDialogClass.setCanceledOnTouchOutside(false);

        initCamera();
        initListener();
        initReels();
        viewModelListener();

    }

    public void takePicture() {
        customDialogClass.show();
        mCamera.setMode(Mode.PICTURE);
        makeCameraSound();
        mCamera.takePicture();
        mCamera.addCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(@NonNull PictureResult result) {
                super.onPictureTaken(result);
                byte[] data = result.getData();
                File imageFile = saveImageToFile(data);
                Log.d(TAG, "run: file.getAbsolutePath() === " + imageFile.getAbsolutePath());
                startActivity(new Intent(GotoLiveActivityNew.this, UploadPostActivity.class).putExtra(Const.CAPTURED_POST_IMAGE, imageFile.getAbsolutePath()).putExtra(Const.IS_CAPTURED, true));
                finish();
                mCamera.setMode(Mode.VIDEO);
            }

        });

    }

    private File saveImageToFile(byte[] data) {
        // Create an image file
        File imageFile = createImageFile();

        // Write the byte array data to the file
        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            fos.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return imageFile;
    }

    private File createImageFile() {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        // Get the directory where you want to save the image (e.g., getExternalFilesDir)
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // Create the File object
        File imageFile = null;
        try {
            imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return imageFile;
    }

    private void initListener() {

        Log.d(TAG, "initListener: >>>>>>>>>>>>>>>>>>  ");
        livePagerApapter = new LivePagerApapter(getSupportFragmentManager(), categories);
        binding.liveViewpager.setAdapter(livePagerApapter);
        binding.liveViewpager.setCurrentItem(0, false);
        if (binding.liveViewpager.getParent() != null) {
            ((ViewGroup) binding.liveViewpager.getParent()).removeView(binding.liveViewpager);
            binding.framelayout.addView(binding.liveViewpager, 0);
        }

        binding.tabTablayout.setupWithViewPager(binding.liveViewpager);
        binding.liveViewpager.setPageTransformer(false, new NoAnimationViewPagerTransformer());

        binding.tabTablayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                binding.liveViewpager.setCurrentItem(tab.getPosition(), false);
                //ll
                View v = tab.getCustomView();
                if (v != null) {
//
                    TextView tv = (TextView) v.findViewById(R.id.tvTab);
                    tv.setTextColor(ContextCompat.getColor(GotoLiveActivityNew.this, R.color.white));
                    tv.setTextSize(20);
                    View indicator = (View) v.findViewById(R.id.indicator);
                    indicator.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                //ll
                View v = tab.getCustomView();
                if (v != null) {
                    TextView tv = (TextView) v.findViewById(R.id.tvTab);
                    tv.setTextColor(ContextCompat.getColor(GotoLiveActivityNew.this, R.color.text_gray));
                    tv.setTextSize(18);
                    View indicator = (View) v.findViewById(R.id.indicator);
                    indicator.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
//ll
            }
        });

        setTab(categories);

        binding.btnPk.setOnClickListener(v -> {
            if (binding.liveViewpager.getCurrentItem() == 0) {
                goToLiveStreamActivity();
            } else if (binding.liveViewpager.getCurrentItem() == 1) {
                if (!sessionManager.getUser().getLevel().getAccessibleFunction().isUploadVideo()) {
                    new PopupBuilder(this).showSimplePopup("You are not able to create relite at your level", "Dismiss", () -> {
                    });
                    return;
                }
                startActivity(new Intent(this, RecorderActivity.class));
                finish();
            } else {
                if (!sessionManager.getUser().getLevel().getAccessibleFunction().isUploadPost()) {
                    new PopupBuilder(this).showSimplePopup("You are not able to upload post at your level", "Dismiss", () -> {
                    });
                    return;
                }
                checkPermission(0);
            }
            v.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
        });

        binding.btnSwitchCamaraFunction.setOnClickListener(v -> {
            switchCamera();
        });

        binding.liveViewpager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onPageSelected(int position) {
                vibrateDevice();
                if (position == 0) {
                    binding.lytLiveFilterFunctions.setVisibility(View.VISIBLE);
                    binding.btnPk.setText("Go Live");
                    binding.btnPk.setVisibility(View.VISIBLE);
                    binding.galleryBtn.setVisibility(View.GONE);
                    binding.btnSwitchCamara.setVisibility(View.GONE);
                    binding.lytReelFilterFunction.setVisibility(View.GONE);
                    binding.constraintLayout.setVisibility(View.GONE);
                    binding.topLayout.setVisibility(View.GONE);
                    binding.segments.setVisibility(View.GONE);
                } else if (position == 1) {
                    binding.lytReelFilterFunction.setVisibility(View.VISIBLE);
                    binding.lytLiveFilterFunctions.setVisibility(View.GONE);
                    binding.btnPk.setVisibility(View.GONE);
                    binding.galleryBtn.setVisibility(View.GONE);
                    binding.btnSwitchCamara.setVisibility(View.GONE);
                    binding.constraintLayout.setVisibility(View.VISIBLE);
                    binding.topLayout.setVisibility(View.VISIBLE);
                    binding.segments.setVisibility(View.VISIBLE);
                } else {
                    binding.lytLiveFilterFunctions.setVisibility(View.GONE);
                    binding.lytReelFilterFunction.setVisibility(View.GONE);
                    binding.btnPk.setVisibility(View.VISIBLE);
                    binding.btnPk.setText("Take Picture");
                    binding.galleryBtn.setVisibility(View.VISIBLE);
                    binding.btnSwitchCamara.setVisibility(View.VISIBLE);
                    binding.constraintLayout.setVisibility(View.GONE);
                    binding.topLayout.setVisibility(View.GONE);
                    binding.segments.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        binding.galleryBtn.setOnClickListener(v -> {
            checkPermission(1);
        });

        binding.btnGifIcon.setOnClickListener(v -> {
            Log.d(TAG, "initListener: ");
            viewModel.isShowFilterSheet.setValue(true);
            binding.rvFilters.setAdapter(viewModel.filterAdapter2);
        });

        binding.btnFilter.setOnClickListener(v -> {
            viewModel.isShowFilterSheet.setValue(true);
            binding.rvFilters.setAdapter(viewModel.filterAdapter_tt);
        });

        binding.btnSwitchCamara.setOnClickListener(v -> {
            switchCamera();
        });

        binding.btnMute.setOnClickListener(v -> {
            viewModel.isMuted = !viewModel.isMuted;
            if (viewModel.isMuted) {
                binding.btnMute.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.mute));
            } else {
                binding.btnMute.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.unmute));
            }
        });

        binding.btnClose.setOnClickListener(view -> {
            onBackPressed();
        });

        viewModelObserver();
    }

    private void goToLiveStreamActivity() {


        if (!sessionManager.getUser().getLevel().getAccessibleFunction().isLiveStreaming()) {
            new PopupBuilder(this).showSimplePopup("You are not eligible live at this level", "Dismiss", () -> {
            });
            return;
        }


        customDialogClass.show();
        filter = viewModel.selectedFilter.getValue();
        if (filter != null && !filter.getTitle().isEmpty()) {
            resultFilter = filter.getTitle();
        }
        try {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("userId", sessionManager.getUser().getId());
            jsonObject.addProperty("isPublic", !isPrivate);
            jsonObject.addProperty("channel", sessionManager.getUser().getId());
            jsonObject.addProperty("filter", resultFilter);
            jsonObject.addProperty("agoraUID", 0);  // just for unique host int id

            Call<LiveStreamRoot> call = RetrofitBuilder.create().makeliveUser(jsonObject);
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<LiveStreamRoot> call, Response<LiveStreamRoot> response) {
                    customDialogClass.dismiss();
                    if (response.code() == 200) {
                        if (response.body().isStatus()) {
                            Intent intent = new Intent(GotoLiveActivityNew.this, HostLiveActivity.class);
                            intent.putExtra(Const.DATA, new Gson().toJson(response.body().getLiveUser()));
                            intent.putExtra(Const.PRIVACY, isPrivate ? "Private" : "Public");
                            startActivity(intent);
                            finish();
                        }
                    }
                }

                @Override
                public void onFailure(Call<LiveStreamRoot> call, Throwable t) {
                    Log.d(TAG, "onFailure: >>>>>>>>>>>>>  " + t.getMessage());
                    customDialogClass.dismiss();
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private View createCustomView(int i, String s) {

        View v = LayoutInflater.from(GotoLiveActivityNew.this).inflate(R.layout.custom_tabhorizontal, null);

        TextView tv = (TextView) v.findViewById(R.id.tvTab);

        tv.setText(s);
        View indicator = (View) v.findViewById(R.id.indicator);

        if (i == 0) {
            tv.setTextColor(ContextCompat.getColor(GotoLiveActivityNew.this, R.color.white));
            indicator.setVisibility(View.VISIBLE);

        } else {
            tv.setTextColor(ContextCompat.getColor(GotoLiveActivityNew.this, R.color.text_gray));
            indicator.setVisibility(View.GONE);
        }
        return v;

    }

    @SuppressLint("RestrictedApi")
    private void initCamera() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, 1);
        } else {
            mCamera = findViewById(R.id.camera);
            mCamera.setLifecycleOwner(this);
            mCamera.setMode(Mode.VIDEO);
        }
    }

    private void setTab(String[] country) {
        binding.tabTablayout.setTabGravity(TabLayout.GRAVITY_FILL);
        binding.tabTablayout.removeAllTabs();

        for (int i = 0; i < country.length; i++) {
            binding.tabTablayout.addTab(binding.tabTablayout.newTab().setCustomView(createCustomView(i, country[i])));
        }
    }

    private void submitUploadReels(Uri data) {
        File copy = TempUtil.createCopy(this, data, ".mp4");

        TrimVideo.activity(String.valueOf(data)).start(this, startForResult);
    }

    private void submitUpload(Uri uri) {
        File copy = TempUtil.createCopy(this, uri, ".mp4");
        Intent intent = new Intent(this, TrimmerActivity.class);
        intent.putExtra(TrimmerActivity.EXTRA_VIDEO, copy.getAbsolutePath());
        startActivity(intent);
        finish();

    }

    private void handleCropResult(@NonNull Intent result) {
        Uri resultUri = UCrop.getOutput(result);
        if (resultUri != null) {

            selectedImage = resultUri;
            Log.d(TAG, "handleCropResult: selectedImage ===== " + selectedImage);
            picturePath = getRealPathFromURI(selectedImage);
            startActivity(new Intent(GotoLiveActivityNew.this, UploadPostActivity.class).putExtra(Const.GALLERY_PHOTO_PATH, picturePath).putExtra(Const.IS_CAPTURED, false));
            finish();

        } else {
            Toast.makeText(this, "toast_cannot_retrieve_cropped_image", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleCropError(@NonNull Intent result) {
        Throwable cropError = UCrop.getError(result);
        if (cropError != null) {
            Log.e("TAG", "handleCropError: ", cropError);
            Toast.makeText(this, cropError.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(this, "toast_unexpected_error", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        new Handler(Looper.getMainLooper()).postDelayed(this::initCamera, 500);
        Log.d(TAG, "onResume: selectedTabPosition -==-===== " + selectedTabPosition);
        binding.tabTablayout.setScrollPosition(selectedTabPosition, 0f, true);
        binding.liveViewpager.setCurrentItem(selectedTabPosition);
    }

    public void makeCameraSound() {
        if (player2 != null) {
            player2.release();
            player2 = null;
        }
        try {
            player2 = new MediaPlayer();
            try {
                AssetFileDescriptor afd2 = getAssets().openFd("camera_shutter.mp3");
                player2.setDataSource(afd2.getFileDescriptor(), afd2.getStartOffset(), afd2.getLength());
                player2.prepare();
                player2.start();
            } catch (IOException e) {
                Log.d(TAG, "initUI: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "initUI: errrr " + e.getMessage());
        }
    }

    public void vibrateDevice() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Output yes if can vibrate, no otherwise
        if (v.hasVibrator()) {
            Log.d(TAG, "vibrateDevice: Can Vibrate yes");
            if (Build.VERSION.SDK_INT >= 26) {
                ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(50);
            }
        } else {
            Log.d(TAG, "vibrateDevice: Can Vibrate no");
        }
    }


    private void viewModelObserver() {
        viewModel.isShowFilterSheet.observe(this, aBoolean -> {
            //Log.d(TAG, "initLister:filter sheet  " + aBoolean);
            if (aBoolean) {
                binding.lytFilters.setVisibility(View.VISIBLE);
            } else {
                binding.lytFilters.setVisibility(View.GONE);
            }
        });
        viewModel.selectedFilter.observe(this, selectedFilter -> {
            Log.d(TAG, "viewModelObserver: electedFilter.toString() ===== " + selectedFilter.getTitle());
            if (selectedFilter.getTitle().equalsIgnoreCase("None")) {
                //  Log.d(TAG, "initLister: null");
                binding.imgFilter.setImageDrawable(null);
            } else {
                if (!isFinishing()) {
                    Glide.with(this).load(FilterUtils.getDraw(selectedFilter.getTitle()))
                            .diskCacheStrategy(DiskCacheStrategy.ALL).into(binding.imgFilter);
                }
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            selectedImage = data.getData();
            startCropActivity(data.getData());
        } else if (requestCode == 69 && resultCode == -1) {
            handleCropResult(data);
        }

        if (resultCode == 96) {
            handleCropError(data);
        }

        if (requestCode == SharedConstants.REQUEST_CODE_PICK_VIDEO && resultCode == RESULT_OK && data != null) {
            double fileSizeMB = getFileSizeInMB(data.getData());
            Log.d(TAG, "onActivityResult: filesizeMB ============================" + fileSizeMB);
            if (fileSizeMB <= Const.UPLOADINGLIMIT) {
                submitUploadReels(data.getData());
            } else {
                Toast.makeText(this, "You cannot Upload video above 30 MB !!!", Toast.LENGTH_SHORT).show();
                return;
            }
        } else if (requestCode == SharedConstants.REQUEST_CODE_PICK_SONG && resultCode == RESULT_OK && data != null) {
            SongRoot.SongItem songDummy = data.getParcelableExtra(EXTRA_SONG);
            Uri audio = data.getParcelableExtra(EXTRA_AUDIO);
            setupSong(songDummy, audio);
        } else if (requestCode == SharedConstants.REQUEST_CODE_PICK_STICKER && resultCode == RESULT_OK && data != null) {
            StickerRoot.StickerItem stickerDummy = data.getParcelableExtra(StickerPickerActivity.EXTRA_STICKER);
            downloadSticker(stickerDummy);
        }
    }

    private void initReels() {
        mModel = new ViewModelProvider(this).get(RecorderActivity.RecorderActivityViewModel.class);
        SongRoot.SongItem songDummy = getIntent().getParcelableExtra(EXTRA_SONG);
        Uri audio = getIntent().getParcelableExtra(EXTRA_AUDIO);

        if (audio != null) {
            setupSong(songDummy, audio);
        }

        binding.done.setOnClickListener(view -> {
            if (mCamera.isTakingVideo()) {
                Toast.makeText(this, R.string.recorder_error_in_progress, Toast.LENGTH_SHORT).show();
            } else if (mModel.segments.isEmpty()) {
                Toast.makeText(this, R.string.recorder_error_no_clips, Toast.LENGTH_SHORT).show();
            } else {
                commitRecordings(mModel.segments, mModel.audio);
            }
        });

        binding.flip.setOnClickListener(view -> {
            switchCamera();
        });

        binding.flash.setOnClickListener(view -> {
            if (mCamera.isTakingVideo()) {
                Toast.makeText(this, R.string.recorder_error_in_progress, Toast.LENGTH_SHORT).show();
            } else {
                mCamera.setFlash(mCamera.getFlash() == Flash.OFF ? Flash.TORCH : Flash.OFF);
            }
        });

        SegmentedGroup speeds = findViewById(R.id.speeds);
        View speed = findViewById(R.id.speed);
        speed.setOnClickListener(view -> {
            binding.filters.setAdapter(null);
            binding.filters.setVisibility(View.GONE);
            if (mCamera.isTakingVideo()) {
                Toast.makeText(this, R.string.recorder_error_in_progress, Toast.LENGTH_SHORT).show();
            } else {
                speeds.setVisibility(speeds.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            }
        });

        speed.setVisibility(View.VISIBLE);
        RadioButton speed05x = findViewById(R.id.speed05x);
        RadioButton speed075x = findViewById(R.id.speed075x);
        RadioButton speed1x = findViewById(R.id.speed1x);
        RadioButton speed15x = findViewById(R.id.speed15x);
        RadioButton speed2x = findViewById(R.id.speed2x);
        speed05x.setChecked(mModel.speed == .5f);
        speed075x.setChecked(mModel.speed == .75f);
        speed1x.setChecked(mModel.speed == 1);
        speed15x.setChecked(mModel.speed == 1.5f);
        speed2x.setChecked(mModel.speed == 2);

        speeds.setOnCheckedChangeListener((group, checked) -> {
            float factor = 1;
            if (checked != R.id.speed05x) {
                speed05x.setChecked(false);
            } else {
                factor = .5f;
            }

            if (checked != R.id.speed075x) {
                speed075x.setChecked(false);
            } else {
                factor = .75f;
            }

            if (checked != R.id.speed1x) {
                speed1x.setChecked(false);
            }

            if (checked != R.id.speed15x) {
                speed15x.setChecked(false);
            } else {
                factor = 1.5f;
            }

            if (checked != R.id.speed2x) {
                speed2x.setChecked(false);
            } else {
                factor = 2;
            }

            mModel.speed = factor;
        });

        RecyclerView filters = findViewById(R.id.filters);
        findViewById(R.id.filter).setOnClickListener(view -> {
            binding.speeds.setVisibility(View.GONE);

            if (mCamera.isTakingVideo()) {
                Toast.makeText(this, R.string.recorder_error_in_progress, Toast.LENGTH_SHORT).show();
            } else if (filters.getVisibility() == View.VISIBLE) {
                filters.setAdapter(null);
                filters.setVisibility(View.GONE);
            } else {
                customDialogClass.show();
                mCamera.takePictureSnapshot();
            }
        });

        TurboImageView stickers = findViewById(R.id.stickerTurbo);

        mCamera.setOnTouchListener((v, event) -> stickers.dispatchTouchEvent(event));

        View remove = findViewById(R.id.remove);

        remove.setOnClickListener(v -> {
            stickers.removeSelectedObject();
            if (stickers.getObjectCount() <= 0) {
                remove.setVisibility(View.GONE);
            }
        });

        findViewById(R.id.sticker).setOnClickListener(v -> {
            Intent intent = new Intent(this, StickerPickerActivity.class);
            startActivityForResult(intent, SharedConstants.REQUEST_CODE_PICK_STICKER);
        });

        View sticker = findViewById(R.id.sticker);
        sticker.setVisibility(getResources().getBoolean(R.bool.stickers_enabled) ? View.VISIBLE : View.GONE);

        View sheet = findViewById(R.id.timer_sheet);
        BottomSheetBehavior<View> bsb = BottomSheetBehavior.from(sheet);
        ImageView close = sheet.findViewById(R.id.btnClose);
        close.setOnClickListener(view -> bsb.setState(BottomSheetBehavior.STATE_COLLAPSED));

        ImageView start = sheet.findViewById(R.id.btnDone);
        start.setOnClickListener(view -> {
            bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);
            startTimer();
        });

        findViewById(R.id.timer).setOnClickListener(view -> {
            if (mCamera.isTakingVideo()) {
                Toast.makeText(this, R.string.recorder_error_in_progress, Toast.LENGTH_SHORT).show();
            } else {
                bsb.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        TextView maximum = findViewById(R.id.maximum);
        View sound = findViewById(R.id.sound);

        sound.setOnClickListener(view -> {
            if (mModel.segments.isEmpty()) {
                Intent intent = new Intent(this, SongPickerActivity.class);
                startActivityForResult(intent, SharedConstants.REQUEST_CODE_PICK_SONG);
            } else if (mModel.audio == null) {
                Toast.makeText(this, R.string.message_song_select, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.message_song_change, Toast.LENGTH_SHORT).show();
            }
        });

        Slider selection = findViewById(R.id.selection);
        selection.setLabelFormatter(value -> TextFormatUtil.toMMSS((long) value));

        View upload = findViewById(R.id.upload);
        upload.setOnClickListener(view -> {
            String[] permissions = new String[0];
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions = new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_AUDIO,};
            } else {
                permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,};
            }
            if (EasyPermissions.hasPermissions(GotoLiveActivityNew.this, permissions)) {
                chooseVideoForUpload();
            } else {
                Toast.makeText(this, "You need Storage Permission", Toast.LENGTH_SHORT).show();
                EasyPermissions.requestPermissions(this, getString(R.string.permission_rationale_upload), SharedConstants.REQUEST_CODE_PERMISSIONS_UPLOAD, permissions);
            }
        });

        bsb.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {

            @Override
            public void onSlide(@NonNull View v, float offset) {
            }

            @Override
            public void onStateChanged(@NonNull View v, int state) {
                if (state == BottomSheetBehavior.STATE_EXPANDED) {
                    long max;
                    max = SharedConstants.MAX_DURATION - mModel.recorded();
                    max = TimeUnit.MILLISECONDS.toSeconds(max);
                    max = TimeUnit.SECONDS.toMillis(max);
                    selection.setValue(0);
                    selection.setValueTo(max);
                    selection.setValue(max);
                    maximum.setText(TextFormatUtil.toMMSS(max));
                }
            }
        });

        SegmentedProgressBar segments = findViewById(R.id.segments);
        segments.enableAutoProgressView(SharedConstants.MAX_DURATION);
        segments.setDividerColor(Color.BLACK);
        segments.setDividerEnabled(true);
        segments.setDividerWidth(2f);
        segments.setListener(l -> { /* eaten */ });
        segments.setShader(new int[]{ContextCompat.getColor(this, R.color.purple), ContextCompat.getColor(this, R.color.pink),});
        mCamera.addCameraListener(new CameraListener() {

            @Override
            public void onPictureTaken(@NonNull PictureResult result) {
                super.onPictureTaken(result);
                result.toBitmap(bitmap -> {
                    if (bitmap != null) {
                        Bitmap square = BitmapUtil.getSquareThumbnail(bitmap, 250);
                        bitmap.recycle();
                        Bitmap rounded = BitmapUtil.addRoundCorners(square, 10);
                        square.recycle();
                        FilterAdapter adapter = new FilterAdapter(GotoLiveActivityNew.this, rounded);
                        adapter.setListener(GotoLiveActivityNew.this::applyPreviewFilter);
                        RecyclerView filters = findViewById(R.id.filters);
                        filters.setAdapter(adapter);
                        filters.setVisibility(View.VISIBLE);
                    }

                    customDialogClass.dismiss();
                });
            }

            @Override
            public void onVideoRecordingEnd() {
                Log.v(TAG, "Video recording has ended.");
                segments.pause();
                segments.addDivider();
                mHandler.removeCallbacks(mStopper);
                mHandler.postDelayed(() -> processCurrentRecording(), 500);
                if (mMediaPlayer != null) {
                    mMediaPlayer.pause();
                }//

                mPulse.stop();
                binding.record.setSelected(false);
                toggleVisibility(true);
            }

            @Override
            public void onVideoRecordingStart() {
                Log.v(TAG, "Video recording has started.");
                segments.resume();
                if (mMediaPlayer != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        float speed = 1f;
                        if (mModel.speed == .5f) {
                            speed = 2f;
                        } else if (mModel.speed == .75f) {
                            speed = 1.5f;
                        } else if (mModel.speed == 1.5f) {
                            speed = .75f;
                        } else if (mModel.speed == 2f) {
                            speed = .5f;
                        }

                        PlaybackParams params = new PlaybackParams();
                        params.setSpeed(speed);
                        mMediaPlayer.setPlaybackParams(params);
                    }

                    mMediaPlayer.start();
                }

                mPulse = YoYo.with(Techniques.Pulse).repeat(YoYo.INFINITE).playOn(binding.record);
                binding.record.setSelected(true);
                toggleVisibility(false);
            }
        });

        binding.record.setOnClickListener(view -> {
            if (mCamera.isTakingVideo()) {
                stopRecording();
            } else {
                filters.setVisibility(View.GONE);
                speeds.setVisibility(View.GONE);
                stickers.deselectAll();
                startRecording();
            }
        });
    }

    private void setupSong(@Nullable SongRoot.SongItem songDummy, Uri file) {
        mMediaPlayer = MediaPlayer.create(this, file);
        mMediaPlayer.setOnCompletionListener(mp -> mMediaPlayer = null);
        TextView sound = findViewById(R.id.sound);
        if (songDummy != null) {
            sound.setText(songDummy.getTitle());
            mModel.song = songDummy.getId();
        } else {
            sound.setText(getString(R.string.audio_from_clip));
        }

        mModel.audio = file;
    }

    private void switchCamera() {
        if (mCamera.isTakingVideo()) {
            Toast.makeText(this, R.string.recorder_error_in_progress, Toast.LENGTH_SHORT).show();
        } else {
            mCamera.toggleFacing();
            Log.d(TAG, "switchCamera: mCamera.getFacing() ===  " + mCamera.getFacing());
            isCameraFacingBack = mCamera.getFacing() == Facing.BACK;
        }
    }

    private void commitRecordings(@NonNull List<RecorderActivity.RecordSegment> segments, @Nullable Uri audio) {
        timeInSeconds = 0;

        customDialogClass.show();

        List<String> videos = new ArrayList<>();
        for (RecorderActivity.RecordSegment segment : segments) {
            videos.add(segment.file);
        }

        File merged = TempUtil.createNewFile(this, ".mp4");
        Data data = new Data.Builder().putStringArray(MergeVideosWorker2.KEY_INPUTS, videos.toArray(new String[0])).putString(MergeVideosWorker2.KEY_OUTPUT, merged.getAbsolutePath()).build();
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(MergeVideosWorker2.class).setInputData(data).build();
        WorkManager wm = WorkManager.getInstance(this);
        wm.enqueue(request);
        wm.getWorkInfoByIdLiveData(request.getId()).observe(this, info -> {
            boolean ended = info.getState() == WorkInfo.State.CANCELLED || info.getState() == WorkInfo.State.FAILED || info.getState() == WorkInfo.State.SUCCEEDED;
            if (ended) {
                customDialogClass.dismiss();
            }

            if (info.getState() == WorkInfo.State.SUCCEEDED) {
                if (audio != null) {

                    proceedToVolume(merged, new File(Objects.requireNonNull(audio.getPath())));
                } else {
                    proceedToFilter(merged);
                }
            }
        });

    }

    @SuppressLint("SetTextI18n")
    private void startTimer() {
        View countdown = findViewById(R.id.countdown);
        TextView count = findViewById(R.id.count);
        count.setText(null);
        Slider selection = findViewById(R.id.selection);
        long duration = (long) selection.getValue();
        CountDownTimer timer = new CountDownTimer(3000, 1000) {

            @Override
            public void onTick(long remaining) {
                mHandler.post(() -> count.setText(TimeUnit.MILLISECONDS.toSeconds(remaining) + 1 + ""));
            }

            @Override
            public void onFinish() {
                mHandler.post(() -> countdown.setVisibility(View.GONE));
                startRecording();
                mHandler.postDelayed(mStopper, duration);
            }
        };

        countdown.setOnClickListener(v -> {
            timer.cancel();
            countdown.setVisibility(View.GONE);
        });

        countdown.setVisibility(View.VISIBLE);
        timer.start();
    }

    private void stopRecording() {
        Log.d(TAG, "stopRecording: ");
        mCamera.stopVideo();
    }

    private void applyPreviewFilter(VideoFilter filter) {

        switch (filter) {
            case BRIGHTNESS -> {
                BrightnessFilter glf = (BrightnessFilter) Filters.BRIGHTNESS.newInstance();
                glf.setBrightness(1.2f);
                mCamera.setFilter(glf);
            }
            case EXPOSURE -> mCamera.setFilter(new ExposureFilter());
            case GAMMA -> {
                GammaFilter glf = (GammaFilter) Filters.GAMMA.newInstance();
                glf.setGamma(2);
                mCamera.setFilter(glf);
            }
            case GRAYSCALE -> mCamera.setFilter(Filters.GRAYSCALE.newInstance());
            case HAZE -> {
                HazeFilter glf = new HazeFilter();
                glf.setSlope(-0.5f);
                mCamera.setFilter(glf);
            }
            case INVERT -> mCamera.setFilter(Filters.INVERT_COLORS.newInstance());
            case MONOCHROME -> mCamera.setFilter(new MonochromeFilter());
            case PIXELATED -> {
                PixelatedFilter glf = new PixelatedFilter();
                glf.setPixel(5);
                mCamera.setFilter(glf);
            }
            case POSTERIZE -> mCamera.setFilter(Filters.POSTERIZE.newInstance());
            case SEPIA -> mCamera.setFilter(Filters.SEPIA.newInstance());
            case SHARP -> {
                SharpnessFilter glf = (SharpnessFilter) Filters.SHARPNESS.newInstance();
                glf.setSharpness(0.25f);
                mCamera.setFilter(glf);
            }
            case SOLARIZE -> mCamera.setFilter(new SolarizeFilter());
            case VIGNETTE -> mCamera.setFilter(Filters.VIGNETTE.newInstance());
            default -> mCamera.setFilter(Filters.NONE.newInstance());
        }
    }

    private void applyVideoSpeed(File file, float speed, long duration) {
        File output = TempUtil.createNewFile(this, ".mp4");

        customDialogClass.show();

        Data data = new Data.Builder().putString(VideoSpeedWorker2.KEY_INPUT, file.getAbsolutePath()).putString(VideoSpeedWorker2.KEY_OUTPUT, output.getAbsolutePath()).putFloat(VideoSpeedWorker2.KEY_SPEED, speed).build();
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(VideoSpeedWorker2.class).setInputData(data).build();
        WorkManager wm = WorkManager.getInstance(this);
        wm.enqueue(request);
        wm.getWorkInfoByIdLiveData(request.getId()).observe(this, info -> {
            boolean ended = info.getState() == WorkInfo.State.CANCELLED || info.getState() == WorkInfo.State.FAILED || info.getState() == WorkInfo.State.SUCCEEDED;
            if (ended) {
                customDialogClass.dismiss();
            }

            if (info.getState() == WorkInfo.State.SUCCEEDED) {
                RecorderActivity.RecordSegment segment = new RecorderActivity.RecordSegment();
                segment.file = output.getAbsolutePath();
                segment.duration = duration;
                mModel.segments.add(segment);
            }
        });

    }

    private void toggleVisibility(boolean show) {
        if (!getResources().getBoolean(R.bool.clutter_free_recording_enabled)) {
            return;
        }

        View top = findViewById(R.id.top);
        AnimationUtil.toggleVisibilityToTop(top, show);
        View right = findViewById(R.id.right);
        AnimationUtil.toggleVisibilityToLeft(right, show);
        View upload = findViewById(R.id.upload);
        AnimationUtil.toggleVisibilityToBottom(upload, show);
        View done = findViewById(R.id.done);
        AnimationUtil.toggleVisibilityToBottom(done, show);

    }

    private void startRecording() {
        Log.d(TAG, "startRecording: ");
        long recorded = mModel.recorded();
        if (recorded >= SharedConstants.MAX_DURATION) {
            Toast.makeText(GotoLiveActivityNew.this, R.string.recorder_error_maxed_out, Toast.LENGTH_SHORT).show();
        } else {
            mModel.video = TempUtil.createNewFile(this, ".mp4");
            mCamera.takeVideoSnapshot(mModel.video, (int) (SharedConstants.MAX_DURATION - recorded));
        }
    }

    private void proceedToFilter(File video) {
        Log.d(TAG, "Proceeding to filter screen with " + video);
        Intent intent = new Intent(this, FilterActivity.class);
        intent.putExtra(FilterActivity.EXTRA_SONG, mModel.song);
        intent.putExtra(FilterActivity.EXTRA_VIDEO, video.getAbsolutePath());
        startActivity(intent);
        finish();
    }

    private void proceedToVolume(File video, File audio) {
        Log.v(TAG, "Proceeding to volume screen with " + video + "; " + audio);
        Intent intent = new Intent(this, VolumeActivity.class);
        intent.putExtra(VolumeActivity.EXTRA_SONG, mModel.song);
        intent.putExtra(VolumeActivity.EXTRA_VIDEO, video.getAbsolutePath());
        intent.putExtra(VolumeActivity.EXTRA_AUDIO, audio.getAbsolutePath());
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }

            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        for (RecorderActivity.RecordSegment segment : mModel.segments) {
            File file = new File(segment.file);
            if (!file.delete()) {
                Log.v(TAG, "Could not delete record segment file: " + file);
            }
        }

    }

    private void downloadSticker(StickerRoot.StickerItem stickerDummy) {
        File stickers = new File(getFilesDir(), "stickers");
        if (!stickers.exists() && !stickers.mkdirs()) {
            Log.w(TAG, "Could not create directory at " + stickers);
        }

        String extension = stickerDummy.getSticker().substring(stickerDummy.getSticker().lastIndexOf(".") + 1);
        File image = new File(stickers, stickerDummy.getId() + extension);
        if (image.exists()) {
            addSticker(image);
            return;
        }

        CustomDialogClass progress = new CustomDialogClass(this, R.style.customStyle);
        progress.setCancelable(false);
        progress.show();
        Data input = new Data.Builder().putString(FileDownloadWorker.KEY_INPUT, stickerDummy.getSticker()).putString(FileDownloadWorker.KEY_OUTPUT, image.getAbsolutePath()).build();
        WorkRequest request = new OneTimeWorkRequest.Builder(FileDownloadWorker.class).setInputData(input).build();
        WorkManager wm = WorkManager.getInstance(this);
        wm.enqueue(request);
        wm.getWorkInfoByIdLiveData(request.getId()).observe(this, info -> {
            boolean ended = info.getState() == WorkInfo.State.CANCELLED || info.getState() == WorkInfo.State.FAILED || info.getState() == WorkInfo.State.SUCCEEDED;
            if (ended) {
                progress.dismiss();
            }

            if (info.getState() == WorkInfo.State.SUCCEEDED) {
                addSticker(image);
            }
        });
    }

    private void addSticker(File file) {
        TurboImageView stickers = findViewById(R.id.stickerTurbo);
        stickers.addObject(this, BitmapFactory.decodeFile(file.getAbsolutePath()));

        View remove = findViewById(R.id.remove);
        remove.setVisibility(View.VISIBLE);
    }

    private void processCurrentRecording() {
        if (mModel.video != null) {
            long duration = VideoUtil.getDuration(this, Uri.fromFile(mModel.video));
            if (mModel.speed != 1) {
                applyVideoSpeed(mModel.video, mModel.speed, duration);
            } else {
                RecorderActivity.RecordSegment segment = new RecorderActivity.RecordSegment();
                segment.file = mModel.video.getAbsolutePath();
                segment.duration = duration;
                mModel.segments.add(segment);
            }
        }
        mModel.video = null;
    }

    public double getFileSizeInMB(Uri fileUri) {
        Cursor returnCursor = getContentResolver().query(fileUri, null, null, null, null);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        long size = returnCursor.getLong(sizeIndex);
        returnCursor.close();
        return (double) size / (1024 * 1024);
    }


    @Override
    protected void onPause() {
        super.onPause();
        selectedTabPosition = binding.tabTablayout.getSelectedTabPosition();
    }

}