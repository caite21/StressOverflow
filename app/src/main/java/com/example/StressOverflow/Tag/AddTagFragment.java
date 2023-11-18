package com.example.StressOverflow.Tag;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.StressOverflow.R;

/**
 * This fragment is called when a user wants to create a new tag
 */
public class AddTagFragment extends DialogFragment {
    private OnFragmentInteractionListener listener;
    private TextView addTagTextView;

    private Tag newTag;

    /**
     * Constructor
     */
    public AddTagFragment() {
        // Required empty public constructor
    }

    /**
     * Interface for interaction between the AddTagFragment and the hosting activity.
     */
    public interface OnFragmentInteractionListener {
        /**
         * Called when user clicks on OK from dialog
         * @param newTag the new Tag that was entered in the dialog
         */
        void onOkPressed(Tag newTag);
    }

    /**
     * Called when the fragment is attached to a context to ensure that the hosting activity
     * implements the necessary interaction listener interface.
     * @param context context to which the fragment is attached
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof AddTagFragment.OnFragmentInteractionListener){
            listener = (AddTagFragment.OnFragmentInteractionListener) context ;
        }else{
            throw new RuntimeException(context.toString()+ " must implement OnFragmentInteractionListener");
        }

    }

    /**
     * Creates the dialog and its contents
     * @param savedInstanceState The last saved instance state of the Fragment,
     * or null if this is a freshly created Fragment.
     *
     * @return the created dialog
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_add_tag,null);
        addTagTextView = view.findViewById(R.id.fragment_add_tag_textView);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder
                .setView(view)
                .setTitle("Add Tag")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    /**
                     * Adds the tag to the tagList
                     * @param dialog the dialog that received the click
                     * @param which the button that was clicked (ex.
                     *              {@link DialogInterface#BUTTON_POSITIVE}) or the position
                     *              of the item clicked
                     */
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newTagName = addTagTextView.getText().toString();
                        //simple error check
                        if (newTagName.isEmpty()){
                            newTagName = "";
                        }
                        listener.onOkPressed(new Tag(newTagName));
                    }
                }).create();
    }
}