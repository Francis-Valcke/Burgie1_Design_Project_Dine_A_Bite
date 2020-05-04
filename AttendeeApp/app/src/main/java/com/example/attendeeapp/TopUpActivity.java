package com.example.attendeeapp;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.attendeeapp.data.LoginDataSource;
import com.example.attendeeapp.data.LoginRepository;
import com.example.attendeeapp.data.model.LoggedInUser;
import com.example.attendeeapp.json.BetterResponseModel;
import com.example.attendeeapp.polling.OkHttpRequestTool;
import com.example.attendeeapp.stripe.DineabiteEphemeralKeyProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import com.example.attendeeapp.json.BetterResponseModel.*;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class TopUpActivity extends AppCompatActivity {

    private PaymentSession paymentSession;
    private PaymentMethod selectedPaymentMethod;
    private Context context;
    private Stripe stripe;
    private LoggedInUser user;

    ProgressBar loadingProgressBar;
    Toolbar toolbar;
    Button paymentSetup;
    Button payButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_up);
        context = this;

        loadingProgressBar = findViewById(R.id.payment_loading);
        toolbar = findViewById(R.id.toolbar);
        paymentSetup = findViewById(R.id.button_payment_setup);
        payButton = findViewById(R.id.button_pay);

        // Custom Toolbar (instead of standard actionbar)
        setSupportActionBar(toolbar);

        loadingProgressBar.setVisibility(View.GONE);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);

        ab.setTitle("Top Up");

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

        paymentSetup.setOnClickListener(button -> {
            paymentSession.presentPaymentMethodSelection(null);
        });


        // Initialize Stipe payment
        CustomerSession.initCustomerSession(this, new DineabiteEphemeralKeyProvider());
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
                        } else throw new Exception("Status not OK from server");

                        stripe = new Stripe(getApplicationContext(), publicKey);

                        assert selectedPaymentMethod.id != null;
                        stripe.confirmPayment(this,
                                ConfirmPaymentIntentParams.createWithPaymentMethodId(
                                        selectedPaymentMethod.id,
                                        clientSecret
                                )
                        );



                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("STRIPE", "doPayment: ", e);
                        loadingProgressBar.setVisibility(View.GONE);
                    }

                },
                throwable -> {
                    Toast.makeText(context, "ERROR", Toast.LENGTH_SHORT).show();
                    Log.e("STRIPE", "doPayment: ", throwable);
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
                        Log.i("STRIPE", "confirmPayment: balance after operation:" + responseModel.getPayload().getBalance());
                        finish();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("STRIPE", "confirmPayment: ", e);
                        loadingProgressBar.setVisibility(View.GONE);
                    }

                },
                throwable -> {
                    Toast.makeText(context, "ERROR", Toast.LENGTH_SHORT).show();
                    Log.e("STRIPE", "confirmPayment: ", throwable);
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

                        Log.i("STRIPE", "cancelPayment: balance after operation:" + responseModel.getPayload().getBalance());
                        loadingProgressBar.setVisibility(View.GONE);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("STRIPE", "cancelPayment: ", e);
                        loadingProgressBar.setVisibility(View.GONE);
                    }

                },
                throwable -> {
                    Toast.makeText(context, "ERROR", Toast.LENGTH_SHORT).show();
                    Log.e("STRIPE", "cancelPayment: ", throwable);
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
                            Log.i("STRIPE", "Ready to charge!");
                        } else {
                            payButton.setEnabled(false);
                        }
                    }

                    @Override
                    public void onCommunicatingStateChanged(boolean isCommunicating) {

                    }

                    @Override
                    public void onError(int errorCode, @NotNull String errorMessage) {
                        Log.e("STRIPE", "onError: " + errorMessage, null);
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
                Log.i("STRIPE", "Payment nog successful, no payment method selected");
                activity.confirmPayment();


            } else if (status == PaymentIntent.Status.RequiresPaymentMethod) {
                // Payment failed – allow retrying using a different payment method

                Toast.makeText(activity, "Payment failed, no payment method selected.", Toast.LENGTH_SHORT).show();
                Log.i("STRIPE", "Payment nog successful, no payment method selected");
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
            Log.e("STRIPE", "onError: ", e);
            activity.cancelPayment();
        }
    }


}
