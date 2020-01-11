package com.shehriyar.meetingsetter.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.AuthCredential;
import com.shehriyar.meetingsetter.network.SignUpActivityNetworkRequestsSingleton;

public class SignUpActivityViewModel extends AndroidViewModel {

    SignUpActivityNetworkRequestsSingleton signUpActivityNetworkRequestsSingleton = SignUpActivityNetworkRequestsSingleton.getInstance();

    private MutableLiveData<Boolean> isUserCreated = signUpActivityNetworkRequestsSingleton.getIsUserCreated();
    private MutableLiveData<Boolean> isUserWithCredCreated = signUpActivityNetworkRequestsSingleton.getIsUserWithCredCreated();
    private MutableLiveData<Boolean> isUserAddedToDB = signUpActivityNetworkRequestsSingleton.getIsUserAddedToDB();
    private MutableLiveData<Boolean> isError = signUpActivityNetworkRequestsSingleton.getIsError();

    public SignUpActivityViewModel(@NonNull Application application) {
        super(application);
    }

    public void defaultSignUpRequest(String email, String password, String name){
        signUpActivityNetworkRequestsSingleton.defaultSignUp(email, password, name);
    }

    public void signInWithCredRequest(AuthCredential credential){
        signUpActivityNetworkRequestsSingleton.signInWithCredRequest(credential);
    }

    public void addUserToDB(){
        signUpActivityNetworkRequestsSingleton.addUserToDB();
    }

    public MutableLiveData<Boolean> getIsUserCreated() {
        return isUserCreated;
    }

    public MutableLiveData<Boolean> getIsUserWithCredCreated() {
        return isUserWithCredCreated;
    }

    public MutableLiveData<Boolean> getIsUserAddedToDB() {
        return isUserAddedToDB;
    }

    public MutableLiveData<Boolean> getIsError() {
        return isError;
    }

    public void resetLiveObjects(){
        signUpActivityNetworkRequestsSingleton.reset();
    }
}
