package com.example.StressOverflow.Image;

import static android.content.ContentValues.TAG;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.example.StressOverflow.Item.Item;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

/**
 * Represents an image which is displayed and stored using a Bitmap
 * and an id
 */
public class Image {
    private UUID id;
    private Bitmap bitmap;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

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
    public UUID getId() {
        return id;
    }

    /**
     * Set id of image
     *
     * @param id of image
     */
    public void setId(UUID id) {
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


    // STATIC METHODS
    public static void uploadPictures(ArrayList<Image> pictures) {
        ArrayList<Bitmap> bitmaps = new ArrayList<>();

        for (Image image : pictures) {
            bitmaps.add(image.getBitmap());
        }

        uploadBitmaps(bitmaps);
    }


    public static void uploadBitmaps(ArrayList<Bitmap> bitmapList) {
        ArrayList<String> downloadUrls = new ArrayList<>();

        // Upload each Bitmap to Firebase Storage and get download URL
        for (Bitmap bitmap : bitmapList) {
            uploadBitmapToStorage(bitmap, new OnImageUploadedListener() {
                @Override
                public void onImageUploaded(String downloadUrl) {
                    downloadUrls.add(downloadUrl);

                    // If all images are uploaded, store downloadUrls in Firestore
                    if (downloadUrls.size() == bitmapList.size()) {
                        storeDownloadUrlsInFirestore(downloadUrls);
                    }
                }

                @Override
                public void onUploadFailure(Exception e) {
                    Log.w("IMAGES", "Uploading image failed: ", e);

                }
            });
        }
    }

    public static void uploadBitmapToStorage(Bitmap bitmap, OnImageUploadedListener listener) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        // Unique filename
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
                        listener.onImageUploaded(downloadUrl);
                    } else {
                        listener.onUploadFailure(downloadUrlTask.getException());
                    }
                });
            } else {
                listener.onUploadFailure(task.getException());
            }
        });
    }

    public static void storeDownloadUrlsInFirestore(ArrayList<String> downloadUrls) {
        // put urls in item's pictures array in database


//        // Create a document in Firestore and store the downloadUrls
//        Map<String, Object> data = new HashMap<>();
//        data.put("imageUrls", downloadUrls);
//
//        db.collection("images")
//                .add(data)
//                .addOnSuccessListener(documentReference -> {
//                    Toast.makeText(this, "Images uploaded successfully", Toast.LENGTH_SHORT).show();
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(this, "Firestore upload failed", Toast.LENGTH_SHORT).show();
//                });

    }

    // Listener interface for image uploads
    public interface OnImageUploadedListener {
        void onImageUploaded(String downloadUrl);
        void onUploadFailure(Exception e);
    }



}
