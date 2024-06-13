package com.example.rayzi.activity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.rayzi.MainApplication;
import com.example.rayzi.R;
import com.example.rayzi.SessionManager;
import com.example.rayzi.modelclass.BannerRoot;
import com.example.rayzi.modelclass.GiftCategoryRoot;
import com.example.rayzi.modelclass.GiftRoot;
import com.example.rayzi.modelclass.IpAddressRoot_e;
import com.example.rayzi.modelclass.SettingRoot;
import com.example.rayzi.modelclass.UserRoot;
import com.example.rayzi.popups.PopupBuilder;
import com.example.rayzi.retrofit.Const;
import com.example.rayzi.retrofit.RetrofitBuilder;
import com.facebook.FacebookSdk;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;

import java.util.List;

import io.branch.referral.Branch;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "splashAct";
    SessionManager sessionManager;
    private String branchData = "";
    private String type = "";
    private int totalCategories;
    private int currentIndex;

    @Override
    public void onStart() {
        super.onStart();
        Branch branch = Branch.getInstance();
        // Branch init
        branch.initSession((referringParams, error) -> {
            if (error == null) {
                Log.i("BRANCH SDK1", referringParams.toString());
                try {
                    boolean isLinkClicked = referringParams.getBoolean("+clicked_branch_link");
                    Log.d(TAG, "onStart:is link clicked   " + isLinkClicked);

                    if (isLinkClicked) {
                        branchData = referringParams.getString(Const.DATA);
                        type = referringParams.getString(Const.TYPE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                Log.i("BRANCH SDK2", error.getMessage());
            }
        }, this.getIntent().getData(), this);
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.setIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splesh);

        sessionManager = new SessionManager(this);

        try {
            FacebookSdk.sdkInitialize(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Animation anim = AnimationUtils.loadAnimation(this, R.anim.splesh);
        anim.setFillAfter(true);
//        findViewById(R.id.spleshtext).setVisibility(View.VISIBLE);
//        findViewById(R.id.spleshtext).startAnimation(anim);

        FirebaseMessaging.getInstance().subscribeToTopic("CHAPI").addOnCompleteListener(task -> Log.d("TAG", "onCreate: init msg"));

        checkNetwork();
        getGiftCategory();

    }

    public void getGiftCategory() {
        Call<GiftCategoryRoot> call = RetrofitBuilder.create().getGiftCategory();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<GiftCategoryRoot> call, Response<GiftCategoryRoot> response) {
                if (response.code() == 200) {
                    if (response.body().isStatus() && !response.body().getCategory().isEmpty()) {
                        sessionManager.saveGiftCategories(response.body().getCategory());
                        totalCategories = response.body().getCategory().size();
                        currentIndex = 0;
                        fetchNextGiftList(currentIndex, totalCategories, response.body().getCategory());

                        Log.d(TAG, "onResponse: sessionManager.getGiftCategoriesList() ====== " + sessionManager.getGiftCategoriesList().toString());
                    }
                }
            }

            @Override
            public void onFailure(Call<GiftCategoryRoot> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void fetchNextGiftList(final int currentIndex, final int totalCategories, List<GiftCategoryRoot.CategoryItem> category) {
        if (currentIndex >= totalCategories) {
            // All requests are done
            return;
        }
        Log.d(TAG, "fetchNextGiftList: currentIndex ==== " + currentIndex);
        String categoryId = category.get(currentIndex).getId();
        getGiftsList(categoryId, currentIndex, new GiftListCallback() {
            @Override
            public void onGiftListFetched(int currentIndex) {
                // Gift list for the current category fetched
                // You can perform any processing with the response here

                // Continue to the next category
                fetchNextGiftList(currentIndex + 1, totalCategories, category);
            }

            @Override
            public void onError(String errorMessage) {
                // Handle the error
            }
        });
    }

    private void getGiftsList(String id, int currentIndex, final GiftListCallback callback) {
        Call<GiftRoot> call = RetrofitBuilder.create().getGiftsByCategory(id);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<GiftRoot> call, Response<GiftRoot> response) {
                if (response.code() == 200) {
                    if (response.body().isStatus() && !response.body().getGift().isEmpty()) {
                        // You can perform any processing with the response here
                        sessionManager.saveGiftsList(response.body().getGift().get(0).getCategory(), response.body().getGift());
                        Log.d(TAG, "onResponse: getSVGAGiftsList call thay ceh =======");
                        Log.d(TAG, "onResponse: getSVGAGiftsList response.body().getGift().get(0).getCategory() " + response.body().getGift().get(0).getCategory());
                        Log.d(TAG, "onResponse: getSVGAGiftsList call thay ceh id ===    =======" + id);
                        // Notify the callback that the current category's gift list has been fetched

                        Log.d(TAG, "onResponse: getGiftsList =================== " + sessionManager.getGiftsList(response.body().getGift().get(0).getCategory()));
                        callback.onGiftListFetched(currentIndex);
                    }
                }
            }

            @Override
            public void onFailure(Call<GiftRoot> call, Throwable t) {
                // Handle the error
                callback.onError(t.getMessage());
            }
        });
    }

    private void getSetting() {

        Call<SettingRoot> call = RetrofitBuilder.create().getSettings();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<SettingRoot> call, Response<SettingRoot> response) {
                if (response.code() == 200) {
                    if (response.body().isStatus()) {
                        sessionManager.saveSetting(response.body().getSetting());
                        ((MainApplication) getApplication()).initAgora(SplashActivity.this);
                        Const.setCurrency(sessionManager.getSetting().getCurrency());

                        if (sessionManager.getSetting().isIsAppActive()) {
                            gotoMainPage();
                        } else {
                            new PopupBuilder(SplashActivity.this).showSimplePopup("We are Under Maintenance", "Dismiss", () -> finishAffinity());
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<SettingRoot> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        });

    }

    private void getIp() {
        Call<IpAddressRoot_e> call = RetrofitBuilder.getIp().getIp();
        call.enqueue(new Callback<IpAddressRoot_e>() {
            @Override
            public void onResponse(Call<IpAddressRoot_e> call, Response<IpAddressRoot_e> response) {
                if (response.code() == 200 && response.body() != null) {
                    if (response.body().getCountry() != null) {
                        Log.d("TAG", "onResponse: get ip");

                        sessionManager.saveStringValue(Const.COUNTRY, response.body().getCountry());
                        sessionManager.saveStringValue(Const.CURRENT_CITY, response.body().getCity());
                        if (response.body().getQuery() != null) {
                            sessionManager.saveStringValue(Const.IPADDRESS, response.body().getQuery());
                        }
                        getSetting();
                    }
                }
            }

            @Override
            public void onFailure(Call<IpAddressRoot_e> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void gotoMainPage() {
        new Handler(Looper.myLooper()).postDelayed(() -> {
            if (sessionManager.getBooleanValue(Const.ISLOGIN)) {
                Call<UserRoot> call = RetrofitBuilder.create().getUser(sessionManager.getUser().getId());
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<UserRoot> call, Response<UserRoot> response) {
                        if (response.code() == 200) {
                            if (response.body().isStatus()) {
                                if (response.body().getUser() != null) {
                                    if (response.body().getUser().isIsBlock()) {
                                        new PopupBuilder(SplashActivity.this).showSimplePopup("You are blocked by admin", "Dismiss", () -> finishAffinity());
                                    } else {
                                        checkUser(response.body().getUser());
                                    }
                                }
                            } else {
                                startActivity(new Intent(SplashActivity.this, LoginActivityActivity.class));
                                finish();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<UserRoot> call, Throwable t) {
                        t.printStackTrace();
                    }
                });
            } else {
                startActivity(new Intent(SplashActivity.this, LoginActivityActivity.class));
            }
        }, 500);
    }

    public interface GiftListCallback {
        void onGiftListFetched(int currentIndex);

        void onError(String errorMessage);
    }

    private void checkUser(UserRoot.User user) {
        Log.d(TAG, "checkUser: local Id " + sessionManager.getUser().getIdentity());
        Log.d(TAG, "checkUser: remote Id " + user.getIdentity());
        if (user.getIdentity().equals(sessionManager.getUser().getIdentity())) {
            sessionManager.saveUser(user);
            startActivity(new Intent(SplashActivity.this, MainActivity.class)
                    .putExtra(Const.DATA, branchData).putExtra(Const.TYPE, type));
        } else {
            new PopupBuilder(this).showSimplePopup("You are logged in other devices", "Dismiss", () -> {
                GoogleSignInOptions gso = new GoogleSignInOptions.
                        Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                        build();

                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
                googleSignInClient.signOut();

                Toast.makeText(this, "Log Out", Toast.LENGTH_SHORT).show();

                sessionManager.saveUser(null);
                sessionManager.saveBooleanValue(Const.ISLOGIN, false);
                startActivity(new Intent(SplashActivity.this, LoginActivityActivity.class));
                finish();

            });
        }

    }

    private void checkNetwork() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo activeNetInfo2 = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isConnected = activeNetInfo != null && activeNetInfo.isConnectedOrConnecting();
        boolean isConnected2 = activeNetInfo2 != null && activeNetInfo2.isConnectedOrConnecting();
        showHideInternet(isConnected || isConnected2);
    }

    private void showHideInternet(Boolean isOnline) {
        Log.d(TAG, "showHideInternet: " + isOnline);
        final TextView tvInternetStatus = findViewById(R.id.tv_internet_status);

        if (isOnline) {
            getIp();
            if (tvInternetStatus != null && tvInternetStatus.getVisibility() == View.VISIBLE && tvInternetStatus.getText().toString().equalsIgnoreCase(getString(R.string.no_internet_connection))) {
                tvInternetStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
                tvInternetStatus.setText(R.string.back_online);
                new Handler().postDelayed(() -> {
                    Animation animation = AnimationUtils.loadAnimation(this, R.anim.enter_up);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            tvInternetStatus.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                    tvInternetStatus.startAnimation(animation);
                }, 200);
            }
        } else {
            if (tvInternetStatus != null) {
                tvInternetStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.red));
                tvInternetStatus.setText(R.string.no_internet_connection);
                if (tvInternetStatus.getVisibility() == View.GONE) {
                    Animation animation = AnimationUtils.loadAnimation(this, R.anim.enter_down);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            tvInternetStatus.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });

                    tvInternetStatus.startAnimation(animation);
                }
            }
        }
    }
}