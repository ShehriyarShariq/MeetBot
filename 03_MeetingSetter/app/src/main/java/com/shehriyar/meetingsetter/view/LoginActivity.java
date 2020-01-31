package com.shehriyar.meetingsetter.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Toast;

import com.shehriyar.meetingsetter.R;
import com.shehriyar.meetingsetter.databinding.ActivityLoginBinding;
import com.shehriyar.meetingsetter.util.LoaderDialog;
import com.shehriyar.meetingsetter.util.UtilFunctions;
import com.shehriyar.meetingsetter.viewmodel.LoginActivityViewModel;

public class LoginActivity extends AppCompatActivity {

    // Binding and Viewmodel classes
    ActivityLoginBinding binding;
    LoginActivityViewModel viewModel;

    // LiveData objects
    LiveData<Boolean> isLoginSuccess, isError;

    // Utilities
    LoaderDialog logInProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        viewModel = ViewModelProviders.of(this).get(LoginActivityViewModel.class);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        float cardViewRadius = (float) (((width * 0.78) * 0.162) / 2);

        binding.emailInpLayout.setRadius(cardViewRadius);
        binding.passwordInpLayout.setRadius(cardViewRadius);
        binding.loginBtn.setRadius(cardViewRadius);
        binding.registerBtn.setRadius(cardViewRadius);

        double appIconWidth = width * 0.378;
        double appIconHeight = appIconWidth * 0.69;
        double appIconDiagonalLength = Math.sqrt(Math.pow(appIconHeight, 2) + Math.pow(appIconWidth, 2));
        double appIconStrikeAngle = 90 - Math.toDegrees(Math.atan(appIconWidth / appIconHeight));

        ConstraintLayout.LayoutParams strike01LayoutParams = (ConstraintLayout.LayoutParams) binding.strike01.getLayoutParams();
        strike01LayoutParams.width = (int) appIconDiagonalLength;
        binding.strike01.setLayoutParams(strike01LayoutParams);

        binding.strike01.setRotation((float) appIconStrikeAngle);

        ConstraintLayout.LayoutParams strike02LayoutParams = (ConstraintLayout.LayoutParams) binding.strike02.getLayoutParams();
        strike02LayoutParams.width = (int) appIconDiagonalLength;
        binding.strike02.setLayoutParams(strike02LayoutParams);

//        binding.strike02.setPivotY(0);
//        binding.strike02.setPivotX((float) appIconHeight);

        binding.strike02.setRotation((float) -appIconStrikeAngle);


        // Initialize Dialog
        logInProgressDialog = new LoaderDialog(this, "Login");

        // Observables
        isLoginSuccess = viewModel.getIsLoginSuccess();
        isLoginSuccess.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isSuccess) {
                if(isSuccess){
                    logInProgressDialog.hideDialog();
                    Toast.makeText(LoginActivity.this, "Login Success...", Toast.LENGTH_SHORT).show();

                    // UNCOMMENT THE BELOW PIECE OF CODE FOR FURTHER IMPLEMENTATION
                    isSuccess();
                }
            }
        });

        isError = viewModel.getIsError();
        isError.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isError) {
                if(isError){
                    logInProgressDialog.hideDialog();
                    Toast.makeText(LoginActivity.this, "Invalid credentials...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = binding.emailInp.getText().toString().trim();
                String password = binding.passInp.getText().toString().trim();

                // Checks for credentials validity
                boolean emailCheck = UtilFunctions.emailValidityCheck(email);
                String passwordCheckRes = UtilFunctions.passValidityCheck(password);
                boolean passwordCheck = passwordCheckRes.equals("valid");

                if(emailCheck && passwordCheck){
                    logInProgressDialog.showDialog();
                    viewModel.loginUserRequest(email, password);
                } else {
                    if(!emailCheck){
                        Toast.makeText(LoginActivity.this, "Invalid Email...", Toast.LENGTH_SHORT).show();
                    } else if(!passwordCheck){
                        Toast.makeText(LoginActivity.this, passwordCheckRes, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // Goto SignUp activity
        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.resetLiveObjects();
    }

    private void isSuccess(){
        // Save email and password in SharedPreferences
        String email = binding.emailInp.getText().toString().trim();
        String password = binding.passInp.getText().toString().trim();

        SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", email);
        editor.putString("password", password);
        editor.commit();

        // Open next activity from here
        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
    }
}
