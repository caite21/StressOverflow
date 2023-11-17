package com.example.StressOverflow.Item;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.StressOverflow.Image.AddImagesFragment;
import com.example.StressOverflow.Tag.AddTagToItemFragment;
import com.example.StressOverflow.AppGlobals;
import com.example.StressOverflow.Db;
import com.example.StressOverflow.FilterDialog;
import com.example.StressOverflow.Image.Image;
import com.example.StressOverflow.R;
import com.example.StressOverflow.Tag.Tag;
import com.example.StressOverflow.Tag.TagList;
import com.example.StressOverflow.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class ListActivity extends AppCompatActivity implements
        AddItemFragment.OnFragmentInteractionListener,
        AddTagToItemFragment.OnFragmentInteractionListener,
        EditItemFragment.OnFragmentInteractionListener,
        AddImagesFragment.OnFragmentInteractionListener {

    ListView itemList;
    ItemListAdapter itemListAdapter;
    Button editButton;
    Button filterButton;
    FloatingActionButton addItemButton;
    FloatingActionButton deleteItemButton;
    FloatingActionButton addTagButton;
    TextView sumOfItemCosts;
    String ownerName;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference items;
    private ArrayList<Image> pictures = new ArrayList<>();
    private ArrayList<String> pictureURLs = new ArrayList<>();
    private boolean picturesChanged = false;
    private CollectionReference tagRef;

    int selected = -1;
    Intent loginIntent;

    private ArrayList<Tag> allTags = new ArrayList<>();

    private boolean inSelectionMode = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.loginIntent = getIntent();
        this.db = FirebaseFirestore.getInstance();
        this.items = this.db.collection("items");
        setContentView(R.layout.activity_item_list);

        this.itemList = findViewById(R.id.activity__item__list__item__list);
        this.editButton = findViewById(R.id.activity__item__list__edit__item__button);
        this.filterButton = findViewById(R.id.activity__item__list__filter__item__button);
        this.addItemButton = findViewById(R.id.activity__item__list__add__item__button);
        this.deleteItemButton = findViewById(R.id.activity__item__list__remove__item__button);
        this.addTagButton = findViewById(R.id.activity__item__list__add__tag__button);
        this.sumOfItemCosts = findViewById(R.id.activity__item__list__cost__sum__text);
        Button showTagListButton = findViewById(R.id.showTagList_button);
        showTagListButton.setOnClickListener(showList);
        this.addTagButton.setOnClickListener(openTagFragment);
        this.deleteItemButton.setOnClickListener(deleteSelectedItems);
        itemList.setOnItemLongClickListener(selectItems);
        this.tagRef = this.db.collection("tags");

        this.ownerName =  AppGlobals.getInstance().getOwnerName();

        ArrayList<Tag> allTags = new ArrayList<>();
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
                                allTags.add(Tag.fromFirebaseObject(data));
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        this.itemList.setOnItemClickListener((parent, view, position, id) -> {
            this.selected = position;
            Item selected = this.itemListAdapter.getItem(position);
            new EditItemFragment(position, selected).show(getSupportFragmentManager(), "EDIT ITEM");
        });
        this.editButton.setOnClickListener((v) -> {
            new AddItemFragment(this.ownerName).show(getSupportFragmentManager(), "ADD_ITEM");
        });

        this.itemListAdapter = new ItemListAdapter(this, new ArrayList<Item>());
        this.itemList.setAdapter(this.itemListAdapter);

        this.sumOfItemCosts.setText(this.ownerName);

        ArrayList<Tag> tags = new ArrayList<Tag>();
        tags.add(new Tag("tag1"));
        tags.add(new Tag("tag2"));
        if(itemListAdapter.getItemListSize()==0){
            exitSelectionMode();
        }
        Dialog filterDialog = new Dialog(ListActivity.this);

        filterButton.setClickable(true);
        this.filterButton.setOnClickListener(v -> new FilterDialog(filterDialog, this.itemListAdapter, this.itemList));

        this.items
                .whereEqualTo("owner",this.ownerName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> data = document.getData();
                                Item item = Item.fromFirebaseObject(data);
                                onSubmitAdd(item);
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    @Override
    /**
     * Receives the Item produced by the item addition dialog fragment, and adds the item
     * to the item list adapter.
     */
    public void onSubmitAdd(Item item) {
        item.addPictureURLs(pictureURLs);
        this.itemListAdapter.add(item);

        this.setSumOfItemCosts();
        this.items
                .document(item.getId().toString())
                .set(item.toFirebaseObject())
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error with item addition on collection items: ", e);
                        throw new RuntimeException("Error with item update on collection items: ", e);
                    }
                });
    }

    /**
     * Lol
     *
     *
     */
    public void onSubmitDelete(Item item) {
        try {
            UUID id_to_delete = item.getId();
            this.items
                    .document(id_to_delete.toString())
                    .delete()
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error with item deletion on collection items: ", e);
                            throw new RuntimeException("Error with item deletion on collection items: ", e);
                        }
                    })
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            // TODO: delete associated images from storage
//                            Image.deletePictures(item.getPictureURLs());
                        }
                    });
            itemListAdapter.remove(item);
            this.setSumOfItemCosts();
        } catch (ArrayIndexOutOfBoundsException e) {
            Util.showShortToast(this.getApplicationContext(), "Choose an item first!");
        }
        this.setSumOfItemCosts();
    }

    public void onSubmitEdit(int position, Item item) {
        if (picturesChanged) {
            // set, not add, because user may have deleted pictures TODO: nothing is deleting the URLs yet
            item.setPictureURLs(pictureURLs);
        }
        try {
            this.itemListAdapter.editItem(position, item);
            this.setSumOfItemCosts();
            this.items
                    .document(item.getId().toString())
                    .update(item.toFirebaseObject())
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error with item update on collection items: ", e);
                            throw new RuntimeException("Error with item update on collection items: ", e);
                        }
                    });
        } catch (ArrayIndexOutOfBoundsException e) {
            Util.showShortToast(this.getApplicationContext(), "Attempted to edit out of bounds object") ;
        } catch (Exception e) {
            Util.showShortToast(this.getApplicationContext(), "Something wrong happened");
        }
    }
    @SuppressLint("SetTextI18n") // ?? man
    /**
     *
     */
    public void setSumOfItemCosts() {
        this.sumOfItemCosts.setText(this.itemListAdapter.getTotalValue().toString());
    }

    /**
     * Called when long clicks are applied to an item
     */
    private AdapterView.OnItemLongClickListener selectItems = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            addTagButton.setVisibility(View.VISIBLE);
            //addItemButton.setVisibility(View.GONE);
            deleteItemButton.setVisibility(View.VISIBLE);
            inSelectionMode = true;
            itemListAdapter.setSelectionMode(true);
            itemListAdapter.toggleSelection(position);

            if (itemListAdapter.getSelectedItems().size() == 0){
                exitSelectionMode();
            }
            return true;
        }

    };

    /**
     * Called when there are no items anymore and when user exits the addTagToItem fragment
     * Styles the selected items accordingly
     */
    private void exitSelectionMode() {
        inSelectionMode = false;
        itemListAdapter.setSelectionMode(false);
        addTagButton.setVisibility(View.GONE);
        //addItemButton.setVisibility(View.VISIBLE);
        deleteItemButton.setVisibility(View.GONE);
    }

    private View.OnClickListener openTagFragment = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AddTagToItemFragment fragment = new AddTagToItemFragment();
            fragment.show(getSupportFragmentManager(),"ADD TAGS");
        }
    };


    /**
     * Called when user adds new tags to an item
     * @param tagsToAdd the tags selected by user
     */
    @Override
    public void addTagPressed(ArrayList<Tag> tagsToAdd) {
        for (Item i: itemListAdapter.getSelectedItems()){
            ArrayList <Tag> currentTags = i.getTags();
            for (Tag newTag : tagsToAdd) {
                if (currentTags.contains(newTag)) {
                    tagsToAdd.remove(newTag);
                }
            }
            i.addTags(tagsToAdd);
            this.items
                    .document(i.getId().toString())
                    .update(i.toFirebaseObject())
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error with item update on collection items: ", e);
                            throw new RuntimeException("Error with item update on collection items: ", e);
                        }
                    });

            itemListAdapter.notifyDataSetChanged();
        }
        exitSelectionMode();

    }

    /**
     * Gets the list of currently selected items and deletes it
     */
    private View.OnClickListener deleteSelectedItems = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ArrayList<Item> itemsToDelete = itemListAdapter.getSelectedItems();
            //iterate through the selected list and delete from adapter and database
            for (Item i: itemsToDelete){
                itemListAdapter.remove(i);
                onSubmitDelete(i);
            }
            //if there are no more items, exit selection mode
            if (itemListAdapter.getItemListSize()==0){
                exitSelectionMode();
            }
        }
    };

    /**
     * Directs user to the TagList Activity that shows user all the current existing tags
     */
    private View.OnClickListener showList = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(ListActivity.this, TagList.class);
            startActivity(intent);
        }
    };



    /**
     * When user confirms adding images, the updated list
     * of pictures is passed so that the pictures can be attached
     * when the user is done adding/editing an item,
     *
     * @param pictures taken with camera or selected from library
     */
    @Override
    public void onConfirmImages(ArrayList<Image> pictures, ArrayList<String> pictureURLs) {
        this.pictures = pictures;
        this.pictureURLs = pictureURLs;
        picturesChanged = true;

        Toast.makeText(this, "Pictures uploaded successfully.", Toast.LENGTH_SHORT).show();
    }

}
