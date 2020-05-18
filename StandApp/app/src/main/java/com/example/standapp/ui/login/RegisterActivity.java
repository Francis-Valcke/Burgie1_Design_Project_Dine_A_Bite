package com.example.standapp.ui.login;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.example.standapp.R;
import com.google.android.material.snackbar.Snackbar;
import com.example.standapp.data.LoginDataSource;
import com.example.standapp.data.LoginRepository;
import com.example.standapp.data.model.LoggedInUser;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class RegisterActivity extends AppCompatActivity {

    // TODO Do something with the received email

    private LoginViewModel loginViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final Context mContext = this;

        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final EditText emailEditText = findViewById(R.id.email);
        final Button registerButton = findViewById(R.id.register);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                registerButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
                if (loginFormState.getEmailError() != null) {
                    emailEditText.setError(getString(loginFormState.getEmailError()));
                }
            }
        });

        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }
                if (loginResult.getSuccess() != null) {
                    updateUiWithUser(loginResult.getSuccess());
                    setResult(Activity.RESULT_OK);

                    LoggedInUser user = LoginRepository.getInstance(new LoginDataSource())
                            .getLoggedInUser();

                    // Save credentials of LoggedInUser
                    try {
                        String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
                        SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                                getString(R.string.shared_pref_file_key),
                                masterKeyAlias,
                                mContext,
                                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                        );
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("username", user.getDisplayName());
                        editor.putString("user_id", user.getUserId()); // token
                        editor.apply();
                    } catch (GeneralSecurityException | IOException e) {
                        e.printStackTrace();
                    }

                    // Complete and destroy login activity once successful
                    finish();
                }
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.registerDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString(), emailEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        emailEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.register(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString(),
                            emailEditText.getText().toString());
                }
                return false;
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);

                loginViewModel.register(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString(), emailEditText.getText().toString());
            }
        });

    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + " " + model.getDisplayName();
        // initiate successful logged in experience
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Snackbar.make(findViewById(R.id.register), errorString, Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onBackPressed() {
        // Do nothing (Android back button is disabled for this activity)
    }
}
