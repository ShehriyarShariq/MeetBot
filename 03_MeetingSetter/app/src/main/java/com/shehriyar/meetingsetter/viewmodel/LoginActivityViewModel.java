package com.shehriyar.meetingsetter.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.shehriyar.meetingsetter.network.LoginActivityNetworkRequestsSingleton;

public class LoginActivityViewModel extends AndroidViewModel {

    LoginActivityNetworkRequestsSingleton loginActivityNetworkRequestsSingleton = LoginActivityNetworkRequestsSingleton.getInstance();

    private MutableLiveData<Boolean> isLoginSuccess = loginActivityNetworkRequestsSingleton.getIsLoginSuccess();
    private MutableLiveData<Boolean> isError = loginActivityNetworkRequestsSingleton.getIsError();

    public LoginActivityViewModel(@NonNull Application application) {
        super(application);
    }

    public void loginUserRequest(String email, String password){
        loginActivityNetworkRequestsSingleton.loginUser(email, password);
    }

    public MutableLiveData<Boolean> getIsLoginSuccess() {
        return isLoginSuccess;
    }

    public MutableLiveData<Boolean> getIsError() {
        return isError;
    }

    public void resetLiveObjects() {
        loginActivityNetworkRequestsSingleton.reset();
    }
}
