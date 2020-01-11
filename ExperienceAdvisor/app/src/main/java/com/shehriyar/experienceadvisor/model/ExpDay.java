package com.shehriyar.experienceadvisor.model;

public class ExpDay {

    private String imgURL, desc, location;

    public ExpDay(String location, String imgURL, String desc) {
        this.imgURL = imgURL;
        this.desc = desc;
        this.location = location;
    }

    public String getImgURL() {
        return imgURL;
    }

    public String getDesc() {
        return desc;
    }

    public String getLocation() {
        return location;
    }
}
