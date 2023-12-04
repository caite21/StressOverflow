/**
 * Adapter class for main list view of items
 */
package com.example.StressOverflow.Item;
import static android.content.ContentValues.TAG;

import com.example.StressOverflow.Image.Image;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.StressOverflow.R;
import com.example.StressOverflow.Tag.Tag;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * An adapter that displays the content in ListActivity
 */
public class ItemListAdapter extends ArrayAdapter<Item> {
    private ArrayList<Item> items;
    private Context context;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference itemRef;
    private Set<Item> selectedItems = new HashSet<>();
    private boolean inSelectionMode = false;

    public ItemListAdapter(Context context, ArrayList<Item> items) {
        super(context, R.layout.listview_item_content, items);
        this.items = items;
        this.context = context;
        this.itemRef = db.collection("items");
    }

    @NonNull
    @Override
    public View getView(int pos, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(this.context).inflate(R.layout.listview_item_content, parent, false);
        }
        Item item = items.get(pos);
        TextView itemTitle = view.findViewById(R.id.listview__item__title);
        TextView itemMakeModel = view.findViewById(R.id.listview__item__model__make);
        TextView itemDescription = view.findViewById(R.id.listview__item__description);
        TextView itemPrice = view.findViewById(R.id.listview__item__price);
        TextView itemDate = view.findViewById(R.id.listview__item__date);
        TextView itemSerial = view.findViewById(R.id.listview__item__serial__number);
        ImageView pictureImageView = view.findViewById(R.id.listview__item__picture);

        // set item picture
        if (item.getPictureURLs().size() > 0 ) {
            String picURL = new String(item.getPictureURLs().get(0));
            Picasso.get()
                    .load(picURL)
                    .error(R.drawable.ic_error_image)
                    .into(pictureImageView);
        }
        else {
            pictureImageView.setImageResource(R.drawable.default_image);
        }

        itemTitle.setText(item.getName());
        itemMakeModel.setText(item.getMakeModel());
        itemDescription.setText(item.getDescription(true));
        itemPrice.setText(item.getValueAsString());
        itemDate.setText(item.getDateAsString());
        itemSerial.setText(item.getSerial());

        addTagChips(view, item);
        applySelectionBackground(view, item);

        return view;
    }

    /**
     * Gets the size of item list
     * @return size of item list
     */
    public int getItemListSize(){
        return items.size();
    }

    /**
     * Changes the selection mode
     * @param enabled the selection mode to be set to
     */
    public void setSelectionMode(boolean enabled) {
        if (!enabled){
            selectedItems.clear();
        }
        inSelectionMode = enabled;
        notifyDataSetChanged(); // Notify the adapter to refresh the view
    }

    /**
     * Toggle selection and add or remove from selectedItems list
     * @param pos position of the item that is being selected or unselected
     */
    public void toggleSelection(int pos) {
        Item item = items.get(pos);
        if (selectedItems.contains(item)) {
            selectedItems.remove(item);
        } else {
            selectedItems.add(item);
        }
        if (selectedItems.isEmpty()){
            setSelectionMode(false);
        }
        notifyDataSetChanged();
    }

    /**
     * Gets the list of items that are currently selected
     * @return list of selected items
     */
    public ArrayList<Item> getSelectedItems() {
        ArrayList<Item> selected = new ArrayList<>();
        for (Item it : selectedItems) {
            selected.add(it);
        }
        return selected;
    }

    /**
     * Sums the value of all items present in the list.
     *
     * @return cumulative value of items in the list
     */
    public String getTotalValue() {
        double cum = 0;
        for (int i = 0; i < items.size(); i++) {
            cum = cum + items.get(i).getValue();
        }
        return String.format("$%.2f", cum);
    }

    /**
     * Adds an item to the database
     * @param item to be added
     */
    public void addItem(Item item) {
        this.items.add(item);
        UUID uuid = item.getId();
        this.itemRef
                .document(uuid.toString())
                .set(item.toFirebaseObject())
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error with item insertion into collection items: ", e);
                        throw new RuntimeException("Error with item insertion into collection items: ", e);
                    }
                });
        this.notifyDataSetChanged();
    }

    /**
     * Removes the item as position `pos`.
     *
     * @param pos the item at this position gets removed
     */
    public void deleteItem(int pos) {
        this.items.remove(pos);
        this.notifyDataSetChanged();
    }

    /**
     * Changes one of the existing items to the item passed into the function.
     *
     * @param pos  position of item to change
     * @param item item to change
     */
    public void editItem(int pos, Item item) {
        this.items.set(pos, item);
        this.notifyDataSetChanged();
    }

    /**
     * Add the tags for the item on runtime
     * @param view gets the current view that it's rendering
     * @param item gets the current item that it's rendering
     */
    private void addTagChips(View view, Item item){
        ArrayList<Tag> tags = item.getTags();
        ChipGroup tagChipGroup = view.findViewById(R.id.itemTagChipGroup);
        tagChipGroup.removeAllViews();

        //display a maximum of 3 tags
        int tagCounter = 0;
        for (Tag t: tags){
            if (tagCounter!=3){
                Chip chip = new Chip(this.context);

                //styling
                chip.setChipBackgroundColorResource(R.color.sagi);
                chip.setTextColor(Color.WHITE);
                chip.setChipCornerRadius(10);
                chip.setChipBackgroundColorResource(R.color.lavender);
                chip.setTextColor(Color.BLACK);
                chip.setText(t.getTagName());
                chip.setClickable(false);
                chip.setFocusable(false);
                chip.setLongClickable(false);
                chip.setEnabled(false);
                tagChipGroup.addView(chip);

                tagCounter++;
            }else{
                break;
            }
        }
    }

    /**
     * Change the background color depending on its selection mode
     * @param view gets the current view that it's rendering
     * @param item gets the item that it's rendering
     */
    private void applySelectionBackground(View view, Item item){
        if (inSelectionMode) {
            if (selectedItems.contains(item)) {
                // Change background color for selected items
                view.setBackgroundColor(ContextCompat.getColor(context, R.color.lavender));
            } else {
                // Restore default appearance for unselected items
                view.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            }
        } else {
            // Normal mode, no selection
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
        }
    }
}