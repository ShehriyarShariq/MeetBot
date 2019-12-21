package com.shehriyar.meetingsetter.util;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.shehriyar.meetingsetter.R;

public class LoaderDialog {

    /*
        MULTI-PURPOSE MESSAGE DIALOG BOX
    */

    Activity activity;
    Dialog dialog;
    String type; // Dialog type

    public LoaderDialog(Activity activity, String type){
        this.activity = activity;
        this.type = type;
    }

    public void showDialog(){
        dialog  = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.loader_layout);

        TextView label = dialog.findViewById(R.id.label);
        switch (type){
            case "Login": // Data Loader Dialog
                label.setText("Signing In...");
                break;
            case "SignUp": // Data Loader Dialog
                label.setText("Signing Up...");
                break;
        }

        dialog.show();
    }

    public void hideDialog(){
        dialog.dismiss();
    }

}
