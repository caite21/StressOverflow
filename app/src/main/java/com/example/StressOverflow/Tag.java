package com.example.StressOverflow;


import java.io.Serializable;

public class Tag implements Serializable {
    private String tagName;

    public Tag(String tagName){
        this.tagName = tagName;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }
}
