package com.shehriyar.meetingsetter.network;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivityNetworkRequestsSingleton {

    private MutableLiveData<Boolean> isLoginSuccess = new MutableLiveData<>();
    private MutableLiveData<Boolean> isError = new MutableLiveData<>();

    FirebaseAuth firebaseAuth;

    LoginActivityNetworkRequestsSingleton(){
        firebaseAuth = FirebaseAuth.getInstance();
    }

    private static LoginActivityNetworkRequestsSingleton holder = new LoginActivityNetworkRequestsSingleton();
    public static LoginActivityNetworkRequestsSingleton getInstance(){
        return holder;
    }

    public void loginUser(String email, String password){
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    setIsLoginSuccess(true);
                } else {
                    setIsError(true);
                }
            }
        });
    }

    public MutableLiveData<Boolean> getIsLoginSuccess() {
        return isLoginSuccess;
    }

    private void setIsLoginSuccess(boolean isLoginSuccess) {
        this.isLoginSuccess.postValue(isLoginSuccess);
    }

    public MutableLiveData<Boolean> getIsError() {
        return isError;
    }

    private void setIsError(boolean isError) {
        this.isError.postValue(isError);
    }

    public void reset() {
        setIsLoginSuccess(false);
        setIsError(false);
    }
}
