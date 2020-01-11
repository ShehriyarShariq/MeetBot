package com.shehriyar.experienceadvisor.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;
import com.shehriyar.experienceadvisor.R;
import com.shehriyar.experienceadvisor.databinding.ActivityHomeBinding;
import com.shehriyar.experienceadvisor.listeners.PopularExperiencesListOnClickListener;
import com.shehriyar.experienceadvisor.model.ExpDay;
import com.shehriyar.experienceadvisor.model.ExpReview;
import com.shehriyar.experienceadvisor.model.Experience;
import com.shehriyar.experienceadvisor.model.ExperiencesSingleton;
import com.shehriyar.experienceadvisor.util.Constants;
import com.shehriyar.experienceadvisor.viewmodel.PopularExperiencesListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class HomeActivity extends FragmentActivity implements OnMapReadyCallback {

    ExperiencesSingleton experiencesSingleton = ExperiencesSingleton.getInstance();

    ActivityHomeBinding binding;
    private GoogleMap mMap;

    ActionBarDrawerToggle drawerToggle;

    ArrayList<Experience> allExperiences;
    ArrayList<ArrayList<Experience>> expsGroupedByPrice;
    PopularExperiencesListAdapter popularExperiencesListAdapter;

    int currPriceGrp = 0;
    double priceDistribution[] = {5000, 10000, 25000, 50000};
    int priceToSliderMapping[];

    HashMap<String, Integer> markerIDToPosMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);

        LinearLayoutManager popularExperiencesLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        binding.popularExpsList.setLayoutManager(popularExperiencesLinearLayoutManager);

        drawerToggle = new ActionBarDrawerToggle(this, binding.navDrawerLayout, R.string.open, R.string.close);
        binding.navDrawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        priceToSliderMapping = new int[priceDistribution.length];
        priceToSliderMapping[0] = 0;
        priceToSliderMapping[priceToSliderMapping.length - 1] = 100;

        int totalGrps = priceDistribution.length - 1;
        int gapVal = (int) Math.ceil(100.0 / totalGrps);
        for(int i = 1; i < priceToSliderMapping.length - 1; i++){
            priceToSliderMapping[i] = priceToSliderMapping[i - 1] + gapVal;
        }

        allExperiences = new ArrayList<>();
        markerIDToPosMap = new HashMap<>();
        try {
            JSONObject experiences = new JSONObject(loadJSONFromAsset());
            Iterator<String> keys = experiences.keys();
            int count = 0;
            while (keys.hasNext()){
                String expID = keys.next();
                JSONObject experience = experiences.getJSONObject(expID);

                JSONObject about = experience.getJSONObject("about");

                String cordStr[] = (experience.getString("cord")).split(",");
                LatLng cord = new LatLng(Double.parseDouble(cordStr[0]), Double.parseDouble(cordStr[1]));

                JSONArray itineraryJSONArr = experience.getJSONArray("itinerary");
                JSONArray attractionsJSONArr = experience.getJSONArray("attractions");
                JSONArray equipmentJSONArr = experience.getJSONArray("equipment");
                JSONArray precautionsJSONArr = experience.getJSONArray("precautions");
                JSONArray ratingsJSONArr = experience.getJSONArray("ratings");
                JSONArray reviewsJSONArr = experience.getJSONArray("reviews");

                ArrayList<ExpDay> itinerary = new ArrayList<>();
                ArrayList<String> attraction = new ArrayList<>();
                ArrayList<String> equipment = new ArrayList<>();
                ArrayList<String> precautions = new ArrayList<>();
                ArrayList<ExpReview> ratings = new ArrayList<>();
                ArrayList<ExpReview> reviews = new ArrayList<>();

                for(int i = 0; i < itineraryJSONArr.length(); i++){
                    HashMap<String, String> day = (HashMap<String, String>) itineraryJSONArr.get(i);
                    itinerary.add(new ExpDay(
                            day.get("location"),
                            day.get("imgURL"),
                            day.get("desc")
                    ));
                }

                for(int i = 0; i < attractionsJSONArr.length(); i++){ attraction.add(attractionsJSONArr.getString(i)); }
                for(int i = 0; i < equipmentJSONArr.length(); i++){ equipment.add(equipmentJSONArr.getString(i)); }
                for(int i = 0; i < precautionsJSONArr.length(); i++){ precautions.add(precautionsJSONArr.getString(i)); }

                for(int i = 0; i < ratingsJSONArr.length(); i++){
                    HashMap<String, String> rating = (HashMap<String, String>) ratingsJSONArr.get(i);
                    ratings.add(new ExpReview(
                            "",
                            rating.get("name"),
                            rating.get("message"),
                            Float.parseFloat(rating.get("rating"))
                    ));
                }

                for(int i = 0; i < reviewsJSONArr.length(); i++){
                    HashMap<String, String> review = (HashMap<String, String>) reviewsJSONArr.get(i);
                    reviews.add(new ExpReview(
                            review.get("imgURL"),
                            review.get("name"),
                            review.get("message"),
                            Float.parseFloat(review.get("rating"))
                    ));
                }


                allExperiences.add(new Experience(
                        "" + count,
                        experience.getString("name"),
                        experience.getString("location"),
                        Double.parseDouble(experience.getString("price")),
                        experience.getString("peopleLimit"),
                        experience.getString("duration"),
                        experience.getString("totalDist"),
                        experience.getString("desc"),
                        about.getString("place"),
                        about.getString("desc"),
                        experience.getString("type"),
                        cord,
                        itinerary,
                        ratings,
                        reviews,
                        attraction,
                        equipment,
                        precautions
                ));
                count++;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            experiencesSingleton.setExperiences(allExperiences);

            expsGroupedByPrice = new ArrayList<>();
            for(int i = 0; i < priceDistribution.length; i++){
                expsGroupedByPrice.add(new ArrayList<Experience>());
            }

            for(Experience experience : allExperiences){
                addToExpPriceGroup(experience);
            }

            if(mMap != null){
                showGroupMarkers(0);
            }
        }

        popularExperiencesListAdapter = new PopularExperiencesListAdapter(allExperiences, new PopularExperiencesListOnClickListener() {
            @Override
            public void OnItemClicked(int pos) {
                Intent expIntent = new Intent(HomeActivity.this, ExperienceActivity.class);
                expIntent.putExtra("pos", pos);
                startActivity(expIntent);
            }
        });
        binding.popularExpsList.setAdapter(popularExperiencesListAdapter);


        binding.searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.defaultLayout.setVisibility(View.GONE);
                binding.searchLayout.setVisibility(View.VISIBLE);

                int statusBarHeight = 0;
                int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
                if (resourceId > 0) {
                    statusBarHeight = getResources().getDimensionPixelSize(resourceId);
                }

                ConstraintLayout.LayoutParams searchBarParams = (ConstraintLayout.LayoutParams) binding.searchBar.getLayoutParams();
                searchBarParams.setMargins(0, statusBarHeight, 0, 0);
                binding.searchBar.setLayoutParams(searchBarParams);

                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
        });

        binding.navigationDrawer.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){

                }

                return false;
            }
        });

        binding.menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.navDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.navDrawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    binding.navDrawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng defaultLoc = new LatLng(36.316700, 74.650000);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(defaultLoc));
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
        mMap.animateCamera(CameraUpdateFactory.zoomTo(8), 2000, null);

        showGroupMarkers(currPriceGrp);

        binding.priceSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int group = getGroup(progress);
                if(group != currPriceGrp){
                    currPriceGrp = group;
                    showGroupMarkers(currPriceGrp);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Intent expIntent = new Intent(HomeActivity.this, ExperienceActivity.class);
                expIntent.putExtra("pos", (int)markerIDToPosMap.get(marker.getId()));
                startActivity(expIntent);
                return false;
            }
        });
    }

    private void showGroupMarkers(int grp){
        mMap.clear();
        markerIDToPosMap.clear();
        ArrayList<Experience> allPossibleExperiences = new ArrayList<>();

        for(int i = 0; i <= grp; i++){
            allPossibleExperiences.addAll(expsGroupedByPrice.get(i));
        }

        for(Experience exp : allPossibleExperiences){
            MarkerOptions markerOptions = new MarkerOptions().position(exp.getCord());

            String expType = exp.getType();
            if(expType.equals(Constants.EXP_TYPE_TREK)){
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.trekking_marker01));
            } else if(expType.equals(Constants.EXP_TYPE_SWIM)){
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.water_marker01));
            } else if(expType.equals(Constants.EXP_TYPE_VIEW)){
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.viewpoint_marker01));
            }

            Marker expMarker = mMap.addMarker(markerOptions);
            markerIDToPosMap.put(expMarker.getId(), Integer.parseInt(exp.getId()));
        }
    }

    private int getGroup(int progress){
        for(int i = 1, grp = 0; i < priceToSliderMapping.length; i++, grp++){
            if((progress >= priceToSliderMapping[i - 1]) && (progress < priceToSliderMapping[i])){
                return grp;
            } else if(progress == 0){
                return 0;
            } else if(progress == 100){
                return priceToSliderMapping.length - 1;
            }
        }
        return -1;
    }

    private void addToExpPriceGroup(Experience exp){
        for(int i = 1, grp = 0; i < priceDistribution.length; i++, grp++){
            if((exp.getPrice() >= priceDistribution[i - 1]) && (exp.getPrice() < priceDistribution[i])){
                ArrayList<Experience> selectedGrp = expsGroupedByPrice.get(grp);
                selectedGrp.add(exp);
                expsGroupedByPrice.set(grp, selectedGrp);

                break;
            } else if((i == priceDistribution.length - 1) && (exp.getPrice() == priceDistribution[i])){
                ArrayList<Experience> selectedGrp = expsGroupedByPrice.get(grp + 1);
                selectedGrp.add(exp);
                expsGroupedByPrice.set(grp + 1, selectedGrp);

                break;
            }
        }
    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getResources().getAssets().open("data.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    @Override
    public void onBackPressed() {
        if(binding.searchLayout.getVisibility() == View.VISIBLE){
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            binding.searchLayout.setVisibility(View.GONE);
            binding.defaultLayout.setVisibility(View.VISIBLE);
        }
    }
}
