/**
 * Adapter class for main list view of items
 */
package com.example.StressOverflow;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Comparator;

public class ItemListAdapter extends ArrayAdapter<Item> {
    private ArrayList<Item> items;
    private Context context;

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

        itemTitle.setText(item.getName());
        itemMakeModel.setText(item.getMakeModel());
        itemDescription.setText(item.getDescription(true));
        itemPrice.setText((item.getValue().toString()));
        itemDate.setText(item.getDate().toString());
        itemSerial.setText(item.getSerial().toString());

        return view;
    }

    /**
     * Sums the value of all items present in the list.
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
     * @param pos the item at this position gets removed
     */
    public void deleteItem(int pos) {
        this.items.remove(pos);
        this.notifyDataSetChanged();
    }

    /**
     * Changes one of the existing items to the item passed into the function.
     * @param pos position of item to change
     * @param item item to change
     */
    public void editItem(int pos, Item item) {
        this.items.set(pos, item);
        this.notifyDataSetChanged();
    }
    /**
     * Sorts the list with the provided comparator that uses Item as a template.
     * @param cmp provides rules for how to sort the ArrayList
     */
    public void sortList(Comparator<Item> cmp) {
        this.items.sort(cmp);
        this.notifyDataSetChanged();
    }

    /**
     * Filters the list according to something somehow
     */
    public void filterList() {
        return;
    }
}
