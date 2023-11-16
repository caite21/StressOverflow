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

import androidx.annotation.NonNull;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.StressOverflow.Item.Item;
import com.example.StressOverflow.Tag.Tag;
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
        onView(ViewMatchers.withId(R.id.addTag_button)).perform(click());
        onView(withText("Add Tag")).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(ViewMatchers.withId(R.id.addTagTextView)).inRoot(isDialog()).perform(typeText(testTagName));
        onView(withText("Cancel")).inRoot(isDialog()).perform(click());
        onView(withId(R.id.addTagTextView)).check(doesNotExist());

        // Perform the query with a callback
        final boolean[] bool = new boolean[1];
        tagRef
                .whereEqualTo("documentID", String.format("%s:%s", ownerName, testTagName))
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()){
                            bool[0] = false;
                        }else{
                            bool[0] = true;
                        }
                    }

                });
        assertTrue(bool[0]);
    }
    @Test
    public void addToTagList() throws InterruptedException {
        testTagName = "testTag";
        onView(ViewMatchers.withId(R.id.addTag_button)).perform(click());
        onView(withText("Add Tag")).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(ViewMatchers.withId(R.id.addTagTextView)).inRoot(isDialog()).perform(typeText(testTagName));
        onView(withText("OK")).inRoot(isDialog()).perform(click());
        onView(withId(R.id.addTagTextView)).check(doesNotExist());

        // Perform the query with a callback
        // Perform the query with a callback
        final boolean[] bool = new boolean[1];
        tagRef
                .whereEqualTo("documentID", String.format("%s:%s", ownerName, testTagName))
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()){
                            bool[0] = false;
                        }else{
                            bool[0] = true;
                        }
                    }

                });
        assertTrue(bool[0]);
    }


    @Test
    public void deleteFromTagList() throws InterruptedException {
        testTagName = "deleteTag";
        onView(ViewMatchers.withId(R.id.addTag_button)).perform(click());
        onView(withText("Add Tag")).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(ViewMatchers.withId(R.id.addTagTextView)).inRoot(isDialog()).perform(typeText(testTagName));
        onView(withText("OK")).inRoot(isDialog()).perform(click());
        onView(withId(R.id.addTagTextView)).check(doesNotExist());

        int listViewId = R.id.tagListView;
        int deleteButtonId = R.id.deleteTag_button;

        // Perform a click on the delete button in the first row of the ListView.
        Espresso.onData(Matchers.anything())
                .inAdapterView(withId(listViewId))
                .atPosition(0)
                .onChildView(withId(deleteButtonId))
                .perform(click());

        // Perform the query with a callback
        // Perform the query with a callback
        final boolean[] bool = new boolean[1];
        tagRef
                .whereEqualTo("documentID", String.format("%s:%s", ownerName, testTagName))
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()){
                            bool[0] = false;
                        }else{
                            bool[0] = true;
                        }
                    }

                });
        assertFalse(bool[0]);
    }


}
