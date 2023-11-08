package com.example.StressOverflow;


import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;

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
    public HashMap<String, Object> toFirebaseObject() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("tagName", this.tagName);

        return data;
    }
}
