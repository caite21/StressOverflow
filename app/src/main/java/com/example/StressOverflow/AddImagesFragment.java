package com.example.StressOverflow;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

//import com.google.firebase.firestore.CollectionReference;
//import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;

public class AddImagesFragment extends DialogFragment  {
    private Uri imageUri;
    private ActivityResultLauncher<Intent> addImagesLauncher;
    private ArrayList<Bitmap> photos = new ArrayList<>();
//    private FirebaseFirestore db;
//    private CollectionReference itemsRef;
    private AddImagesFragment.OnFragmentInteractionListener listener;
    private ContentResolver contentResolver;
    private ArrayList<Image> imagesList;
    private ArrayAdapter<Image> imageAdapter;
    private GridView imageDisplay;
    private Image clickedImage;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof AddImagesFragment.OnFragmentInteractionListener) {
            listener = (AddImagesFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context + "OnFragmentInteractionListener is not implemented");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.select_image_fragment, null);
        contentResolver = requireContext().getContentResolver();

        // TODO: get pictures already attached from the database
        imagesList = new ArrayList<>();

        imageDisplay = view.findViewById(R.id.images_area);
        imageAdapter = new ImagesDisplayAdapter(requireContext(), imagesList);
        imageDisplay.setAdapter(imageAdapter);

        // TODO: get firebase collection
//        db = FirebaseFirestore.getInstance();
//        itemsRef = db.collection("items");

        imageDisplay.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // save which item is clicked
                clickedImage = imagesList.get(i);
            }
        });

        final Button delButton = view.findViewById(R.id.delete_button);
        delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (clickedImage != null) {
                    imagesList.remove(clickedImage);
                    imageAdapter.notifyDataSetChanged();
                    // TODO: update firestore
                }
            }
        });

        final Button addImageButton = view.findViewById(R.id.add_image_button);
        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageChooser();
            }
        });


        // TODO: addSnapShotListener for firestore


        addImagesLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // get image(s) from launcher and add it to images list
                        Intent data = result.getData();
                        if (data == null) return;

                        if (data.getClipData() != null) {
                            // One image selected from library
                            int itemCount = data.getClipData().getItemCount();
                            for (int i = 0; i < itemCount; i++) {
                                Uri image = data.getClipData().getItemAt(i).getUri();
                                Bitmap selectedBitmap = getBitmapFromUri(image);
                                imagesList.add(new Image(selectedBitmap));
                            }
                        } else if (data.getData() != null) {
                            // Multiple images selected from library
                            Uri image = data.getData();
                            Bitmap selectedBitmap = getBitmapFromUri(image);
                            imagesList.add(new Image(selectedBitmap));
                        } else {
                            // Image captured with camera
                            Bitmap selectedBitmap = getBitmapFromUri(imageUri);
                            imagesList.add(new Image(selectedBitmap));
                        }

                        // display image(s)
                        imageAdapter.notifyDataSetChanged();
                    }
                });


        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder
                .setView(view)
                .setTitle("Attach Images")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Confirm", (dialog, which) -> {
//                    listener.onConfirmAddImagesPressed(images);
                }).create();
    }

    public interface OnFragmentInteractionListener {
//        void onConfirmAddImagesPressed(ArrayList<Image> images);
    }

    private void openImageChooser() {
        // Intent to capture a photo
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Image details for saving
        String fileName = "image.jpg";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, fileName);
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image taken with camera");
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

        // Intent to pick photo(s)
        Intent pickPhotosIntent = new Intent(Intent.ACTION_GET_CONTENT);
        pickPhotosIntent.setType("image/*");
        pickPhotosIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        // Intent to choose which intent
        Intent chooser = new Intent(Intent.ACTION_CHOOSER);
        chooser.putExtra(Intent.EXTRA_INTENT, pickPhotosIntent);
        chooser.putExtra(Intent.EXTRA_TITLE, "Choose images or take a picture");
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{takePictureIntent});
        addImagesLauncher.launch(chooser);
    }

    private Bitmap getBitmapFromUri(Uri uri) {
        try {
            InputStream inputStream = contentResolver.openInputStream(uri);
            return BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
