package com.example.StressOverflow;

import static android.content.ContentValues.TAG;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Checks.checkNotNull;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.espresso.matcher.RootMatchers;
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


import org.hamcrest.Description;
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
/**
 * Tests the Filter Dialog Fragment's UI and the filtering/sorting of the list activity.
 */
public class FilterTest {
    private FirebaseFirestore firestore;
    private CollectionReference tagRef;
    private CollectionReference itemRef;
    private String ownerName;
    Item item1;
    Item item2;
    Item item3;

    /**
     * Populates list activity and firestore with test data.
     */
    @Before
    public void setUp() {
        firestore = FirebaseFirestore.getInstance();

        ArrayList<Tag> testTags = new ArrayList<>();
        ArrayList<String> images = new ArrayList<>();

        tagRef = firestore.collection("tags");
        itemRef = firestore.collection("items");
        ownerName = "testUser";
        AppGlobals.getInstance().setOwnerName(ownerName);

        Tag tag1 = new Tag("testTag");
        testTags.add(tag1);
        tagRef.document(String.format("%s:%s", ownerName, "testTag"))
            .set(tag1.toFirebaseObject(ownerName))
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Error with item insertion into collection items: ", e);
                    throw new RuntimeException("Error with item insertion into collection items: ", e);
                }
            });

        ArrayList<Tag> tagList = new ArrayList<>();
        tagList.add(tag1);
        item1 = new Item("Test1", "make", "model", "this is the first item",
            new GregorianCalendar(2023, 1, 1),5.00,
            "Comments", tagList, images, "123456", AppGlobals.getInstance().getOwnerName());
        itemRef.document(item1.getId().toString())
            .set(item1.toFirebaseObject())
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Error with item insertion into collection items: ", e);
                    throw new RuntimeException("Error with item insertion into collection items: ", e);
                }
            });

        Tag tag2 = new Tag("testTag2");
        testTags.add(tag2);
        tagList.add(tag2);
        tagRef.document(String.format("%s:%s", ownerName, "testTag2"))
                .set(tag2.toFirebaseObject(ownerName))
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error with item insertion into collection items: ", e);
                        throw new RuntimeException("Error with item insertion into collection items: ", e);
                    }
                });

        item2 = new Item("Test2", "make1", "model", "this is the second item",
            new GregorianCalendar(2022, 1, 1), 6.00, "Comments",
                tagList, images, "123456", AppGlobals.getInstance().getOwnerName());
        itemRef.document(item2.getId().toString())
            .set(item2.toFirebaseObject())
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Error with item insertion into collection items: ", e);
                    throw new RuntimeException("Error with item insertion into collection items: ", e);
                }
            });

        tagList.remove(tag1);
        item3 = new Item("Test3", "make2", "model", "this is the second add 1 and last item",
                new GregorianCalendar(2001, 1, 1),7.00,
                "Comments", tagList, images, "123456", AppGlobals.getInstance().getOwnerName());
        itemRef.document(item3.getId().toString())
                .set(item3.toFirebaseObject())
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error with item insertion into collection items: ", e);
                        throw new RuntimeException("Error with item insertion into collection items: ", e);
                    }
                });
    }

    /**
     * Removes firestore test data.
     */
    @After
    public void cleanUp() {
        itemRef
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

        tagRef
            .document(String.format("%s:%s", ownerName, "TestTag1"))
            .delete()
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Error with item deletion into collection items: ", e);
                    throw new RuntimeException("Error with item deletion into collection items: ", e);
                }
            });
        tagRef
                .document(String.format("%s:%s", ownerName, "TestTag2"))
                .delete()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error with item deletion into collection items: ", e);
                        throw new RuntimeException("Error with item deletion into collection items: ", e);
                    }
                });
    }

    /**
     * Sets the initial activity to the ListActivity.
     */
    @Rule
    public ActivityScenarioRule<ListActivity> scenario =
            new ActivityScenarioRule<ListActivity>(ListActivity.class);

    /**
     * Tests keyword autocomplete and filtering.
     */
    @Test
    public void testKeywordFilter() {
        onView(withId(R.id.activity_item_list_filter_item_button)).perform(click());

        // Test autocomplete
        onView(withId(R.id.fragment_filter_items_keywords_textedit)).perform(typeText("se"));
        onView(withText("second")).inRoot(RootMatchers.isPlatformPopup()).perform(click());
        onView(withId(R.id.fragment_filter_items_keywords_textedit)).perform(pressImeActionButton());
        onView(ViewMatchers.isRoot()).perform(ViewActions.closeSoftKeyboard());
        onView(withText("Filter/Sort")).inRoot(isDialog()).perform(click());

        // Test filtering
        onData(allOf(is(instanceOf(Item.class)), withItemText("Test2")))
            .inAdapterView(withId(R.id.activity_item_list_item_list))
            .onChildView(withId(R.id.listview__item__title))
            .check(matches(isDisplayed()));
        onData(allOf(is(instanceOf(Item.class)), withItemText("Test3")))
            .inAdapterView(withId(R.id.activity_item_list_item_list))
            .onChildView(withId(R.id.listview__item__title))
            .check(matches(isDisplayed()));
        onView(withId(R.id.activity_item_list_item_list))
                .check(matches(withItemCount(2)));
        onView(withId(R.id.activity_item_list_cost_sum_text)).check(matches(withText("$13.00")));
    }

    /**
     * Tests removing the keyword chip from filtering selection.
     */
    @Test
    public void testRemoveKeywordFilter() {
        onView(withId(R.id.activity_item_list_filter_item_button)).perform(click());

        onView(withId(R.id.fragment_filter_items_keywords_textedit)).perform(typeText("second"));
        onView(withId(R.id.fragment_filter_items_keywords_textedit)).perform(pressImeActionButton());

        // Tests removing a filter
        onView(withId(R.id.fragment_filter_items_keywords_textedit)).perform(typeText("last"));
        onView(withId(R.id.fragment_filter_items_keywords_textedit)).perform(pressImeActionButton());
        onView(ViewMatchers.isRoot()).perform(ViewActions.closeSoftKeyboard());
        onView(allOf(withText("last"), isDescendantOfA(withId(R.id.fragment_filter_items_keywords_chipgroup)))).perform(click());

        onView(withText("Filter/Sort")).inRoot(isDialog()).perform(click());

        // Tests filtering
        onData(allOf(is(instanceOf(Item.class)), withItemText("Test2")))
                .inAdapterView(withId(R.id.activity_item_list_item_list))
                .onChildView(withId(R.id.listview__item__title))
                .check(matches(isDisplayed()));
        onData(allOf(is(instanceOf(Item.class)), withItemText("Test3")))
                .inAdapterView(withId(R.id.activity_item_list_item_list))
                .onChildView(withId(R.id.listview__item__title))
                .check(matches(isDisplayed()));
        onView(withId(R.id.activity_item_list_item_list))
                .check(matches(withItemCount(2)));
        onView(withId(R.id.activity_item_list_cost_sum_text)).check(matches(withText("$13.00")));
    }

    /**
     * Tests multiple keywords.
     */
    @Test
    public void testManyKeywordFilter() {
        onView(withId(R.id.activity_item_list_filter_item_button)).perform(click());

        // Tests multiple filters
        onView(withId(R.id.fragment_filter_items_keywords_textedit)).perform(typeText("second"));
        onView(withId(R.id.fragment_filter_items_keywords_textedit)).perform(pressImeActionButton());
        onView(withId(R.id.fragment_filter_items_keywords_textedit)).perform(typeText("last"));
        onView(withId(R.id.fragment_filter_items_keywords_textedit)).perform(pressImeActionButton());
        onView(ViewMatchers.isRoot()).perform(ViewActions.closeSoftKeyboard());
        onView(withText("Filter/Sort")).inRoot(isDialog()).perform(click());

        // Tests filtering
        onData(allOf(is(instanceOf(Item.class)), withItemText("Test3")))
                .inAdapterView(withId(R.id.activity_item_list_item_list))
                .onChildView(withId(R.id.listview__item__title))
                .check(matches(isDisplayed()));
        onView(withId(R.id.activity_item_list_item_list))
                .check(matches(withItemCount(1)));
        onView(withId(R.id.activity_item_list_cost_sum_text)).check(matches(withText("$7.00")));
    }

    /**
     * Tests filtering after a date.
     */
    @Test
    public void testStartDateFilter() {
        onView(withId(R.id.activity_item_list_filter_item_button)).perform(click());

        onView(withId(R.id.fragment_filter_items_start_date_edittext)).perform(click());
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName())))
                .inRoot(isDialog())
                .perform(setDate(2023, 1, 1));
        onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click());
        onView(withText("Filter/Sort")).inRoot(isDialog()).perform(click());

        onData(allOf(is(instanceOf(Item.class)), withItemText("Test1")))
            .inAdapterView(withId(R.id.activity_item_list_item_list))
            .onChildView(withId(R.id.listview__item__title))
            .check(matches(isDisplayed()));
        onView(withId(R.id.activity_item_list_item_list))
            .check(matches(withItemCount(1)));
        onView(withId(R.id.activity_item_list_cost_sum_text)).check(matches(withText("$5.00")));
    }

    /**
     * Tests filtering before a date.
     */
    @Test
    public void testEndDateFilter() {
        onView(withId(R.id.activity_item_list_filter_item_button)).perform(click());

        onView(withId(R.id.fragment_filter_items_end_date_edittext)).perform(click());
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName())))
                .inRoot(isDialog())
                .perform(setDate(2023, 2, 1));
        onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click());
        onView(withText("Filter/Sort")).inRoot(isDialog()).perform(click());

        onData(allOf(is(instanceOf(Item.class)), withItemText("Test2")))
                .inAdapterView(withId(R.id.activity_item_list_item_list))
                .onChildView(withId(R.id.listview__item__title))
                .check(matches(isDisplayed()));
        onData(allOf(is(instanceOf(Item.class)), withItemText("Test3")))
                .inAdapterView(withId(R.id.activity_item_list_item_list))
                .onChildView(withId(R.id.listview__item__title))
                .check(matches(isDisplayed()));
        onView(withId(R.id.activity_item_list_item_list))
                .check(matches(withItemCount(2)));
        onView(withId(R.id.activity_item_list_cost_sum_text)).check(matches(withText("$13.00")));
    }

    /**
     * Tests filtering between dates
     */
    @Test
    public void testStartEndDateFilter() {
        onView(withId(R.id.activity_item_list_filter_item_button)).perform(click());

        onView(withId(R.id.fragment_filter_items_start_date_edittext)).perform(click());
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName())))
                .inRoot(isDialog())
                .perform(setDate(2022, 1, 1));
        onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click());

        onView(withId(R.id.fragment_filter_items_end_date_edittext)).perform(click());
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName())))
                .inRoot(isDialog())
                .perform(setDate(2023, 1, 1));
        onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click());

        onView(withText("Filter/Sort")).inRoot(isDialog()).perform(click());

        onData(allOf(is(instanceOf(Item.class)), withItemText("Test2")))
                .inAdapterView(withId(R.id.activity_item_list_item_list))
                .onChildView(withId(R.id.listview__item__title))
                .check(matches(isDisplayed()));
        onView(withId(R.id.activity_item_list_item_list))
                .check(matches(withItemCount(1)));
        onView(withId(R.id.activity_item_list_cost_sum_text)).check(matches(withText("$6.00")));
    }

    /**
     * Tests make filtering
     */
    @Test
    public void testMakeFilter() {
        onView(withId(R.id.activity_item_list_filter_item_button)).perform(click());

        onView(allOf(withText("make"), isDescendantOfA(withId(R.id.fragment_filter_items_makes_chipgroup)))).perform(click());
        onView(withText("Filter/Sort")).inRoot(isDialog()).perform(click());

        onData(allOf(is(instanceOf(Item.class)), withItemText("Test1")))
                .inAdapterView(withId(R.id.activity_item_list_item_list))
                .onChildView(withId(R.id.listview__item__title))
                .check(matches(isDisplayed()));
        onView(withId(R.id.activity_item_list_item_list))
                .check(matches(withItemCount(1)));
        onView(withId(R.id.activity_item_list_cost_sum_text)).check(matches(withText("$5.00")));
    }

    /**
     * Tests tag filtering.
     */
    @Test
    public void testTagFilter() {
        onView(withId(R.id.activity_item_list_filter_item_button)).perform(click());

        onView(allOf(withText("testTag"), isDescendantOfA(withId(R.id.fragment_filter_items_tags_chipgroup)))).perform(click());
        onView(withText("Filter/Sort")).inRoot(isDialog()).perform(click());

        onData(allOf(is(instanceOf(Item.class)), withItemText("Test1")))
                .inAdapterView(withId(R.id.activity_item_list_item_list))
                .onChildView(withId(R.id.listview__item__title))
                .check(matches(isDisplayed()));
        onData(allOf(is(instanceOf(Item.class)), withItemText("Test2")))
                .inAdapterView(withId(R.id.activity_item_list_item_list))
                .onChildView(withId(R.id.listview__item__title))
                .check(matches(isDisplayed()));
        onView(withId(R.id.activity_item_list_item_list))
                .check(matches(withItemCount(2)));
        onView(withId(R.id.activity_item_list_cost_sum_text)).check(matches(withText("$11.00")));
    }

    /**
     * Tests check all and all tag filtering.
     */
    @Test
    public void testAllTagFilter() {
        onView(withId(R.id.activity_item_list_filter_item_button)).perform(click());

        onView(withId(R.id.fragment_filter_items_all_tags_checkbox)).inRoot(isDialog()).perform(click());
        onView(withText("Filter/Sort")).inRoot(isDialog()).perform(click());

        onData(allOf(is(instanceOf(Item.class)), withItemText("Test2")))
                .inAdapterView(withId(R.id.activity_item_list_item_list))
                .onChildView(withId(R.id.listview__item__title))
                .check(matches(isDisplayed()));
        onView(withId(R.id.activity_item_list_item_list))
                .check(matches(withItemCount(1)));
        onView(withId(R.id.activity_item_list_cost_sum_text)).check(matches(withText("$6.00")));
    }

    /**
     * Tests sorting date in ascending order.
     */
    @Test
    public void testDateAscSort() {
        onView(withId(R.id.activity_item_list_filter_item_button)).perform(click());

        onView(withId(R.id.fragment_filter_items_date_sort_button)).inRoot(isDialog()).perform(click());
        onView(withText("Filter/Sort")).inRoot(isDialog()).perform(click());

        onData(anything())
                .inAdapterView(withId(R.id.activity_item_list_item_list))
                .atPosition(0)
                .onChildView(withId(R.id.listview__item__title))
                .check(matches(withText("Test3")));
        onData(anything())
                .inAdapterView(withId(R.id.activity_item_list_item_list))
                .atPosition(1)
                .onChildView(withId(R.id.listview__item__title))
                .check(matches(withText("Test2")));
        onData(anything())
                .inAdapterView(withId(R.id.activity_item_list_item_list))
                .atPosition(2)
                .onChildView(withId(R.id.listview__item__title))
                .check(matches(withText("Test1")));
        onView(withId(R.id.activity_item_list_item_list))
                .check(matches(withItemCount(3)));
        onView(withId(R.id.activity_item_list_cost_sum_text)).check(matches(withText("$18.00")));
    }

    /**
     * Tests sorting date in descending order.
     */
    @Test
    public void testDateDescSort() {
        onView(withId(R.id.activity_item_list_filter_item_button)).perform(click());

        onView(withId(R.id.fragment_filter_items_date_sort_button)).inRoot(isDialog()).perform(click());
        onView(withId(R.id.fragment_filter_items_desc_sort_button)).inRoot(isDialog()).perform(click());
        onView(withText("Filter/Sort")).inRoot(isDialog()).perform(click());

        onData(anything())
                .inAdapterView(withId(R.id.activity_item_list_item_list))
                .atPosition(0)
                .onChildView(withId(R.id.listview__item__title))
                .check(matches(withText("Test1")));
        onData(anything())
                .inAdapterView(withId(R.id.activity_item_list_item_list))
                .atPosition(1)
                .onChildView(withId(R.id.listview__item__title))
                .check(matches(withText("Test2")));
        onData(anything())
                .inAdapterView(withId(R.id.activity_item_list_item_list))
                .atPosition(2)
                .onChildView(withId(R.id.listview__item__title))
                .check(matches(withText("Test3")));
        onView(withId(R.id.activity_item_list_item_list))
                .check(matches(withItemCount(3)));
        onView(withId(R.id.activity_item_list_cost_sum_text)).check(matches(withText("$18.00")));
    }

    /**
     * Tests sorting description in ascending order.
     */
    @Test
    public void testDescriptionAscSort() {
        onView(withId(R.id.activity_item_list_filter_item_button)).perform(click());

        onView(withId(R.id.fragment_filter_items_description_sort_button)).inRoot(isDialog()).perform(click());
        onView(withText("Filter/Sort")).inRoot(isDialog()).perform(click());

        onData(anything())
                .inAdapterView(withId(R.id.activity_item_list_item_list))
                .atPosition(0)
                .onChildView(withId(R.id.listview__item__title))
                .check(matches(withText("Test2")));
        onData(anything())
                .inAdapterView(withId(R.id.activity_item_list_item_list))
                .atPosition(1)
                .onChildView(withId(R.id.listview__item__title))
                .check(matches(withText("Test3")));
        onData(anything())
                .inAdapterView(withId(R.id.activity_item_list_item_list))
                .atPosition(2)
                .onChildView(withId(R.id.listview__item__title))
                .check(matches(withText("Test1")));
        onView(withId(R.id.activity_item_list_item_list))
                .check(matches(withItemCount(3)));
        onView(withId(R.id.activity_item_list_cost_sum_text)).check(matches(withText("$18.00")));
    }

    /**
     * Tests sorting description in descending order.
     */
    @Test
    public void testDescriptionDescSort() {
        onView(withId(R.id.activity_item_list_filter_item_button)).perform(click());

        onView(withId(R.id.fragment_filter_items_description_sort_button)).inRoot(isDialog()).perform(click());
        onView(withId(R.id.fragment_filter_items_desc_sort_button)).inRoot(isDialog()).perform(click());
        onView(withText("Filter/Sort")).inRoot(isDialog()).perform(click());

        onData(anything())
                .inAdapterView(withId(R.id.activity_item_list_item_list))
                .atPosition(0)
                .onChildView(withId(R.id.listview__item__title))
                .check(matches(withText("Test1")));
        onData(anything())
                .inAdapterView(withId(R.id.activity_item_list_item_list))
                .atPosition(1)
                .onChildView(withId(R.id.listview__item__title))
                .check(matches(withText("Test3")));
        onData(anything())
                .inAdapterView(withId(R.id.activity_item_list_item_list))
                .atPosition(2)
                .onChildView(withId(R.id.listview__item__title))
                .check(matches(withText("Test2")));
        onView(withId(R.id.activity_item_list_item_list))
                .check(matches(withItemCount(3)));
        onView(withId(R.id.activity_item_list_cost_sum_text)).check(matches(withText("$18.00")));
    }

    /**
     * Tests sorting make in ascending order.
     */
    @Test
    public void testMakeAscSort() {
        onView(withId(R.id.activity_item_list_filter_item_button)).perform(click());

        onView(withId(R.id.fragment_filter_items_make_sort_button)).inRoot(isDialog()).perform(click());
        onView(withText("Filter/Sort")).inRoot(isDialog()).perform(click());

        onData(anything())
                .inAdapterView(withId(R.id.activity_item_list_item_list))
                .atPosition(0)
                .onChildView(withId(R.id.listview__item__title))
                .check(matches(withText("Test3")));
        onData(anything())
                .inAdapterView(withId(R.id.activity_item_list_item_list))
                .atPosition(1)
                .onChildView(withId(R.id.listview__item__title))
                .check(matches(withText("Test2")));
        onData(anything())
                .inAdapterView(withId(R.id.activity_item_list_item_list))
                .atPosition(2)
                .onChildView(withId(R.id.listview__item__title))
                .check(matches(withText("Test1")));
        onView(withId(R.id.activity_item_list_item_list))
                .check(matches(withItemCount(3)));
        onView(withId(R.id.activity_item_list_cost_sum_text)).check(matches(withText("$18.00")));
    }

    /**
     * Tests sorting make in descending order.
     */
    @Test
    public void testMakeDescSort() {
        onView(withId(R.id.activity_item_list_filter_item_button)).perform(click());

        onView(withId(R.id.fragment_filter_items_make_sort_button)).inRoot(isDialog()).perform(click());
        onView(withId(R.id.fragment_filter_items_desc_sort_button)).inRoot(isDialog()).perform(click());
        onView(withText("Filter/Sort")).inRoot(isDialog()).perform(click());

        onData(anything())
                .inAdapterView(withId(R.id.activity_item_list_item_list))
                .atPosition(0)
                .onChildView(withId(R.id.listview__item__title))
                .check(matches(withText("Test1")));
        onData(anything())
                .inAdapterView(withId(R.id.activity_item_list_item_list))
                .atPosition(1)
                .onChildView(withId(R.id.listview__item__title))
                .check(matches(withText("Test2")));
        onData(anything())
                .inAdapterView(withId(R.id.activity_item_list_item_list))
                .atPosition(2)
                .onChildView(withId(R.id.listview__item__title))
                .check(matches(withText("Test3")));
        onView(withId(R.id.activity_item_list_item_list))
                .check(matches(withItemCount(3)));
        onView(withId(R.id.activity_item_list_cost_sum_text)).check(matches(withText("$18.00")));
    }

    /**
     * Tests sorting value in ascending order.
     */
    @Test
    public void testValueAscSort() {
        onView(withId(R.id.activity_item_list_filter_item_button)).perform(click());

        onView(withId(R.id.fragment_filter_items_value_sort_button)).inRoot(isDialog()).perform(click());
        onView(withText("Filter/Sort")).inRoot(isDialog()).perform(click());

        onData(anything())
                .inAdapterView(withId(R.id.activity_item_list_item_list))
                .atPosition(0)
                .onChildView(withId(R.id.listview__item__title))
                .check(matches(withText("Test1")));
        onData(anything())
                .inAdapterView(withId(R.id.activity_item_list_item_list))
                .atPosition(1)
                .onChildView(withId(R.id.listview__item__title))
                .check(matches(withText("Test2")));
        onData(anything())
                .inAdapterView(withId(R.id.activity_item_list_item_list))
                .atPosition(2)
                .onChildView(withId(R.id.listview__item__title))
                .check(matches(withText("Test3")));
        onView(withId(R.id.activity_item_list_item_list))
                .check(matches(withItemCount(3)));
        onView(withId(R.id.activity_item_list_cost_sum_text)).check(matches(withText("$18.00")));
    }

    /**
     * Tests sorting value in descending order.
     * TODO: FAILS FOR NO REASON WTF
     */
    @Test
    public void testValueDescSort() {
        onView(withId(R.id.activity_item_list_filter_item_button)).perform(click());

        onView(withId(R.id.fragment_filter_items_value_sort_button)).inRoot(isDialog()).perform(click());
        onView(withId(R.id.fragment_filter_items_desc_sort_button)).inRoot(isDialog()).perform(click());
        onView(withText("Filter/Sort")).inRoot(isDialog()).perform(click());

        onData(anything())
                .inAdapterView(withId(R.id.activity_item_list_item_list))
                .atPosition(0)
                .onChildView(withId(R.id.listview__item__title))
                .check(matches(withText("Test3")));
        onData(anything())
                .inAdapterView(withId(R.id.activity_item_list_item_list))
                .atPosition(1)
                .onChildView(withId(R.id.listview__item__title))
                .check(matches(withText("Test2")));
        onData(anything())
                .inAdapterView(withId(R.id.activity_item_list_item_list))
                .atPosition(2)
                .onChildView(withId(R.id.listview__item__title))
                .check(matches(withText("Test1")));
        onView(withId(R.id.activity_item_list_item_list))
                .check(matches(withItemCount(3)));
        onView(withId(R.id.activity_item_list_cost_sum_text)).check(matches(withText("$18.00")));
    }

    /**
     * Tests sorting tags in ascending order.
     */
    @Test
    public void testTagsAscSort() {
        onView(withId(R.id.activity_item_list_filter_item_button)).perform(click());

        onView(withId(R.id.fragment_filter_items_tag_sort_button)).inRoot(isDialog()).perform(click());
        onView(withText("Filter/Sort")).inRoot(isDialog()).perform(click());

        onData(anything())
                .inAdapterView(withId(R.id.activity_item_list_item_list))
                .atPosition(0)
                .onChildView(withId(R.id.listview__item__title))
                .check(matches(withText("Test3")));
        onView(withId(R.id.activity_item_list_item_list))
                .check(matches(withItemCount(3)));
        onView(withId(R.id.activity_item_list_cost_sum_text)).check(matches(withText("$18.00")));
    }

    /**
     * Tests sorting tags in descending order.
     */
    @Test
    public void testTagsDescSort() {
        onView(withId(R.id.activity_item_list_filter_item_button)).perform(click());

        onView(withId(R.id.fragment_filter_items_tag_sort_button)).inRoot(isDialog()).perform(click());
        onView(withId(R.id.fragment_filter_items_desc_sort_button)).inRoot(isDialog()).perform(click());
        onView(withText("Filter/Sort")).inRoot(isDialog()).perform(click());

        onData(anything())
                .inAdapterView(withId(R.id.activity_item_list_item_list))
                .atPosition(2)
                .onChildView(withId(R.id.listview__item__title))
                .check(matches(withText("Test3")));
        onView(withId(R.id.activity_item_list_item_list))
                .check(matches(withItemCount(3)));
        onView(withId(R.id.activity_item_list_cost_sum_text)).check(matches(withText("$18.00")));
    }

    /**
     * Tests no dialog input after filtering (un-filter).
     */
    @Test
    public void testUnFilter() {
        onView(withId(R.id.activity_item_list_filter_item_button)).perform(click());
        onView(withId(R.id.fragment_filter_items_keywords_textedit)).perform(typeText("second"));
        onView(withId(R.id.fragment_filter_items_keywords_textedit)).perform(pressImeActionButton());
        onView(ViewMatchers.isRoot()).perform(ViewActions.closeSoftKeyboard());
        onView(withText("Filter/Sort")).inRoot(isDialog()).perform(click());
        onView(withId(R.id.activity_item_list_item_list))
                .check(matches(withItemCount(2)));
        onView(withId(R.id.activity_item_list_cost_sum_text)).check(matches(withText("$13.00")));

        // un-filter
        onView(withId(R.id.activity_item_list_filter_item_button)).perform(click());
        onView(withText("Filter/Sort")).inRoot(isDialog()).perform(click());
        onView(withId(R.id.activity_item_list_item_list))
                .check(matches(withItemCount(3)));
        onView(withId(R.id.activity_item_list_cost_sum_text)).check(matches(withText("$18.00")));
    }

    /**
     * Tests a combination of filtering and sorting.
     */
    @Test
    public void testFilterSortCombo() {
        onView(withId(R.id.activity_item_list_filter_item_button)).perform(click());

        onView(withId(R.id.fragment_filter_items_keywords_textedit)).perform(typeText("second"));
        onView(withId(R.id.fragment_filter_items_keywords_textedit)).perform(pressImeActionButton());
        onView(ViewMatchers.isRoot()).perform(ViewActions.closeSoftKeyboard());

        onView(withId(R.id.fragment_filter_items_value_sort_button)).inRoot(isDialog()).perform(click());
        onView(withId(R.id.fragment_filter_items_desc_sort_button)).inRoot(isDialog()).perform(click());

        onView(withText("Filter/Sort")).inRoot(isDialog()).perform(click());

        onData(anything())
                .inAdapterView(withId(R.id.activity_item_list_item_list))
                .atPosition(0)
                .onChildView(withId(R.id.listview__item__title))
                .check(matches(withText("Test3")));
        onData(anything())
                .inAdapterView(withId(R.id.activity_item_list_item_list))
                .atPosition(1)
                .onChildView(withId(R.id.listview__item__title))
                .check(matches(withText("Test2")));
        onView(withId(R.id.activity_item_list_item_list))
                .check(matches(withItemCount(2)));
        onView(withId(R.id.activity_item_list_cost_sum_text)).check(matches(withText("$13.00")));
    }

    private static Matcher<Object> withItemText(String expectedText) {
        checkNotNull(expectedText);
        return new BoundedMatcher<Object, Item>(Item.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("with item text: " + expectedText);
            }

            @Override
            protected boolean matchesSafely(Item item) {
                return item.getName().equals(expectedText);
            }
        };
    }

    /**
     * Helper method to count the number of items in ListView.
     * Generated by ChatGPT
     *
     * @param expectedCount
     * @return
     */
    private static Matcher<View> withItemCount(int expectedCount) {
        return new BoundedMatcher<View, ListView>(ListView.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("with item count: " + expectedCount);
            }

            @Override
            protected boolean matchesSafely(ListView listView) {
                ListAdapter adapter = listView.getAdapter();
                return adapter != null && adapter.getCount() == expectedCount;
            }
        };
    }

    /**
     * Helper method to set the date on DatePicker using a custom ViewAction
     * Generated using ChatGPT
     *
     * @param year year to input to DatePicker
     * @param monthOfYear month to input to DatePicker (1-12)
     * @param dayOfMonth day to input to DatePicker (1-31)
     * @return
     */
    private static ViewAction setDate(final int year, final int monthOfYear, final int dayOfMonth) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(DatePicker.class);
            }

            @Override
            public String getDescription() {
                return "set date on date picker";
            }

            @Override
            public void perform(UiController uiController, View view) {
                DatePicker datePicker = (DatePicker) view;
                datePicker.updateDate(year, monthOfYear, dayOfMonth);
            }
        };
    }
}

