/**
 * Adapter class for main list view of items
 */
package com.example.StressOverflow;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Layout;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public class ItemListAdapter extends ArrayAdapter<Item> {
    private ArrayList<Item> items;
    private Context context;

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
        ArrayList<Tag> tags = item.getTags();
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
//        itemDate.setText(item.getDate().toString());
        itemSerial.setText(item.getSerial().toString());

        // the first picture is shown
        if (item.getPictures().size() > 0) {
            itemPicture.setImageBitmap((item.getPictures().get(0)).getBitmap());
        }


        ChipGroup tagChipGroup = view.findViewById(R.id.itemTagChipGroup);
        tagChipGroup.removeAllViews();
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

        if (inSelectionMode) {
            // Handle the selection mode appearance
            if (selectedItems.contains(item)) {
                // Change background color or apply other visual cues for selected items
                view.setBackgroundColor(ContextCompat.getColor(context, R.color.lavender));
            } else {
                // Restore default appearance for unselected items
                view.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            }
        } else {
            // Normal mode, no selection
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
        }
        return view;
    }

    public int getItemListSize(){
        return items.size();
    }
    public void setSelectionMode(boolean enabled) {
        inSelectionMode = enabled;
        notifyDataSetChanged(); // Notify the adapter to refresh the view
    }
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