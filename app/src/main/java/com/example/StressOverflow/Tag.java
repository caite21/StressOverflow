package com.example.StressOverflow;


import static android.content.ContentValues.TAG;

import android.os.Parcelable;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Tag implements Serializable {
    private String tagName;

    /**
     * Constructor for tag, sets the tag name
     * @param tagName
     */
    public Tag(String tagName){
        this.tagName = tagName;
    }

    /**
     * gets the current tag name
     * @return name of the tag
     */
    public String getTagName() {
        return tagName;
    }

    /**
     * Updates the tagName when editing tag
     * Not implemented yet
     * @param tagName new tag name
     */
    public void setTagName(String tagName) {
        this.tagName = tagName;
    }
    public HashMap<String, Object> toFirebaseObject(String ownerName) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("tagName", this.tagName);
        data.put("ownerName", ownerName);
        return data;
    }

    public static Tag fromFirebaseObject(Map<String, Object> data) {
        try {
            @SuppressWarnings("unchecked") // just trust me bro
            Tag out = new Tag(
                    (String) data.get("tagName")
            );
            return out;
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "Creation of item from Firebase hashmap went wrong: ", e);
        }
        return null;
    }
}
