package com.shehriyar.experienceadvisor.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.shehriyar.experienceadvisor.R;
import com.shehriyar.experienceadvisor.databinding.ActivityExperienceBinding;
import com.shehriyar.experienceadvisor.model.ExpDay;
import com.shehriyar.experienceadvisor.model.ExpReview;
import com.shehriyar.experienceadvisor.model.Experience;
import com.shehriyar.experienceadvisor.model.ExperiencesSingleton;
import com.shehriyar.experienceadvisor.util.Constants;
import com.shehriyar.experienceadvisor.util.GmailSender;
import com.shehriyar.experienceadvisor.viewmodel.ExperienceExtraDetailsListAdapter;
import com.shehriyar.experienceadvisor.viewmodel.ExperienceItineraryListAdapter;
import com.shehriyar.experienceadvisor.viewmodel.ExperienceRatingsListAdapter;
import com.shehriyar.experienceadvisor.viewmodel.ExperienceReviewsListAdapter;

import java.util.ArrayList;

public class ExperienceActivity extends AppCompatActivity {

    ExperiencesSingleton experiencesSingleton = ExperiencesSingleton.getInstance();

    ActivityExperienceBinding binding;

    AlertDialog detailsDialog;

    Experience experience;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_experience);

        Intent intent = getIntent();
        int pos = intent.getIntExtra("pos", -1);
        if(pos == -1){
            finish();
        } else {
            experience = experiencesSingleton.getExperience(pos);
        }

        binding.expName.setText(experience.getName());
        binding.location.setText(experience.getLocation());
        binding.price.setText(Constants.PRICE_UNIT + experience.getPrice());

        if(experience.getPeopleLimit().equals("none")){
            binding.personLimit.setVisibility(View.GONE);
        } else {
            binding.personLimit.setText(experience.getPeopleLimit());
        }

        binding.duration.setText(experience.getDuration());

        if(experience.getTotalDist().equals("none")){
            binding.distance.setText("N/A");
        } else {
            binding.distance.setText(experience.getTotalDist());
        }

        binding.desc.setText(experience.getDesc());
        binding.aboutHeading.setText("About " + experience.getAboutPlace());
        binding.about.setText(experience.getAboutLocation());

        if(experience.getItinerary().size() > 0){
            binding.itinerary.setLayoutManager(new LinearLayoutManager(this));
            binding.itinerary.setAdapter(new ExperienceItineraryListAdapter(experience.getItinerary()));
        } else {
            binding.itinerary.setVisibility(View.GONE);
            binding.emptyItinerary.setVisibility(View.VISIBLE);
        }

        if(experience.getRatings().size() > 0 || experience.getReviews().size() > 0){
            binding.ratingsList.setLayoutManager(new LinearLayoutManager(this));
            binding.reviewsList.setLayoutManager(new LinearLayoutManager(this));
            binding.ratingsList.setAdapter(new ExperienceRatingsListAdapter(experience.getRatings()));
            binding.reviewsList.setAdapter(new ExperienceReviewsListAdapter(experience.getReviews()));
        } else {
            binding.ratingsList.setVisibility(View.GONE);
            binding.reviewsList.setVisibility(View.GONE);
            binding.emptyReviews.setVisibility(View.VISIBLE);
        }

        binding.attractionsList.setLayoutManager(new LinearLayoutManager(this));
        binding.equipmentsList.setLayoutManager(new LinearLayoutManager(this));
        binding.precautionsList.setLayoutManager(new LinearLayoutManager(this));

        binding.attractionsList.setAdapter(new ExperienceExtraDetailsListAdapter(experience.getAttractions()));
        binding.equipmentsList.setAdapter(new ExperienceExtraDetailsListAdapter(experience.getEquipment()));
        binding.precautionsList.setAdapter(new ExperienceExtraDetailsListAdapter(experience.getPrecautions()));

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(ExperienceActivity.this);
        builder.setTitle("Required Details");

        LinearLayout dialogInputLayout = new LinearLayout(this);
        dialogInputLayout.setOrientation(LinearLayout.VERTICAL);

        final EditText nameInp = new EditText(this);
        nameInp.setHint("Name");
        nameInp.setInputType(InputType.TYPE_CLASS_TEXT);
        nameInp.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));

        LinearLayout.LayoutParams nameInpParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        nameInpParam.setMargins(0, 30, 0, 0);
        nameInp.setLayoutParams(nameInpParam);

        dialogInputLayout.addView(nameInp);

        final EditText emailInp = new EditText(this);
        emailInp.setHint("Email");
        emailInp.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        emailInp.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));

        LinearLayout.LayoutParams emailInpParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        emailInpParam.setMargins(0, 30, 0, 0);
        emailInp.setLayoutParams(emailInpParam);

        dialogInputLayout.addView(emailInp);

        builder.setView(dialogInputLayout);
        builder.setPositiveButton("Go", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = nameInp.getText().toString().trim();
                String email = emailInp.getText().toString().trim();
                if(name.length() > 0 && emailValidityCheck(email)){
                    dialog.dismiss();

                    String message = "Name: " + name + ",\n" +
                                     "Email: " + email + ",\n" +
                                     "Experience Details: \n" +
                                     "\tTitle: " + experience.getName() + "\n" +
                                     "\tLocation: " + experience.getLocation() + "\n" +
                                     "\tPrice: " + Constants.PRICE_UNIT + experience.getPrice() + "\n" +
                                     "\tDescription: " + experience.getDesc() + "\n";

                    sendMessage("Roam Booking", message);
                } else {
                    Toast.makeText(ExperienceActivity.this, "Invalid Details...", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        detailsDialog = builder.create();


        binding.bookNowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detailsDialog.show();
                detailsDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.grayishBlue));
                detailsDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.grayishBlue));
            }
        });
    }

    private void sendMessage(final String subject, final String message) {
        final ProgressDialog dialog = new ProgressDialog(ExperienceActivity.this);
        dialog.setTitle("Sending Email");
        dialog.setMessage("Please wait...");
        dialog.show();
        Thread sender = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    GmailSender sender = new GmailSender(Constants.USER, Constants.PASSWORD);
                    sender.sendMail(subject,
                            message,
                            Constants.USER,
                            "talha_jav@rocketmail.com");
                    dialog.dismiss();
                } catch (Exception e) {
                    //Log.e("mylog", "Error: " + e.getMessage());
                }
            }
        });
        sender.start();
    }

    // Uses email regex to check for email input validity
    public static boolean emailValidityCheck(String email){
        return email.matches("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])");
    }
}
