package com.example.standapp.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.util.Patterns;

import com.example.standapp.data.LoginRepository;
import com.example.standapp.data.Result;
import com.example.standapp.data.model.LoggedInUser;
import com.example.standapp.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginViewModel extends ViewModel {

    private MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private LoginRepository loginRepository;

    LoginViewModel(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public void login(final String username, final String password) {
        // (Google says) can be launched in a separate asynchronous job

        // Handle logging in and authentication of user to server in separate thread
        // because main thread is UI thread
        // In the mean time, the progress bar will keep on loading
        // until there is a LoginResult
        new android.os.Handler().post(new Runnable() {
            @Override
            public void run() {
                Result<LoggedInUser> result = loginRepository.login(username, password);

                if (result instanceof Result.Success) {
                    LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
                    loginResult.setValue(new LoginResult(new LoggedInUserView(data.getDisplayName())));
                } else {
                    loginResult.setValue(new LoginResult(R.string.login_failed));
                }
            }
        });
    }

    public void register(final String username, final String password, final String email) {
        // (Google says) can be launched in a separate asynchronous job

        // Handle registering and authentication of user to server in separate thread
        // because main thread is UI thread
        // In the mean time, the progress bar will keep on loading
        // until there is a LoginResult
        new android.os.Handler().post(new Runnable() {
            @Override
            public void run() {
                Result<LoggedInUser> result = loginRepository.register(username, password, email);

                if (result instanceof Result.Success) {
                    LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
                    loginResult.setValue(new LoginResult(new LoggedInUserView(data.getDisplayName())));
                } else {
                    loginResult.setValue(new LoginResult(R.string.login_failed));
                }
            }
        });
    }

    void loginDataChanged(String username, String password) {
        if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    void registerDataChanged(String username, String password, String email) {
        if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null,
                    null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password,
                    null));
        } else if (!isEmailValid(email)) {
            loginFormState.setValue(new LoginFormState(null, null,
                    R.string.invalid_email));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    // A placeholder username validation check

    /**
     * Method for checking valid username format
     *
     * @param username username of user
     * @return boolean true for valid, false for invalid username format
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
        if (username.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        } else {
            return !username.trim().isEmpty();
        }
    }

    // A placeholder password validation check

    /**
     * Method for checking valid password format
     *
     * @param password password chosen by user
     * @return boolean true for valid, false for invalid password format
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }

    // A placeholder for email validation check
    /**
     * Method for checking valid email format
     *
     * @param email email from user
     * @return boolean true for valid, false for invalid email format
     */
    @SuppressWarnings("RegExpRedundantEscape")
    private boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

}
