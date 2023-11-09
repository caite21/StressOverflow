package com.example.StressOverflow;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertTrue;

import android.view.View;

import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class TagListTest {
    private CollectionReference tagsRef;
    private FirebaseFirestore firestore;
    Db database;
    @Before
    public void setUp() {
        firestore = FirebaseFirestore.getInstance();
        database = new Db(firestore);
        AppGlobals.getInstance().setOwnerName("testUser");
    }

    @Rule
    public ActivityScenarioRule<TagList> scenario =
            new ActivityScenarioRule<TagList>(TagList.class);

    @Test
    public void addToTagList() throws InterruptedException {
        String testTagName = "testTag";
        onView(ViewMatchers.withId(R.id.addTag_button)).perform(click());
        onView(withText("Add Tag")).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(ViewMatchers.withId(R.id.addTagTextView)).inRoot(isDialog()).perform(typeText(testTagName));
        onView(withText("OK")).inRoot(isDialog()).perform(click());
        onView(withId(R.id.addTagTextView)).check(doesNotExist());

        // Perform the query with a callback
        database.checkTagExist(new Tag(testTagName), new Db.TagExistCallback() {
            @Override
            public void onTagExist(boolean exists) {
                // Assert that the document exists
                assertTrue(exists);
            }
        });

    }

    @Test
    public void deleteFromTagList() throws InterruptedException {
        String testTagName = "deleteTag";
        onView(ViewMatchers.withId(R.id.addTag_button)).perform(click());
        onView(withText("Add Tag")).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(ViewMatchers.withId(R.id.addTagTextView)).inRoot(isDialog()).perform(typeText(testTagName));
        onView(withText("OK")).inRoot(isDialog()).perform(click());
        onView(withId(R.id.addTagTextView)).check(doesNotExist());

        // Perform the query with a callback
        database.checkTagExist(new Tag(testTagName), new Db.TagExistCallback() {
            @Override
            public void onTagExist(boolean exists) {
                // Assert that the document exists
                assertTrue(exists);
            }
        });

    }


}
