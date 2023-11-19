/**
 * Alert dialog displayed when user tries to add an item
 */
package com.example.StressOverflow.Item;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.StressOverflow.Image.AddImagesFragment;
import com.example.StressOverflow.AppGlobals;
import com.example.StressOverflow.Image.Image;
import com.example.StressOverflow.R;
import com.example.StressOverflow.Tag.AddTagFragment;
import com.example.StressOverflow.Tag.AddTagToItemFragment;
import com.example.StressOverflow.Tag.Tag;
import com.example.StressOverflow.Tag.TagList;
import com.example.StressOverflow.Util;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.GregorianCalendar;

public class AddItemFragment extends DialogFragment{

    private EditText itemTitleField;
    private EditText itemMakeField;
    private EditText itemModelField;
    private EditText itemDescriptionField;
    private EditText itemDateField;
    private EditText itemMonthField;
    private EditText itemYearField;
    private EditText itemValueField;
    private EditText itemCommentsField;
    private Button itemPicturesButton;
    private EditText itemSerialField;
    private ChipGroup tagChipGroup;

    private Button addTagButton;
    private Button refreshTagButton;
    private OnFragmentInteractionListener listener;
    private String owner;

    public AddItemFragment(String owner) {
        this.owner = owner;
    }



    public interface OnFragmentInteractionListener {
        void onSubmitAdd(Item item);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            listener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException("activity lacks implementation of OnFragmentInteractionListener");
        }

    }

    @NonNull
    @Override
    /**
     * Called when the floating action button to create an item is pressed
     * Ya
     */
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_add_edit_item, null);
        itemTitleField = view.findViewById(R.id.add__item__fragment__edit__title);
        itemMakeField = view.findViewById(R.id.add__item__fragment__edit__make);
        itemModelField = view.findViewById(R.id.add__item__fragment__edit__model);
        itemDescriptionField = view.findViewById(R.id.add__item__fragment__edit__description);
        itemMonthField = view.findViewById(R.id.add__item__fragment__edit__month);
        itemYearField = view.findViewById(R.id.add__item__fragment__edit__year);
        itemDateField = view.findViewById(R.id.add__item__fragment__edit__date);
        itemValueField = view.findViewById(R.id.add__item__fragment__edit__value);
        itemCommentsField = view.findViewById(R.id.add__item__fragment__edit__comment);
        itemPicturesButton = view.findViewById(R.id.add__item__fragment__edit__pictures);
        itemSerialField = view.findViewById(R.id.add__item__fragment__edit__serial);
        tagChipGroup = view.findViewById(R.id.add__item__fragment__chipGroup);
        addTagButton = view.findViewById(R.id.add_item_fragment_add_tag_button);
        refreshTagButton = view.findViewById(R.id.add_item_fragment_refresh_tags_button);
        addTagsToChipGroup();

        itemPicturesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Opens fragment that shows the item's pictures
                new AddImagesFragment().show(getChildFragmentManager(), "ADD_IMAGES");
            }
        });
        addTagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TagList.class);
                startActivity(intent);
            }
        });

        refreshTagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTagsToChipGroup();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        return builder
                .setView(view)
                .setTitle("Add an item")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        String title = itemTitleField.getText().toString();
                        String make = itemMakeField.getText().toString();
                        String model = itemModelField.getText().toString();
                        String desc = itemDescriptionField.getText().toString();
                        GregorianCalendar date = new GregorianCalendar(
                                Integer.parseInt(itemYearField.getText().toString()),
                                Integer.parseInt(itemMonthField.getText().toString()),
                                Integer.parseInt(itemDateField.getText().toString()));
                        Double value = Double.parseDouble(itemValueField.getText().toString());
                        String comments = itemCommentsField.getText().toString();
                        ArrayList<Tag> newTags = new ArrayList<>();
                        ArrayList<String> emptyPictureURLs = new ArrayList<>();
                        String serial = itemSerialField.getText().toString();

                        for (int chipID : tagChipGroup.getCheckedChipIds()){
                            Chip newChip = tagChipGroup.findViewById(chipID);
                            Tag newTag = new Tag(newChip.getText().toString());
                            newTags.add(newTag);
                        }
                        try {
                            listener.onSubmitAdd(new Item(
                                    title,
                                    make,
                                    model,
                                    desc,
                                    date,
                                    value,
                                    comments,
                                    newTags,
                                    emptyPictureURLs,
                                    Integer.valueOf(serial),
                                    owner
                            ));
                        } catch (IllegalArgumentException e) {
                            Util.showLongToast(
                                    getContext(),
                                    String.format("Invalid argument: %s", e.getMessage())
                            );
                        } catch (Exception e) {
                            Util.showLongToast(
                                    getContext(),
                                    String.format("An unexpected error occurred.")
                            );
                        }
                    }
                }).create();
    }

    /**
     * Adds all the tags to the chipGroup
     */
    private void addTagsToChipGroup(){
        ArrayList <Tag> allTags = AppGlobals.getInstance().getAllTags();
        tagChipGroup.removeAllViews();
        //add all the tags as chips in the dialog
        for (Tag t: allTags){
            Chip chip = new Chip(getContext());
            chip.setText(t.getTagName());
            chip.setCheckedIconVisible(true);
            chip.setCheckable(true);
            chip.setActivated(false);
            tagChipGroup.addView(chip);
            chip.setOnClickListener(v -> chip.setActivated(!chip.isActivated()));
        }
    }
}
