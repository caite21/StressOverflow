package com.example.StressOverflow;

import static android.content.ContentValues.TAG;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.pressBack;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.StressOverflow.Image.Image;
import com.example.StressOverflow.Image.ImagesDisplayAdapter;
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


import junit.framework.AssertionFailedError;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
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
        pictureURLs.add("https://firebasestorage.googleapis.com/v0/b/stressoverflow.appspot.com/o/images%2Fimage_1701480005690.jpg?alt=media&token=400b81e3-97da-4c1f-b1e0-98cceb4d3b62");
        item = new Item("testItem","make","model","description",
                new GregorianCalendar(),10.0, "Comments",testTags,pictureURLs,
                123456L, AppGlobals.getInstance().getOwnerName());

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
    public void ListActivitytoTagList(){
        onView(withId(R.id.showTagList_button)).perform(click());
        onView(ViewMatchers.withId(R.id.activity_tag_list_add_tag_button)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void editItemToAddImagesFragment() {
//        int listViewId = R.id.activity__item__list__item__list;
//        SystemClock.sleep(2000);
//
//        onData(Matchers.anything())
//                .inAdapterView(withId(listViewId))
//                .atPosition(0)
//                .onChildView(withId(R.id.listview__item__title))
//                .perform(longClick());
//        onView(ViewMatchers.withId(R.id.activity__item__list__add__tag__button)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
//        onView(ViewMatchers.withId(R.id.activity__item__list__add__tag__button)).perform(click());
//        SystemClock.sleep(2000);
//
//        onView(withId(R.id.add__item__fragment__edit__pictures))
//                .perform(scrollTo())
//                .perform(click());
//        SystemClock.sleep(4000);

                // go to image selection
        onData(is(instanceOf(Item.class)))
                .inAdapterView(withId(R.id.activity__item__list__item__list)).atPosition(0)
                .onChildView(withId(R.id.listview__item__title))
                .perform(click());
        SystemClock.sleep(2000);
        onView(withId(R.id.add__item__fragment__edit__pictures))
                .perform(scrollTo())
                .perform(click());
    }

    @Test
    public void deleteImagesTest(){
//        editItemToAddImagesFragment();
//        // go to image selection
//        onData(is(instanceOf(Item.class)))
//                .inAdapterView(withId(R.id.activity__item__list__item__list)).atPosition(0)
//                .onChildView(withId(R.id.listview__item__title))
//                .perform(click());
//        SystemClock.sleep(2000);
//        onView(withId(R.id.add__item__fragment__edit__pictures))
//                .perform(scrollTo())
//                .perform(click());
//        SystemClock.sleep(4000);
//        // click on the very first picture (fails if it doesn't exist)
//        onData(anything())
//                .inAdapterView(withId(R.id.images_area))
//                .atPosition(0)
//                .perform(click());
//
//        SystemClock.sleep(2000);
//        onView(withId(R.id.delete_button))
//                .perform(click());
//        // go back to ListActivity
//        SystemClock.sleep(2000);
//        onView(withText("OK")). perform(pressBack());
//        SystemClock.sleep(2000);
//        onView(withText("OK")). perform(pressBack());
//        SystemClock.sleep(2000);
//
//        // same process
//        onData(is(instanceOf(Item.class)))
//                .inAdapterView(withId(R.id.activity__item__list__item__list)).atPosition(0)
//                .onChildView(withId(R.id.listview__item__title))
//                .perform(click());
//        SystemClock.sleep(2000);
//        onView(withId(R.id.add__item__fragment__edit__pictures))
//                .perform(scrollTo())
//                .perform(click());
//        SystemClock.sleep(4000);
//        // check if the first image is not displayed
//        onData(anything())
//                .inAdapterView(withId(R.id.images_area))
//                .atPosition(0)
//                .check(matches(not(isDisplayed())));
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
