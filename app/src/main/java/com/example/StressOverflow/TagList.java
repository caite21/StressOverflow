package com.example.StressOverflow;

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

public class TagList extends AppCompatActivity {
    ArrayList<Tag> tagList = new ArrayList<>();
    Button addTag_button;

    TextView addTagTextView;
    Button confirmAdd_button;
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
        addTagTextView = findViewById(R.id.addTagTextView);
        confirmAdd_button = findViewById(R.id.confirmAdd_button);
        addTag_button.setOnClickListener(enterText);
        confirmAdd_button.setOnClickListener(addTag);
        back_button = findViewById(R.id.tagListBack_button);

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

    private View.OnClickListener enterText = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            addTagTextView.setVisibility(View.VISIBLE);
            confirmAdd_button.setVisibility(View.VISIBLE);
        }
    };

    private View.OnClickListener addTag = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            String tagName = String.valueOf(addTagTextView.getText());
            boolean valid = Validate(tagName);
            if (valid){
                Tag tagToAdd = new Tag(tagName);
                tagAdapter.addTag(tagToAdd);
                HashMap<String, String> data = new HashMap<>();
                data.put("Tag", tagName);
                tagsRef.document(tagName).set(data);
                addTagTextView.setText(null);
                addTagTextView.setVisibility(View.GONE);
                confirmAdd_button.setVisibility(View.GONE);
            }else{
                addTagTextView.setError("Duplicate name, choose another name");
            }
        }
    };

    private Boolean Validate(String tagName){
        boolean valid = true;
        for (Tag t: tagList){
            if (t.getTagName().equals(tagName)){
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

}