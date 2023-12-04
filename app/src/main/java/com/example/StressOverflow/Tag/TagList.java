package com.example.StressOverflow.Tag;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.StressOverflow.AppGlobals;
import com.example.StressOverflow.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

/**
 * Holds all the tag objects on the listview
 */
public class TagList extends AppCompatActivity implements AddTagFragment.OnFragmentInteractionListener {
    ArrayList<Tag> tagList = new ArrayList<>();
    Button addTag_button;
    Button back_button;
    TagListAdapter tagAdapter;
    private FirebaseFirestore db;
    private CollectionReference tagRef;
    private String ownerName;

    /**
     * sets up the event listeners of the different views on this activity
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_list);

        db = FirebaseFirestore.getInstance();
        tagRef = db.collection("tags");
        addTag_button = findViewById(R.id.activity_tag_list_add_tag_button);
        back_button = findViewById(R.id.activity_tag_list_back_button);
        addTag_button.setOnClickListener(addTag);
        back_button.setOnClickListener(backToMain);
        ListView tagListView = findViewById(R.id.activity_tag_list_listView);
        this.ownerName = AppGlobals.getInstance().getOwnerName();
        tagAdapter = new TagListAdapter(TagList.this, tagList);
        tagListView.setAdapter(tagAdapter);

        //displays tags that belong to the user on tagList Activity
        tagRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot querySnapshots,
                                @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                    return;
                }
                if (querySnapshots != null) {
                    tagList.clear();
                    for (QueryDocumentSnapshot doc: querySnapshots) {
                        String ownerNameTagName = doc.getId();
                        //Checks that the username is valid
                        if (!ownerNameTagName.isEmpty() && ownerNameTagName.contains(":")){
                            String[] parts = ownerNameTagName.split(":");
                            String tagName = parts[1];
                            String ownerName = parts[0];
                            if (ownerName.equals(AppGlobals.getInstance().getOwnerName())){
                                tagList.add(new Tag(tagName));
                            }
                        }
                        AppGlobals.getInstance().setAllTags(tagList);
                    }
                    tagAdapter.notifyDataSetChanged();
                }
            }
        });
    }


    /**
     * Called when the addTag button is clicked, shows user the add tag dialog.
     */
    private View.OnClickListener addTag = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            new AddTagFragment().show(getSupportFragmentManager(), "ADD TAG");
        }
    };

    /**
     * Simple error checking that checks for duplicate names and if the field is empty
     * @param tagName
     * @return if it is valid or not
     */
    private Boolean Validate(String tagName){
        boolean valid = true;
        for (Tag t: tagList){
            //convert the strings to lowercase to compare
            if ((t.getTagName().toLowerCase()).equals(tagName.toLowerCase()) || tagName.isEmpty()){
                valid = false;
                break;
            }
        }
        return valid;
    }

    /**
     * Called when back button is clicked, directs user to the activity they were last on
     */
    private View.OnClickListener backToMain = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };

    /**
     * Called when OK button on dialog is pressed, adds new tag to the listview and the database
     * @param newTag the new Tag that was entered in the dialog
     */
    @Override
    public void onOkPressed(Tag newTag) {
        String tagName = newTag.getTagName();
        boolean valid = Validate(tagName);
        if (valid){
            Tag tagToAdd = new Tag(tagName);
            tagAdapter.addTag(tagToAdd);

        }else{
            Toast toast = Toast.makeText(this, "Duplicate/Invalid Tag Name", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}