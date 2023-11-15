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
    public interface TagListCallback{
        /**
         * Called when the tagList has been loaded from the database
         * @param tags array list of tags from the database
         */
        void onTagListReceived(ArrayList<Tag> tags);
    }
    public Db(@NonNull FirebaseFirestore database) {
        this.db = database;
        this.items = this.db.collection("items");
        this.tags = this.db.collection("tags");
        this.images = this.db.collection("images");
        this.credentials = this.db.collection("credentials");
    }

    /**
     * returns the collection reference of the tags collection
     * @return the collection reference of the tags
     */
    public CollectionReference getTagsCollectionReference(){
        return this.tags;
    }

    public CollectionReference getItemsCollectionReference(){
        return this.items;
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
     * Deletes an item
     * @param item selected item
     */
    public void deleteItem(Item item){
        UUID uuid = item.getId();
        this.items
                .document(uuid.toString())
                .delete()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error with item insertion into collection items: ", e);
                        throw new RuntimeException("Error with item insertion into collection items: ", e);
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

    /**
     * Adds a tag to the database
     * @param tag tag to be added
     */
    public void addTag(Tag tag){
        String ownerName = AppGlobals.getInstance().getOwnerName();
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

    /**
     * Deletes a tag from database
     * @param tag tag to be deleted
     */
    public void deleteTag(Tag tag){
        String tagName = tag.getTagName();
        String ownerName = AppGlobals.getInstance().getOwnerName();
        this.tags
                .document(String.format("%s:%s", ownerName, tagName))
                .delete()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error with item deletion into collection items: ", e);
                        throw new RuntimeException("Error with item deletion into collection items: ", e);
                    }
                });

    }

    /**
     * Gets all the tags from the database, initializes the tag list from start up
     * @param callback the context of where it's called from
     * @return an arraylist of all the tags
     * TODO: call this whenever there is a change to the method, currently getting all the tags through global variables
     *
     */
    public ArrayList<Tag> getAllTags(final TagListCallback callback){
        ArrayList<Tag> out = new ArrayList<>();
        String ownerName = AppGlobals.getInstance().getOwnerName();
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

    public void checkTagExist(Tag tag, TagExistCallback callback){
        String ownerName = AppGlobals.getInstance().getOwnerName();
        String tagName = tag.getTagName();
        this.db.collection("tags")
                .whereEqualTo("documentID", String.format("%s:%s", ownerName, tagName))
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()){
                            callback.onTagExist(false);
                        }else{
                            callback.onTagExist(true);
                        }
                    }

                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle the failure if needed
                        // You can also invoke the callback with an error flag
                        callback.onTagExist(false);
                    }
                });
    }

    public interface TagExistCallback {
        void onTagExist(boolean exists);
    }
}
