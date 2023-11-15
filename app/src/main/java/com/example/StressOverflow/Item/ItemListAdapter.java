/**
 * Adapter class for main list view of items
 */
package com.example.StressOverflow.Item;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.StressOverflow.Db;
import com.example.StressOverflow.R;
import com.example.StressOverflow.Tag.Tag;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * An adapter that displays the content in ListActivity
 */
public class ItemListAdapter extends ArrayAdapter<Item> {
    private ArrayList<Item> items;
    private Context context;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Db database = new Db(db);
    private Set<Item> selectedItems = new HashSet<>();
    private boolean inSelectionMode = false;

    public ItemListAdapter(Context context, ArrayList<Item> items) {
        super(context, R.layout.listview_item_content, items);
        this.items = items;
        this.context = context;
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
        ImageView itemPicture = view.findViewById(R.id.listview__item__picture);

        itemTitle.setText(item.getName());
        itemMakeModel.setText(item.getMakeModel());
        itemDescription.setText(item.getDescription(true));
        itemPrice.setText((item.getValue().toString()));
        itemDate.setText(item.getDateAsString());
        itemSerial.setText(item.getSerial().toString());

        addTagChips(view, item);
        applySelectionBackground(view, item);
        // the first picture is shown
        if (item.getPictures().size() > 0) {
            itemPicture.setImageBitmap((item.getPictures().get(0)).getBitmap());
        }



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
    public Double getTotalValue() {
        double cum = 0;
        for (int i = 0; i < items.size(); i++) {
            cum = cum + items.get(i).getValue();
        }
        return Math.round(cum * 100.0d) / 100.0d;
    }

    public void addItem(Item item) {
        this.items.add(item);
        database.addItem(item);
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
     * Sorts the list with the provided comparator that uses Item as a template.
     *
     * @param cmp provides rules for how to sort the ArrayList
     */
    public void sortList(Comparator<Item> cmp) {
        this.items.sort(cmp);
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
    /**
     * Filters the list according to something somehow
     */
    public ArrayList<Item> filterList(Map<String, ArrayList<String>> conditions) throws ParseException {
        // Remove Filters
        if (conditions.get("keywords").isEmpty() & conditions.get("dates").isEmpty() & conditions.get("tags").isEmpty()) {
            return this.items;
        }

        ArrayList<Item> filtered = new ArrayList<Item>();

        for (int i = 0; i < this.items.size(); i++) {
            Item item = this.items.get(i);
            int finalI = i;  // I don't really understand why i needs to be final but...

            if (!conditions.get("keywords").stream().allMatch(keyword -> item.getDescription().contains(keyword))) {
                continue;

            } else if (!conditions.get("dates").get(0).isEmpty()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                Date parsedDate = dateFormat.parse(conditions.get("dates").get(0));
                GregorianCalendar parseFrom = new GregorianCalendar();
                parseFrom.setTime(parsedDate);
                if (!item.getDate().after(parseFrom)) continue;

            } else if (!conditions.get("dates").get(1).isEmpty()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                Date parsedDate = dateFormat.parse(conditions.get("dates").get(1));
                GregorianCalendar parseTo = new GregorianCalendar();
                parseTo.setTime(parsedDate);
                if (!item.getDate().before(parseTo)) continue;

            } else if (!conditions.get("makes").stream().allMatch(make -> make.contains(item.getMake()))) {
                continue;

            } else if (!conditions.get("tags").stream().allMatch(tagList -> item.getTags().stream().anyMatch(tag -> tag.getTagName().contains(tagList)))) {
                continue;
            }
            filtered.add(this.items.get(i));
        }
        return filtered;
    }
}