package com.example.StressOverflow;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.UUID;

public class ListActivity extends AppCompatActivity implements AddItemFragment.OnFragmentInteractionListener,
AddTagToItemFragment.OnFragmentInteractionListener, EditItemFragment.OnFragmentInteractionListener, 
AddImagesFragment.OnFragmentInteractionListener{
    ListView itemList;
    ItemListAdapter itemListAdapter;
    Button editButton;
    Button filterButton;
    FloatingActionButton addItemButton;
    FloatingActionButton deleteItemButton;
    FloatingActionButton addTagButton;
    TextView sumOfItemCosts;
    private FirebaseFirestore db;
    private CollectionReference items;
    ArrayList<Image> addedPictures;

    int selected = -1;
    Intent loginIntent;

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

        showTagListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListActivity.this, TagList.class);
                startActivity(intent);
            }
        });
        this.addTagButton.setOnClickListener(openTagFragment);
        this.deleteItemButton.setOnClickListener(deleteSelectedItems);
        itemList.setOnItemLongClickListener(selectItems);
        this.itemList.setOnItemClickListener((parent, view, position, id) -> {
            this.selected = position;
            Item selected = this.itemListAdapter.getItem(position);
            new EditItemFragment(position, selected).show(getSupportFragmentManager(), "EDIT ITEM");
        });
        this.editButton.setOnClickListener((v) -> {
            new AddItemFragment(this.loginIntent.getStringExtra("login")).show(getSupportFragmentManager(), "ADD_ITEM");
        });

        this.itemListAdapter = new ItemListAdapter(this, new ArrayList<Item>());
        this.itemList.setAdapter(this.itemListAdapter);

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
                .whereEqualTo("owner", this.loginIntent.getStringExtra("login"))
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
        item.setPictures(addedPictures);
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
     * @param position pos of item to delete
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
                    });
            itemListAdapter.remove(item);
            this.setSumOfItemCosts();
        } catch (ArrayIndexOutOfBoundsException e) {
            Util.showShortToast(this.getApplicationContext(), "Choose an item first!");
        }
        this.setSumOfItemCosts();
    }

    public void onSubmitEdit(int position, Item item) {
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

    private AdapterView.OnItemLongClickListener selectItems = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            addTagButton.setVisibility(View.VISIBLE);
            addItemButton.setVisibility(View.GONE);
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

    private void exitSelectionMode() {
        inSelectionMode = false;
        itemListAdapter.setSelectionMode(false);
        addTagButton.setVisibility(View.GONE);
        addItemButton.setVisibility(View.VISIBLE);
        deleteItemButton.setVisibility(View.GONE);
    }

    private View.OnClickListener openTagFragment = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new AddTagToItemFragment().show(getSupportFragmentManager(), "ADD TAGS");
        }
    };


    @Override
    public void addTagPressed(ArrayList<Tag> tagsToAdd) {
        for (Item i: itemListAdapter.getSelectedItems()){
            i.addTags(tagsToAdd);
            itemListAdapter.notifyDataSetChanged();
        }

    }

    private View.OnClickListener deleteSelectedItems = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ArrayList<Item> itemsToDelete = itemListAdapter.getSelectedItems();
            for (Item i: itemsToDelete){
                onSubmitDelete(i);
            }
            if (itemListAdapter.getItemListSize()==0){
                exitSelectionMode();
            }
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
    public void onConfirmImages(ArrayList<Image> pictures) {
        addedPictures = pictures;
    }
}
