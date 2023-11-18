package com.example.StressOverflow;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.example.StressOverflow.Item.Item;
import com.example.StressOverflow.Tag.Tag;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class Db {

    private FirebaseFirestore db;
    private CollectionReference items;
    private CollectionReference tags;
    private CollectionReference images;
    private CollectionReference credentials;

    /**
     * Used when the asynchronous data is finished querying
     */

    public Db(@NonNull FirebaseFirestore database) {
        this.db = database;
        this.items = this.db.collection("items");
        this.tags = this.db.collection("tags");
        this.images = this.db.collection("images");
        this.credentials = this.db.collection("credentials");
    }


    /**
     * gets an item with a specific uuid
     *
     * @param uuid the uuid to search
     * @return the item found, or null if there are no items with this uuid
     */
    public Item getItem(@NonNull UUID uuid) {
        final Item[] item = new Item[1];
        this.items
                .document(uuid.toString())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                item[0] = Item.fromFirebaseObject(document.getData());
                            } else {
                                Log.d(TAG, "Document not found with uuid " + uuid);
                            }
                        }
                    }
                });
        return item[0];
    }








}
