package com.example.StressOverflow;

import static android.content.ContentValues.TAG;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSubstring;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.StressOverflow.Item.Item;
import com.example.StressOverflow.Item.ListActivity;
import com.example.StressOverflow.Tag.Tag;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

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

/**
 * Testing entering barcode (serial number) and the functionality of
 * receiving results and checking results
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class TestBarcodeLookup {
    private CollectionReference tagsRef;
    private FirebaseFirestore firestore;
    private String testTagName;
    private Item item;

    @Rule
    public ActivityScenarioRule<ListActivity> scenario =
            new ActivityScenarioRule<ListActivity>(ListActivity.class);


    /**
     * Create and add dummy item
     */
    @Before
    public void setUp() {
        firestore = FirebaseFirestore.getInstance();
        AppGlobals.getInstance().setOwnerName("testUser");

        ArrayList<Tag> testTags = new ArrayList<>();
        ArrayList<String> pictureURLs = new ArrayList<>();
        pictureURLs.add("https://firebasestorage.googleapis.com/v0/b/stressoverflow.appspot.com/o/images%2Fimage_1701480005690.jpg?alt=media&token=400b81e3-97da-4c1f-b1e0-98cceb4d3b62");
        item = new Item("testItem","make","model","description",
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
        SystemClock.sleep(2000);
    }

    /**
     * Forcefully deletes items. For use when test crashes app and
     * clean up fails
     */
    @Ignore("For forcefully cleaning up")
    public void deleteItems() {
        int listViewId = R.id.activity__item__list__item__list;
        SystemClock.sleep(4000);

        // delete 1
        onData(Matchers.anything())
                .inAdapterView(withId(listViewId))
                .atPosition(1)
                .onChildView(withId(R.id.listview__item__title))
                .perform(longClick());
        SystemClock.sleep(1000);

//         delete 2
        onData(Matchers.anything())
                .inAdapterView(withId(listViewId))
                .atPosition(2)
                .onChildView(withId(R.id.listview__item__title))
                .perform(longClick());
        SystemClock.sleep(1000);

        SystemClock.sleep(5000);
        onView(ViewMatchers.withId(R.id.activity__item__list__remove__item__button)).perform(click());
        SystemClock.sleep(2000);
    }


    /**
     * Add an item with a serial number: 12345 and check that it does not
     * receive a description, and check that the description is unchanged
     */
    @Test
    public void testInvalidBarcode() {
        SystemClock.sleep(2000);
        onView(withId(R.id.activity_item_list_add_item_button)).perform(click());
        SystemClock.sleep(3000);

        onView(withId(R.id.add__item__fragment__edit__serial)).
                perform(scrollTo());
        onView(withId(R.id.add__item__fragment__edit__serial)).perform(closeSoftKeyboard());
        onView(withId(R.id.add__item__fragment__edit__serial)).
                perform(ViewActions.typeText("12345"));
        SystemClock.sleep(2000);
        onView(withId(R.id.add__item__fragment__edit__serial)).perform(closeSoftKeyboard());
        onView(withId(R.id.add_item_fragment_button_lookup)).perform(click());

        SystemClock.sleep(7000);
        onView(withSubstring("Description:")).check(doesNotExist());
        onView(withSubstring("Earl Grey")).check(doesNotExist());
        onView(withId(R.id.add__item__fragment__edit__description)).check(matches(withText("")));

        SystemClock.sleep(2000);
        onView(withSubstring("Cancel")).perform(click());
        SystemClock.sleep(3000);
    }

    /**
     * Add an item with a serial number: 077652082272 and check
     * that it receives a description, check that it can select a description,
     * check that it enters the description, and check that the description
     * is as expected (Earl Grey Tea) and since the make and model were
     * not selected ensure that they are empty
     */
    @Test
    public void testDescriptionFoundFromAddItem() {
        onView(withId(R.id.activity_item_list_add_item_button)).perform(click());
        SystemClock.sleep(2000);
        onView(withId(R.id.add__item__fragment__edit__serial)).
                perform(scrollTo());
        onView(withId(R.id.add__item__fragment__edit__serial)).perform(closeSoftKeyboard());
        onView(withId(R.id.add__item__fragment__edit__serial)).
                perform(ViewActions.typeText("077652082272"));
        SystemClock.sleep(2000);
        onView(withId(R.id.add__item__fragment__edit__serial)).perform(closeSoftKeyboard());
        onView(withId(R.id.add_item_fragment_button_lookup)).perform(click());

        // wait for lookup result
        SystemClock.sleep(4000);
        onView(withSubstring("Description:")).perform(click());
        SystemClock.sleep(2000);
        onView(withSubstring("USE SELECTED")).perform(click());
        SystemClock.sleep(2000);
        onView(withId(R.id.add__item__fragment__edit__description)).check(matches(withSubstring("Earl Grey")));
        onView(withId(R.id.add__item__fragment__edit__make)).check(ViewAssertions.matches(withText("")));
        onView(withId(R.id.add__item__fragment__edit__model)).check(ViewAssertions.matches(withText("")));

        SystemClock.sleep(2000);
        onView(withSubstring("Cancel")).perform(click());
        SystemClock.sleep(2000);
    }

    /**
     * Edit an item with a serial number: 077652082272 and check
     * that it receives a description, check that it can select a description,
     * check that it enters the description, and check that the description
     * is overwritten as expected (Earl Grey Tea)
     */
    @Test
    public void testDescriptionFoundFromEditItem() {
        int listViewId = R.id.activity__item__list__item__list;
        SystemClock.sleep(3000);
        onData(Matchers.anything())
                .inAdapterView(withId(listViewId))
                .atPosition(0)
                .onChildView(withId(R.id.listview__item__title))
                .perform(click());

        SystemClock.sleep(2000);
        onView(withId(R.id.add__item__fragment__edit__serial)).
                perform(scrollTo());
        onView(withId(R.id.add__item__fragment__edit__serial)).perform(closeSoftKeyboard());

        // clear serial number
        onView(withId(R.id.add__item__fragment__edit__serial)).
                perform(ViewActions.clearText());
        onView(withId(R.id.add__item__fragment__edit__serial)).
                perform(ViewActions.typeText("077652082272"));

        SystemClock.sleep(2000);
        onView(withId(R.id.add__item__fragment__edit__serial)).perform(closeSoftKeyboard());
        onView(withId(R.id.add_item_fragment_button_lookup)).perform(click());
        SystemClock.sleep(4000);
        onView(withSubstring("Description:")).perform(click());
        SystemClock.sleep(2000);
        onView(withSubstring("USE SELECTED")).perform(click());
        SystemClock.sleep(2000);
        onView(withId(R.id.add__item__fragment__edit__description)).check(matches(withSubstring("Earl Grey")));

        SystemClock.sleep(2000);
        onView(withSubstring("Cancel")).perform(click());
        SystemClock.sleep(2000);
    }

}
