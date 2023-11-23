package com.example.StressOverflow.Tag;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.example.StressOverflow.AppGlobals;
import com.example.StressOverflow.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * Called when a user wants to add a tag to an already existing item
 */
public class AddTagToItemFragment extends DialogFragment{
    private OnFragmentInteractionListener listener;
    private ChipGroup chipGroup;
    private ArrayList<Tag> newTags = new ArrayList<>();
    private ArrayList<Tag> allTags = new ArrayList<>();
    private FirebaseFirestore db;
    private CollectionReference tagsRef;
    private String ownerName;


    public AddTagToItemFragment() {
        this.ownerName = AppGlobals.getInstance().getOwnerName();
    }


    /**
     * Interface for interaction between the AddTagFragment and the hosting activity
     */
    public interface OnFragmentInteractionListener{
        /**
         * Called when OK on addTagToItem dialog is clicked
         * @param tagsToAdd the tags selected by user
         */
        void addTagPressed(ArrayList<Tag> tagsToAdd);
    }


    /**
     * Called when the fragment is attached to a context to ensure that the hosting activity
     * implements the necessary interaction listener interface.
     * @param context context to which the fragment is attached
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof AddTagToItemFragment.OnFragmentInteractionListener) {
            listener = (AddTagToItemFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException("activity lacks implementation of OnFragmentInteractionListener");
        }
    }


    /**
     * Create dialog and its contents
     * @param savedInstanceState The last saved instance state of the Fragment,
     * or null if this is a freshly created Fragment.
     *
     * @return
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_add_tag_to_item, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        Button makeNewTag = view.findViewById(R.id.fragment_add_tag_to_item_make_new_tag_button);
        Button refreshTag = view.findViewById(R.id.fragment_add_tag_to_item_refresh_tag_button);

        makeNewTag.setOnClickListener(openTagList);
        chipGroup = view.findViewById(R.id.fragment_add_tag_to_item_tag_chipGroup);

        allTags = AppGlobals.getInstance().getAllTags();
        addTagsToChipGroup();
        refreshTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTagsToChipGroup();
            }
        });

        return builder.setView(view)
                .setTitle("Add Tag")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (int chipID : chipGroup.getCheckedChipIds()){
                            Chip newChip = chipGroup.findViewById(chipID);
                            Tag newTag = new Tag(newChip.getText().toString());
                            newTags.add(newTag);
                        }
                        listener.addTagPressed(newTags);
                    }
                }).create();
    }

    private void addTagsToChipGroup(){
        allTags = AppGlobals.getInstance().getAllTags();
        chipGroup.removeAllViews();
        //add all the tags as chips in the dialog
        for (Tag t: allTags){
            Chip chip = new Chip(getContext());
            chip.setText(t.getTagName());
            chip.setCheckedIconVisible(true);
            chip.setCheckable(true);
            chip.setActivated(false);
            chipGroup.addView(chip);
            chip.setOnClickListener(v -> chip.setActivated(!chip.isActivated()));
        }
    }
    /**
     * Direct user to master TagList to add new tags.
     */
    private View.OnClickListener openTagList = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), TagList.class);
            startActivity(intent);
        }
    };

}