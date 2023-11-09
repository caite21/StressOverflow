package com.example.StressOverflow;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FilterDialog {

    private final AutoCompleteTextView keywordInput;
    private final ChipGroup keywordChips;
    private final EditText startDateInput;
    private final EditText endDateInput;
//    private final CheckBox checkAllMakes;
    private final ChipGroup makeChips;
    private final CheckBox checkAllTags;
    private final ChipGroup tagChips;
    private final MaterialButtonToggleGroup sortField;
    private final MaterialButtonToggleGroup sortOrder;
    private final Button backBtn;
    private final Button FilterBtn;

    /*
    Get keywords from the descriptions of all the listview items.
     */
    private ArrayList<String> getKeywords(ItemListAdapter adapter){
        ArrayList<String> keywords;
        keywords = new ArrayList<String>();
        for (int i=0; i<adapter.getCount(); i++){
            String[] descriptionWords = Objects.requireNonNull(adapter.getItem(i)).getDescription().split(" ");
            for (String descriptionWord : descriptionWords) {
                if (!keywords.contains(descriptionWord)) {
                    keywords.add(descriptionWord);
                }
            }
        }
        return keywords;
    }

    /*

     */
    private ArrayList<String> getTags(ItemListAdapter adapter){
        ArrayList<String> tags;
        tags = new ArrayList<String>();
        for (int i=0; i<adapter.getCount(); i++){
            ArrayList<Tag> tagList = Objects.requireNonNull(adapter.getItem(i)).getTags();
            for (int j=0; j<tagList.size(); j++) {
                if (!tags.contains(tagList.get(j).getTagName())) {
                    tags.add(tagList.get(j).getTagName());
                }
            }
        }
        return tags;
    }

    private ArrayList<String> getMakes(ItemListAdapter adapter){
        ArrayList<String> makes;
        makes = new ArrayList<String>();
        for (int i=0; i<adapter.getCount(); i++){
            if (!makes.contains(adapter.getItem(i).getMake())) {
                makes.add(adapter.getItem(i).getMake());
            }
        }
        return makes;
    }

    public FilterDialog(Dialog dialog, ItemListAdapter itemAdapter, ListView itemList) {
        dialog.setContentView(R.layout.filter_dialog);
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        dialog.getWindow().getAttributes().windowAnimations = R.style.animation;

        this.keywordInput = dialog.findViewById(R.id.filter__dialog__keywords__textedit);
        this.keywordChips = dialog.findViewById(R.id.filter__dialog__keywords__chipgroup);
        this.startDateInput = dialog.findViewById(R.id.filter__dialog__start__date);
        this.endDateInput = dialog.findViewById(R.id.filter__dialog__end__date);
//        this.checkAllMakes = dialog.findViewById(R.id.filter__dialog__all__makes__checkbox);
        this.makeChips = dialog.findViewById(R.id.filter__dialog__makes__chipgroup);
        this.checkAllTags = dialog.findViewById(R.id.filter__dialog__all__tags__checkbox);
        this.tagChips = dialog.findViewById(R.id.filter__dialog__tags__chipgroup);
        this.sortField = dialog.findViewById(R.id.filter__dialog__sort__field);
        this.sortOrder = dialog.findViewById(R.id.filter__dialog__sort__order);
        this.backBtn = dialog.findViewById(R.id.filter__dialog__back__btn);
        this.FilterBtn = dialog.findViewById(R.id.filter__dialog__filter__btn);

        // Set autocorrect based on description words in listview
        ArrayList<String> keywords = getKeywords(itemAdapter);
        ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter<String>(dialog.getContext(), android.R.layout.simple_list_item_1, keywords);
        this.keywordInput.setAdapter(adapter);

        this.keywordInput.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // Add chip if user hits enter
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    Chip chip = new Chip(dialog.getContext());
                    chip.setText(keywordInput.getText());
                    chip.setCloseIconVisible(true);
                    chip.setOnClickListener(v1 -> keywordChips.removeView(chip));
                    keywordChips.addView(chip);
                    keywordInput.setText("");
                    return true;
                }
                return false;
            }
        });

        // Gets calendar popup for date selects
//        this.startDateInput.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final Calendar c = Calendar.getInstance();
//                int year = c.get(Calendar.YEAR);
//                int month = c.get(Calendar.MONTH);
//                int day = c.get(Calendar.DAY_OF_MONTH);
//
//                DatePickerDialog datePickerDialog = new DatePickerDialog(
//                    dialog.getContext(),
//                    (view, year12, monthOfYear, dayOfMonth) ->
//                        startDateInput.setText(String.format("%d-%d-%d", dayOfMonth, monthOfYear + 1, year12)),
//                    year, month, day
//                );
//                datePickerDialog.show();
//            }
//        });
//        this.endDateInput.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final Calendar c = Calendar.getInstance();
//                int year = c.get(Calendar.YEAR);
//                int month = c.get(Calendar.MONTH);
//                int day = c.get(Calendar.DAY_OF_MONTH);
//
//                DatePickerDialog datePickerDialog = new DatePickerDialog(
//                    dialog.getContext(),
//                    (view, year1, monthOfYear, dayOfMonth) ->
//                        endDateInput.setText(String.format("%d-%d-%d", dayOfMonth, monthOfYear + 1, year1)),
//                    year, month, day
//                );
//                datePickerDialog.show();
//            }
//        });

        this.checkAllTags.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for (int i=0; i<tagChips.getChildCount(); i++) {
                    Chip chip = (Chip) tagChips.getChildAt(i);
                    chip.setChecked(isChecked);
                }
            }
        });

        // Adds tags based on what's set in listview
        ArrayList<String> tags = getTags(itemAdapter);
        for (int i=0; i<tags.size(); i++) {
            Chip chip = new Chip(dialog.getContext());
            chip.setText(tags.get(i));
            chip.setCheckedIconVisible(true);
            chip.setCheckable(true);
            chip.setActivated(false);
            chip.setOnClickListener(v -> chip.setActivated(!chip.isActivated()));
            this.tagChips.addView(chip);
        }

//        this.checkAllMakes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                for (int i=0; i<makeChips.getChildCount(); i++) {
//                    Chip chip = (Chip) makeChips.getChildAt(i);
//                    chip.setChecked(isChecked);
//                }
//            }
//        });

        ArrayList<String> makes = getMakes(itemAdapter);
        for (int i=0; i<makes.size(); i++) {
            Chip chip = new Chip(dialog.getContext());
            chip.setText(makes.get(i));
            chip.setCheckedIconVisible(true);
            chip.setCheckable(true);
            chip.setActivated(false);
            chip.setOnClickListener(v -> chip.setActivated(!chip.isActivated()));
            this.makeChips.addView(chip);
        }

        // Just input handing for sorting since sortOrder and sortField are mutually dependant
        // TODO: Doesn't fully work since depends on top to bottom, uh either scrap or figure out...
        this.sortField.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                sortOrder.setSelectionRequired(isChecked);
                if (!isChecked) sortOrder.clearChecked();
                else sortOrder.check(sortOrder.getChildAt(0).getId());
            }
        });

        this.backBtn.setOnClickListener(v -> {
            dialog.dismiss();
        });

        /*
        I know its messy... A bit more of a proof of concept than a final idea
         */
        this.FilterBtn.setOnClickListener(v -> {
            Map<String, ArrayList<String>> filterConds = new HashMap<String, ArrayList<String>>();

            ArrayList<String> keywordConds = new ArrayList<String>();
            for (int i=0; i<keywordChips.getChildCount(); i++) {
                Chip chip = (Chip) keywordChips.getChildAt(i);
                keywordConds.add(chip.getText().toString());
            }
            filterConds.put("keywords", keywordConds);

            ArrayList<String> dateConds = new ArrayList<String>();
            dateConds.add(startDateInput.getText().toString());
            dateConds.add(endDateInput.getText().toString());
            filterConds.put("dates", dateConds);

            ArrayList<String> makeConds = new ArrayList<String>();
            for (int chipId : makeChips.getCheckedChipIds()) {
                Chip chip = makeChips.findViewById(chipId);
                makeConds.add(chip.getText().toString());
            }
            filterConds.put("makes", makeConds);

            ArrayList<String> tagConds = new ArrayList<String>();
            for (int chipId : tagChips.getCheckedChipIds()) {
                Chip chip = tagChips.findViewById(chipId);
                tagConds.add(chip.getText().toString());
            }
            filterConds.put("tags", tagConds);

            ArrayList<Item> filteredList = null;
            try {
                filteredList = itemAdapter.filterList(filterConds);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            ItemListAdapter filtereditemListAdapter = new ItemListAdapter(dialog.getContext(), filteredList);
            itemList.setAdapter(filtereditemListAdapter);
            itemAdapter.notifyDataSetChanged();

            dialog.dismiss();
        });

        dialog.show();
    }
}
