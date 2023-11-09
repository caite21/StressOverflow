package com.example.StressOverflow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class ImagesDisplayAdapter extends ArrayAdapter<Image> {
    private final Context context;
    private ArrayList<Image> images;

    public ImagesDisplayAdapter(Context context, ArrayList<Image> images) {
        super(context, 0, images);
        this.images = images;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.image_in_grid, parent, false);
        }

        Image image = images.get(position);
        ImageView imageView = view.findViewById(R.id.image);
        imageView.setImageBitmap(image.getBitmap());

        return view;
    }
}

