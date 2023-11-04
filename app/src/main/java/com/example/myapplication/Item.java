/**
 * Represents a single item
 * Should pictures be their own class, or is simply a link to a picture enough?
 */

package com.example.myapplication;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class Item {

    private String name;
    private String make;
    private String model;
    private String description;
    private Date date;
    private Double value;
    private String comments;
    private ArrayList<Tag> tags = new ArrayList<Tag>();
    private ArrayList<UUID> pictures = new ArrayList<UUID>();
    private Integer serial;

    public Item(
            String name,
            String make,
            String model,
            String description,
            Date date,
            Double value,
            String comments,
            ArrayList<Tag> tags,
            ArrayList<UUID> pictures,
            Integer serial
    ) {
        this.setName(name);
        this.setMake(make);
        this.setModel(model);
        this.setDescription(description);
        this.setDate(date);
        this.setValue(value);
        this.setComments(comments);
        this.addTags(tags);
        this.addPictures(pictures);
        this.setSerial(serial);
    }

    public void setName(String name) {
        if (name.equals("")) {
            throw new IllegalArgumentException("empty name not allowed");
        } else if (name.length() > Util.MAX_ITEM_NAME_LENGTH) {
            throw new IllegalArgumentException(String.format("name exceeds maximum name length (%d)", Util.MAX_ITEM_NAME_LENGTH));
        }
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getMake() {
        return this.make;
    }

    public String getModel() {
        return this.getModel();
    }

    public String getDescription() {
        return this.description;
    }

    /**
     * Most likely to be used on the list view (truncate the description if its too long)
     * TODO: BREAKS IF IT ENCOUNTERS A WORD WITH LENGTH GREATER THAN Util.MAX_LINE_LENGTH
     * @param brief whether or not to return first 2 lines of description
     * @return
     */
    public String getDescription(boolean brief) {
        if (brief) {
            StringBuilder out = new StringBuilder();
            String[] words = this.getDescription().split(" ");
            int line_length = 0;
            boolean second_line = false;
            for (String word : words) {
                out.append(word);
                out.append(" ");
                line_length += word.length() + 1;
                if (line_length > Util.MAX_LINE_LENGTH - (second_line ? 1 : 0) * 3) {
                    if (second_line) {
                        out.append("...");
                        break;
                    }
                    line_length = 0;
                    second_line = true;
                    out.append("\n");
                }
            }
            return out.toString();
        }
        return this.getDescription();
    }

    public Date getDate() {
        return date;
    }

    public ArrayList<Tag> getTags() {
        return tags;
    }

    public ArrayList<UUID> getPictures() {
        return pictures;
    }

    public Integer getSerial() {
        return serial;
    }

    public Double getValue() {
        if (this.value == null) {
            return 0.0d;
        }
        return this.value;
    }

    public String getComments() {
        return comments;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setValue(Double value) {
        if (value < 0.0d) {
            throw new IllegalArgumentException(String.format("negative value not allowed for item %s", this.getName()));
        }
        this.value = value;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public void addTags(ArrayList<Tag> tags) {
        for (int i = 0; i < tags.size(); i++) {
            Tag tag = tags.get(i);
            if (!this.tags.contains(tag)) {
                this.tags.add(tag);
            }
        }
    }

    /**
     * Should we allow duplicate images here??
     * @param pictures
     */
    public void addPictures(ArrayList<UUID> pictures) {
        this.pictures.addAll(pictures);
    }

    public void setSerial(int serial) {
        this.serial = serial;
    }
}
