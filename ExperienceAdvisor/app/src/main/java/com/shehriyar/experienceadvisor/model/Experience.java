package com.shehriyar.experienceadvisor.model;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class Experience {

    private String id, name, location, peopleLimit, duration, totalDist, desc, aboutPlace, aboutLocation, type;
    private double price;
    private LatLng cord;
    private ArrayList<ExpDay> itinerary;
    private ArrayList<ExpReview> ratings, reviews;
    private ArrayList<String> attractions, equipment, precautions;
    private String thumbnailUrl;

    public Experience(String id, String thumbnailUrl, double price, String type, LatLng cord) {
        this.id = id;
        this.thumbnailUrl = thumbnailUrl;
        this.price = price;
        this.type = type;
        this.cord = cord;
    }

    public Experience(String id, String name, String location, double price, String peopleLimit, String duration, String totalDist, String desc, String aboutPlace, String aboutLocation, String type, LatLng cord, ArrayList<ExpDay> itinerary, ArrayList<ExpReview> ratings, ArrayList<ExpReview> reviews, ArrayList<String> attractions, ArrayList<String> equipment, ArrayList<String> precautions) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.price = price;
        this.peopleLimit = peopleLimit;
        this.duration = duration;
        this.totalDist = totalDist;
        this.desc = desc;
        this.aboutPlace = aboutPlace;
        this.aboutLocation = aboutLocation;
        this.type = type;
        this.cord = cord;
        this.itinerary = itinerary;
        this.ratings = ratings;
        this.reviews = reviews;
        this.attractions = attractions;
        this.equipment = equipment;
        this.precautions = precautions;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public double getPrice() {
        return price;
    }

    public String getPeopleLimit() {
        return peopleLimit;
    }

    public String getDuration() {
        return duration;
    }

    public String getTotalDist() {
        return totalDist;
    }

    public String getDesc() {
        return desc;
    }

    public String getType() {
        return type;
    }

    public LatLng getCord() {
        return cord;
    }

    public ArrayList<ExpDay> getItinerary() {
        return itinerary;
    }

    public ArrayList<ExpReview> getReviews() {
        return reviews;
    }

    public String getAboutPlace() {
        return aboutPlace;
    }

    public String getAboutLocation() {
        return aboutLocation;
    }

    public ArrayList<ExpReview> getRatings() {
        return ratings;
    }

    public ArrayList<String> getAttractions() {
        return attractions;
    }

    public ArrayList<String> getEquipment() {
        return equipment;
    }

    public ArrayList<String> getPrecautions() {
        return precautions;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
}
