package com.example.StressOverflow.Item;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.StressOverflow.Image.AddImagesFragment;
import com.example.StressOverflow.Tag.AddTagToItemFragment;
import com.example.StressOverflow.Item.FilterItemsFragment;
import com.example.StressOverflow.AppGlobals;
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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ListActivity extends AppCompatActivity implements
        AddItemFragment.OnFragmentInteractionListener,
        AddTagToItemFragment.OnFragmentInteractionListener,
        EditItemFragment.OnFragmentInteractionListener,
        AddImagesFragment.OnFragmentInteractionListener,
        FilterItemsFragment.OnFragmentInteractionListener {

    ListView itemList;
    ItemListAdapter itemListAdapter;
    Button editButton;
    Button filterButton;
    Button showTagListButton;
    FloatingActionButton addItemButton;
    FloatingActionButton deleteItemButton;
    FloatingActionButton addTagButton;
    TextView sumOfItemCosts;
    String ownerName;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference itemRef;
    private ArrayList<String> pictureURLsToDelete;
    private ArrayList<Image> pictures;
    private ArrayList<Item> items = new ArrayList<>();
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
        this.itemRef = this.db.collection("items");
        setContentView(R.layout.activity_item_list);

        this.itemList = findViewById(R.id.activity__item__list__item__list);
        this.editButton = findViewById(R.id.activity__item__list__edit__item__button);
        this.filterButton = findViewById(R.id.activity__item__list__filter__item__button);
        this.addItemButton = findViewById(R.id.activity__item__list__add__item__button);
        this.deleteItemButton = findViewById(R.id.activity__item__list__remove__item__button);
        this.addTagButton = findViewById(R.id.activity__item__list__add__tag__button);
        this.sumOfItemCosts = findViewById(R.id.activity__item__list__cost__sum__text);
        this.showTagListButton = findViewById(R.id.showTagList_button);
        this.addTagButton.setOnClickListener(openTagFragment);
        this.deleteItemButton.setOnClickListener(deleteSelectedItems);
        this.showTagListButton.setOnClickListener(showList);
        itemList.setOnItemLongClickListener(selectItems);
        this.tagRef = this.db.collection("tags");

        this.ownerName =  AppGlobals.getInstance().getOwnerName();
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
        AppGlobals.getInstance().setAllTags(allTags);

        this.itemList.setOnItemClickListener((parent, view, position, id) -> {
            this.selected = position;
            Item selected = this.itemListAdapter.getItem(position);
            resetPictureVars();
            new EditItemFragment(position, selected).show(getSupportFragmentManager(), "EDIT ITEM");
        });
        this.editButton.setOnClickListener((v) -> {
            resetPictureVars();
            new AddItemFragment(this.ownerName).show(getSupportFragmentManager(), "ADD_ITEM");
        });

        this.itemListAdapter = new ItemListAdapter(this, items);
        this.itemList.setAdapter(this.itemListAdapter);

        this.sumOfItemCosts.setText(this.ownerName);

        if(itemListAdapter.getItemListSize()==0){
            exitSelectionMode();
        }

        this.filterButton.setOnClickListener(v -> {
            new FilterItemsFragment(this.itemList, this.itemListAdapter).show(getSupportFragmentManager(), "FILTER");
        });

        this.itemRef
                .whereEqualTo("owner",this.ownerName)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null){
                            Log.e("Firestore", error.toString());
                            return;
                        }
                        if (value != null){
                            items.clear();
                            for (QueryDocumentSnapshot document : value) {
                                Map<String, Object> data = document.getData();
                                Item item = Item.fromFirebaseObject(data);
                                items.add(item);
                            }
                            itemListAdapter.notifyDataSetChanged();
                        }
                    }

                });
    }


    /**
     * Receives the Item produced by the item addition dialog fragment, and adds the item
     * to the item list adapter.
     */
    public void addItem(Item item) {
        this.itemListAdapter.add(item);

        this.setSumOfItemCosts();
        this.itemRef
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
     * Wait for pictures to upload and to receive download URL
     * before setting item in database.
     * @param item to add
     */
    @Override
    public void onSubmitAdd(Item item) {
        if (!picturesChanged) {
            addItem(item);
            resetPictureVars();
        } else {
            Image.uploadPictures(pictures, new Image.OnAllImagesUploadedListener() {
                @Override
                public void onAllImagesUploaded(ArrayList<String> downloadURLs) {
                    item.addPictureURLs(downloadURLs);
                    addItem(item);
                    resetPictureVars();
                }
            });
        }
    }

    /**
     * Lol
     *
     *
     */
    public void onSubmitDelete(Item item) {
        try {
            UUID id_to_delete = item.getId();
            this.itemRef
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
                            // delete associated images from storage
                            for (String URL : item.getPictureURLs()) {
                                Image.deletePictureFromStorage(URL);
                            }
                        }
                    });
            itemListAdapter.remove(item);
            this.setSumOfItemCosts();
        } catch (ArrayIndexOutOfBoundsException e) {
            Util.showShortToast(this.getApplicationContext(), "Choose an item first!");
        }
        this.setSumOfItemCosts();
    }

    public void editItem(int position, Item item) {
        try {
            this.itemListAdapter.editItem(position, item);
            this.setSumOfItemCosts();
            this.itemRef
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

    /**
     * Wait for pictures to upload and to receive download URL
     * before setting item in database.
     * @param position of item to edit
     * @param item to edit
     */
    @Override
    public void onSubmitEdit(int position, Item item) {
        if (!picturesChanged) {
            editItem(position, item);
        } else {
            ArrayList<Image> pics = new ArrayList<>();
            pics.addAll(pictures);
            Image.uploadPictures(pics, new Image.OnAllImagesUploadedListener() {
                @Override
                public void onAllImagesUploaded(ArrayList<String> downloadURLs) {
                    item.setPictureURLs(downloadURLs);
                    editItem(position, item);
                }
            });

            for (String URL : pictureURLsToDelete) {
                Image.deletePictureFromStorage(URL);
            }
        }
        resetPictureVars();
    }

    @SuppressLint("SetTextI18n") // ?? man
    /**
     *
     */
    public void setSumOfItemCosts() {
        ItemListAdapter adapter = (ItemListAdapter) this.itemList.getAdapter();
        this.sumOfItemCosts.setText(adapter.getTotalValue().toString());
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
//            itemListAdapter.setSelectionMode(true);
//            itemListAdapter.toggleSelection(position);
            ItemListAdapter adapter = (ItemListAdapter) itemList.getAdapter();
            adapter.setSelectionMode(true);
            adapter.toggleSelection(position);
            if (adapter.getSelectedItems().size() == 0){
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
        ItemListAdapter adapter = (ItemListAdapter) itemList.getAdapter();
        adapter.setSelectionMode(false);
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
            this.itemRef
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
            ItemListAdapter adapter = (ItemListAdapter) itemList.getAdapter();
            ArrayList<Item> itemsToDelete = adapter.getSelectedItems();
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
     * When user confirms adding images, picture updates are
     * passed so that the pictures can be attached
     * when the user confirms adding/editing an item
     *
     * @param imagesList pictures that should be attached to image
     * @param deleteURLs picture URLs that need to be removed from Storage
     */
    @Override
    public void onConfirmImages(ArrayList<Image> imagesList, ArrayList<String> deleteURLs) {
        this.pictureURLsToDelete.addAll(deleteURLs);
        this.pictures = imagesList;
        picturesChanged = true;
    }

    /**
     * At each new add/edit fragment, reset variables involving pictures
     */
    private void resetPictureVars() {
        this.pictures = new ArrayList<>();
        this.pictureURLsToDelete = new ArrayList<>();
        this.picturesChanged = false;
    }

    /**
     * Catches the output of the filter dialog fragment and passes output as arguments to filter and
     * sort methods. Then updates sum of costs and list adapter.
     *
     * @param filterConds map of field to filter by as keys and strings to filter by as values
     * @param sortType field to sort by. "No Sort" if a sort field was not selected.
     * @param isAsc boolean true for ascending order and false for descending
     */
    @Override
    public void onFilterPressed(Map<String, ArrayList<String>> filterConds, String sortType, boolean isAsc) {
        ArrayList<Item> filteredList;
        try {
            filteredList = this.itemListAdapter.filterList(filterConds);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        if (!Objects.equals(sortType, "No Sort")) sortBy(sortType, filteredList, isAsc);

        ItemListAdapter filteredItemListAdapter = new ItemListAdapter(this, filteredList);

        this.itemList.setAdapter(filteredItemListAdapter);
        this.setSumOfItemCosts();

        this.itemListAdapter.notifyDataSetChanged();
    }

    /**
     * Sorts the elements of the given ArrayList using the provided Comparator.
     *
     * @param list arraylist to be sorted
     * @param comparator comparator to determine order of elements
     * @param <T> type of elements in list
     */
    public static <T> void sort(ArrayList<T> list, Comparator<T> comparator) {
        Collections.sort(list, comparator);
    }

    /**
     * Sorts an ArrayList of items based on the specified sort type and order.
     *
     * @param sortType field by which unsortedList should be sorted ("Date", "Desc", "Make",
     *                 "Value", or "Tags")
     * @param unsortedList ArrayList of items to be sorted
     * @param isAsc boolean true for ascending order and false for descending
     */
    public void sortBy(String sortType, ArrayList<Item> unsortedList, boolean isAsc) {
        sort(unsortedList, new Comparator<Item>() {
            @Override
            public int compare(Item obj1, Item obj2) {
                int result;
                if (Objects.equals(sortType, "Date")) {
                    result = obj1.getDate().compareTo(obj2.getDate());
                } else if (Objects.equals(sortType, "Desc")) {
                    result = obj1.getDescription().compareTo(obj2.getDescription());
                } else if (Objects.equals(sortType, "Make")) {
                    result = obj1.getMake().compareTo(obj2.getMake());
                } else if (Objects.equals(sortType, "Value")) {
                    result = obj1.getValue().compareTo(obj2.getValue());
                } else if (Objects.equals(sortType, "Tags")) {
                    result = obj1.getTags().get(0).getTagName().compareTo(obj2.getTags().get(0).getTagName());
                } else {
                    result = 0;
                }

                return isAsc ? result : -result;
            }
        });
    }

}
