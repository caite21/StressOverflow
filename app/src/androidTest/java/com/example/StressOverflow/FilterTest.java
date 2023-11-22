package com.example.StressOverflow;

import static android.content.ContentValues.TAG;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withChild;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.google.common.base.Predicates.not;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import android.content.Context;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.StressOverflow.Item.Item;
import com.example.StressOverflow.Item.ListActivity;
import com.example.StressOverflow.Item.ItemListAdapter;
import com.example.StressOverflow.Tag.Tag;
import com.example.StressOverflow.Image.Image;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.FirebaseFirestore;


import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.GregorianCalendar;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class FilterTest {
    private FirebaseFirestore firestore;
    Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    Item item1;
    Item item2;
    Item item3;

    public ItemListAdapter getAdapter() {
        return new ItemListAdapter(appContext, new ArrayList<Item>());
    }

    @Before
    public void setUp() {
        firestore = FirebaseFirestore.getInstance();
        AppGlobals.getInstance().setOwnerName("testUser");

        ArrayList<Tag> testTags = new ArrayList<>();
        ArrayList<String> images = new ArrayList<>();

        //Add a tag first
        testTags.add(new Tag("testTag1"));
        item1 = new Item("Test1","make1","model","this is the first tag", new GregorianCalendar(2023, 1, 1),77.00, "Comments",testTags,images,
                123456, AppGlobals.getInstance().getOwnerName());
        item2 = new Item("Test2","make1","model","this is the second tag", new GregorianCalendar(2022, 1, 1),77.00, "Comments",testTags,images,
                123456, AppGlobals.getInstance().getOwnerName());
        testTags.add(new Tag("testTag2"));
        item3 = new Item("Test3","make2","model","this is the second second tag", new GregorianCalendar(2001, 1, 1),77.00, "Comments",testTags,images,
                123456, AppGlobals.getInstance().getOwnerName());


    }

    @After
    public void cleanUp() {
        firestore.collection("items").document(item1.getId().toString())
            .delete()
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Error with item deletion on collection items: ", e);
                    throw new RuntimeException("Error with item deletion on collection items: ", e);
                }
            });
        firestore.collection("items").document(item2.getId().toString())
            .delete()
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Error with item deletion on collection items: ", e);
                    throw new RuntimeException("Error with item deletion on collection items: ", e);
                }
            });
        firestore.collection("items").document(item3.getId().toString())
            .delete()
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Error with item deletion on collection items: ", e);
                    throw new RuntimeException("Error with item deletion on collection items: ", e);
                }
            });
    }

    @Rule
    public ActivityScenarioRule<ListActivity> scenario =
            new ActivityScenarioRule<ListActivity>(ListActivity.class);

    @Test
    public void testKeywordFilter() throws InterruptedException {
        onView(withId(R.id.activity__item__list__filter__item__button)).perform(click());

        onView(withId(R.id.filter__dialog__keywords__textedit)).perform(typeText("second"));
        onView(withId(R.id.filter__dialog__keywords__textedit)).perform(pressImeActionButton());
        onView(ViewMatchers.isRoot()).perform(ViewActions.closeSoftKeyboard());
        onView(withId(R.id.filter__dialog__filter__btn)).inRoot(isDialog()).perform(click());

        onData(is(instanceOf(Item.class)))
            .inAdapterView(withId(R.id.activity__item__list__item__list)).atPosition(0)
            .onChildView(withId(R.id.listview__item__title))
            .check(matches(withText("Test2")));
        onData(is(instanceOf(Item.class)))
            .inAdapterView(withId(R.id.activity__item__list__item__list)).atPosition(1)
            .onChildView(withId(R.id.listview__item__title))
            .check(matches(withText("Test3")));
        // idk how to check length of listview
    }

    // Espresso is unable to find chip group
    @Test
    public void testMakeFilter() throws InterruptedException {
        onView(withId(R.id.activity__item__list__filter__item__button)).perform(click());
        onView(withId(R.id.filter__dialog__filter__btn)).inRoot(isDialog()).perform(click());

        // I don't know why make and tags don't populate the first click (it works manually)
        onView(withId(R.id.activity__item__list__filter__item__button)).perform(click());
        onView(allOf(withText("make1"), isDescendantOfA(withId(R.id.filter__dialog__makes__chipgroup)))).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.filter__dialog__back__btn)).inRoot(isDialog()).perform(click());
        Thread.sleep(1000);

        onData(is(instanceOf(Item.class)))
            .inAdapterView(withId(R.id.activity__item__list__item__list)).atPosition(0)
            .onChildView(withId(R.id.listview__item__title))
            .check(matches(withText("Test2")));
    }

    // Works manually but I can't get it working on espresso
//    @Test
//    public void testTagFilter() throws InterruptedException {
//        onView(withId(R.id.activity__item__list__filter__item__button)).perform(click());
//        onView(withId(R.id.filter__dialog__filter__btn)).inRoot(isDialog()).perform(click());
//
//        // I don't know why make and tags don't populate the first click (it works manually)
//        onView(withId(R.id.activity__item__list__filter__item__button)).perform(click());
//        onView(allOf(withText("testTag2"), isDescendantOfA(withId(R.id.filter__dialog__tags__chipgroup)))).perform(click());
//        Thread.sleep(1000);
//        onView(withId(R.id.filter__dialog__back__btn)).inRoot(isDialog()).perform(click());
//        Thread.sleep(1000);
//
//        onData(is(instanceOf(Item.class)))
//            .inAdapterView(withId(R.id.activity__item__list__item__list)).atPosition(0)
//            .onChildView(withId(R.id.listview__item__title))
//            .check(matches(withText("Test3")));
//    }
}
