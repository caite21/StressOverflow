package com.example.StressOverflow;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TagList extends AppCompatActivity {
    ArrayList<Tag> tagList = new ArrayList<>();;
    Button addTag_button;

    TextView addTagTextView;
    Button confirmAdd_button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_list);
        Tag Tag1 = new Tag("purple");
        Tag Tag2 = new Tag("blue");
        Tag Tag3 = new Tag("yellow");

        List<Tag> tagstoAdd = Arrays.asList(Tag1,Tag2,Tag3);

        tagList.addAll(tagstoAdd);
        addTag_button = findViewById(R.id.addTag_button);
        addTagTextView = findViewById(R.id.addTagTextView);
        confirmAdd_button = findViewById(R.id.confirmAdd_button);
        addTag_button.setOnClickListener(enterText);
        confirmAdd_button.setOnClickListener(addTag);
        ListView tagListView = findViewById(R.id.tagListView);
        TagListAdapter tagAdapter = new TagListAdapter(TagList.this, tagList);
        tagListView.setAdapter(tagAdapter);

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
            Tag tagToAdd = new Tag(tagName);
            tagList.add(tagToAdd);
        }
    };


}