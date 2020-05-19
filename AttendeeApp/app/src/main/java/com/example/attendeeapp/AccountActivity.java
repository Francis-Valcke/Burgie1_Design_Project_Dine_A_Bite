package com.example.attendeeapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.example.attendeeapp.appDatabase.OrderDatabaseService;
import com.example.attendeeapp.data.LoginDataSource;
import com.example.attendeeapp.data.LoginRepository;
import com.example.attendeeapp.data.model.LoggedInUser;
import com.example.attendeeapp.json.BetterResponseModel;
import com.example.attendeeapp.json.BetterResponseModel.GetBalanceResponse;
import com.example.attendeeapp.polling.OkHttpRequestTool;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.security.GeneralSecurityException;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import okhttp3.Request;

/**
 * Activity that handles the account user interface.
 */
public class AccountActivity extends ToolbarActivity {

    private static final String TAG = AccountActivity.class.getSimpleName();

    public static final int REQUEST_TOP_TUP = 0;

    private LoggedInUser user;
    private TextView balance;
    CompositeDisposable disposables;

    /**
     * Method to setup the activity.
     *
     * @param savedInstanceState The previously saved activity state, if available.
     */
    @SuppressLint("ApplySharedPref")
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
        TextView username = findViewById(R.id.username);
        username.setText(user.getDisplayName());

        balance = findViewById(R.id.balance);
        updateBalance();


        Button logOutButton = findViewById(R.id.button_log_out);
        logOutButton.setOnClickListener(v -> {
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
        });


        Button topUp = findViewById(R.id.button_top_up);
        topUp.setOnClickListener(button -> {
            Intent intent = new Intent(this, TopUpActivity.class);
            startActivityForResult(intent, REQUEST_TOP_TUP);
        });

    }

    /**
     * Method that requests and displays the user balance from the server.
     */
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
                                om.readValue(response, new TypeReference<BetterResponseModel<GetBalanceResponse>>() {
                                });

                        if (object.isOk()) {
                            balance.setText(String.valueOf(object.getPayload().getBalance()));
                        } else throw object.getException();

                    } catch (Throwable throwable) {
                        Toast.makeText(this, "Could not retrieve balance from the server.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "updateBalance: ", throwable);
                    }
                },
                throwable -> {
                    Toast.makeText(this, "Could not retrieve balance from the server.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "updateBalance: ", throwable);
                }
        ));

    }

    /**
     * Extends the toolbar option selection to exclude the account selection button.
     *
     * @param item The selected item in the toolbar menu.
     * @return If the click event should be consumed or forwarded.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (item.getItemId() == R.id.account_action) {
            // User chooses the "Account" item
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when the result from TopUpActivity is available to update the current user balance.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_TOP_TUP) {
            updateBalance();
        }

    }
}
