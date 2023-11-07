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
import android.view.ViewGroup;

import java.util.ArrayList;


public class TagFragment extends DialogFragment {
    private OnFragmentInteractionListener listener;
    public TagFragment() {
        // Required empty public constructor
    }

    public interface OnFragmentInteractionListener{
        void addTagPressed(ArrayList<Tag> tagsToAdd);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof TagFragment.OnFragmentInteractionListener) {
            listener = (TagFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException("activity lacks implementation of OnFragmentInteractionListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_tag, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());


        return builder.setView(view)
                .setTitle("AddTag")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //ArrayList<Tag> tagsToAdd = TagList.getTagList();
                    }
                }).create();
    }


}