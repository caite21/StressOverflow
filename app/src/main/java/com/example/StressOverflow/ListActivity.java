package com.example.StressOverflow;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.UUID;

public class ListActivity extends AppCompatActivity implements AddItemFragment.OnFragmentInteractionListener,
AddTagToItemFragment.OnFragmentInteractionListener, Db.TagListCallback{
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
    Db database = new Db(db);
    int selected = -1;
    Intent loginIntent;

    private ArrayList<Tag> allTags = new ArrayList<>();

    private boolean inSelectionMode = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);
        this.loginIntent = getIntent();

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

        this.ownerName = loginIntent.getStringExtra("login");
        AppGlobals.getInstance().setOwnerName(this.ownerName);


        database.getAllTags( this);
        //Fragment newItemFragment = new AddItemFragment();
        //newItemFragment.setArguments(new Bundle());
        //getSupportFragmentManager()
        //        .beginTransaction()
        //        .add(R.id.activity__item__list__container, newItemFragment, "test")
        //        .commit();
        /**
         this.addItemButton.setOnClickListener((v) -> {
         Fragment newItemFragment = new AddItemFragment();
         newItemFragment.setArguments(new Bundle());
         getSupportFragmentManager()
         .beginTransaction()
         .add(R.id.activity__item__list__add__fragment__layout, newItemFragment, "test")
         .commit();
         });
         */
        this.itemList.setOnItemClickListener((parent, view, position, id) -> {
            this.selected = position;
            new AddItemFragment().show(getSupportFragmentManager(), "EDIT ITEM");
        });

        this.itemListAdapter = new ItemListAdapter(this, new ArrayList<Item>());
        this.itemList.setAdapter(this.itemListAdapter);
        ArrayList<Tag> tags = new ArrayList<Tag>();
        tags.add(new Tag("tag1"));
        tags.add(new Tag("tag2"));
        GregorianCalendar cal1 = new GregorianCalendar(2023, 11, 5);
        this.itemListAdapter.addItem(
                new Item("Test 1",
                        "Make1",
                        "Model",
                        "I need to stop procrastinating... common word",
                        new GregorianCalendar(2023, 11, 5),
                        100.0d,
                        "asdf",
                        tags,
                        new ArrayList<UUID>(),
                        2000,
                        "Crystal"
                )
        );
        tags.add(new Tag("tag3"));

        this.itemListAdapter.addItem(
                new Item("Test 2",
                        "Make2",
                        "Model",
                        "keywords are very hard to think of; common words",
                        new GregorianCalendar(2020, 1, 15),
                        100.0d,
                        "asdf",
                        tags,
                        new ArrayList<UUID>(),
                        2000,
                        "Ying"
                )
        );

        this.itemListAdapter.addItem(
                new Item("Test 1",
                        "Make2",
                        "Model",
                        "I need to stop procrastinating... uncommon word",
                        new GregorianCalendar(2023, 11, 5),
                        100.0d,
                        "asdf",
                        tags,
                        new ArrayList<UUID>(),
                        2000,
                        "Sunny"
                )
        );
        this.sumOfItemCosts.setText(loginIntent.getStringExtra("login"));
        if(itemListAdapter.getItemListSize()==0){
            exitSelectionMode();
        }
        Dialog filterDialog = new Dialog(ListActivity.this);


        filterButton.setClickable(true);
        this.filterButton.setOnClickListener(v -> new FilterDialog(filterDialog, this.itemListAdapter, this.itemList));
    }

    @Override
    /**
     * Receives the Item produced by the item addition dialog fragment, and adds the item
     * to the item list adapter.
     */
    public void onSubmitAdd(Item item) {
        this.itemListAdapter.add(item);
        this.setSumOfItemCosts();
    }

    /**
     * Lol
     *
     * @param position pos of item to delete
     */
    public void onSubmitDelete(int position) {
        try {
            this.itemListAdapter.deleteItem(position);
        } catch (ArrayIndexOutOfBoundsException e) {
            Util.showShortToast(this.getApplicationContext(), "Choose an item first!");
        }
        this.setSumOfItemCosts();
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
            database.updateItem(i);
            itemListAdapter.notifyDataSetChanged();
        }
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
                database.deleteItem(i);
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
     * Gets the list of all the tags on app startup
     * @param tags array list of tags from the database
     */
    @Override
    public void onTagListReceived(ArrayList<Tag> tags) {
        AppGlobals.getInstance().setAllTags(tags);
    }
}
