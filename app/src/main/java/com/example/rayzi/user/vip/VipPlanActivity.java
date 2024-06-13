package com.example.rayzi.user.vip;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.example.rayzi.R;
import com.example.rayzi.activity.BaseActivity;
import com.example.rayzi.activity.SplashActivity;
import com.example.rayzi.adapter.DotAdaptr;
import com.example.rayzi.databinding.ActivityVipPlan1Binding;
import com.example.rayzi.databinding.BottomSheetPaymentBinding;
import com.example.rayzi.modelclass.CreateUserStripe;
import com.example.rayzi.modelclass.SettingRoot;
import com.example.rayzi.modelclass.UserRoot;
import com.example.rayzi.modelclass.VipPlanRoot;
import com.example.rayzi.popups.PopupBuilder;
import com.example.rayzi.retrofit.RetrofitBuilder;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.JsonObject;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VipPlanActivity extends BaseActivity {
    private static final String TAG = "vipplanact";
    private static final String STR_GP = "google pay";
    private static final String STR_STRIPE = "stripe";
    ActivityVipPlan1Binding binding;
    VipImagesAdapter vipImagesAdapter = new VipImagesAdapter();
    VipPlanAdapter vipPlanAdapter = new VipPlanAdapter();
    List<String> paymentGateways = new ArrayList<>();
    MutableLiveData<VipPlanRoot.VipPlanItem> selectedPlan = new MutableLiveData<>(null);
    String country;
    String currency;
    String paymentGateway;
    String planId;
    BillingClient billingClient;
    int selPosition;
    boolean apiCalled = false;
    VipPlanRoot.VipPlanItem selectedPlan1;
    int price = 0;
    BottomSheetDialog bottomSheetDialog;
    SettingRoot.Setting setting;
    String productId;
    String selectedPlanId;
    boolean isVip = true;
    PaymentSheet paymentSheet;
    String paymentClientSecret;
    String paymentIntent;
    PaymentSheet.CustomerConfiguration customerConfig;


    private PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
            // To be implemented in a later section.
            Log.d(TAG, "onPurchasesUpdated: 1");
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                    && purchases != null) {
                Log.d(TAG, "onPurchasesUpdated: size  " + purchases.size());
                if (!purchases.isEmpty()) {
                    if (this != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                handlePurchase(purchases.get(0));
                            }
                        });
                    }
                }
                for (Purchase purchase : purchases) {
                    //  Toast.makeText(WalletActivity.this, "thy gyu", Toast.LENGTH_SHORT).show();

                }
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                // Handle an error caused by a user cancelling the purchase flow.
            } else {
                // Handle any other error codes.
            }
        }
    };
    private UserRoot.User user;

    private void setupLogicAutoSlider() {
        DotAdaptr dotAdapter = new DotAdaptr(vipImagesAdapter.getItemCount(), R.color.pink);
        binding.rvDots.setAdapter(dotAdapter);
        binding.rvBanner.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager myLayoutManager = (LinearLayoutManager) binding.rvBanner.getLayoutManager();
                int scrollPosition = myLayoutManager.findFirstVisibleItemPosition();
                dotAdapter.changeDot(scrollPosition);
            }
        });
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            int pos = 0;
            boolean flag = true;

            @Override
            public void run() {
                if (pos == vipImagesAdapter.getItemCount() - 1) {
                    flag = false;
                } else if (pos == 0) {
                    flag = true;
                }
                if (flag) {
                    pos++;
                } else {
                    pos--;
                }
                binding.rvBanner.smoothScrollToPosition(pos);
                handler.postDelayed(this, 2500);
            }
        };
        handler.postDelayed(runnable, 2500);
    }

    void handlePurchase(Purchase purchase) {

        ConsumeParams consumeParams =
                ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();

        if (!apiCalled) {
            Log.d(TAG, "handlePurchase: qwetuioooi2wqwertyukiol==================");
            apiCalled = true;
            callPurchaseApiGooglePay(purchase);

        } else {
            Log.d(TAG, "handlePurchase: sdsd");
        }

        ConsumeResponseListener listener = (billingResult, purchaseToken) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "handlePurchase: consume");
            }
        };
        billingClient.consumeAsync(consumeParams, listener);
    }

    private void callPurchaseApiGooglePay(Purchase purchase) {
        if (this == null) return;
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("userId", sessionManager.getUser().getId());
        jsonObject.addProperty("planId", selectedPlan1.getId());
        jsonObject.addProperty("productId", selectedPlan1.getProductKey());
        jsonObject.addProperty("packageName", this.getPackageName());
        jsonObject.addProperty("token", purchase.getPurchaseToken());
        Call<UserRoot> call = RetrofitBuilder.create().callPurchaseApiGooglePayVip(jsonObject);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<UserRoot> call, Response<UserRoot> response) {
                if (response.code() == 200) {

                    if (response.body().isStatus() && response.body().getUser() != null) {
                        Toast.makeText(VipPlanActivity.this, "Purchased", Toast.LENGTH_SHORT).show();
                        sessionManager.saveUser(response.body().getUser());
                    } else {
                        Toast.makeText(VipPlanActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                    }

                    apiCalled = false;
                }
            }

            @Override
            public void onFailure(Call<UserRoot> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.parseColor("#170D1F"));
        binding = DataBindingUtil.setContentView(this, R.layout.activity_vip_plan1);

        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);

        binding.rvBanner.setAdapter(vipImagesAdapter);
        new PagerSnapHelper().attachToRecyclerView(binding.rvBanner);
        setVIpSlider();
        setupLogicAutoSlider();

        if (!isFinishing()) {
            binding.ivUser.setUserImage(sessionManager.getUser().getImage(), sessionManager.getUser().isIsVIP(), VipPlanActivity.this, 18);
        }
        binding.tvName.setText(sessionManager.getUser().getName());

        PaymentConfiguration.init(this, sessionManager.getSetting().getStripePublishableKey());
        country = "US";
        currency = "USD";

        if (isRTL(this)) {
            binding.ivBack.setScaleX(isRTL(this) ? -1 : 1);
        }

        binding.ivBack.setOnClickListener(view -> {
            finish();
        });

        setting = sessionManager.getSetting();
        if (setting.isGooglePlaySwitch()) {
            paymentGateways.add("google pay");
        }
        if (setting.isStripeSwitch()) {
            paymentGateways.add("stripe");
        }

        getUserData();

        vipPlanAdapter.setOnPlanClickListener(vipPlanItem -> {
            selectedPlan1 = vipPlanItem;
            Log.d(TAG, "onPlanClick: selectedPlan1+++++++++++++++++++++++++++++++++++ " + selectedPlan1);
        });

        binding.btnPurchase.setOnClickListener(view -> {
            if (selectedPlan1 == null) {
                Toast.makeText(this, "First select the plan.", Toast.LENGTH_SHORT).show();
                return;
            }
            openBottomSheet(selectedPlan1);

        });
        billingClient = BillingClient.newBuilder(this)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "onBillingSetupFinished: ");
                    if (this != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                binding.rvPlan.setAdapter(vipPlanAdapter);
                                initData();
                            }
                        });
                    }
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.d(TAG, "onBillingServiceDisconnected: ");
            }
        });
    }

    private void openBottomSheet(VipPlanRoot.VipPlanItem dataItem) {
        country = "US";
        currency = "USD";

        bottomSheetDialog = new BottomSheetDialog(VipPlanActivity.this, R.style.CustomBottomSheetDialogTheme);
        bottomSheetDialog.setOnShowListener(dialog -> {
            BottomSheetDialog d = (BottomSheetDialog) dialog;
            FrameLayout bottomSheet = (FrameLayout) d.findViewById(R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet)
                        .setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        BottomSheetPaymentBinding bottomSheetPaymentBinding = DataBindingUtil.inflate(LayoutInflater.from(VipPlanActivity.this), R.layout.bottom_sheet_payment, null, false);
        bottomSheetDialog.setContentView(bottomSheetPaymentBinding.getRoot());
        bottomSheetDialog.show();
        bottomSheetPaymentBinding.btnclose.setOnClickListener(v -> bottomSheetDialog.dismiss());
        List<String> paymentGateways = getPaymentGateways();

        if (paymentGateways.contains(STR_GP)) {
            bottomSheetPaymentBinding.lytgooglepay.setVisibility(View.VISIBLE);

            bottomSheetPaymentBinding.lytgooglepay.setOnClickListener(v -> {
                paymentGateway = STR_GP;
                bottomSheetDialog.dismiss();
                buyItem(dataItem);
            });
        } else {
            bottomSheetPaymentBinding.lytgooglepay.setVisibility(View.GONE);
        }

        if (paymentGateways.contains(STR_STRIPE)) {
            bottomSheetPaymentBinding.lytstripe.setVisibility(View.VISIBLE);
            bottomSheetPaymentBinding.lytstripe.setOnClickListener(v -> {
                paymentGateway = STR_STRIPE;
                bottomSheetDialog.dismiss();
                buyItem(dataItem);
            });
        } else {
            bottomSheetPaymentBinding.lytstripe.setVisibility(View.GONE);
        }

    }

    public void setSelectedPlanId(String selectedPlanId, boolean isVip) {
        this.selectedPlanId = selectedPlanId;
        this.isVip = isVip;
    }

    private void buyItem(VipPlanRoot.VipPlanItem dataItem) {
        Log.d(TAG, "buyItem: buy item caal -0-------------------------------------------======================= ");
        planId = dataItem.getId();
        setSelectedPlanId(dataItem.getId(), true);
        if (paymentGateway.equals(STR_GP)) {

            planId = dataItem.getId();
            productId = dataItem.getProductKey();
            planId = dataItem.getId();
            setSelectedPlanId(planId, true);
            makeGooglePurchase(productId);

        } else if (paymentGateway.equals(STR_STRIPE)) {
            binding.pd.setVisibility(View.VISIBLE);

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("userId", sessionManager.getUser().getId());
            jsonObject.addProperty("planId", planId);
            jsonObject.addProperty("currency", currency);
            jsonObject.addProperty("isVip", true);

            Call<CreateUserStripe> call = RetrofitBuilder.create().getStripeCustomer(jsonObject);
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<CreateUserStripe> call, @NonNull Response<CreateUserStripe> response) {


                    if (response.code() == 200 && response.body().isStatus() && !response.body().getClientSecret().isEmpty()) {


                        customerConfig = new PaymentSheet.CustomerConfiguration(response.body().getCustomer(), response.body().getEphemeralKey());
                        paymentClientSecret = response.body().getClientSecret();
                        paymentIntent = response.body().getPaymentIntent();
                        PaymentConfiguration.init(VipPlanActivity.this, response.body().getPublishableKey());

                        PaymentSheet.Address address = new PaymentSheet.Address.Builder()
                                .country("IN")
                                .build();
                        PaymentSheet.BillingDetails billingDetails = new PaymentSheet.BillingDetails.Builder()
                                .address(address)
                                .build();

                        PaymentSheet.BillingDetailsCollectionConfiguration billingDetailsCollectionConfiguration = new PaymentSheet.BillingDetailsCollectionConfiguration(
                                PaymentSheet.BillingDetailsCollectionConfiguration.CollectionMode.Always,
                                PaymentSheet.BillingDetailsCollectionConfiguration.CollectionMode.Never,
                                PaymentSheet.BillingDetailsCollectionConfiguration.CollectionMode.Never,
                                PaymentSheet.BillingDetailsCollectionConfiguration.AddressCollectionMode.Full,
                                true
                        );

                        final PaymentSheet.Configuration configuration = new PaymentSheet.Configuration.Builder(getString(R.string.app_name))
                                .customer(customerConfig)
                                .billingDetailsCollectionConfiguration(billingDetailsCollectionConfiguration)
                                .defaultBillingDetails(billingDetails)
                                .allowsPaymentMethodsRequiringShippingAddress(true)
                                .allowsDelayedPaymentMethods(true)
                                .build();

                        paymentSheet.presentWithPaymentIntent(
                                paymentClientSecret,
                                configuration
                        );

                        binding.pd.setVisibility(View.GONE);


                    }

                }

                @Override
                public void onFailure(@NonNull Call<CreateUserStripe> call, @NonNull Throwable t) {
                    t.printStackTrace();
                }
            });

        }
    }

    public List<String> getPaymentGateways() {
        return paymentGateways;
    }

    public void makeGooglePurchase(String productId) {
        if (billingClient.isReady()) {
            setUpSku(productId);
            Log.d(TAG, "makeGooglePurchase:  setUpSku(productid); ===================================================");
        } else {
            Log.d(TAG, "paymetMethord: bp not init");
        }
    }

    private void showSuccessPopup() {
        new PopupBuilder(VipPlanActivity.this).showReliteDiscardPopup("You are VIP now", "Restart app", "Continue", "", () -> {
            Intent intent = new Intent(VipPlanActivity.this, SplashActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            finishAffinity();
            startActivity(intent);
        });
    }

    private void initData() {
        binding.shimmer.setVisibility(View.VISIBLE);
        binding.rvPlan.setVisibility(View.GONE);
        Call<VipPlanRoot> call = RetrofitBuilder.create().getVipPlan();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<VipPlanRoot> call, Response<VipPlanRoot> response) {
                if (response.code() == 200) {
                    if (response.body().isStatus() && !response.body().getVipPlan().isEmpty()) {
                        selectedPlan1 = response.body().getVipPlan().get(0);
                        vipPlanAdapter.addData(response.body().getVipPlan());
                        selectedPlan.setValue(response.body().getVipPlan().get(0));
                    }
                }
                binding.shimmer.setVisibility(View.GONE);
                binding.rvPlan.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<VipPlanRoot> call, Throwable t) {

            }
        });

    }

    private void setVIpSlider() {

        DotAdaptr dotAdapter = new DotAdaptr(vipImagesAdapter.getItemCount(), R.color.white);
        binding.rvDots.setAdapter(dotAdapter);
        binding.rvBanner.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager myLayoutManager = (LinearLayoutManager) binding.rvBanner.getLayoutManager();
                int scrollPosition = myLayoutManager.findFirstVisibleItemPosition();
                dotAdapter.changeDot(scrollPosition);
            }
        });
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            int pos = 0;
            boolean flag = true;

            @Override
            public void run() {
                if (pos == vipImagesAdapter.getItemCount() - 1) {
                    flag = false;
                } else if (pos == 0) {
                    flag = true;
                }
                if (flag) {
                    pos++;
                } else {
                    pos--;
                }
//                binding.rvBanner.smoothScrollToPosition(pos);
                handler.postDelayed(this, 2000);
            }
        };
        handler.postDelayed(runnable, 2000);
    }

    private void setUpSku(String productid) {
        Log.d(TAG, "setUpSku: " + productid);
        List<String> skuList = new ArrayList<>();

        skuList.add(productid);

        Log.d(TAG, "setUpSku: skulist size==== =========================" + skuList.size());

        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);

        billingClient.querySkuDetailsAsync(params.build(),
                (billingResult, skuDetailsList) -> {
                    // Process the result.
                    if (VipPlanActivity.this != null) {
                        VipPlanActivity.this.runOnUiThread(() -> {
                            try {
                                Log.d(TAG, "run: " + skuDetailsList.size());
                                Log.d(TAG, "run: " + skuDetailsList);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (skuDetailsList.isEmpty()) {
                                return;

                            } else {

                                lunchPayment(skuDetailsList.get(0));
                            }
                            Log.d(TAG, "setUpSku: skuDetailsList ================================================ " + skuDetailsList);

                        });

                    } else {
                        Log.d(TAG, "setUpSku: get act is null");
                    }

                });
    }

    private void lunchPayment(SkuDetails s) {
        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(s)
                .build();

        int responseCode = billingClient.launchBillingFlow(VipPlanActivity.this, billingFlowParams).getResponseCode();

        Log.d(TAG, "lunchPayment: responseCode ==========================================" + responseCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClickBack(View view) {
        super.onClickBack(view);
    }

    private void displayAlert(@NonNull String title, @Nullable String message, boolean b) {
        binding.pd.setVisibility(View.GONE);
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message);
        builder.setPositiveButton("Ok", (dialog, which) -> {
            if (b) {
                Toast.makeText(this, "Purchase Done", Toast.LENGTH_SHORT).show();
            } else {
                builder.create().dismiss();
            }
        });
        builder.create().show();
    }

    public void callPurchaseDoneApi(String planId, String paymentGateway) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("userId", sessionManager.getUser().getId());
        jsonObject.addProperty("planId", planId);
        jsonObject.addProperty("paymentGateway", paymentGateway);
        jsonObject.addProperty("payment_intent_id", paymentIntent);

        Call<UserRoot> call = RetrofitBuilder.create().purchsePlanStripeVip(jsonObject);
        call.enqueue(new Callback<
                >() {
            @Override
            public void onResponse(Call<UserRoot> call, Response<UserRoot> response) {
                if (response.code() == 200) {
                    if (response.body().getUser() != null && response.body().isStatus() && response.body().isStatus()) {
                        Toast.makeText(VipPlanActivity.this, "Purchased", Toast.LENGTH_SHORT).show();
                        sessionManager.saveUser(response.body().getUser());
                        showSuccessPopup();
                    } else {
                        Log.d(TAG, "onResponse: 285");
                    }
                }
            }

            @Override
            public void onFailure(Call<UserRoot> call, Throwable t) {
                Log.d(TAG, "onResponse: 293");
            }
        });
    }

    private void getUserData() {
        Log.d("HELLO ", "getData:  my wallet 171");
        Call<UserRoot> call = RetrofitBuilder.create().getUser(sessionManager.getUser().getId());
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<UserRoot> call, Response<UserRoot> response) {
                if (response.code() == 200 && response.body().isStatus() && response.body().getUser() != null) {
                    user = response.body().getUser();
                    sessionManager.saveUser(user);
                }
            }

            @Override
            public void onFailure(Call<UserRoot> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    void onPaymentSheetResult(final PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            Log.d("TAG", "Canceled");
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            Log.e("TAG", "Got error: ", ((PaymentSheetResult.Failed) paymentSheetResult).getError());
            Toast.makeText(VipPlanActivity.this, "" + ((PaymentSheetResult.Failed) paymentSheetResult).getError(), Toast.LENGTH_SHORT).show();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            // Display for example, an order confirmation screen
            Log.d("TAG", "Completed");
            callPurchaseDoneApi(planId, "Stripe");
            Toast.makeText(VipPlanActivity.this, "Payment Done", Toast.LENGTH_SHORT).show();
        }
    }
}