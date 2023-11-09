package com.example.StressOverflow;

import static java.security.AccessController.getContext;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class TagList extends AppCompatActivity implements AddTagFragment.OnFragmentInteractionListener {
    ArrayList<Tag> tagList = new ArrayList<>();
    Button addTag_button;

    Button back_button;
    TagListAdapter tagAdapter;
    private FirebaseFirestore db;
    private CollectionReference tagsRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_list);
        db = FirebaseFirestore.getInstance();
        tagsRef = db.collection("tags");
//        Tag Tag1 = new Tag("purple");
//        Tag Tag2 = new Tag("blue");
//        Tag Tag3 = new Tag("yellow");
//
//        List<Tag> tagstoAdd = Arrays.asList(Tag1,Tag2,Tag3);
//
//        tagList.addAll(tagstoAdd);
        addTag_button = findViewById(R.id.addTag_button);
        back_button = findViewById(R.id.tagListBack_button);
        addTag_button.setOnClickListener(addTag);
        back_button.setOnClickListener(backToMain);
        ListView tagListView = findViewById(R.id.tagListView);
        tagAdapter = new TagListAdapter(TagList.this, tagList);
        tagListView.setAdapter(tagAdapter);
        tagsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                        String tagName = doc.getId();
                        tagList.add(new Tag(tagName));
                    }
                    tagAdapter.notifyDataSetChanged();
                }
            }
        });
        //Intent intent = new Intent(this, TagFragment.class);
        //intent.putParcelableArrayListExtra("tagList", (ArrayList<? extends Parcelable>) tagList);

    }



    private View.OnClickListener addTag = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            new AddTagFragment().show(getSupportFragmentManager(), "ADD TAG");
        }
    };

    private Boolean Validate(String tagName){
        boolean valid = true;
        for (Tag t: tagList){
            if (t.getTagName().equals(tagName) || tagName.isEmpty()){
                valid = false;
                break;
            }
        }
        return valid;
    }

    private View.OnClickListener backToMain = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };

    @Override
    public void onOkPressed(Tag newTag) {
        String tagName = newTag.getTagName();
        boolean valid = Validate(tagName);
        if (valid){
            Tag tagToAdd = new Tag(tagName);
            tagAdapter.addTag(tagToAdd);
            HashMap<String, String> data = new HashMap<>();
            data.put("Tag", tagName);
            tagsRef.document(tagName).set(data);

        }else{
            Toast toast = Toast.makeText(this, "Duplicate/Invalid Tag Name", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}