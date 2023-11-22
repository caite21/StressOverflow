/**
 * Represents a single item
 * Should pictures be their own class, or is simply a link to a picture enough?
 * TODO: Throw more illegal argument exceptions or something
 * reminder: tags are not hashable, dont make tags or pictures a HashSet (which also might muck
 * around with mutex's if we require in future)
 */
package com.example.StressOverflow.Item;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.StressOverflow.Image.Image;
import com.example.StressOverflow.Tag.Tag;
import com.example.StressOverflow.Util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import kotlin.NotImplementedError;

public class Item {

    private UUID id;
    private String name;
    private String make;
    private String model;
    private String description;
    private GregorianCalendar date;
    private Double value;
    private String comments;
    private ArrayList<Tag> tags = new ArrayList<Tag>();
    private ArrayList<String> pictureURLs = new ArrayList<>();
    private ArrayList<Image> pictures = new ArrayList<>();
    private Integer serial;
    private String owner;

    public Item() {}

    public Item(
            String name,
            String make,
            String model,
            String description,
            GregorianCalendar date,
            Double value,
            String comments,
            ArrayList<Tag> tags,
            ArrayList<String> pictureURLs,
            Integer serial,
            String owner
    ) {
        this.id = UUID.randomUUID();
        this.setName(name);
        this.setMake(make);
        this.setModel(model);
        this.setDescription(description);
        this.setDate(date);
        this.setValue(value);
        this.setComments(comments);
        this.addTags(tags);
        this.addPictureURLs(pictureURLs);
        this.setSerial(serial);
        this.setOwner(owner);
    }

    public Item(
            UUID uuid,
            String name,
            String make,
            String model,
            String description,
            GregorianCalendar date,
            Double value,
            String comments,
            ArrayList<Tag> tags,
            ArrayList<String> pictureURLs,
            Integer serial,
            String owner
    ) {
        this.id = uuid;
        this.setName(name);
        this.setMake(make);
        this.setModel(model);
        this.setDescription(description);
        this.setDate(date);
        this.setValue(value);
        this.setComments(comments);
        this.addTags(tags);
        this.addPictureURLs(pictureURLs);
        this.setSerial(serial);
        this.setOwner(owner);
    }

    public void setName(String name) throws IllegalArgumentException {
        if (name.equals("")) {
            throw new IllegalArgumentException("empty name not allowed");
        } else if (name.length() > Util.MAX_ITEM_NAME_LENGTH) {
            throw new IllegalArgumentException(String.format("name exceeds maximum name length (%d)", Util.MAX_ITEM_NAME_LENGTH));
        }
        this.name = name;
    }

    public UUID getId() {
        return this.id;
    }
    public String getName() {
        return this.name;
    }

    public String getMake() {
        return this.make;
    }

    public String getModel() {
        return this.model;
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

    public GregorianCalendar getDate() {
        return this.date;
    }

    public String getDateAsString() {
        return String.format("%s/%s/%s",
                this.getDate().get(Calendar.YEAR),
                this.getDate().get(Calendar.MONTH) + 1,
                this.getDate().get(Calendar.DATE)
        );
    }

    public String getDateYear() {
        return String.format("%s", this.getDate().get(Calendar.YEAR));
    }

    public String getDateMonth() {
        return String.format("%s", this.getDate().get(Calendar.MONTH) + 1);
    }

    public String getDateDate() {
        return String.format("%s", this.getDate().get(Calendar.DATE));
    }

    public ArrayList<Tag> getTags() {
        return this.tags;
    }

    /**
     * Get image objects that are attached to the item
     * @return image objects
     */
    public ArrayList<Image> getPictures() {
        if (pictures == null) {
            setPictures(new ArrayList<>());
        }
        return pictures;
    }

    /**
     * Get image URLs that are attached to the item
     * @return URLs of images
     */
    public ArrayList<String> getPictureURLs() {
        if (pictures.size() == 0) {
            setPictureURLs(new ArrayList<>());
        }
        return pictureURLs;
    }

    public Integer getSerial() {
        return this.serial;
    }

    public String getOwner() {
        return this.owner;
    }

    /**
     * TODO: This should be able to return any number of possible serial number formats.
     * Examples: this.getSerial = 12345678905, this.getSerialAsString("0-ddddd-ddddd-d")
     *           returns 0-12345-67890-5. will implement once i find a need for it
     *           (which may never come)
     *
     * @return serial number formatted as string
     */
    public String getSerialAsString(String format) {
        throw new NotImplementedError();
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

    /**
     * Returns "make / model", "make", "model", or an empty string depending on which
     * of the properties are not empty strings
     *
     * @return make and model together
     */
    public String getMakeModel() {
        StringBuilder out = new StringBuilder();
        if (this.getMake().equals("") && this.getModel().equals("")) {
            return "";
        } else if (this.getMake().equals("") || this.getModel().equals("")) {
            out.append(this.getMake());
            out.append(this.getModel());
        } else {
            out.append(this.getMake());
            out.append(" / ");
            out.append(this.getModel());
        }
        return out.toString();
    }

    /**
     * Set URLs of images attached to item and sets Image objects with the URLs.
     * @param pictureURLs URLs (strings) of pictures in storage
     */
    public void setPictureURLs(ArrayList<String> pictureURLs) {
        this.pictureURLs = pictureURLs;
        ArrayList<Image> newImages = new ArrayList<>();
        for (String url : pictureURLs) {
            newImages.add(new Image(url));
        }
        setPictures(newImages);
    }

    /**
     * Set image objects attached to item.
     * Should only be used by setPictureURLs().
     * @param pictures image objects
     */
    private void setPictures(ArrayList<Image> pictures) {
        this.pictures = pictures;
    }

    /**
     * TODO: Should we allow duplicate images here??
     * Add URLs of images attached to item and add Image objects with the URLs.
     * @param pictureURLs URLs (strings) of pictures in storage
     */
    public void addPictureURLs(@NonNull ArrayList<String> pictureURLs) {
        this.pictureURLs.addAll(pictureURLs);
        ArrayList<Image> newImages = new ArrayList<>();
        for (String url : pictureURLs) {
            newImages.add(new Image(url));
        }
        addPictures(newImages);
    }

    /**
     * Add image objects attached to item.
     * Should only be used by addPictureURLs().
     * @param pictures image objects
     */
    private void addPictures(ArrayList<Image> pictures) {
        this.pictures.addAll(pictures);
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

    public void setDate(GregorianCalendar date) {
        this.date = date;
    }

    public void setValue(Double value) throws IllegalArgumentException {
        if (value < 0.0d) {
            throw new IllegalArgumentException(String.format("negative value not allowed for item %s", this.getName()));
        }
        this.value = value;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    /**
     * Adds all tags in tags to this object's tags. If a tag is already owned by this object, then
     * don't add the tag.
     *
     * @param tags
     */
    public void addTags(@NonNull ArrayList<Tag> tags) {
        for (int i = 0; i < tags.size(); i++) {
            Tag tag = tags.get(i);
            if (!this.tags.contains(tag)) {
                this.tags.add(tag);
            }
        }
    }

    /**
     * Removes all tags which exist in both this item's tags and passed in tag list.
     *
     * @param tags The tags to remove from this item
     */
    public void removeTags(@NonNull ArrayList<Tag> tags) {
        this.tags.removeAll(tags);
    }

    public void setSerial(int serial) {
        this.serial = serial;
    }

    /**
     * converts this thing to firebase object to store
     *
     * @return stuff i guess
     */
    public HashMap<String, Object> toFirebaseObject() {
        List<Tag> tagsList = this.getTags();
        ArrayList<String> tagNamesArrayList = new ArrayList<>();
        for (Tag tag : tagsList) {
            tagNamesArrayList.add(tag.getTagName());
        }
        HashMap<String, Object> data = new HashMap<>();
        data.put("id", this.getId());
        data.put("name", this.getName());
        data.put("make", this.getMake());
        data.put("model", this.getModel());
        data.put("description", this.getDescription());
        // unknown how firebase handles dates, we can fix this later.
        data.put("year", this.getDate().get(Calendar.YEAR));
        data.put("month", this.getDate().get(Calendar.MONTH) + 1);
        data.put("day", this.getDate().get(Calendar.DATE));
        data.put("value", this.getValue());
        data.put("comments", this.getComments());
        data.put("owner", this.getOwner());
        data.put("serial", this.getSerial());
        data.put("pictures", this.getPictureURLs());
        data.put("tags", tagNamesArrayList);
        return data;
    }

    /**
     * turns a hashmap from firebase into an item object
     *
     * @param data the hashmap stored in firebase
     * @return stuff the item
     */
    public static Item fromFirebaseObject(Map<String, Object> data) {
        try {
            ArrayList<Tag> tags = new ArrayList<>();
            UUID uid = new UUID(
                    ((Map<String, Long>) data.get("id")).get("mostSignificantBits"),
                    ((Map<String, Long>) data.get("id")).get("leastSignificantBits")
            );
            //@SuppressWarnings({"unchecked", "ConstantConditions"}) // just trust me bro
                    // TODO: sunny should not be trusted.
            for (String tagName:(ArrayList<String>) data.get("tags")){
                tags.add(new Tag(tagName));
            }

            // get pictures and catch errors
            ArrayList<String> pictureURLs = Image.URLsFromFirebaseObject(data);

            Item out = new Item(
                    uid,
                    (String) data.get("name"),
                    (String) data.get("make"),
                    (String) data.get("model"),
                    (String) data.get("description"),
                    new GregorianCalendar(
                            ((Long) data.get("year")).intValue(),
                            ((Long) data.get("month")).intValue(),
                            ((Long) data.get("day")).intValue()
                    ),
                    (Double) data.get("value"),
                    (String) data.get("comments"),
                    tags,
                    pictureURLs,
                    ((Long) data.get("serial")).intValue(),
                    (String) data.get("owner")
            );
            return out;
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "Creation of item from Firebase hashmap went wrong: ", e);
        }
        return null;
    }
}
