package com.example.attendeeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.example.attendeeapp.appDatabase.OrderDatabaseService;
import com.example.attendeeapp.data.LoginDataSource;
import com.example.attendeeapp.data.LoginRepository;
import com.example.attendeeapp.data.model.LoggedInUser;
import com.example.attendeeapp.polling.RequestQueueSingleton;
import com.example.attendeeapp.stripe.DineabiteEphemeralKeyProvider;
import com.stripe.android.CustomerSession;
import com.stripe.android.PaymentSession;
import com.stripe.android.PaymentSessionConfig;
import com.stripe.android.PaymentSessionData;
import com.stripe.android.Stripe;
import com.stripe.android.model.Address;
import com.stripe.android.model.PaymentMethod;
import com.stripe.android.view.ShippingInfoWidget;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.HashSet;

public class AccountActivity extends AppCompatActivity {

    TextView username;
    private PaymentSession paymentSession;
    Button topUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        // Custom Toolbar (instead of standard actionbar)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);

        ab.setTitle("Account");

        // Show username of currently logged in user
        LoggedInUser user = LoginRepository.getInstance(new LoginDataSource()).getLoggedInUser();
        username = findViewById(R.id.username);
        username.setText(user.getDisplayName());

        Button logOutButton = findViewById(R.id.button_log_out);
        logOutButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Delete persistent data from the currently logged in user
                // (clear db of all entries)
                OrderDatabaseService orderDatabaseService =
                        new OrderDatabaseService(getApplicationContext());
                orderDatabaseService.deleteAllOrders();

                // Clear Shared Preference file, this will reset the logged in user
                try {
                    String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
                    SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                            getString(R.string.shared_pref_file_key),
                            masterKeyAlias,
                            getApplicationContext(),
                            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                    );
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    // Ignore warning: needs to be commit to be synchronous
                    editor.commit();
                } catch (GeneralSecurityException | IOException e) {
                    e.printStackTrace();
                }

                LoginRepository.getInstance(new LoginDataSource()).logout();

                // Start MainActivity again
                Intent intent = new Intent(AccountActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

        });

        // Initialize Stipe payment
        CustomerSession.initCustomerSession(this, new DineabiteEphemeralKeyProvider());
        paymentSession = new PaymentSession(this, createPaymentSessionConfig());
        setupPaymentSession();



        Button topUp = findViewById(R.id.button_top_up);
        topUp.setOnClickListener(button -> {
            paymentSession.presentPaymentMethodSelection(null);
        });


        Button paymentSetup = findViewById(R.id.button_payment_setup);
        paymentSetup.setOnClickListener(button -> {
            paymentSession.presentShippingFlow();
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This takes the user 'back', as if they pressed the left-facing triangle icon
                // on the main android toolbar.
                onBackPressed();
                return true;
            case R.id.orders_action:
                // User chooses the "My Orders" item
                Intent intent = new Intent(AccountActivity.this, OrderActivity.class);
                startActivity(intent);
                return true;
            case R.id.account_action:
                // User chooses the "Account" item
                Intent intent2 = new Intent(AccountActivity.this, AccountActivity.class);
                startActivity(intent2);
                return true;
            case R.id.settings_action:
                // User chooses the "Settings" item
                // TODO make settings activity
                return true;
            case R.id.map_action:
                //User chooses the "Map" item
                Intent mapIntent = new Intent(AccountActivity.this, MapsActivity.class);
                startActivity(mapIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @NonNull
    private PaymentSessionConfig createPaymentSessionConfig() {
        return new PaymentSessionConfig.Builder()

                // hide the phone field on the shipping information form
                .setHiddenShippingInfoFields(
                        ShippingInfoWidget.CustomizableShippingField.PHONE_FIELD
                )

                // make the address line 2 field optional
                .setOptionalShippingInfoFields(
                        ShippingInfoWidget.CustomizableShippingField.ADDRESS_LINE_TWO_FIELD
                )

                // collect shipping information
                .setShippingInfoRequired(false)

                // collect shipping method
                .setShippingMethodsRequired(false)

                // specify the payment method types that the customer can use;
                // defaults to PaymentMethod.Type.Card
                .setPaymentMethodTypes(
                        Arrays.asList(PaymentMethod.Type.Card)
                )

                // if `true`, will show "Google Pay" as an option on the
                // Payment Methods selection screen
                .setShouldShowGooglePay(true)

                .build();
    }

    private void setupPaymentSession() {
        paymentSession.init(
                new PaymentSession.PaymentSessionListener() {
                    @Override
                    public void onPaymentSessionDataChanged(@NotNull PaymentSessionData data) {
                        if (data.getUseGooglePay()) {
                            // customer intends to pay with Google Pay
                        } else {
                            final PaymentMethod paymentMethod = data.getPaymentMethod();
                            if (paymentMethod != null) {
                                // Display information about the selected payment method
                            }
                        }

                        // Update your UI here with other data
                        if (data.isPaymentReadyToCharge()) {
                            // Use the data to complete your charge - see below.
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
