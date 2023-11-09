package com.example.StressOverflow;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.protobuf.Any;

import androidx.annotation.NonNull;

import java.security.acl.Owner;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Db {

    private FirebaseFirestore db;
    private CollectionReference items;
    private CollectionReference tags;
    private CollectionReference images;
    private CollectionReference credentials;

    public interface TagListCallback{
        void onTagListReceived(ArrayList<Tag> tags);
    }
    public Db(@NonNull FirebaseFirestore database) {
        this.db = database;
        this.items = this.db.collection("items");
        this.tags = this.db.collection("tags");
        this.images = this.db.collection("images");
        this.credentials = this.db.collection("credentials");
    }
    public CollectionReference getTagsCollectionReference(){
        return this.tags;
    }
    /**
     * adds a new item to firebase. to update an existing item, use updateItem(Item item).
     *
     * @param item the item to add
     */
    public void addItem(@NonNull Item item) {
        UUID uuid = item.getId();
        this.items
                .document(uuid.toString())
                .set(item.toFirebaseObject())
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error with item insertion into collection items: ", e);
                        throw new RuntimeException("Error with item insertion into collection items: ", e);
                    }
                });
    }

    /**
     * updates the item found in firebase
     *
     * @param item the item to update, with changes made
     */
    public void updateItem(@NonNull Item item) {
        UUID uuid = item.getId();
        this.items
                .document(uuid.toString())
                .update(item.toFirebaseObject())
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error with item update on collection items: ", e);
                        throw new RuntimeException("Error with item update on collection items: ", e);
                    }
                });
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

    /**
     * Fetches all items that are owned by the provided owner
     *
     * @param owner the owner to query
     */
    public ArrayList<Item> getItems(@NonNull String owner) {
        ArrayList<Item> out = new ArrayList<>();
        this.db.collection("items")
                .whereEqualTo("owner", owner)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> data = document.getData();
                                out.add(Item.fromFirebaseObject(data));
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
        return out;
    }

    public void addTag(Tag tag, String ownerName){
        String tagName = tag.getTagName();
        this.tags
                .document(String.format("%s:%s", ownerName, tagName))
                .set(tag.toFirebaseObject(ownerName))
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error with item insertion into collection items: ", e);
                        throw new RuntimeException("Error with item insertion into collection items: ", e);
                    }
                });

    }

    public void deleteTag(Tag tag, String ownerName){
        String tagName = tag.getTagName();
        this.tags
                .document(String.format("%s:%s", ownerName, tagName))
                .delete()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error with item insertion into collection items: ", e);
                        throw new RuntimeException("Error with item insertion into collection items: ", e);
                    }
                });

    }

    public ArrayList<Tag> getAllTags(String ownerName, final TagListCallback callback){
        ArrayList<Tag> out = new ArrayList<>();
        this.db.collection("tags")
                .whereEqualTo("ownerName", ownerName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> data = document.getData();
                                out.add(Tag.fromFirebaseObject(data));
                            }
                            callback.onTagListReceived(out);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
        return out;
    };

}
