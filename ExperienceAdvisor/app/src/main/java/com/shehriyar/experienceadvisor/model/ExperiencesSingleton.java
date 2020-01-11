package com.shehriyar.experienceadvisor.model;

import java.util.ArrayList;

public class ExperiencesSingleton {

    private ArrayList<Experience> experiences;

    public ExperiencesSingleton() {
        experiences = new ArrayList<>();
    }

    private static ExperiencesSingleton holder = new ExperiencesSingleton();
    public static ExperiencesSingleton getInstance(){
        return holder;
    }

    public ArrayList<Experience> getExperiences() {
        return experiences;
    }

    public Experience getExperience(int pos){
        return experiences.get(pos);
    }

    public void setExperiences(ArrayList<Experience> experiences) {
        this.experiences = experiences;
    }
}
