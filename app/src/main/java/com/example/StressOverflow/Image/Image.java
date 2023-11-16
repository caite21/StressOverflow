package com.example.StressOverflow.Image;

import android.graphics.Bitmap;

/**
 * Represents an image which is displayed and stored using a Bitmp
 * and an id
 */
public class Image {
    private int id;
    private Bitmap bitmap;

    /**
     * Create image by passing image bitmap
     *
     * @param bitmap of image
     */
    public Image(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    /**
     * Get id
     *
     * @return id of image
     */
    public int getId() {
        return id;
    }

    /**
     * Set id of image
     *
     * @param id of image
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Get bitmap of image. Can be used to display image.
     *
     * @return bitmap of image
     */
    public Bitmap getBitmap() {
        return bitmap;
    }

    /**
     * Change bitmap of image
     *
     * @param bitmap of new image
     */
    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }


}
