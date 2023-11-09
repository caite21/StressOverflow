package com.example.StressOverflow;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class AddTagToItemFragment extends DialogFragment{
    private OnFragmentInteractionListener listener;
    private ChipGroup chipGroup;
    private ArrayList<Tag> newTags = new ArrayList<>();
    private ArrayList<Tag> allTags = new ArrayList<>();
    private FirebaseFirestore db;
    private CollectionReference tagsRef;
    private Db tagDb;
    private String ownerName;
    public AddTagToItemFragment(String ownerName) {
        this.ownerName = ownerName;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        Button makeNewTag = view.findViewById(R.id.makeNewTag_button);
        makeNewTag.setOnClickListener(openTagList);
        chipGroup = view.findViewById(R.id.tagFragment_chipGroup);


        if (getArguments() != null) {
            allTags = (ArrayList<Tag>) getArguments().getSerializable("allTags");
        }
        for (Tag t: allTags){
            Chip chip = new Chip(getContext());
            chip.setText(t.getTagName());
            chip.setCheckedIconVisible(true);
            chip.setCheckable(true);
            chip.setActivated(false);
            chipGroup.addView(chip);
            chip.setOnClickListener(v -> chip.setActivated(!chip.isActivated()));
        }
        return builder.setView(view)
                .setTitle("AddTag")
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

//    private void createAndShowDialog(){
//        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_add_tag_to_item, null);
//        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//        Button makeNewTag = view.findViewById(R.id.makeNewTag_button);
//        makeNewTag.setOnClickListener(openTagList);
//        chipGroup = view.findViewById(R.id.tagFragment_chipGroup);
//
//        for (Tag t: allTags){
//            Chip chip = new Chip(getContext());
//            chip.setText(t.getTagName());
//            chip.setCheckedIconVisible(true);
//            chip.setCheckable(true);
//            chip.setActivated(false);
//            chipGroup.addView(chip);
//            chip.setOnClickListener(v -> chip.setActivated(!chip.isActivated()));
//        }
//        builder.setView(view)
//                .setTitle("AddTag")
//                .setNegativeButton("Cancel", null)
//                .setPositiveButton("OK", new DialogInterface.OnClickListener(){
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        for (int chipID : chipGroup.getCheckedChipIds()){
//                            Chip newChip = chipGroup.findViewById(chipID);
//                            Tag newTag = new Tag(newChip.getText().toString());
//                            newTags.add(newTag);
//                        }
//                        listener.addTagPressed(newTags);
//                        dialog.dismiss();
//                    }
//                }).create();
//
//        AlertDialog dialog = builder.create();
//        dialog.show();
//    }

}