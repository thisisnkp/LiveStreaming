package com.example.rayzi.user.wallet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

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
import com.example.rayzi.SessionManager;
import com.example.rayzi.activity.BaseFragment;
import com.example.rayzi.databinding.BottomSheetPaymentBinding;
import com.example.rayzi.databinding.FragmentRechargeBinding;
import com.example.rayzi.modelclass.CreateUserStripe;
import com.example.rayzi.modelclass.DiamondPlanRoot;
import com.example.rayzi.modelclass.SettingRoot;
import com.example.rayzi.modelclass.UserRoot;
import com.example.rayzi.retrofit.Const;
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

public class RechargeFragment extends BaseFragment {

    private static final String TAG = "rechargefragment";
    private static final String STR_STRIPE = "stripe";
    private static final String STR_GP = "google pay";
    List<String> paymentGateways = new ArrayList<>();
    SettingRoot.Setting setting;
    String productId;
    String country;
    String currency;
    ActivityResultLauncher<Intent> activityResultLauncher;
    String popularPlanId;
    String popularProductId;
    DiamondPlanRoot.DiamondPlanItem popularPlanItem;
    SessionManager sessionManager;
    FragmentRechargeBinding binding;
    DiamondPlanRoot.DiamondPlanItem selectedPlan;
    int price;
    String selectedPlanId;
    boolean isVip;
    PaymentSheet paymentSheet;
    String paymentClientSecret;
    String paymentIntent;
    PaymentSheet.CustomerConfiguration customerConfig;
    private BillingClient billingClient;
    private String planId;
    private boolean apiCalled = false;
    private PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
            // To be implemented in a later section.
            Log.d(TAG, "onPurchasesUpdated: 1");
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                Log.d(TAG, "onPurchasesUpdated: size  " + purchases.size());
                if (!purchases.isEmpty()) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
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
    private String paymentGateway;
    private BottomSheetDialog bottomSheetDialog;
    public RechargeFragment() {
        // Required empty public constructor
    }

    void handlePurchase(Purchase purchase) {

        ConsumeParams consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_recharge, container, false);
        sessionManager = new SessionManager(requireActivity());
        if (sessionManager.getSetting().getStripePublishableKey() != null && !sessionManager.getSetting().getStripePublishableKey().isEmpty()) {
            PaymentConfiguration.init(getActivity(), sessionManager.getSetting().getStripePublishableKey());
        }

        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);
        binding.tvMyCoins.setText(String.valueOf(sessionManager.getUser().getDiamond()));
        initMain();

        setting = sessionManager.getSetting();
        if (setting.isGooglePlaySwitch()) {
            paymentGateways.add("google pay");
        }
        if (setting.isStripeSwitch()) {
            paymentGateways.add("stripe");
        }

        billingClient = BillingClient.newBuilder(getActivity()).setListener(purchasesUpdatedListener).enablePendingPurchases().build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    Log.d(TAG, "onBillingSetupFinished: ");
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
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

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Log.d(TAG, "onCreateView: ============================================ ==  onactvity result");
                Intent data = result.getData();
                // Handle the result of the activity
            }
        });

        return binding.getRoot();
    }

    private void initMain() {
        binding.layPopularPurchase.setVisibility(View.GONE);
        binding.shimmer.setVisibility(View.VISIBLE);
        Call<DiamondPlanRoot> call = RetrofitBuilder.create().getDiamondsPlan();
        call.enqueue(new Callback<>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(Call<DiamondPlanRoot> call, Response<DiamondPlanRoot> response) {
                if (response.code() == 200) {
                    ArrayList<DiamondPlanRoot.DiamondPlanItem> finalPlanList = new ArrayList<>();
                    if (response.body().isStatus() && !response.body().getCoinPlan().isEmpty()) {

                        for (int i = 0; i < response.body().getCoinPlan().size(); i++) {
                            if (response.body().getCoinPlan().get(i).isTop()) {
                                popularPlanItem = response.body().getCoinPlan().get(i);
                                popularPlanId = response.body().getCoinPlan().get(i).getId();
                                popularProductId = response.body().getCoinPlan().get(i).getProductKey();
                                binding.tvPopulatPlanCoin.setText("X " + response.body().getCoinPlan().get(i).getDiamonds());
                                binding.tvPopularPlanAmount.setText(Const.getCurrency() + response.body().getCoinPlan().get(i).getDollar());
                            } else {
                                finalPlanList.add(response.body().getCoinPlan().get(i));
                            }
                        }

                        if (popularPlanItem == null) {
                            binding.layPopularPurchase.setVisibility(View.GONE);
                        } else {
                            binding.layPopularPurchase.setVisibility(View.VISIBLE);
                        }

                        binding.layPopularPurchase.setOnClickListener(view -> {
                            if (popularPlanItem != null) {
                                openBottomSheet(popularPlanItem);
                            }
                        });

                        CoinPurchaseAdapter moreCoinAdapter = new CoinPurchaseAdapter(finalPlanList, new CoinPurchaseAdapter.OnBuyCoinClickListnear() {
                            @Override
                            public void onButClick(DiamondPlanRoot.DiamondPlanItem dataItem) {
                                openBottomSheet(dataItem);
                            }
                        });
                        binding.rvRecharge.setAdapter(moreCoinAdapter);
                        binding.shimmer.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<DiamondPlanRoot> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        });

    }

    public List<String> getPaymentGateways() {
        return paymentGateways;
    }

    private void openBottomSheet(DiamondPlanRoot.DiamondPlanItem dataItem) {
        selectedPlan = dataItem;
//        if (sessionManager.getStringValue(Const.COUNTRY).equalsIgnoreCase("India")) {
//            country = "IN";
//            currency = "INR";
//            price = dataItem.getRupee();
//        } else {
//            country = "US";
//            currency = "USD";
//            price = dataItem.getDollar();
//        }


        country = "US";
        currency = "USD";
        price = dataItem.getDollar();

        if (getActivity() == null) return;
        bottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.CustomBottomSheetDialogTheme);
        bottomSheetDialog.setOnShowListener(dialog -> {
            BottomSheetDialog d = (BottomSheetDialog) dialog;
            FrameLayout bottomSheet = d.findViewById(R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        BottomSheetPaymentBinding bottomSheetPaymentBinding = DataBindingUtil.inflate(LayoutInflater.from(getActivity()), R.layout.bottom_sheet_payment, null, false);
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

    private void setUpSku(String productId) {
        Log.d(TAG, "setUpSku: " + productId);
        List<String> skuList = new ArrayList<>();

        skuList.add(productId);

        Log.d(TAG, "setUpSku: skulist size==== =========================" + skuList.size());

        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);

        billingClient.querySkuDetailsAsync(params.build(), (billingResult, skuDetailsList) -> {
            // Process the result.
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
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

    public void makeGooglePurchase(String productId) {
        if (billingClient.isReady()) {
            setUpSku(productId);
        } else {
            Log.d(TAG, "paymetMethord: bp not init");
        }
    }

    private void buyItem(DiamondPlanRoot.DiamondPlanItem dataItem) {
        planId = dataItem.getId();
        setSelectedPlanId(dataItem.getId(), false);

        if (paymentGateway.equals(STR_GP)) {

            planId = dataItem.getId();
            productId = dataItem.getProductKey();
            Log.d(TAG, "buyItem: " + productId);
            setSelectedPlanId(planId, false);
            makeGooglePurchase(productId);

        } else if (paymentGateway.equals(STR_STRIPE)) {

            binding.pd.setVisibility(View.VISIBLE);

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("userId", sessionManager.getUser().getId());
            jsonObject.addProperty("planId", planId);
            jsonObject.addProperty("currency", currency.toLowerCase());
            jsonObject.addProperty("isVip", false);

            Call<CreateUserStripe> call = RetrofitBuilder.create().getStripeCustomer(jsonObject);
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<CreateUserStripe> call, @NonNull Response<CreateUserStripe> response) {

                    if (response.body().isStatus() && response.code() == 200) {

                        customerConfig = new PaymentSheet.CustomerConfiguration(response.body().getCustomer(), response.body().getEphemeralKey());
                        paymentClientSecret = response.body().getClientSecret();
                        paymentIntent = response.body().getPaymentIntent();
                        PaymentConfiguration.init(getActivity(), response.body().getPublishableKey());

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
                    }
                    binding.pd.setVisibility(View.GONE);

                }

                @Override
                public void onFailure(@NonNull Call<CreateUserStripe> call, @NonNull Throwable t) {
                    t.printStackTrace();
                }
            });

        }
    }

    private void lunchPayment(SkuDetails s) {
        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder().setSkuDetails(s).build();

        int responseCode = billingClient.launchBillingFlow(getActivity(), billingFlowParams).getResponseCode();

        Log.d(TAG, "lunchPayment: responseCode ==========================================" + responseCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: ");
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void callPurchaseApiGooglePay(Purchase purchase) {
        if (getActivity() == null) return;
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("userId", sessionManager.getUser().getId());
        jsonObject.addProperty("planId", selectedPlan.getId());
        jsonObject.addProperty("productId", selectedPlan.getProductKey());
        jsonObject.addProperty("packageName", getActivity().getPackageName());
        jsonObject.addProperty("token", purchase.getPurchaseToken());
        Call<UserRoot> call = RetrofitBuilder.create().callPurchaseApiGooglePayDiamond(jsonObject);
        call.enqueue(new Callback<UserRoot>() {
            @Override
            public void onResponse(Call<UserRoot> call, Response<UserRoot> response) {
                if (response.code() == 200) {

                    if (response.body().isStatus() && response.body().getUser() != null) {
                        Toast.makeText(getActivity(), "Purchased", Toast.LENGTH_SHORT).show();
                        sessionManager.saveUser(response.body().getUser());
                        binding.tvMyCoins.setText(String.valueOf(sessionManager.getUser().getDiamond()));
                    } else {
                        Toast.makeText(getActivity(), response.message(), Toast.LENGTH_SHORT).show();
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

    public void callPurchaseDoneApi(String planId, String paymentGateway) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("userId", sessionManager.getUser().getId());
        jsonObject.addProperty("planId", planId);
        jsonObject.addProperty("currency", paymentGateway);
        jsonObject.addProperty("payment_intent_id", paymentIntent);

        Call<UserRoot> call = RetrofitBuilder.create().purchsePlanStripeDiamons(jsonObject);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<UserRoot> call, Response<UserRoot> response) {
                if (response.code() == 200) {
                    if (response.body().getUser() != null && response.body().isStatus() && response.body().isStatus()) {
                        Toast.makeText(requireActivity(), "Purchased", Toast.LENGTH_SHORT).show();
                        sessionManager.saveUser(response.body().getUser());
                        binding.tvMyCoins.setText(String.valueOf(response.body().getUser().getDiamond()));
                    } else {
                        Log.d(TAG, "onResponse: 285");
                    }
                }
            }

            @Override
            public void onFailure(Call<UserRoot> call, Throwable t) {
                Log.d(TAG, "onResponse: 293");
                // Toast.makeText(MyWalletActivity.this, "Something Went Wrong..", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void onPaymentSheetResult(final PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            Log.d("TAG", "Canceled");
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            Log.e("TAG", "Got error: ", ((PaymentSheetResult.Failed) paymentSheetResult).getError());
            Toast.makeText(getContext(), "" + ((PaymentSheetResult.Failed) paymentSheetResult).getError(), Toast.LENGTH_SHORT).show();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            // Display for example, an order confirmation screen
            Log.d("TAG", "Completed");
            callPurchaseDoneApi(planId, "Stripe");
            Toast.makeText(getContext(), "Payment Done", Toast.LENGTH_SHORT).show();
        }
    }

}