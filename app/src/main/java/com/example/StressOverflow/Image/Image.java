package com.example.StressOverflow.Image;

import static android.content.ContentValues.TAG;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.StressOverflow.Item.Item;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.StressOverflow.R;
import com.example.StressOverflow.Util;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

/**
 * An image that can be displayed using a bitmap
 * or a download URL
 */
public class Image {
    private Bitmap bitmap;
    private String URL;


    /**
     * Create image by passing image bitmap
     * @param bitmap of image
     */
    public Image(Bitmap bitmap) {
        this.bitmap = bitmap;
        this.URL = null;
    }

    /**
     * Create image by passing URL of stored image
     * @param URL of stored image
     */
    public Image(String URL) {
        this.URL = URL;
        this.bitmap = null;
    }

    /**
     * Listener interface for when an image has been uploaded
     */
    public interface OnImageUploadedListener {
        void onImageUploaded(String downloadUrl, int insert_index);
        void onUploadFailure(Exception e);
    }

    /**
     * Listener interface for when all images have been uploaded
     */
    public interface OnAllImagesUploadedListener {
        void onAllImagesUploaded(ArrayList<String> downloadURLs);
    }

    /**
     * Get URL of stored image
     * @return URL as a string
     */
    public String getURL() {
        return URL;
    }

    /**
     * Set URL of stored image
     * @param URL of stored image
     */
    public void setURL(String URL) {
        this.URL = URL;
    }

    /**
     * Get bitmap of image. Can be used to display image.
     * @return bitmap of image
     */
    public Bitmap getBitmap() {
        return bitmap;
    }

    /**
     * Change bitmap of image
     * @param bitmap of new image
     */
    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    /**
     * Sets an ImageView to display Image object.
     * @param imageView where to display picture
     */
    public void displayImage(ImageView imageView) {
        if (getURL() == null) {
            imageView.setImageBitmap(getBitmap());
        } else {
            try {
                Picasso.get()
                        .load(getURL())
                        .error(R.drawable.ic_error_image)
                        .into(imageView);
            } catch (Exception e) {
                Log.d("IMAGES", "Unexpected error displaying URL.", e);
            }
        }
    }


    // STATIC METHODS

    /**
     * Get all URLs of pictures from an Item as a Firebase object.
     * @param data Firebase object of an Item
     * @return URLs of all pictures in the item
     */
    public static ArrayList<String> URLsFromFirebaseObject(Map<String, Object> data) {
        ArrayList<String> pictureURLs = new ArrayList<String>();

        if (data != null && data.containsKey("pictures")) {
            Object picturesObject = data.get("pictures");
            if (picturesObject instanceof ArrayList) {
                try {
                    pictureURLs.addAll( (ArrayList<String>) picturesObject );
                } catch (ClassCastException e) {
                    Log.d("IMAGE", "Error casting 'pictures' to ArrayList<String>: ", e);
                }
            } else {
                Log.d("IMAGE", "'pictures' is not an ArrayList.'");
            }
        } else {
            Log.d("IMAGE", "Data does not contain 'pictures' field.");
        }
        return pictureURLs;
    }


    /**
     * Upload pictures to Firebase Storage.
     * @param pictures Image objects to upload
     * @param listener for when all images are done uploading
     */
    public static void uploadPictures(ArrayList<Image> pictures, OnAllImagesUploadedListener listener) {
        ArrayList<Bitmap> bitmaps = new ArrayList<>();
        ArrayList<String> downloadURLs = new ArrayList<>();
        int insert_index = 0;

        // Upload each Bitmap to Storage and get URL
        for (Image image : pictures) {

            if (image.getURL() != null) {
                // already uploaded
                String url = new String(image.getURL());
                downloadURLs.add(url);
                insert_index++;
            }
            else {
                Bitmap bitmap = image.getBitmap();
                int copy_index = 0 + insert_index; // trust me, do not change this
                insert_index++;
                uploadBitmapToStorage(bitmap, copy_index, new OnImageUploadedListener() {
                    @Override
                    public void onImageUploaded(String downloadUrl, int index) {
                        downloadURLs.add(index, downloadUrl);

                        if (downloadURLs.size() == pictures.size()) {
                            listener.onAllImagesUploaded(downloadURLs);
                        }
                    }

                    @Override
                    public void onUploadFailure(Exception e) {
                        Log.w("IMAGES", "Uploading image failed: ", e);
                    }
                });
            }
        }
        // if no new pictures are uploaded
        if (downloadURLs.size() == pictures.size()) {
            listener.onAllImagesUploaded(downloadURLs);
        }
    }

    /**
     * Upload bitmap to Firebase Storage.
     * @param bitmap bitmap of picture
     * @param listener for when picture is done uploading
     */
    public static void uploadBitmapToStorage(Bitmap bitmap, int insert_index, OnImageUploadedListener listener) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
         // unique name
        String filename = "image_" + System.currentTimeMillis() + ".jpg";

        // Upload to storage
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("images/" + filename);
        UploadTask uploadTask = storageRef.putBytes(data);

        uploadTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Get download URL
                storageRef.getDownloadUrl().addOnCompleteListener(downloadUrlTask -> {
                    if (downloadUrlTask.isSuccessful()) {
                        String downloadUrl = downloadUrlTask.getResult().toString();
                        listener.onImageUploaded(downloadUrl, insert_index);
                    } else {
                        listener.onUploadFailure(downloadUrlTask.getException());
                    }
                });
            } else {
                listener.onUploadFailure(task.getException());
            }
        });
    }

    /**
     * Delete picture in database using its URL.
     * @param URL link to picture in database
     */
    public static void deletePictureFromStorage(String URL) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(URL);
        storageRef.delete()
                .addOnFailureListener(e -> {
                    Log.d("IMAGE", "Failed to delete picture from Storage.");
                });
    }

}
