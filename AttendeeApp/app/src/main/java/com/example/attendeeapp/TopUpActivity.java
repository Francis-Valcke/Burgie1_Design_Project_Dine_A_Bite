package com.example.attendeeapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.attendeeapp.data.LoginDataSource;
import com.example.attendeeapp.data.LoginRepository;
import com.example.attendeeapp.data.model.LoggedInUser;
import com.example.attendeeapp.json.BetterResponseModel;
import com.example.attendeeapp.json.BetterResponseModel.CreatePaymentIntentResponse;
import com.example.attendeeapp.json.BetterResponseModel.GetBalanceResponse;
import com.example.attendeeapp.json.BetterResponseModel.Status;
import com.example.attendeeapp.polling.OkHttpRequestTool;
import com.example.attendeeapp.stripe.DineABiteEphemeralKeyProvider;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.android.ApiResultCallback;
import com.stripe.android.CustomerSession;
import com.stripe.android.PaymentIntentResult;
import com.stripe.android.PaymentSession;
import com.stripe.android.PaymentSessionConfig;
import com.stripe.android.PaymentSessionData;
import com.stripe.android.Stripe;
import com.stripe.android.model.ConfirmPaymentIntentParams;
import com.stripe.android.model.PaymentIntent;
import com.stripe.android.model.PaymentMethod;
import com.stripe.android.view.BillingAddressFields;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class TopUpActivity extends ToolbarActivity {

    private static final String TAG = TopUpActivity.class.getSimpleName();

    private PaymentSession paymentSession;
    private PaymentMethod selectedPaymentMethod;
    private Context context;
    private Stripe stripe;
    private LoggedInUser user;

    ProgressBar loadingProgressBar;
    Button paymentSetup;
    Button payButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_up);
        context = this;

        loadingProgressBar = findViewById(R.id.payment_loading);
        paymentSetup = findViewById(R.id.button_payment_setup);
        payButton = findViewById(R.id.button_pay);
        loadingProgressBar.setVisibility(View.GONE);

        // Initialize the toolbar
        initToolbar();
        upButtonToolbar();

        // Show username of currently logged in user
        user = LoginRepository.getInstance(new LoginDataSource()).getLoggedInUser();

        payButton.setEnabled(false);
        payButton.setOnClickListener(v -> {
            if (!v.isEnabled()){
                Toast.makeText(context, "Please select a valid payment method first.", Toast.LENGTH_SHORT).show();
            } else {
                // Execute the payment
                loadingProgressBar.setVisibility(View.VISIBLE);
                EditText amount = findViewById(R.id.plain_text_amount);
                doPayment(amount.getText().toString());
            }
        });

        paymentSetup.setOnClickListener(button ->
                paymentSession.presentPaymentMethodSelection(null));


        // Initialize Stripe payment
        CustomerSession.initCustomerSession(this, new DineABiteEphemeralKeyProvider());
        paymentSession = new PaymentSession(this, createPaymentSessionConfig());
        setupPaymentSession();
    }

    private void doPayment(String amount) {

        HashMap<String,String> params = new HashMap<>();
        params.put("amount", amount);

        String url = ServerConfig.AS_ADDRESS + "/stripe/createPaymentIntent";
        url = OkHttpRequestTool.buildUrl(url, params);

        RequestBody body = RequestBody.create("", MediaType.parse("application/json"));

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .method("POST", body)
                .addHeader("Authorization", user.getAuthorizationToken())
                .build();

        OkHttpRequestTool.wrapRequest(request).subscribe(
                response -> {

                    String clientSecret;
                    String publicKey;

                    try {

                        ObjectMapper om = new ObjectMapper();
                        BetterResponseModel<CreatePaymentIntentResponse> responseModel =
                                om.readValue(response, new TypeReference<BetterResponseModel<CreatePaymentIntentResponse>>() {});

                        if (responseModel.getStatus().equals(Status.OK)){
                            clientSecret = responseModel.getPayload().getClientSecret();
                            publicKey = responseModel.getPayload().getPublicKey();
                        } else throw responseModel.getException();

                        stripe = new Stripe(getApplicationContext(), publicKey);

                        assert selectedPaymentMethod.id != null;
                        stripe.confirmPayment(this,
                                ConfirmPaymentIntentParams.createWithPaymentMethodId(
                                        selectedPaymentMethod.id,
                                        clientSecret
                                )
                        );

                    } catch (Exception e) {
                        Toast.makeText(context, "Payment could not be processed.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "doPayment: ", e);
                        loadingProgressBar.setVisibility(View.GONE);
                    }

                },
                throwable -> {
                    Toast.makeText(context, "Payment could not be processed.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "doPayment: ", throwable);
                    loadingProgressBar.setVisibility(View.GONE);
                }
        );

    }

    private void confirmPayment() {

        String url = ServerConfig.AS_ADDRESS + "/stripe/confirmTransaction";

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .method("GET", null)
                .addHeader("Authorization", user.getAuthorizationToken())
                .build();

        OkHttpRequestTool.wrapRequest(request).subscribe(
                response -> {

                    try {
                        ObjectMapper om = new ObjectMapper();
                        BetterResponseModel<GetBalanceResponse> responseModel =
                                om.readValue(response, new TypeReference<BetterResponseModel<GetBalanceResponse>>() {});
                        if (responseModel.isOk()){
                            Log.i(TAG, "confirmPayment: confirmPayment: balance after operation: "+ responseModel.getPayload().getBalance());
                            finish();
                        } else throw responseModel.getException();

                    } catch (Exception e) {
                        Toast.makeText(context, "Payment could not be confirmed.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "confirmPayment: ", e);
                        loadingProgressBar.setVisibility(View.GONE);
                    }

                },
                throwable -> {
                    Toast.makeText(context, "Payment could not be confirmed.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "confirmPayment: ", throwable);
                    loadingProgressBar.setVisibility(View.GONE);
                }
        );

    }

    private void cancelPayment() {

        String url = ServerConfig.AS_ADDRESS + "/stripe/cancelTransaction";

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .method("GET", null)
                .addHeader("Authorization", user.getAuthorizationToken())
                .build();

        OkHttpRequestTool.wrapRequest(request).subscribe(
                response -> {

                    try {
                        ObjectMapper om = new ObjectMapper();
                        BetterResponseModel<GetBalanceResponse> responseModel =
                                om.readValue(response, new TypeReference<BetterResponseModel<GetBalanceResponse>>() {});

                        if (responseModel.isOk()){
                            Log.i(TAG, "confirmPayment: cancelPayment: balance after operation:" + responseModel.getPayload().getBalance());
                            loadingProgressBar.setVisibility(View.GONE);
                        } else throw responseModel.getException();

                    } catch (Exception e) {
                        Toast.makeText(context, "Payment could not be cancelled.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "cancelPayment: ", e);
                        loadingProgressBar.setVisibility(View.GONE);
                    }
                },
                throwable -> {
                    Toast.makeText(context, "Payment could not be cancelled.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "cancelPayment: ", throwable);
                    loadingProgressBar.setVisibility(View.GONE);
                }
        );

    }

    private PaymentSessionConfig createPaymentSessionConfig() {
        return new PaymentSessionConfig.Builder()

                .setBillingAddressFields(BillingAddressFields.Full)

                // collect shipping information
                .setShippingInfoRequired(false)

                // collect shipping method
                .setShippingMethodsRequired(false)

                .build();
    }

    private void setupPaymentSession() {
        paymentSession.init(
                new PaymentSession.PaymentSessionListener() {
                    @Override
                    public void onPaymentSessionDataChanged(@NotNull PaymentSessionData data) {

                        final PaymentMethod paymentMethod = data.getPaymentMethod();
                        if (paymentMethod != null) {
                            selectedPaymentMethod = paymentMethod;
                        }

                        // Update your UI here with other data
                        if (data.isPaymentReadyToCharge()) {
                            payButton.setEnabled(true);
                            Log.i(TAG, "onPaymentSessionDataChanged: Ready to charge!");
                        } else {
                            payButton.setEnabled(false);
                        }
                    }

                    @Override
                    public void onCommunicatingStateChanged(boolean isCommunicating) {

                    }

                    @Override
                    public void onError(int errorCode, @NotNull String errorMessage) {
                        Log.e(TAG, "onError: "+ errorMessage, null);
                    }
                }
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 6000 && data != null) {
            paymentSession.handlePaymentData(requestCode, resultCode, data);
        }

        if (requestCode == 50000) {
            stripe.onPaymentResult(requestCode, data, new PaymentResultCallback(this));
        }

    }

    private static final class PaymentResultCallback implements ApiResultCallback<PaymentIntentResult> {
        @NonNull private final WeakReference<TopUpActivity> activityRef;

        PaymentResultCallback(@NonNull TopUpActivity activity) {
            activityRef = new WeakReference<>(activity);
        }

        @Override
        public void onSuccess(@NonNull PaymentIntentResult result) {
            final TopUpActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }

            PaymentIntent paymentIntent = result.getIntent();
            PaymentIntent.Status status = paymentIntent.getStatus();

            if (status == PaymentIntent.Status.Succeeded) {
                // Complete the payment by topping up the digital wallet at the server

                Toast.makeText(activity, "Payment Success", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "onSuccess: payment success.");
                activity.confirmPayment();


            } else if (status == PaymentIntent.Status.RequiresPaymentMethod) {
                // Payment failed – allow retrying using a different payment method

                Toast.makeText(activity, "Payment failed, no payment method selected.", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Payment failed. Requires a payment method to be selected.");
                activity.cancelPayment();

            }
        }

        @Override
        public void onError(@NonNull Exception e) {
            final TopUpActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }

            // Payment request failed – allow retrying using the same payment method
            Toast.makeText(activity, "There was an error executing the payment.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "onError: ", e);
            activity.cancelPayment();
        }
    }


}
