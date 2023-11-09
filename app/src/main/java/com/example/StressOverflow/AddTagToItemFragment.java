package com.example.StressOverflow;

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

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class AddTagToItemFragment extends DialogFragment {
    private OnFragmentInteractionListener listener;
    private ChipGroup chipGroup;
    private ArrayList<Tag> newTags = new ArrayList<>();
    public AddTagToItemFragment() {
    }

    public interface OnFragmentInteractionListener{
        void addTagPressed(ArrayList<Tag> tagsToAdd);
    }

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
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_add_tag_to_item, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        //Hardcoded tagList
        ArrayList<Tag> tags = new ArrayList<>();
        Tag tag1 = new Tag("Tag1");
        Tag tag2 = new Tag("Tag2");
        Tag tag3 = new Tag("Tag3");
        List<Tag> tagstoAdd = Arrays.asList(tag1,tag2,tag3);
        tags.addAll(tagstoAdd);
        Button makeNewTag = view.findViewById(R.id.makeNewTag_button);
        makeNewTag.setOnClickListener(openTagList);
        chipGroup = view.findViewById(R.id.tagFragment_chipGroup);

        for (Tag t: tags){
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

    private View.OnClickListener openTagList = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), TagList.class);
            startActivity(intent);
        }
    };

}