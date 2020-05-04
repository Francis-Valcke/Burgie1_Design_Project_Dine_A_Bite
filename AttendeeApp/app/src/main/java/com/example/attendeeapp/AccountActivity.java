package com.example.attendeeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.example.attendeeapp.appDatabase.OrderDatabaseService;
import com.example.attendeeapp.data.LoginDataSource;
import com.example.attendeeapp.data.LoginRepository;
import com.example.attendeeapp.data.model.LoggedInUser;
import com.example.attendeeapp.json.BetterResponseModel;
import com.example.attendeeapp.json.BetterResponseModel.*;
import com.example.attendeeapp.polling.OkHttpRequestTool;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.android.PaymentSession;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Objects;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import okhttp3.Request;

public class AccountActivity extends ToolbarActivity {

    public static final int REQUEST_TOP_TUP = 0;

    LoggedInUser user;
    TextView username;
    TextView balance;
    private PaymentSession paymentSession;
    Button topUp;
    CompositeDisposable disposables;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        disposables = new CompositeDisposable();

        // Initialize the toolbar
        initToolbar();
        upButtonToolbar();

        // Show username of currently logged in user
        user = LoginRepository.getInstance(new LoginDataSource()).getLoggedInUser();
        username = findViewById(R.id.username);
        username.setText(user.getDisplayName());

        balance = findViewById(R.id.balance);
        updateBalance();


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


        Button topUp = findViewById(R.id.button_top_up);
        topUp.setOnClickListener(button -> {
            Intent intent = new Intent(this, TopUpActivity.class);
            startActivityForResult(intent, REQUEST_TOP_TUP);
        });

    }

    private void updateBalance() {

        String url = ServerConfig.AS_ADDRESS + "/user/balance";
        Request request = new Request.Builder()
                .url(url)
                .method("GET", null)
                .addHeader("Authorization", user.getAuthorizationToken())
                .build();

        disposables.add(OkHttpRequestTool.wrapRequest(request).subscribe(
                response -> {
                    try {
                        ObjectMapper om = new ObjectMapper();
                        BetterResponseModel<GetBalanceResponse> object =
                                om.readValue(response, new TypeReference<BetterResponseModel<GetBalanceResponse>>() {});

                        if (object.isOk()){
                            balance.setText(String.valueOf(object.getPayload().getBalance()));
                        } else {
                            // There was an error processing the request
                            System.out.println();
                        }
                    } catch (Throwable throwable) {
                        System.out.println();
                    }
                },
                throwable -> {
                    // There was an error executing the request
                    throw throwable;
                }
        ));

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (item.getItemId() == R.id.account_action) {
            // User chooses the "Account" item
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_TOP_TUP) {
            updateBalance();
        }

    }
}
