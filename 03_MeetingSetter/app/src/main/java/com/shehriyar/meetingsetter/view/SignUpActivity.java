package com.shehriyar.meetingsetter.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthProvider;
import com.google.firebase.auth.GoogleAuthProvider;
import com.shehriyar.meetingsetter.R;
import com.shehriyar.meetingsetter.databinding.ActivitySignUpBinding;
import com.shehriyar.meetingsetter.util.LoaderDialog;
import com.shehriyar.meetingsetter.util.UtilFunctions;
import com.shehriyar.meetingsetter.viewmodel.SignUpActivityViewModel;

public class SignUpActivity extends AppCompatActivity {

    private final int GOOGLE_SIGN_IN_REQUEST_CODE = 101;

    // Binding and viewmodel classes
    ActivitySignUpBinding binding;
    SignUpActivityViewModel viewModel;

    // LiveData objects
    LiveData<Boolean> isUserCreated, isUserWithCredCreated, isError;

    GoogleSignInClient googleSignInClient;
    CallbackManager fbCallbackManager;

    LoaderDialog signUpProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up);
        viewModel = ViewModelProviders.of(this).get(SignUpActivityViewModel.class);

        // Initialize Facebook signin
        FacebookSdk.sdkInitialize(getApplicationContext());
        fbCallbackManager = CallbackManager.Factory.create();
        binding.facebookSignUpBtn.setReadPermissions("email", "public_profile");

        // Initialize Google signin
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        signUpProgressDialog = new LoaderDialog(this, "SignUp");

        isUserCreated = viewModel.getIsUserCreated();
        isUserCreated.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isSuccess) {
                if (isSuccess) {
                    signUpProgressDialog.hideDialog();
                    Toast.makeText(SignUpActivity.this, "SignUp Success...", Toast.LENGTH_SHORT).show();

                    // UNCOMMENT THE BELOW PIECE OF CODE FOR FURTHER IMPLEMENTATION+
//                    isSuccess();
                }
            }
        });

        isUserWithCredCreated = viewModel.getIsUserWithCredCreated();
        isUserWithCredCreated.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isSuccess) {
                if (isSuccess) {
                    Toast.makeText(SignUpActivity.this, "SignUp Success...", Toast.LENGTH_SHORT).show();

                    // Open next activity from here
//                    startActivity(new Intent(SignUpActivity.this, * YOUR_ACTIVITY_GOES_HERE *.class));
                }
            }
        });

        isError = viewModel.getIsError();
        isError.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean error) {
                if(error){
                    Toast.makeText(SignUpActivity.this, "Some error occurred...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstName = binding.firstNameInp.getText().toString().trim();
                String lastName = binding.lastNameInp.getText().toString().trim();
                String email = binding.emailInp.getText().toString().trim();
                String password = binding.passInp.getText().toString().trim();

                boolean firstNameCheck = firstName.length() > 0;
                boolean lastNameCheck = lastName.length() > 0;
                boolean emailCheck = UtilFunctions.emailValidityCheck(email);
                String passwordCheckRes = UtilFunctions.passValidityCheck(password);
                boolean passwordCheck = passwordCheckRes.equals("valid");

                if (firstNameCheck && lastNameCheck && emailCheck && passwordCheck) {
                    signUpProgressDialog.showDialog();
                    viewModel.defaultSignUpRequest(email, password, firstName + " " + lastName);
                } else {
                    if (firstNameCheck) {
                        Toast.makeText(SignUpActivity.this, "Invalid First Name...", Toast.LENGTH_SHORT).show();
                    } else if (lastNameCheck) {
                        Toast.makeText(SignUpActivity.this, "Invalid Last Name...", Toast.LENGTH_SHORT).show();
                    } else if (!emailCheck) {
                        Toast.makeText(SignUpActivity.this, "Invalid Email...", Toast.LENGTH_SHORT).show();
                    } else if (!passwordCheck) {
                        Toast.makeText(SignUpActivity.this, passwordCheckRes, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        binding.googleSignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, GOOGLE_SIGN_IN_REQUEST_CODE);
            }
        });

        binding.facebookSignUpBtn.registerCallback(fbCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AuthCredential credential = FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken());
                viewModel.signInWithCredRequest(credential);
            }

            @Override
            public void onCancel() {
                Toast.makeText(SignUpActivity.this, "Facebook SignUp Cancelled...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(SignUpActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.resetLiveObjects();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_SIGN_IN_REQUEST_CODE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                viewModel.signInWithCredRequest(credential);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(SignUpActivity.this, "Google sign in failed", Toast.LENGTH_SHORT).show();
                // ...
            }
        } else {
            fbCallbackManager.onActivityResult(requestCode, resultCode, data);
        }

    }

    private void isSuccess() {
        String email = binding.emailInp.getText().toString().trim();
        String password = binding.passInp.getText().toString().trim();

        SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", email);
        editor.putString("password", password);
        editor.commit();

        // Open next activity from here
//        startActivity(new Intent(SignUpActivity.this, *YOUR_ACTIVITY_GOES_HERE*.class));
    }
}
