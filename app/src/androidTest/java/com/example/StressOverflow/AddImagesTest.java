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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.view.View;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.assertion.ViewAssertions;
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


import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class AddImagesTest {
    private CollectionReference tagsRef;
    private FirebaseFirestore firestore;
    private String testTagName;

    @Before
    public void setUp() {
        firestore = FirebaseFirestore.getInstance();

//        onView(withId(R.id.add__item__fragment__edit__pictures)).perform(click());
    }

    @Test
    public void addImageFromLibraryTest(){
        // from addItemFragment
        // click pictures
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

}
