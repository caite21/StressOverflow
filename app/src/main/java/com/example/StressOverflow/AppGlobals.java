package com.example.StressOverflow;

import java.util.ArrayList;

public class AppGlobals {
    private static AppGlobals instance;
    private ArrayList<Tag> allTags;
    private String ownerName;

    private AppGlobals() {
        // Initialize your global variables here
        allTags = new ArrayList<>();
        ownerName = "";
    }

    public static AppGlobals getInstance() {
        if (instance == null) {
            instance = new AppGlobals();
        }
        return instance;
    }

    public ArrayList<Tag> getAllTags() {
        return allTags;
    }

    public void setAllTags(ArrayList<Tag> allTags) {
        this.allTags = allTags;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
}

