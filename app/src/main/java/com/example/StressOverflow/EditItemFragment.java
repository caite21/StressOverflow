/**
 * Alert dialog displayed when user tries to add an item
 */
package com.example.StressOverflow;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.StressOverflow.Util;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

public class EditItemFragment extends DialogFragment {

    private EditText itemTitleField;
    private EditText itemMakeField;
    private EditText itemModelField;
    private EditText itemDescriptionField;
    private EditText itemDateField;
    private EditText itemYearField;
    private EditText itemMonthField;
    private EditText itemValueField;
    private EditText itemCommentsField;
    private EditText itemSerialField;
    private OnFragmentInteractionListener listener;
    private Item selectedItem;
    private int pos;

    public EditItemFragment(int pos, Item selectedItem) {
        this.selectedItem = selectedItem;
        this.pos = pos;
    }
    public interface OnFragmentInteractionListener {
        void onSubmitEdit(int id, Item item);
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
        itemYearField = view.findViewById(R.id.add__item__fragment__edit__year);
        itemMonthField = view.findViewById(R.id.add__item__fragment__edit__month);
        itemDateField = view.findViewById(R.id.add__item__fragment__edit__date);
        itemValueField = view.findViewById(R.id.add__item__fragment__edit__value);
        itemCommentsField = view.findViewById(R.id.add__item__fragment__edit__comment);
        itemSerialField = view.findViewById(R.id.add__item__fragment__edit__serial);

        itemTitleField.setText(this.selectedItem.getName());
        itemMakeField.setText(this.selectedItem.getMake());
        itemModelField.setText(this.selectedItem.getModel());
        itemDescriptionField.setText(this.selectedItem.getDescription());
        itemValueField.setText(Double.toString(selectedItem.getValue()));
        itemYearField.setText(this.selectedItem.getDateYear());
        itemMonthField.setText(this.selectedItem.getDateMonth());
        itemDateField.setText(this.selectedItem.getDateDate());
        itemCommentsField.setText(this.selectedItem.getComments());
        itemSerialField.setText(Integer.toString(selectedItem.getSerial()));

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        return builder
                .setView(view)
                .setTitle("Edit an item")
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
                                Integer.parseInt(itemDateField.getText().toString())
                        );
                        Double value = Double.valueOf(itemValueField.getText().toString());
                        String comments = itemCommentsField.getText().toString();
                        Integer serial = Integer.valueOf(itemSerialField.getText().toString());

                        try {
                            listener.onSubmitEdit(
                                    pos,
                                    new Item(
                                        selectedItem.getId(),
                                        title,
                                        make,
                                        model,
                                        desc,
                                        date,
                                        value,
                                        comments,
                                        selectedItem.getTags(),
                                        selectedItem.getPictures(),
                                        serial,
                                        selectedItem.getOwner()
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
}
