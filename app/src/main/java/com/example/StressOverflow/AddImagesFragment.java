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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.UUID;

import android.app.AlertDialog;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.FirebaseFirestore;


/**
 * Fragment displays an item's images and allows the user to add
 * multiple images from their library, add a picture with the
 * camera, and delete images
 */
public class AddImagesFragment extends DialogFragment  {
    private Uri imageUri;
    private ActivityResultLauncher<Intent> addImagesLauncher;
    private ArrayList<Bitmap> photos = new ArrayList<>();
    private FirebaseFirestore db;
    private OnFragmentInteractionListener listener;
    private ContentResolver contentResolver;
    private ArrayList<Image> imagesList;
    private ArrayAdapter<Image> imageAdapter;
    private GridView imageDisplay;
    private Image clickedImage;
    private UUID itemUUID;
    private Item item;


    /**
     * Fragment listener must receive updated list of images
     */
    public interface OnFragmentInteractionListener {
        void onConfirmImages(ArrayList<Image> images);
    }


    public AddImagesFragment() {
        // required
    }

    /**
     * View images of an item
     *
     * @param item which contains the images
     */
    public AddImagesFragment(Item item) {
        // for editing an item
        this.item = item;
        itemUUID = null;
    }

    /**
     * View images of a uuid
     *
     * @param uuid which is associated with the images
     */
    public AddImagesFragment(UUID uuid) {
        // for editing an item
        item = null;
        this.itemUUID = uuid;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (OnFragmentInteractionListener) context;

        // do not want to check if the activity is implementing the interface
//        if (context instanceof AddImagesFragment.OnFragmentInteractionListener) {
//            listener = (AddImagesFragment.OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context + "OnFragmentInteractionListener is not implemented");
//        }
    }

    /**
     * Displays images in the grid and implements Add and Delete buttons.
     *
     * @param savedInstanceState The last saved instance state of the Fragment,
     * or null if this is a freshly created Fragment.
     * @return built dialog fragment
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.select_image_fragment, null);
        contentResolver = requireContext().getContentResolver();
        Db db = new Db(FirebaseFirestore.getInstance());

        if (item == null & itemUUID != null) {
            item = db.getItem(itemUUID);
        }

        // Get pictures already attached pictures
        if (item != null ) {
            imagesList = item.getPictures();
        }

        imageDisplay = view.findViewById(R.id.images_area);
        imageAdapter = new ImagesDisplayAdapter(requireContext(), imagesList);
        imageDisplay.setAdapter(imageAdapter);

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
                    listener.onConfirmImages(imagesList);
                }).create();
    }

    /**
     * A pop-up that gives the options of adding images by taking a picture with the
     * camera or selecting pictures from the library
     */
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

    /**
     * Converts uri to bitmap
     *
     * @param uri to convert to bitmap
     * @return bitmap of uri
     */
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
