package com.example.StressOverflow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TagListAdapter extends ArrayAdapter<Tag> {
    private ArrayList<Tag> tags;
    private Context context;

    public TagListAdapter(Context context, ArrayList<Tag> tags) {
        super(context, R.layout.listview_tag_content, tags);
        this.context = context;
        this.tags = tags;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView ==null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.listview_tag_content, parent,false);
        }
        Tag tag = getItem(position);
        TextView tagName = convertView.findViewById(R.id.tagContent);
        tagName.setText(tag.getTagName());
        Button deleteButton = convertView.findViewById(R.id.deleteTag_button);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tagName = tags.get(position).getTagName();
                for(Tag t : tags){
                    if (t.getTagName() == tagName){
                        tags.remove(t);
                        notifyDataSetChanged();
                        break;
                    }
                }
            }
        });
        return convertView;
    }

    public void addTag(Tag tag) {
        this.tags.add(tag);
        this.notifyDataSetChanged();
    }
}
