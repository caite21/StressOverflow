package com.example.StressOverflow;

import com.example.StressOverflow.Tag.Tag;

import java.util.ArrayList;


/**
 * This class sets up global variables that can be used throughout the project
 */
public class AppGlobals {
    private static AppGlobals instance;
    private ArrayList<Tag> allTags;
    private String ownerName;

    /**
     * Initializes the global variables
     */
    private AppGlobals() {
        allTags = new ArrayList<>();
        ownerName = "testUser";
    }

    /**
     * gets the instance
     * @return AppGlobal instance
     */
    public static AppGlobals getInstance() {
        if (instance == null) {
            instance = new AppGlobals();
        }
        return instance;
    }

    /**
     * gets the arraylist of all the tags
     * @return an arrayList of all the tags
     */
    public ArrayList<Tag> getAllTags() {
        return allTags;
    }

    /**
     * sets the arrayList of all the tags
     * @param allTags arrayList of tags, is set when there is a change to the tag list
     */
    public void setAllTags(ArrayList<Tag> allTags) {
        this.allTags = allTags;
    }

    /**
     * gets the owner name
     * @return name of the owner
     */
    public String getOwnerName() {
        return ownerName;
    }

    /**
     * sets the owner name
     * @param ownerName is set when user logs in
     */
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
}

