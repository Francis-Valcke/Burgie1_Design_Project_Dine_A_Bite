package com.example.attendeeapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.attendeeapp.data.LoginDataSource;
import com.example.attendeeapp.data.LoginRepository;
import com.example.attendeeapp.data.model.LoggedInUser;
import com.example.attendeeapp.json.BetterResponseModel;
import com.example.attendeeapp.stripe.DineabiteEphemeralKeyProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.android.CustomerSession;
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
import org.json.JSONException;

import java.util.Objects;

public class TopUpActivity extends AppCompatActivity {

    TextView username;
    private PaymentSession paymentSession;
    Button payButton;
    Button payentSetup;
    PaymentMethod selectedPaymentMethod;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_up);
        context = this;

        // Custom Toolbar (instead of standard actionbar)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);

        ab.setTitle("Top Up");

        // Show username of currently logged in user
        LoggedInUser user = LoginRepository.getInstance(new LoginDataSource()).getLoggedInUser();

        payButton = findViewById(R.id.button_pay);
        payButton.setEnabled(false);
        payButton.setOnClickListener(v -> {
            if (!v.isEnabled()){
                Toast.makeText(context, "Please select a valid payment method first.", Toast.LENGTH_SHORT).show();
            } else {
                // Execute the payment
                EditText amount = findViewById(R.id.plain_text_amount);
                pay(Double.valueOf(amount.getText().toString()));
            }
        });

        Button paymentSetup = findViewById(R.id.button_payment_setup);
        paymentSetup.setOnClickListener(button -> {
            paymentSession.presentPaymentMethodSelection(null);
        });


        // Initialize Stipe payment
        CustomerSession.initCustomerSession(this, new DineabiteEphemeralKeyProvider());
        paymentSession = new PaymentSession(this, createPaymentSessionConfig());
        setupPaymentSession();
    }

    private void pay(double amount) {




        String url = ServerConfig.AS_ADDRESS + "/stripe/createPaymentIntent";
        JsonObjectRequest paymentIntentRequest = new JsonObjectRequest(Request.Method.GET, url,null,
                response -> {

                    Stripe stripe;
                    String clientSecret;
                    String publicKey;

                    try {

                        ObjectMapper om = new ObjectMapper();
                        BetterResponseModel<CreatePaymentIntentResponse> responseModel =
                                om.readValue(response.toString(), new TypeReference<BetterResponseModel<CreatePaymentIntentResponse>>(){});
                        if (responseModel.getStatus().equals(Status.OK)){
                            clientSecret = responseModel.getDetails().getClientSecret();
                            publicKey = responseModel.getDetails().getPublicKey();
                        } else throw new Exception("Status not OK from server");

                        stripe = new Stripe(getApplicationContext(), publicKey);

                        stripe.confirmPayment(this,
                                ConfirmPaymentIntentParams.createWithPaymentMethodId(
                                        selectedPaymentMethod.id,
                                        clientSecret
                                )
                        );


                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                },
                error -> {
                    Toast.makeText(context, "ERROR", Toast.LENGTH_SHORT).show();

                }
            );








        this.finish();
    }




    @NonNull
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
        if (data != null) {
            paymentSession.handlePaymentData(requestCode, resultCode, data);
        }
    }
}
