package com.example.StressOverflow.Item;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.example.StressOverflow.R;
import com.example.StressOverflow.Tag.Tag;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Dialog fragment that allows input for filtering and sorting
 */
public class FilterItemsFragment extends DialogFragment {

    private FilterItemsFragment.OnFragmentInteractionListener listener;
    private View view;
    private ItemListAdapter itemAdapter;

    private ListView itemList;
    private AutoCompleteTextView keywordInput;
    private ChipGroup keywordChips;
    private EditText startDateInput;
    private EditText endDateInput;
    private ChipGroup makeChips;
    private CheckBox checkAllTags;
    private ChipGroup tagChips;
    private MaterialButtonToggleGroup sortField;
    private MaterialButtonToggleGroup sortOrder;
    private Button sortAsc;

    private Drawable textview_design;

    /**
     * Constructor for passing in variables from the activity
     * @param itemList
     * @param itemAdapter
     */
    public FilterItemsFragment(ListView itemList, ItemListAdapter itemAdapter) {
        this.itemList = itemList;
        this.itemAdapter = itemAdapter;
    }

    /**
     * Interface for interaction between the AddTagFragment and the hosting activity.
     */
    public interface OnFragmentInteractionListener {
        void onFilterPressed(Map<String, ArrayList<String>> filterConds, String sortType, boolean isAsc);
    }

    /**
     * Called when the fragment is attached to a context to ensure that the hosting activity
     * implements the necessary interaction listener interface.
     * @param context context to which the fragment is attached
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FilterItemsFragment.OnFragmentInteractionListener) {
            this.listener = (FilterItemsFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException("activity lacks implementation of OnFragmentInteractionListener");
        }
    }

    /**
     * Displays and configures all the dialog box components for filtering.
     * @param savedInstanceState The last saved instance state of the Fragment,
     * or null if this is a freshly created Fragment.
     *
     * @return
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_filter_items, null);
        this.keywordInput = view.findViewById(R.id.fragment_filter_items_keywords_textedit);
        this.keywordChips = view.findViewById(R.id.fragment_filter_items_keywords_chipgroup);
        this.startDateInput = view.findViewById(R.id.fragment_filter_items_start_date_edittext);
        this.endDateInput = view.findViewById(R.id.fragment_filter_items_end_date_edittext);
        this.makeChips = view.findViewById(R.id.fragment_filter_items_makes_chipgroup);
        this.checkAllTags = view.findViewById(R.id.fragment_filter_items_all_tags_checkbox);
        this.tagChips = view.findViewById(R.id.fragment_filter_items_tags_chipgroup);
        this.sortField = view.findViewById(R.id.fragment_filter_items_sort_type_buttongroup);
        this.sortOrder = view.findViewById(R.id.fragment_filter_items_sort_order_buttongroup);
        this.sortAsc = view.findViewById(R.id.fragment_filter_items_asc_sort_button);

        textview_design = ContextCompat.getDrawable(this.getContext(), R.drawable.sagi_textview);

        setupKeywordInput();

        setupDateInputListener(this.startDateInput);
        setupDateInputListener(this.endDateInput);

        setupMakesChipGroup();

        setupTagChipGroup();

        setupSortListeners();

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder
            .setView(view)
            .setTitle("Filter")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Filter/Sort", (dialog, which) -> {
                Map<String, ArrayList<String>> filterConds = new HashMap<String, ArrayList<String>>();
                filterConds.put("keywords", getCheckedChips(this.keywordChips));
                filterConds.put("dates", getDateInputs());
                filterConds.put("makes", getCheckedChips(this.makeChips));
                filterConds.put("tags", getCheckedChips(this.tagChips));

                String sortType;
                if (sortField.getCheckedButtonIds().size() == 1) {
                    MaterialButton checkedSortField = sortField.findViewById(sortField.getCheckedButtonId());
                    sortType = checkedSortField.getText().toString();
                } else {
                    sortType = "No Sort";
                }

                MaterialButton checkedSortOrder = sortOrder.findViewById(sortOrder.getCheckedButtonId());
                boolean isAsc = (checkedSortOrder == sortAsc);

                listener.onFilterPressed(filterConds, sortType, isAsc);
            }).create();
    }

    /**
     * Changes the background and text color, as well as radius, to match rest of application.
     * @param chip chip to change layout of
     */
    private void format_chips(Chip chip) {
        chip.setChipBackgroundColorResource(R.color.sagi);
        chip.setTextColor(Color.WHITE);
        chip.setChipCornerRadius(10);
    }

    /**
     * Gets all description words for autocompleting in the text edit. Also sets up a chip when
     * ENTER is pressed to add to the list of keyword filters.
     */
    private void setupKeywordInput() {
        // Autocorrect based on item descriptions
        ArrayList<String> keywords;
        keywords = new ArrayList<String>();
        for (int i = 0; i < this.itemAdapter.getCount(); i++) {
            String[] descriptionWords = Objects.requireNonNull(this.itemAdapter.getItem(i)).getDescription().toLowerCase().replaceAll("[^\\sa-zA-Z0-9]", "").split(" ");
            for (String descriptionWord : descriptionWords) {
                if (!keywords.contains(descriptionWord)) {
                    keywords.add(descriptionWord);
                }
            }
        }
        ArrayAdapter<String> autocorrectAdapter;
        autocorrectAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, keywords);
        this.keywordInput.setAdapter(autocorrectAdapter);

        // Listener to add a chip when ENTER is pressed
        this.keywordInput.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // Add chip if user hits enter
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    Chip chip = new Chip(getContext());
                    format_chips(chip);
                    chip.setText(keywordInput.getText());
                    chip.setCloseIconVisible(true);
                    chip.setCheckable(true);
                    chip.setChecked(true);
                    chip.setActivated(true);
                    chip.setOnClickListener(v1 -> keywordChips.removeView(chip));
                    keywordChips.addView(chip);
                    keywordInput.setText("");
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * Sets up text edits to open a date picking dialog box when clicked on.
     * @param dateInput editText field to add dialog to
     */
    private void setupDateInputListener(EditText dateInput) {
        dateInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        getContext(),
                        (view, yearVal, monthOfYear, dayOfMonth) ->
                                dateInput.setText(String.format("%d-%d-%d", dayOfMonth, monthOfYear + 1, yearVal)),
                        year, month, day
                );
                datePickerDialog.show();
            }
        });
    }

    /**
     * Adds a single unactivated, checkable chip to a chip group.
     * @param chipLabel test to put on chip
     * @param group chip group to place chip in
     */
    private void setupChip(String chipLabel, ChipGroup group) {
        Chip chip = new Chip(getContext());
        this.format_chips(chip);
        chip.setText(chipLabel);
        chip.setCheckedIconVisible(true);
        chip.setCheckable(true);
        chip.setActivated(false);
        chip.setVisibility(VISIBLE);
        chip.setOnClickListener(v -> chip.setActivated(!chip.isActivated()));
        group.addView(chip);
    }

    /**
     * Gets all items' makes and adds them to the chip group
     */
    private void setupMakesChipGroup() {
        ArrayList<String> makes;
        makes = new ArrayList<String>();
        for (int i = 0; i < this.itemAdapter.getCount(); i++) {
            if (!makes.contains(this.itemAdapter.getItem(i).getMake())) {
                makes.add(this.itemAdapter.getItem(i).getMake());
                setupChip(this.itemAdapter.getItem(i).getMake(), this.makeChips);
            }
        }
        this.makeChips.setSingleSelection(true);
    }

    /**
     * Sets up the check all checkbox to check all chips in the chip group. Also gets all tags and
     * adds them to the chip group.
     */
    private void setupTagChipGroup() {
        // Check all listener
        this.checkAllTags.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for (int i = 0; i < tagChips.getChildCount(); i++) {
                    Chip chip = (Chip) tagChips.getChildAt(i);
                    chip.setChecked(isChecked);
                }
            }
        });

        // Chip group
        ArrayList<String> tags;
        tags = new ArrayList<String>();
        for (int i = 0; i < this.itemAdapter.getCount(); i++) {
            ArrayList<Tag> tagList = Objects.requireNonNull(this.itemAdapter.getItem(i)).getTags();
            for (int j = 0; j < tagList.size(); j++) {
                if (!tags.contains(tagList.get(j).getTagName())) {
                    tags.add(tagList.get(j).getTagName());
                    setupChip(tagList.get(j).getTagName(), this.tagChips);
                }
            }
        }
    }

    /**
     * Sets up button linking between sortField (sorting target) and sortOrder button groups. When
     * a sort target is selected, the sort order button appears and disappears if no target is
     * selected
     */
    private void setupSortListeners() {
        // Makes sort order button visible depending on whether a sort target is selected or not
        sortField.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            // If a sort target is selected and there wasn't a target previously
            if (isChecked && sortOrder.getVisibility()==View.INVISIBLE) {
                sortOrder.setVisibility(View.VISIBLE);
                // If the sort order has not been initialized, automatically set to ascending
                if (sortOrder.getCheckedButtonIds().size()==0) {
                    sortAsc.setActivated(true);
                    sortAsc.performClick();
                }
            // If there's no sort option selected
            } else if (group.getCheckedButtonIds().size()==0){
                sortOrder.setVisibility(View.INVISIBLE);
            }
        });

        // Makes sort order mandatory by not allowing button un-clicking
        sortOrder.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (group.getCheckedButtonIds().size()==0 && group.getVisibility()==View.VISIBLE) {
                Button button = view.findViewById(checkedId);
                button.setActivated(true);
                button.performClick();
            }
        });
    }

    /**
     * Gets all the checked chips in the given chip group
     * @param group chip group to get checked chips from
     * @return array list of checked chips in group
     */
    private ArrayList<String> getCheckedChips(ChipGroup group) {
        ArrayList<String> chips = new ArrayList<String>();
        for (int chipId : group.getCheckedChipIds()) {
            Chip chip = group.findViewById(chipId);
            chips.add(chip.getText().toString());
        }
        return chips;
    }

    /**
     * Gets both the date inputs.
     * @return arraylist of length 2 where the first item is the start date and the second is the
     *         end date. Adds a empty string if no date input is given.
     */
    private ArrayList<String> getDateInputs() {
        ArrayList<String> inputs = new ArrayList<String>();
        inputs.add(this.startDateInput.getText().toString());
        inputs.add(this.endDateInput.getText().toString());
        return inputs;
    }

}

