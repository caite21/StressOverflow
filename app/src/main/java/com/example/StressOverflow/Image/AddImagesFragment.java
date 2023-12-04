package com.example.StressOverflow.Image;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.StressOverflow.Item.Item;
import com.example.StressOverflow.R;
import com.example.StressOverflow.Util;


/**
 * Fragment displays an item's images and allows the user to add
 * multiple images from their library, add a picture with the
 * camera, and delete images
 */
public class AddImagesFragment extends DialogFragment  {
    private Uri imageUri;
    private ActivityResultLauncher<Intent> addPicturesLauncher;
    private OnFragmentInteractionListener listener;
    private ContentResolver contentResolver;
    private ArrayList<Image> imagesList;
    private ArrayList<String> URLs;
    private ArrayList<String> URLsToDelete;
    private ArrayAdapter<Image> imageAdapter;
    private GridView imageDisplay;
    private Image clickedImage;
    private Item item;


    /**
     * Fragment listener must receive updated list of images
     */
    public interface OnFragmentInteractionListener {
        void onConfirmImages(ArrayList<Image> imagesList, ArrayList<String> deleteURLs);
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
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (OnFragmentInteractionListener) context;
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
        URLsToDelete = new ArrayList<>();

        imagesList = new ArrayList<>();
        if (item != null ) {
            // get already attached pictures
            imagesList.addAll(item.getPictures());
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
                    if (clickedImage.getURL() != null) {
                        URLsToDelete.add(clickedImage.getURL());
                    }
                    clickedImage = null;
                }
            }
        });

        final Button firstButton = view.findViewById(R.id.move_to_first_button);
        firstButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (clickedImage != null) {
                    imagesList.remove(clickedImage);
                    imagesList.add(0, clickedImage);
                    imageAdapter.notifyDataSetChanged();
                }
            }
        });


        final Button addImageButton = view.findViewById(R.id.add_image_button);
        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    openImageChooser();
                }
                catch (Exception e) {
                    Log.d("IMAGES", "Android system picture intents failed: ", e);
                }
            }
        });

        addPicturesLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // get image(s) from launcher and add it to images list
                        Intent data = result.getData();
                        if (data == null) return;

                        if (data.getClipData() != null) {
                            Util.showShortToast(getContext(), "Ying: Got mult Uri from library");
                            // Pictures selected from library
                            int itemCount = data.getClipData().getItemCount();
                            for (int i = 0; i < itemCount; i++) {
                                Uri image = data.getClipData().getItemAt(i).getUri();
                                Bitmap selectedBitmap = getBitmapFromUri(image);
                                imagesList.add(new Image(selectedBitmap));
                            }
                            imageAdapter.notifyDataSetChanged();
                        } else if (data.getData() != null) {
                            Util.showShortToast(getContext(), "Ying: Got 1 Uri from library");
                            // Picture selected from library
                            Uri image = data.getData();
                            Bitmap selectedBitmap = getBitmapFromUri(image);
                            imagesList.add(new Image(selectedBitmap));
                            imageAdapter.notifyDataSetChanged();
                        } else if (imageUri != null) {
                            Util.showShortToast(getContext(), "Ying: Got Uri type from camera");
                            // Image captured with camera
                            Bitmap selectedBitmap = getBitmapFromUri(imageUri);
                            imagesList.add(new Image(selectedBitmap));
                            imageAdapter.notifyDataSetChanged();
                        } else if (data.getExtras() != null) {
                            Util.showShortToast(getContext(), "Ying: thumbnail method");
                            // Failed to write image captured with camera, but can still save it
                            Bitmap capturedBitmap = (Bitmap) data.getExtras().get("data");
                            imagesList.add(new Image(capturedBitmap));
                            imageAdapter.notifyDataSetChanged();
                        } else {
                            Util.showShortToast(getContext(), "Image Error: result is null");
                        }

                        // display image(s)
//                        imageAdapter.notifyDataSetChanged();
                    }
                });


        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder
                .setView(view)
                .setTitle("Attach Pictures")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("OK", (dialog, which) -> {
                    item.setPictures(imagesList);
                    // upload list of images then send URLs to ListActivity to be added to an Item
                    listener.onConfirmImages(imagesList, URLsToDelete);
                }).create();
    }

    /**
     * A pop-up that gives the options of adding images by taking a picture with the
     * camera or selecting pictures from the library. Also asks for camera permission
     * if necessary.
     */
    private void openImageChooser() {
        // ask for camera permission
        final int CAMERA_PERMISSION_REQUEST_CODE = 100;
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }

        // Intent to capture a photo
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Image details for saving
//        String fileName = "image.jpg";
//        ContentValues values = new ContentValues();
//        values.put(MediaStore.Images.Media.TITLE, fileName);
//        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

        // Intent to pick photo(s)
        Intent pickPicturesIntent = new Intent(Intent.ACTION_GET_CONTENT);
        pickPicturesIntent.setType("image/*");
        pickPicturesIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        // Intent to choose which intent
        Intent chooser = new Intent(Intent.ACTION_CHOOSER);
        chooser.putExtra(Intent.EXTRA_INTENT, pickPicturesIntent);
        chooser.putExtra(Intent.EXTRA_TITLE, "Select images or take a picture");
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{takePictureIntent});
        addPicturesLauncher.launch(chooser);
    }

    /**
     * Converts uri to bitmap
     *
     * @param uri to convert to bitmap
     * @return bitmap of uri
     */
    private Bitmap getBitmapFromUri(Uri uri) {
        try {
//            InputStream inputStream = contentResolver.openInputStream(uri);
//            return BitmapFactory.decodeStream(inputStream);

            Bitmap bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri);
            Bitmap rotatedBitmap = ExifRotateBitmap(contentResolver, uri, bitmap);

//            Matrix matrix = new Matrix();
//            matrix.postRotate(90);
//            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            return rotatedBitmap;
        } catch (IOException e) {
            Log.d("IMAGES", "Converting picture to bitmap failed");
            Util.showShortToast(getContext(), "Image Error: Converting picture to bitmap failed or rotation failed.");
            return null;
        }
    }

    private Bitmap ExifRotateBitmap(ContentResolver contentResolver, Uri uri, Bitmap bitmap) throws IOException {
        if (bitmap == null)
            return null;

        InputStream inputStream = contentResolver.openInputStream(uri);
        ExifInterface ei = new ExifInterface(inputStream);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        float rotationInDegrees = exifToDegrees(orientation);
        if (rotationInDegrees == 0) {
            return bitmap;
        }

        Matrix matrix = new Matrix();
        matrix.preRotate(rotationInDegrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);


    }

    /**
     * Gets the Amount of Degress of rotation using the exif integer to determine how much
     * we should rotate the image. This is from:
     * https://stackoverflow.com/questions/29971319/image-orientation-android
     * @param exifOrientation - the Exif data for Image Orientation
     * @return - how much to rotate in degress
     */
    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) { return 90; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {  return 180; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {  return 270; }
        return 0;
    }

}
