package com.example.StressOverflow;

import static android.content.ContentValues.TAG;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.StressOverflow.Tag.TagList;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;


import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;
import java.util.concurrent.CountDownLatch;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class TagListTest {
    private FirebaseFirestore db;
    private String testTagName;
    private CollectionReference tagRef;
    private String ownerName;
    @Before
    public void setUp() {
        db = FirebaseFirestore.getInstance();
        tagRef = db.collection("tags");
        AppGlobals.getInstance().setOwnerName("testUser");
        ownerName = "testUser";

    }

    @After
    public void cleanUp(){
        tagRef
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

    @Rule
    public ActivityScenarioRule<TagList> tagListRule =
            new ActivityScenarioRule<TagList>(TagList.class);

    @Test
    public void cancelAddToTagList() throws InterruptedException {
        testTagName = "cancelTestTag";
        onView(ViewMatchers.withId(R.id.activity_tag_list_add_tag_button)).perform(click());
        onView(withText("Add Tag")).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(ViewMatchers.withId(R.id.fragment_add_tag_textView)).inRoot(isDialog()).perform(typeText(testTagName));
        onView(withText("Cancel")).inRoot(isDialog()).perform(click());
        onView(withId(R.id.fragment_add_tag_textView)).check(doesNotExist());

        // Perform the query with a callback
        final boolean[] bool = new boolean[1];
        final CountDownLatch latch = new CountDownLatch(1);

        tagRef
                .document(String.format("%s:%s", ownerName, testTagName))
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        bool[0] = true;
                        // Process the data here
                    } else {
                        // Document doesn't exist
                        bool[0] = false;
                    }
                    latch.countDown();
                });
        try {
            latch.await(); // Wait for the latch to count down to 0
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertFalse(bool[0]);
        onView(withId(R.id.activity_tag_list_listView)).check(matches(hasChildCount(0)));
    }
    @Test
    public void addToTagList() throws InterruptedException {
        testTagName = "testTag";
        onView(ViewMatchers.withId(R.id.activity_tag_list_add_tag_button)).perform(click());
        onView(withText("Add Tag")).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(ViewMatchers.withId(R.id.fragment_add_tag_textView)).inRoot(isDialog()).perform(typeText(testTagName));
        onView(withText("OK")).inRoot(isDialog()).perform(click());
        onView(withId(R.id.fragment_add_tag_textView)).check(doesNotExist());

        // Perform the query with a callback
        final boolean[] bool = new boolean[1];
        final CountDownLatch latch = new CountDownLatch(1);

        tagRef
                .document(String.format("%s:%s", ownerName, testTagName))
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        bool[0] = true;
                        // Process the data here
                    } else {
                        // Document doesn't exist
                        bool[0] = false;
                    }
                    latch.countDown();
                });
        try {
            latch.await(); // Wait for the latch to count down to 0
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(bool[0]);
        onView(withId(R.id.activity_tag_list_listView)).check(matches(hasChildCount(1)));

    }

    @Test
    public void addDuplicateTag(){
        testTagName = "testTag";
        onView(ViewMatchers.withId(R.id.activity_tag_list_add_tag_button)).perform(click());
        onView(withText("Add Tag")).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(ViewMatchers.withId(R.id.fragment_add_tag_textView)).inRoot(isDialog()).perform(typeText(testTagName));
        onView(withText("OK")).inRoot(isDialog()).perform(click());
        onView(withId(R.id.fragment_add_tag_textView)).check(doesNotExist());

        onView(ViewMatchers.withId(R.id.activity_tag_list_add_tag_button)).perform(click());
        onView(withText("Add Tag")).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(ViewMatchers.withId(R.id.fragment_add_tag_textView)).inRoot(isDialog()).perform(typeText(testTagName));
        onView(withText("OK")).inRoot(isDialog()).perform(click());
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] bool = new boolean[1];
        tagRef
                .document(String.format("%s:%s", ownerName, testTagName))
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        bool[0] = true;
                        // Process the data here
                    } else {
                        // Document doesn't exist
                        bool[0] = false;
                    }
                    latch.countDown();
                });
        try {
            latch.await(); // Wait for the latch to count down to 0
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(bool[0]);
        onView(withId(R.id.activity_tag_list_listView)).check(matches(hasChildCount(1)));

    }


    @Test
    public void deleteFromTagList() throws InterruptedException {
        testTagName = "deleteTag";
        onView(ViewMatchers.withId(R.id.activity_tag_list_add_tag_button)).perform(click());
        onView(withText("Add Tag")).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(ViewMatchers.withId(R.id.fragment_add_tag_textView)).inRoot(isDialog()).perform(typeText(testTagName));
        onView(withText("OK")).inRoot(isDialog()).perform(click());
        onView(withId(R.id.fragment_add_tag_textView)).check(doesNotExist());

        int listViewId = R.id.activity_tag_list_listView;
        int deleteButtonId = R.id.listview_delete_tag_button;

        // Perform a click on the delete button in the first row of the ListView.
        Espresso.onData(Matchers.anything())
                .inAdapterView(withId(listViewId))
                .atPosition(0)
                .onChildView(withId(deleteButtonId))
                .perform(click());


        final boolean[] bool = new boolean[1];
        final CountDownLatch latch = new CountDownLatch(1);
        tagRef
                .document(String.format("%s:%s", ownerName, testTagName))
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        bool[0] = true;
                    } else {
                        // Document doesn't exist
                        bool[0] = false;
                    }
                    latch.countDown();
                });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertFalse(bool[0]);
        onView(withId(R.id.activity_tag_list_listView)).check(matches(hasChildCount(0)));

    }



}
