package com.example.StressOverflow;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddTagFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddTagFragment extends DialogFragment {
    private OnFragmentInteractionListener listener;
    private TextView addTagTextView;

    private Tag newTag;
    public AddTagFragment() {
        // Required empty public constructor
    }


    public interface OnFragmentInteractionListener {
        void onOkPressed(Tag newTag);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof AddTagFragment.OnFragmentInteractionListener){
            listener = (AddTagFragment.OnFragmentInteractionListener) context ;
        }else{
            throw new RuntimeException(context.toString()+ " must implement OnFragmentInteractionListener");
        }

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_add_tag,null);
        addTagTextView = view.findViewById(R.id.addTagTextView);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder
                .setView(view)
                .setTitle("Add Tag")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newTagName = addTagTextView.getText().toString();
                        if (newTagName.isEmpty()){
                            newTagName = "";
                        }
                        listener.onOkPressed(new Tag(newTagName));
                    }
                }).create();
    }
}