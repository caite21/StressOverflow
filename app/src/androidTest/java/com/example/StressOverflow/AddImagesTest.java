package com.example.StressOverflow;

import static android.content.ContentValues.TAG;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.StressOverflow.Image.Image;
import com.example.StressOverflow.Item.Item;
import com.example.StressOverflow.Item.ListActivity;
import com.example.StressOverflow.Tag.Tag;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.UUID;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class AddImagesTest {
    private CollectionReference tagsRef;
    private FirebaseFirestore firestore;
    private String testTagName;
    private Item item;


    @Before
    public void setUp() {
        firestore = FirebaseFirestore.getInstance();
        AppGlobals.getInstance().setOwnerName("testUser");

        ArrayList<Tag> testTags = new ArrayList<>();
        ArrayList<String> pictureURLs = new ArrayList<>();
        item = new Item("testItem","make","model","description",
                new GregorianCalendar(),10.0, "Comments",testTags,pictureURLs,
                123456, AppGlobals.getInstance().getOwnerName());

        firestore.collection("items").document(item.getId().toString())
                .set(item.toFirebaseObject())
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error with item insertion into collection items: ", e);
                        throw new RuntimeException("Error with item insertion into collection items: ", e);
                    }
                });

    }

    @After
    public void cleanUp(){
        UUID uuid = item.getId();
        this.firestore.collection("items")
                .document(uuid.toString())
                .delete()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error with item insertion into collection items: ", e);
                        throw new RuntimeException("Error with item insertion into collection items: ", e);
                    }
                });

    }
    @Rule
    public ActivityScenarioRule<ListActivity> listActivityRule = new ActivityScenarioRule<>(ListActivity.class);

    @Test
    public void ListActivityToEditFragmentToAddImagesFragment(){
        onView(withId(R.id.activity__item__list__edit__item__button)).perform(click());
//        onView(ViewMatchers.withId(R.id.fragment_add_edit_item)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void addImageFromLibraryTest(){
        // from addItemFragment
        onView(withId(R.id.activity__item__list__edit__item__button)).perform(click());

        // fill in all text boxes

        // click pictures
        onView(withId(R.id.add__item__fragment__edit__pictures)).perform(click());
        // check that count is 0

        // click add
        // click from files
        // select one
        // check that count is 1
        onView(withId(R.id.add_image_button)).perform(click());

    }

    @Test
    public void addMultipleImagesFromLibraryTest(){
        // click pictures
        // check that count is 0
        // click add
        // click from files
        // select 3
        // check that count is 3
    }

    @Test
    public void addImageFromCameraTest() throws InterruptedException {
        // click pictures
        // check that count is 0
        // click add
        // click from camera
        // take photo
        // check that count is 1
    }

    @Test
    public void addFromCameraAndLibraryTest(){
        // click pictures
        // check that count is 0
        // click add
        // click from files
        // select 3

        // click add
        // click from camera
        // take photo
        // check that count is 4
    }

    @Test
    public void deleteImagesTest(){
        // add 3 pictures
        // check count is 3
        // click one
        // click delete
        // check count is 2
        // click one
        // click delete
        // check count is 1
    }

    @Test
    public void imagesSavedToDatabaseTest(){
        // add images to item
        // close
        // edit item
        // click pictures
        // check that all expected pictures are there/compare bitmaps
    }

    @Test
    public void imagesDeletedFromDatabaseTest(){
        // add images to item
        // close
        // edit item
        // click pictures
        // check that all expected pictures are there/compare bitmaps
    }

    @Test
    public void imagesSavedToStorageTest(){
        // add images to item
        // close
        // edit item
        // click pictures
        // check that all expected pictures are there/compare bitmaps
    }

    @Test
    public void imagesDeletedFromStorageTest(){
        // add images to item
        // close
        // edit item
        // click pictures
        // check that all expected pictures are there/compare bitmaps
    }

    // also test going back and forth from fragments with Cancel button
}
