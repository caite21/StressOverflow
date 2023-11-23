package com.example.StressOverflow.Tag;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.StressOverflow.AppGlobals;
import com.example.StressOverflow.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class TagListAdapter extends ArrayAdapter<Tag> {
    private ArrayList<Tag> tags;
    private Context context;
    private String ownerName;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference tagRef;
    /**
     * Constructor for the adapter
     * @param context context of the adapter
     * @param tags list of all tags
     */
    public TagListAdapter(Context context, ArrayList<Tag> tags) {
        super(context, R.layout.listview_tag_content, tags);
        this.context = context;
        this.tags = tags;
        this.tagRef = this.db.collection("tags");
        this.ownerName = AppGlobals.getInstance().getOwnerName();
    }

    /**
     * Set up the components of each tag, sets the event listeners
     * @param position The position of the item within the adapter's data set of the item whose view
     *        we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *        is non-null and of an appropriate type before using. If it is not possible to convert
     *        this view to display the correct data, this method can create a new view.
     *        Heterogeneous lists can specify their number of view types, so that this View is
     *        always of the right type (see {@link #getViewTypeCount()} and
     *        {@link #getItemViewType(int)}).
     * @param parent The parent that this view will eventually be attached to
     * @return
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView ==null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.listview_tag_content, parent,false);
        }
        Tag tag = getItem(position);
        TextView tagName = convertView.findViewById(R.id.listview_tag_content_textView);
        tagName.setText(tag.getTagName());
        Button deleteButton = convertView.findViewById(R.id.listview_delete_tag_button);

        /**
         * Is called when the deleteButton is clicked on for a tag
         * this method must be in getView in order to get the correct tag
         */
        deleteButton.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String tagName = tag.getTagName();
                for (Tag t: tags){
                    if (tagName.equals(t.getTagName())){
                        tags.remove(t);
                        String ownerName = AppGlobals.getInstance().getOwnerName();
                        tagRef
                                .document(String.format("%s:%s", ownerName, tagName))
                                .delete()
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error with item deletion into collection items: ", e);
                                        throw new RuntimeException("Error with item deletion into collection items: ", e);
                                    }
                                });
                        CollectionReference items = db.collection("items");
                        Query query = items.whereEqualTo("owner", ownerName).whereArrayContains("tags", tagName);
                        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        // Remove the tag from the 'tags' array in 'items' collection
                                        DocumentReference docRef = items.document(document.getId());
                                        docRef.update("tags", FieldValue.arrayRemove(tagName))
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w(TAG, "Error updating document: ", e);
                                                        throw new RuntimeException("Error updating document: ", e);
                                                    }
                                                });

                                    }
                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });
                        break;
                    }
                }
                AppGlobals.getInstance().setAllTags(tags);
                notifyDataSetChanged();
            }
        });

        return convertView;
    }




    /**
     * Add tag to tag list, called when addTag button is clicked from the AddTag dialog
     * @param tag new tag to add
     */
    public void addTag(Tag tag) {
        tags.add(tag);

        String ownerName = AppGlobals.getInstance().getOwnerName();
        String tagName = tag.getTagName();
        tagRef
                .document(String.format("%s:%s", ownerName, tagName))
                .set(tag.toFirebaseObject(ownerName))
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error with item insertion into collection items: ", e);
                        throw new RuntimeException("Error with item insertion into collection items: ", e);
                    }
                });
        notifyDataSetChanged();
    }
}
