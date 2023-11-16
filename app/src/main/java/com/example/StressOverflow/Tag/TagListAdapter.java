package com.example.StressOverflow.Tag;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.StressOverflow.AppGlobals;
import com.example.StressOverflow.Db;
import com.example.StressOverflow.R;

import java.util.ArrayList;

public class TagListAdapter extends ArrayAdapter<Tag> {
    private ArrayList<Tag> tags;
    private Context context;
    private Db db;
    private String ownerName;

    /**
     * Constructor for the adapter
     * @param context context of the adapter
     * @param tags list of all tags
     */
    public TagListAdapter(Context context, ArrayList<Tag> tags,Db db) {
        super(context, R.layout.listview_tag_content, tags);
        this.context = context;
        this.tags = tags;
        this.db = db;
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
        TextView tagName = convertView.findViewById(R.id.tagContent);
        tagName.setText(tag.getTagName());
        Button deleteButton = convertView.findViewById(R.id.deleteTag_button);

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
                        db.deleteTag(tag);
                        break;
                    }
                }
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
        db.addTag(tag);
        notifyDataSetChanged();
    }
}
