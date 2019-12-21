package com.shehriyar.meetingsetter.network;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignUpActivityNetworkRequestsSingleton {

    FirebaseAuth firebaseAuth;

    private MutableLiveData<Boolean> isUserCreated = new MutableLiveData<>();
    private MutableLiveData<Boolean> isUserWithCredCreated = new MutableLiveData<>();
    private MutableLiveData<Boolean> isError = new MutableLiveData<>();

    SignUpActivityNetworkRequestsSingleton(){
        firebaseAuth = FirebaseAuth.getInstance();
    }

    private static SignUpActivityNetworkRequestsSingleton holder = new SignUpActivityNetworkRequestsSingleton();
    public static SignUpActivityNetworkRequestsSingleton getInstance(){
        return holder;
    }

    public void defaultSignUp(String email, String password, final String name){
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build();

                    user.updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                setIsUserCreated(true);
                            } else {
                                firebaseAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        setIsError(true);
                                    }
                                });
                            }
                        }
                    });
                } else {
                    setIsError(true);
                }
            }
        });
    }

    public void signInWithCredRequest(AuthCredential credential){
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    setIsUserWithCredCreated(true);
                } else {
                    setIsUserWithCredCreated(false);
                }
            }
        });
    }

    public MutableLiveData<Boolean> getIsUserCreated() {
        return isUserCreated;
    }

    private void setIsUserCreated(boolean isUserCreated) {
        this.isUserCreated.postValue(isUserCreated);
    }

    public MutableLiveData<Boolean> getIsUserWithCredCreated() {
        return isUserWithCredCreated;
    }

    private void setIsUserWithCredCreated(Boolean isUserWithCredCreated) {
        this.isUserWithCredCreated.postValue(isUserWithCredCreated);
    }

    public MutableLiveData<Boolean> getIsError() {
        return isError;
    }

    public void setIsError(boolean isError) {
        this.isError.postValue(isError);
    }

    public void reset(){
        setIsUserCreated(false);
        setIsUserWithCredCreated(false);
        setIsError(false);
    }
}
