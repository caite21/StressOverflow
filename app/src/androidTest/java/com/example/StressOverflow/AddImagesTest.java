package com.example.StressOverflow;

import static android.content.ContentValues.TAG;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSubstring;
import static org.hamcrest.Matchers.anything;
import static org.junit.Assert.assertEquals;

import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.StressOverflow.Item.Item;
import com.example.StressOverflow.Item.ListActivity;
import com.example.StressOverflow.Tag.Tag;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.UUID;


/**
 * Tests adding and deleting images which are attached to an item.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AddImagesTest {
    private CollectionReference itemsRef;
    private FirebaseFirestore firestore;
    private String testTagName;
    private Item item;


    @Rule
    public ActivityScenarioRule<ListActivity> listActivityRule = new ActivityScenarioRule<>(ListActivity.class);

    /**
     * Create and add dummy item
     */
    public void setUp(int numberOfPics) {
        firestore = FirebaseFirestore.getInstance();
        AppGlobals.getInstance().setOwnerName("testUser");

        ArrayList<Tag> testTags = new ArrayList<>();
        ArrayList<String> pictureURLs = new ArrayList<>();
        for (int i=0; i<numberOfPics; i++) {
            pictureURLs.add("https://firebasestorage.googleapis.com/v0/b/stressoverflow.appspot.com/o/images%2Fimage_1701667607368.jpg?alt=media&token=1df046d0-834f-4079-96ea-9f62e69e65b0"
            );
        }

        item = new Item("testUser","make","model","description",
                new GregorianCalendar(),10.0, "Comments",testTags,pictureURLs,
                "123456", AppGlobals.getInstance().getOwnerName());

        firestore.collection("items")
                .document(item.getId().toString())
                .set(item.toFirebaseObject())
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error with item insertion into collection items: ", e);
                        throw new RuntimeException("Error with item insertion into collection items: ", e);
                    }
                });
    }

    /**
     * Delete dummy item
     */
    public void cleanUp(){
        this.firestore.collection("items")
                .whereEqualTo("owner", "testUser")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            documentSnapshot.getReference().delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // Deletion successful
                                            Log.d(TAG, "Document successfully deleted");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Handle deletion failure
                                            Log.w(TAG, "Error deleting document", e);
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle query failure
                        Log.w(TAG, "Error getting documents: ", e);
                    }
                });
        String ownerName = AppGlobals.getInstance().getOwnerName();
        this.firestore.collection("tags")
                .document(String.format("%s:%s", ownerName, testTagName))
                .delete()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error with item deletion into collection items: ", e);
                        throw new RuntimeException("Error with item deletion into collection items: ", e);
                    }
                });

    }

    @Ignore("For forced cleaning up")
    public void deleteItems() {
        int listViewId = R.id.activity_item_list_item_list;
        SystemClock.sleep(4000);

        for (int i=0; i<6; i++) {
            onData(Matchers.anything())
                    .inAdapterView(withId(listViewId))
                    .atPosition(0)
                    .onChildView(withId(R.id.listview__item__title))
                    .perform(longClick());
            SystemClock.sleep(2000);
            onView(ViewMatchers.withId(R.id.activity_item_list_remove_item_button)).perform(click());
            SystemClock.sleep(3000);
        }

    }

    /**
     * test ability to attach 1 image to an item and test
     * that it is displayed when editing the after
     */
    @Test
    public void attachOneImageToItem(){
        setUp(1);

        // go to edit fragment for the item
        int listViewId = R.id.activity_item_list_item_list;
        SystemClock.sleep(3000);
        onData(anything())
                .inAdapterView(withId(listViewId))
                .atPosition(0)
                .onChildView(withId(R.id.listview__item__title))
                .perform(click());
        SystemClock.sleep(2000);

        // go to add pictures fragment
        onView(withId(R.id.add_item_fragment_edit_pictures)).
                perform(scrollTo());
        onView(withId(R.id.add_item_fragment_edit_pictures)).perform(click());
        // you'll see the images here
        SystemClock.sleep(2000);
        assertEquals(1, item.getPictureURLs().size());

        boolean isFailure = false;
        try {
            onData(anything())
                    .inAdapterView(withId(R.id.images_area))
                    .atPosition(0)
                    .perform(click());
        } catch (Exception e) {
            isFailure = true;
        }
        assert(!isFailure);

        onView(withSubstring("Cancel")).perform(click());
        SystemClock.sleep(1000);
        onView(withSubstring("Cancel")).perform(click());
        SystemClock.sleep(1000);
        cleanUp();
    }

    /**
     * test ability to attach 3 images to an item and test
     * that it is displayed when editing the item after
     */
    @Test
    public void attachMultipleImagesToItem(){
        setUp(3);

        // go to edit fragment for the item
        int listViewId = R.id.activity_item_list_item_list;
        SystemClock.sleep(2000);
        onData(anything())
                .inAdapterView(withId(listViewId))
                .atPosition(0)
                .onChildView(withId(R.id.listview__item__title))
                .perform(click());
        SystemClock.sleep(2000);

        // go to add pictures fragment
        onView(withId(R.id.add_item_fragment_edit_pictures)).
                perform(scrollTo());
        onView(withId(R.id.add_item_fragment_edit_pictures)).perform(click());
        // you'll see the images here
        SystemClock.sleep(3000);
        assertEquals(3, item.getPictureURLs().size());

        boolean isFailure = false;
        try {
            onData(anything())
                    .inAdapterView(withId(R.id.images_area))
                    .atPosition(2)
                    .perform(click());
        } catch (Exception e) {
            isFailure = true;
        }
        assert(!isFailure);

        onView(withSubstring("Cancel")).perform(click());
        SystemClock.sleep(1000);
        onView(withSubstring("Cancel")).perform(click());
        SystemClock.sleep(1000);
        cleanUp();
    }

    /**
     * test ability to delete an image from an item and test
     * that it is reflected when editing the item after
     */
    @Test
    public void deleteImageFromItem(){
        setUp(2);
        // view item's pics
        int listViewId = R.id.activity_item_list_item_list;
        SystemClock.sleep(3000);
        onData(anything())
                .inAdapterView(withId(listViewId))
                .atPosition(0)
                .onChildView(withId(R.id.listview__item__title))
                .perform(click());
        SystemClock.sleep(2000);
        onView(withId(R.id.add_item_fragment_edit_pictures)).
                perform(scrollTo());
        onView(withId(R.id.add_item_fragment_edit_pictures)).perform(click());
        // you'll see the images here
        SystemClock.sleep(2000);

        // delete a pic
        onData(anything())
                .inAdapterView(withId(R.id.images_area))
                .atPosition(0)
                .perform(click());
        SystemClock.sleep(1000);
        onView(withId(R.id.delete_button)).perform(click());
        SystemClock.sleep(1000);

        // check fragment saved
        onView(withSubstring("OK")).perform(click());
        SystemClock.sleep(2000);
        onView(withId(R.id.add_item_fragment_edit_pictures)).
                perform(scrollTo());
        onView(withId(R.id.add_item_fragment_edit_pictures)).perform(click());
        // you'll see the images here
        SystemClock.sleep(3000);

        // save in db
        onView(withSubstring("OK")).perform(click());
        SystemClock.sleep(1000);
        onView(withSubstring("OK")).perform(click());
        SystemClock.sleep(5000);

        // check saved in db
        onData(anything())
                .inAdapterView(withId(listViewId))
                .atPosition(0)
                .onChildView(withId(R.id.listview__item__title))
                .perform(click());
        SystemClock.sleep(2000);
        // go to add pictures fragment
        onView(withId(R.id.add_item_fragment_edit_pictures)).
                perform(scrollTo());
        onView(withId(R.id.add_item_fragment_edit_pictures)).perform(click());
        // you'll see the images here
        SystemClock.sleep(3000);

        boolean isFailure = false;
        try {
            onData(anything())
                    .inAdapterView(withId(R.id.images_area))
                    .atPosition(0)
                    .perform(click());
        } catch (Exception e) {
            isFailure = true;
        }
        assert(!isFailure);
        isFailure = false;
        try {
            onData(anything())
                    .inAdapterView(withId(R.id.images_area))
                    .atPosition(1)
                    .perform(click());
        } catch (Exception e) {
            isFailure = true;
        }
        assert(isFailure);

        onView(withSubstring("Cancel")).perform(click());
        SystemClock.sleep(1000);
        onView(withSubstring("Cancel")).perform(click());
        SystemClock.sleep(1000);
        cleanUp();
    }

    /**
     * test ability to delete every image from an item and test
     * that it is reflected when editing the item after
     */
    @Test
    public void deleteAllImagesFromItem(){
        setUp(3);
        // view item's pics
        int listViewId = R.id.activity_item_list_item_list;
        SystemClock.sleep(3000);
        onData(anything())
                .inAdapterView(withId(listViewId))
                .atPosition(0)
                .onChildView(withId(R.id.listview__item__title))
                .perform(click());
        SystemClock.sleep(2000);
        onView(withId(R.id.add_item_fragment_edit_pictures)).
                perform(scrollTo());
        onView(withId(R.id.add_item_fragment_edit_pictures)).perform(click());
        // you'll see the images here
        SystemClock.sleep(2000);

        // delete pics
        onData(anything())
                .inAdapterView(withId(R.id.images_area))
                .atPosition(0)
                .perform(click());
        SystemClock.sleep(1000);
        onView(withId(R.id.delete_button)).perform(click());
        SystemClock.sleep(1000);
        onData(anything())
                .inAdapterView(withId(R.id.images_area))
                .atPosition(0)
                .perform(click());
        SystemClock.sleep(1000);
        onView(withId(R.id.delete_button)).perform(click());
        SystemClock.sleep(1000);
        onData(anything())
                .inAdapterView(withId(R.id.images_area))
                .atPosition(0)
                .perform(click());
        SystemClock.sleep(1000);
        onView(withId(R.id.delete_button)).perform(click());
        SystemClock.sleep(1000);

        // check fragment saved
        onView(withSubstring("OK")).perform(click());
        SystemClock.sleep(2000);
        onView(withId(R.id.add_item_fragment_edit_pictures)).
                perform(scrollTo());
        onView(withId(R.id.add_item_fragment_edit_pictures)).perform(click());
        // you'll see the (no) images here
        SystemClock.sleep(3000);

        // save in db
        onView(withSubstring("OK")).perform(click());
        SystemClock.sleep(1000);
        onView(withSubstring("OK")).perform(click());
        SystemClock.sleep(5000);

        // check saved in db
        onData(anything())
                .inAdapterView(withId(listViewId))
                .atPosition(0)
                .onChildView(withId(R.id.listview__item__title))
                .perform(click());
        SystemClock.sleep(2000);
        // go to add pictures fragment
        onView(withId(R.id.add_item_fragment_edit_pictures)).
                perform(scrollTo());
        onView(withId(R.id.add_item_fragment_edit_pictures)).perform(click());
        // you'll see the images here
        SystemClock.sleep(3000);
        boolean isFailure = false;
        try {
            onData(anything())
                    .inAdapterView(withId(R.id.images_area))
                    .atPosition(0)
                    .perform(click());
        } catch (Exception e) {
            isFailure = true;
        }
        assert(isFailure);

        onView(withSubstring("Cancel")).perform(click());
        SystemClock.sleep(1000);
        onView(withSubstring("Cancel")).perform(click());
        SystemClock.sleep(1000);
        cleanUp();
    }

    /**
     * Check that there are the options and capabilities to add images
     * from the android camera or system library. These cannot be
     * tested with espresso as they are handled by the android system
     */
    @Test
    public void addImageFromAndroidResources(){
        SystemClock.sleep(2000);
        onView(withId(R.id.activity_item_list_add_item_button)).perform(click());
        SystemClock.sleep(2000);
        onView(withId(R.id.add_item_fragment_edit_pictures)).
                perform(scrollTo());
        onView(withId(R.id.add_item_fragment_edit_pictures)).perform(click());
        SystemClock.sleep(2000);

        onView(withId(R.id.add_image_button)).perform(click());
        // displays options, cannot be tested since handled by android system
        SystemClock.sleep(4000);
    }

}
