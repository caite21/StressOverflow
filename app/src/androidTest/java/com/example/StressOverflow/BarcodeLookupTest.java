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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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
 * Tests entering barcode (serial number) and the functionality of
 * receiving results and checking that the result is correct
 * and that a user can choose to use the found description
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class BarcodeLookupTest {
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
    }

    /**
     * Forcefully deletes items. For use when test crashes app and
     * clean up fails
     */
    @Ignore("For forced cleaning up")
    public void deleteItems() {
        int listViewId = R.id.activity_item_list_item_list;
        SystemClock.sleep(4000);

        // delete 1
        onData(Matchers.anything())
                .inAdapterView(withId(listViewId))
                .atPosition(1)
                .onChildView(withId(R.id.listview__item__title))
                .perform(longClick());
        SystemClock.sleep(1000);

        // delete 2
        onData(Matchers.anything())
                .inAdapterView(withId(listViewId))
                .atPosition(2)
                .onChildView(withId(R.id.listview__item__title))
                .perform(longClick());
        SystemClock.sleep(1000);

        SystemClock.sleep(5000);
        onView(ViewMatchers.withId(R.id.activity_item_list_remove_item_button)).perform(click());
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

        onView(withId(R.id.add_item_fragment_edit_serial)).
                perform(scrollTo());
        onView(withId(R.id.add_item_fragment_edit_serial)).perform(closeSoftKeyboard());
        onView(withId(R.id.add_item_fragment_edit_serial)).
                perform(ViewActions.typeText("12345"));
        SystemClock.sleep(2000);
        onView(withId(R.id.add_item_fragment_edit_serial)).perform(closeSoftKeyboard());
        onView(withId(R.id.add_item_fragment_button_lookup)).perform(click());

        SystemClock.sleep(7000);
        onView(withSubstring("Description:")).check(doesNotExist());
        onView(withSubstring("Earl Grey")).check(doesNotExist());
        onView(withId(R.id.add_item_fragment_edit_description)).check(matches(withText("")));

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
        onView(withId(R.id.add_item_fragment_edit_serial)).
                perform(scrollTo());
        onView(withId(R.id.add_item_fragment_edit_serial)).perform(closeSoftKeyboard());
        onView(withId(R.id.add_item_fragment_edit_serial)).
                perform(ViewActions.typeText("077652082272"));
        SystemClock.sleep(2000);
        onView(withId(R.id.add_item_fragment_edit_serial)).perform(closeSoftKeyboard());
        onView(withId(R.id.add_item_fragment_button_lookup)).perform(click());

        // wait for lookup result
        SystemClock.sleep(4000);
        onView(withSubstring("Description:")).perform(click());
        SystemClock.sleep(2000);
        onView(withSubstring("USE SELECTED")).perform(click());
        SystemClock.sleep(2000);
        onView(withId(R.id.add_item_fragment_edit_description)).check(matches(withSubstring("Earl Grey")));
        onView(withId(R.id.add_item_fragment_edit_make)).check(ViewAssertions.matches(withText("")));
        onView(withId(R.id.add_item_fragment_edit_model)).check(ViewAssertions.matches(withText("")));

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
        int listViewId = R.id.activity_item_list_item_list;
        SystemClock.sleep(3000);
        onData(Matchers.anything())
                .inAdapterView(withId(listViewId))
                .atPosition(0)
                .onChildView(withId(R.id.listview__item__title))
                .perform(click());

        SystemClock.sleep(2000);
        onView(withId(R.id.add_item_fragment_edit_serial)).
                perform(scrollTo());
        onView(withId(R.id.add_item_fragment_edit_serial)).perform(closeSoftKeyboard());

        // clear serial number
        onView(withId(R.id.add_item_fragment_edit_serial)).
                perform(ViewActions.clearText());
        onView(withId(R.id.add_item_fragment_edit_serial)).
                perform(ViewActions.typeText("077652082272"));

        SystemClock.sleep(2000);
        onView(withId(R.id.add_item_fragment_edit_serial)).perform(closeSoftKeyboard());
        onView(withId(R.id.add_item_fragment_button_lookup)).perform(click());
        SystemClock.sleep(4000);
        onView(withSubstring("Description:")).perform(click());
        SystemClock.sleep(2000);
        onView(withSubstring("USE SELECTED")).perform(click());
        SystemClock.sleep(2000);
        onView(withId(R.id.add_item_fragment_edit_description)).check(matches(withSubstring("Earl Grey")));

        SystemClock.sleep(2000);
        onView(withSubstring("Cancel")).perform(click());
        SystemClock.sleep(2000);
    }

}
