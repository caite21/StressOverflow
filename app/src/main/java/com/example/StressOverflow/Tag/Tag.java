package com.example.StressOverflow.Tag;


import static android.content.ContentValues.TAG;

import android.util.Log;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

    /**
     * formats data to a firebaseObject
     * @param ownerName name of the owner that's logged in
     * @return formatted data
     */
    public HashMap<String, Object> toFirebaseObject(String ownerName) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("tagName", this.tagName);
        data.put("ownerName", ownerName);
        return data;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Tag otherTag = (Tag) obj;
        return Objects.equals(tagName, otherTag.tagName);
    }

    /**
     * format data from firebaseObject to a tag
     * @param data data that's passed in from firebase
     * @return new Tag
     */

    public static Tag fromFirebaseObject(Map<String, Object> data) {
        try {
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
