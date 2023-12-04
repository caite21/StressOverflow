/**
 * Alert dialog displayed when user tries to add an item
 */
package com.example.StressOverflow.Item;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.StressOverflow.Image.AddImagesFragment;
import com.example.StressOverflow.AppGlobals;
import com.example.StressOverflow.R;
import com.example.StressOverflow.Scan.BarcodeLookup;
import com.example.StressOverflow.Scan.ScanSerialActivity;
import com.example.StressOverflow.Tag.Tag;
import com.example.StressOverflow.Tag.TagList;
import com.example.StressOverflow.Util;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Map;

/**
 * Fragment where a user can adds attributes of a new item.
 */
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
    private Button serialScanButton;
    ActivityResultLauncher<ScanOptions> serialLauncher;
    private EditText itemSerialField;
    private ChipGroup tagChipGroup;

    private Button addTagButton;
    private Button refreshTagButton;
    private OnFragmentInteractionListener listener;
    private String owner;
    private View view;

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
        serialLauncher = registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() != null) {
                itemSerialField.setText(result.getContents());
            }
        });
    }

    @NonNull
    @Override
    /**
     * Called when the floating action button to create an item is pressed
     * Ya
     */
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_add_edit_item, null);
        itemTitleField = view.findViewById(R.id.add_item_fragment_edit_title);
        itemMakeField = view.findViewById(R.id.add_item_fragment_edit_make);
        itemModelField = view.findViewById(R.id.add_item_fragment_edit_model);
        itemDescriptionField = view.findViewById(R.id.add_item_fragment_edit_description);
        itemMonthField = view.findViewById(R.id.add_item_fragment_edit_month);
        itemYearField = view.findViewById(R.id.add_item_fragment_edit_year);
        itemDateField = view.findViewById(R.id.add_item_fragment_edit_date);
        itemValueField = view.findViewById(R.id.add_item_fragment_edit_value);
        itemCommentsField = view.findViewById(R.id.add_item_fragment_edit_comment);
        itemPicturesButton = view.findViewById(R.id.add_item_fragment_edit_pictures);
        itemSerialField = view.findViewById(R.id.add_item_fragment_edit_serial);
        tagChipGroup = view.findViewById(R.id.add_item_fragment_chipGroup);

        serialScanButton = view.findViewById(R.id.add__item__fragment__button__serial);

        addTagButton = view.findViewById(R.id.add_item_fragment_add_tag_button);
        refreshTagButton = view.findViewById(R.id.add_item_fragment_refresh_tags_button);
        addTagsToChipGroup();

        Item tempItem = new Item();
        itemPicturesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Opens fragment that shows the item's pictures
                new AddImagesFragment(tempItem).show(getChildFragmentManager(), "ADD_IMAGES");
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
        serialScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanSerial();
            }
        });

        Button barcodeLookupButton = view.findViewById(R.id.add_item_fragment_button_lookup);
        barcodeLookupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String entered_barcode = itemSerialField.getText().toString();
                if (BarcodeLookup.isUPCValid(entered_barcode)) {
                    BarcodeLookup.get(entered_barcode, info -> handleBarcodeLookupResponse(info), getContext());
                } else {
                    Util.showShortToast(getContext(), "Invalid serial number");
                }
            }
        });

        final AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(view)
                .setTitle("Add an item")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("OK", null).create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        GregorianCalendar date;
                        Double doubleValue;
                        String value = itemValueField.getText().toString();
                        String title = itemTitleField.getText().toString();
                        String make = itemMakeField.getText().toString();
                        String model = itemModelField.getText().toString();
                        String desc = itemDescriptionField.getText().toString();
                        try {
                            Integer year = Integer.parseInt(itemYearField.getText().toString());
                            Integer month = Integer.parseInt(itemMonthField.getText().toString());
                            Integer day = Integer.parseInt(itemDateField.getText().toString());
                            if (!Util.isDateValid(year, month, day)) {
                                Util.showLongToast(getContext(), "You must enter a valid date!");
                                return;
                            }
                            date = new GregorianCalendar(
                                    year,
                                    month,
                                    day);
                        } catch (NumberFormatException e) {
                            Util.showLongToast(getContext(), "You must enter a valid date!");
                            return;
                        }
                        try {
                            if (value.equals("")) {
                                doubleValue = null;
                            } else {
                                doubleValue = Double.parseDouble(value);
                            }
                        } catch (IllegalArgumentException e) {
                            Util.showLongToast(getContext(), "You must enter a valid value!");
                            return;
                        }
                        String comments = itemCommentsField.getText().toString();
                        ArrayList<Tag> newTags = new ArrayList<>();
                        ArrayList<String> emptyPictureURLs = new ArrayList<>();
                        String serial = itemSerialField.getText().toString();

                        for (int chipID : tagChipGroup.getCheckedChipIds()){
                            Chip newChip = tagChipGroup.findViewById(chipID);
                            newChip.setChipBackgroundColorResource(R.color.sagi);
                            newChip.setTextColor(Color.WHITE);
                            newChip.setChipCornerRadius(10);
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
                                    doubleValue,
                                    comments,
                                    newTags,
                                    emptyPictureURLs,
                                    serial,
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
                        dialog.dismiss();
                    }
                });
            }
        });

        return dialog;
    }

    /**
     * Prompts the user to scan a barcode. Sets the item serial number field to the scanned number.
     */
    private void scanSerial() {
        ScanOptions o = new ScanOptions();
        o.setCaptureActivity(ScanSerialActivity.class);
        serialLauncher.launch(o);
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
            chip.setChipBackgroundColorResource(R.color.sagi);
            chip.setTextColor(Color.WHITE);
            chip.setChipCornerRadius(10);
            chip.setText(t.getTagName());
            chip.setCheckedIconVisible(true);
            chip.setCheckable(true);
            chip.setActivated(false);
            tagChipGroup.addView(chip);
            chip.setOnClickListener(v -> chip.setActivated(!chip.isActivated()));
        }
    }

    /**
     * Displays found product details that can be selected to enter
     * if the serial number is found in the UPC database.
     * @param info map of category to found product details
     */
    public void handleBarcodeLookupResponse(Map<String, String> info) {
        if (info.values().stream().allMatch(value -> value.equals(""))) {
            Util.showShortToast(getContext(), "No product information found");
            return;
        }

        // display found info
        int size = info.size();
        String[] options = {"Title", "Description","Make","Model"};
        EditText[] fields = {itemTitleField, itemDescriptionField, itemMakeField, itemModelField};
        boolean[] checkedItems = new boolean[size];
        String[] formattedOptions = new String[size];
        for (int i = 0; i < options.length; i++) {
            String key = options[i];
            formattedOptions[i] = key + ": " + info.get(key);
        }

        // .setMessage("Select categories to overwrite\n") TODO: create xml for nicer format

        // show found, ask to overwrite
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder
            .setTitle("Found product information:")
            .setMultiChoiceItems(formattedOptions, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    checkedItems[which] =  isChecked;
                }
            })
            .setNegativeButton("Cancel", (dialog, which) ->
                    dialog.dismiss()
            )
            .setPositiveButton("Use selected", (dialog, which) -> {
                // overwrite selected
                for (int i = 0; i < options.length; i++) {
                    String value = info.get(options[i]);
                    if (value!=null && !value.equals("") && !value.equals("null") && checkedItems[i]) {
                        fields[i].setText(value);
                    }
                }
                dialog.dismiss();
            })
            .create()
            .show();
    }

}
