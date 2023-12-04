package com.example.StressOverflow;
import com.example.StressOverflow.Item.ListActivity;
import static android.content.ContentValues.TAG;
import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withChild;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.StressOverflow.Item.Item;
import com.example.StressOverflow.Tag.Tag;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class TagItemTest {
    private FirebaseFirestore db;
    Item item;
    private CollectionReference tagRef;
    private CollectionReference itemRef;
    private String ownerName;
    private String testTagName;
    Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

    @Before
    public void setUp() {
        db = FirebaseFirestore.getInstance();
        AppGlobals.getInstance().setOwnerName("testUser");

        ArrayList<Tag> testTags = new ArrayList<>();
        ArrayList<String> images = new ArrayList<>();
        tagRef = db.collection("tags");
        itemRef = db.collection("items");
        ownerName = "testUser";
        AppGlobals.getInstance().setOwnerName(ownerName);
        testTagName = "testTag1";
        //Add a tag first
        Tag newTag = new Tag(testTagName);
        testTags.add(newTag);
        tagRef.document(String.format("%s:%s", ownerName, testTagName))
                .set(newTag.toFirebaseObject(ownerName))
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error with item insertion into collection items: ", e);
                        throw new RuntimeException("Error with item insertion into collection items: ", e);
                    }
                });

        ArrayList<Tag> emptyTagArrayList = new ArrayList<>();
        item = new Item("deleteItemTest","make","model","description", new GregorianCalendar(),77.00, "Comments",emptyTagArrayList,images,
                "123456", AppGlobals.getInstance().getOwnerName());
        itemRef.document(item.getId().toString())
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
    public ActivityScenarioRule<ListActivity> listActivityRule =
            new ActivityScenarioRule<>(ListActivity.class);

    @Test
    public void ListActivitytoTagList(){
        onView(withId(R.id.activity_item_list_show_tags_button)).perform(click());
        onView(ViewMatchers.withId(R.id.activity_tag_list_add_tag_button)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void addTagAndBack(){
        //checks that addTag by long selecting opens up TagList Activity
        int listViewId = R.id.activity_item_list_item_list;
        SystemClock.sleep(2000);

        onData(Matchers.anything())
                .inAdapterView(withId(listViewId))
                .atPosition(0)
                .onChildView(withId(R.id.listview__item__title))
                .perform(longClick());
        onView(ViewMatchers.withId(R.id.activity_item_list_add_tag_button)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        onView(ViewMatchers.withId(R.id.activity_item_list_add_tag_button)).perform(click());
        onView(ViewMatchers.withId(R.id.fragment_add_tag_to_item_make_new_tag_button)).perform(click());
        onView(ViewMatchers.withId(R.id.activity_tag_list_listView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        //return back to dialog
        onView(ViewMatchers.withId(R.id.activity_tag_list_back_button)).perform(click());
        onView(withText("Add Tag")).inRoot(isDialog()).check(matches(isDisplayed()));
    }

    @Test
    public void showTagListAndBack(){
        onView(ViewMatchers.withId(R.id.activity_item_list_show_tags_button)).perform(click());
        onView(ViewMatchers.withId(R.id.activity_tag_list_listView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        onView(ViewMatchers.withId(R.id.activity_tag_list_back_button)).perform(click());
        onView(ViewMatchers.withId(R.id.activity_item_list_item_list)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

    }

    @Test
    public void addTags(){
        int listViewId = R.id.activity_item_list_item_list;
        SystemClock.sleep(2000);

        onData(Matchers.anything())
                .inAdapterView(withId(listViewId))
                .atPosition(0)
                .onChildView(withId(R.id.listview__item__title))
                .perform(longClick());
        onView(ViewMatchers.withId(R.id.activity_item_list_add_tag_button)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        onView(ViewMatchers.withId(R.id.activity_item_list_add_tag_button)).perform(click());
        int chipGroupID = R.id.fragment_add_tag_to_item_tag_chipGroup;

        onView(allOf(withText(testTagName), isDescendantOfA(withId(chipGroupID))))
                .perform(click());

        onView(withText("OK")).inRoot(isDialog()).perform(click());

        final CountDownLatch latch = new CountDownLatch(1);

        final Item[] itemInfo = new Item[1];
        itemRef
                .document(item.getId().toString())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            Map<String, Object> data = document.getData();
                            Item item = Item.fromFirebaseObject(data);
                            itemInfo[0] = item;
                        }
                        latch.countDown();
                    }
                });
        try {
            latch.await(); // Wait for the latch to count down to 0
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        boolean tagExists = false;
        for (Tag t: itemInfo[0].getTags()){
            if (t.getTagName().equals(testTagName)){
                tagExists = true;
                break;
            }
        }

        assertTrue(tagExists);
    }

    @Test
    public void addDuplicateTag(){
        int listViewId = R.id.activity_item_list_item_list;
        SystemClock.sleep(2000);

        onData(Matchers.anything())
                .inAdapterView(withId(listViewId))
                .atPosition(0)
                .onChildView(withId(R.id.listview__item__title))
                .perform(longClick());
        onView(ViewMatchers.withId(R.id.activity_item_list_add_tag_button)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        onView(ViewMatchers.withId(R.id.activity_item_list_add_tag_button)).perform(click());
        int chipGroupID = R.id.fragment_add_tag_to_item_tag_chipGroup;

        onView(allOf(withText(testTagName), isDescendantOfA(withId(chipGroupID))))
                .perform(click());

        onView(withText("OK")).inRoot(isDialog()).perform(click());

        //add second time
        onData(Matchers.anything())
                .inAdapterView(withId(listViewId))
                .atPosition(0)
                .onChildView(withId(R.id.listview__item__title))
                .perform(longClick());
        onView(ViewMatchers.withId(R.id.activity_item_list_add_tag_button)).perform(click());

        onView(allOf(withText(testTagName), isDescendantOfA(withId(chipGroupID))))
                .perform(click());

        onView(withText("OK")).inRoot(isDialog()).perform(click());

        final CountDownLatch latch = new CountDownLatch(1);

        final Item[] itemInfo = new Item[1];
        itemRef
                .document(item.getId().toString())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            Map<String, Object> data = document.getData();
                            Item item = Item.fromFirebaseObject(data);
                            itemInfo[0] = item;
                        }
                        latch.countDown();
                    }
                });
        try {
            latch.await(); // Wait for the latch to count down to 0
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        boolean tagExists = false;
        if (itemInfo[0].getTags().size()==1){
            tagExists = true;
        }

        assertTrue(tagExists);
    }

    @Test
    public void deleteItem(){
        int listViewId = R.id.activity_item_list_item_list;
        SystemClock.sleep(2000);

        onData(Matchers.anything())
                .inAdapterView(withId(listViewId))
                .atPosition(0)
                .onChildView(withId(R.id.listview__item__title))
                .perform(longClick());

        onView(ViewMatchers.withId(R.id.activity_item_list_remove_item_button)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        onView(ViewMatchers.withId(R.id.activity_item_list_remove_item_button)).perform(click());


        final boolean[] itemBool = new boolean[1];
        itemRef
                .document(item.getId().toString())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                itemBool[0] = true;
                            } else {
                                itemBool[0] = false;
                                Log.d(TAG, "Document not found with uuid " + item.getId().toString());
                            }
                        }
                    }
                });

        assertFalse(itemBool[0]);
    }

    @Test
    public void deleteTagCascade(){
        //add tag to item first
        int listViewId = R.id.activity_item_list_item_list;
        SystemClock.sleep(2000);

        onData(Matchers.anything())
                .inAdapterView(withId(listViewId))
                .atPosition(0)
                .onChildView(withId(R.id.listview__item__title))
                .perform(longClick());
        onView(ViewMatchers.withId(R.id.activity_item_list_add_tag_button)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        onView(ViewMatchers.withId(R.id.activity_item_list_add_tag_button)).perform(click());
        int chipGroupID = R.id.fragment_add_tag_to_item_tag_chipGroup;

        onView(allOf(withText(testTagName), isDescendantOfA(withId(chipGroupID))))
                .perform(click());

        onView(withText("OK")).inRoot(isDialog()).perform(click());

        //Go to tag listview
        onView(ViewMatchers.withId(R.id.activity_item_list_show_tags_button)).perform(click());

        int deleteButtonId = R.id.listview_delete_tag_button;
        int tagListId = R.id.activity_tag_list_listView;
        // Perform a click on the delete button in the first row of the ListView.
        Espresso.onData(Matchers.anything())
                .inAdapterView(withId(tagListId))
                .atPosition(0)
                .onChildView(withId(deleteButtonId))
                .perform(click());

        //Go back to itemList
        onView(ViewMatchers.withId(R.id.activity_tag_list_back_button)).perform(click());

        final Item[] itemInfo = new Item[1];
        final CountDownLatch latch = new CountDownLatch(1);

        itemRef
                .document(item.getId().toString())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            Map<String, Object> data = document.getData();
                            Item item = Item.fromFirebaseObject(data);
                            itemInfo[0] = item;
                        }
                        latch.countDown();
                    }
                });
        try {
            latch.await(); // Wait for the latch to count down to 0
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        boolean tagExists = true;
        if (itemInfo[0].getTags().size()==0){
            tagExists = false;
        }

        assertFalse(tagExists);
    }

    @Test
    public void RefreshTagsFromAddTags(){
        //checks refresh button on addTag Dialog by long selecting refreshes
        int listViewId = R.id.activity_item_list_item_list;
        SystemClock.sleep(2000);

        onData(Matchers.anything())
                .inAdapterView(withId(listViewId))
                .atPosition(0)
                .onChildView(withId(R.id.listview__item__title))
                .perform(longClick());
        onView(ViewMatchers.withId(R.id.activity_item_list_add_tag_button)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        onView(ViewMatchers.withId(R.id.activity_item_list_add_tag_button)).perform(click());
        onView(ViewMatchers.withId(R.id.fragment_add_tag_to_item_make_new_tag_button)).perform(click());
        onView(ViewMatchers.withId(R.id.activity_tag_list_listView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        int deleteButtonId = R.id.listview_delete_tag_button;
        int tagListViewId = R.id.activity_tag_list_listView;

        //Delete existing tag
        Espresso.onData(Matchers.anything())
                .inAdapterView(withId(tagListViewId))
                .atPosition(0)
                .onChildView(withId(deleteButtonId))
                .perform(click());

        //return back to dialog
        onView(ViewMatchers.withId(R.id.activity_tag_list_back_button)).perform(click());
        onView(withText("Add Tag")).inRoot(isDialog()).check(matches(isDisplayed()));

        //Click on refresh button
        onView(ViewMatchers.withId(R.id.fragment_add_tag_to_item_refresh_tag_button)).perform(click());

        //Check to see that chipGroup is empty
        onView(ViewMatchers.withId(R.id.fragment_add_tag_to_item_tag_chipGroup)).check(matches(hasChildCount(0)));

    }

    @Test
    public void addItemWithTags(){
        SystemClock.sleep(2000);
        onView(ViewMatchers.withId(R.id.activity_item_list_add_item_button)).perform(click());

        //title
        onView(ViewMatchers.withId(R.id.add_item_fragment_edit_title)).perform(click());
        onView(withId(R.id.add_item_fragment_edit_title))
                .perform(ViewActions.typeText("TestItem2"));
        closeSoftKeyboard();

        //make
        onView(ViewMatchers.withId(R.id.add_item_fragment_edit_make)).perform(click());
        onView(withId(R.id.add_item_fragment_edit_make))
                .perform(ViewActions.typeText("Make1"));
        closeSoftKeyboard();

        //model
        onView(ViewMatchers.withId(R.id.add_item_fragment_edit_model)).perform(click());
        onView(withId(R.id.add_item_fragment_edit_model))
                .perform(ViewActions.typeText("Model1"));
        closeSoftKeyboard();

        //description
        onView(ViewMatchers.withId(R.id.add_item_fragment_edit_description)).perform(click());
        onView(withId(R.id.add_item_fragment_edit_description))
                .perform(ViewActions.typeText("Description1"));
        closeSoftKeyboard();

        //Year month day
        onView(ViewMatchers.withId(R.id.add_item_fragment_edit_year)).perform(click());
        onView(withId(R.id.add_item_fragment_edit_year))
                .perform(ViewActions.typeText("2022"));
        closeSoftKeyboard();

        onView(ViewMatchers.withId(R.id.add_item_fragment_edit_month)).perform(click());
        onView(withId(R.id.add_item_fragment_edit_month))
                .perform(ViewActions.typeText("03"));
        closeSoftKeyboard();

        onView(ViewMatchers.withId(R.id.add_item_fragment_edit_date)).perform(click());
        onView(withId(R.id.add_item_fragment_edit_date))
                .perform(ViewActions.typeText("11"));
        closeSoftKeyboard();

        //Value
        onView(ViewMatchers.withId(R.id.add_item_fragment_edit_value)).perform(click());
        onView(withId(R.id.add_item_fragment_edit_value))
                .perform(ViewActions.typeText("55"));
        closeSoftKeyboard();

        //tag
        int chipGroupID = R.id.add_item_fragment_chipGroup;

        onView(allOf(withText(testTagName), isDescendantOfA(withId(chipGroupID))))
                .perform(click());
        //Serial number
        onView(withId(R.id.add_item_fragment_edit_serial))
                .perform(ViewActions.scrollTo());
        onView(ViewMatchers.withId(R.id.add_item_fragment_edit_serial)).perform(click());
        onView(withId(R.id.add_item_fragment_edit_serial))
                .perform(ViewActions.typeText("98765"));
        closeSoftKeyboard();

        onView(withText("OK")).inRoot(isDialog()).perform(click());

        final Item[] itemInfo = new Item[1];
        final CountDownLatch latch = new CountDownLatch(1);

        itemRef
                .whereEqualTo("owner", "testUser")
                .whereEqualTo("name", "TestItem2")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                Map<String, Object> data = document.getData();
                                Item item = Item.fromFirebaseObject(data);
                                itemInfo[0] = item;
                            }
                        }
                        latch.countDown();
                    }
                });
        try {
            latch.await(); // Wait for the latch to count down to 0
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        boolean tagExists = true;
        if (itemInfo[0].getTags().size()==0){
            tagExists = false;
        }
        assertTrue(tagExists);

    }

    @Test
    public void addItemRefreshTags(){
        SystemClock.sleep(2000);
        onView(ViewMatchers.withId(R.id.activity_item_list_add_item_button)).perform(click());
        int tagListViewId = R.id.activity_tag_list_listView;
        int deleteButtonId = R.id.listview_delete_tag_button;

        onView(ViewMatchers.withId(R.id.add_item_fragment_add_tag_button)).perform(click());
        onView(ViewMatchers.withId(R.id.activity_tag_list_listView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        //Delete existing tag
        Espresso.onData(Matchers.anything())
                .inAdapterView(withId(tagListViewId))
                .atPosition(0)
                .onChildView(withId(deleteButtonId))
                .perform(click());

        //Click on back button to go back
        onView(ViewMatchers.withId(R.id.activity_tag_list_back_button)).perform(click());
        onView(ViewMatchers.withId(R.id.add_item_fragment_add_tag_button)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        //Click on refresh button
        onView(ViewMatchers.withId(R.id.add_item_fragment_refresh_tags_button)).perform(click());

        //Check to see that chipGroup is empty
        onView(withId(R.id.add_item_fragment_chipGroup))
                .check((view, noViewFoundException) -> {
                    if (view instanceof ChipGroup) {
                        ChipGroup chipGroup = (ChipGroup) view;
                        int chipCount = chipGroup.getChildCount();
                        assertThat(chipCount, is(0));
                    } else {
                        throw new IllegalStateException("The asserted view is not a ChipGroup");
                    }
                });
    }

    @Test
    public void addItemToTagList(){
        //click on item, click add tags, click back
        int listViewId = R.id.activity_item_list_item_list;
        SystemClock.sleep(2000);

        onView(ViewMatchers.withId(R.id.activity_item_list_add_item_button)).perform(click());

        //Click to add tag on tagList View
        onView(ViewMatchers.withId(R.id.add_item_fragment_add_tag_button)).perform(click());
        onView(ViewMatchers.withId(R.id.activity_tag_list_listView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        //Click on back button to go back
        onView(ViewMatchers.withId(R.id.activity_tag_list_back_button)).perform(click());
        onView(ViewMatchers.withId(R.id.add_item_fragment_add_tag_button)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

    }

    @Test
    public void editItemCheckTags(){
        //add tag, and look at it again to make sure that the tag is checked
        int listViewId = R.id.activity_item_list_item_list;
        SystemClock.sleep(2000);

        onData(Matchers.anything())
                .inAdapterView(withId(listViewId))
                .atPosition(0)
                .onChildView(withId(R.id.listview__item__title))
                .perform(click());

        onView(ViewMatchers.withId(R.id.add_item_fragment_edit_title)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        int chipGroupID = R.id.add_item_fragment_chipGroup;

        onView(allOf(withText(testTagName), isDescendantOfA(withId(chipGroupID))))
                .perform(click());
        onView(withText("OK")).inRoot(isDialog()).perform(click());

        //click on item again to check that chip is checked
        onData(Matchers.anything())
                .inAdapterView(withId(listViewId))
                .atPosition(0)
                .onChildView(withId(R.id.listview__item__title))
                .perform(click());

        onView(ViewMatchers.withId(chipGroupID)).check(matches(withCheckedChipAndText(testTagName)));

    }

    private static Matcher<View> withCheckedChipAndText(final String chipText) {
        return withChild(allOf(withText(chipText), isChecked()));
    }

    @Test
    public void editItemUnselectTags(){
        //add tag, click on item, unselect tag
        int listViewId = R.id.activity_item_list_item_list;
        SystemClock.sleep(2000);

        onData(Matchers.anything())
                .inAdapterView(withId(listViewId))
                .atPosition(0)
                .onChildView(withId(R.id.listview__item__title))
                .perform(click());

        onView(ViewMatchers.withId(R.id.add_item_fragment_edit_title)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        int chipGroupID = R.id.add_item_fragment_chipGroup;

        onView(allOf(withText(testTagName), isDescendantOfA(withId(chipGroupID))))
                .perform(click());
        onView(withText("OK")).inRoot(isDialog()).perform(click());

        //click on item again
        onData(Matchers.anything())
                .inAdapterView(withId(listViewId))
                .atPosition(0)
                .onChildView(withId(R.id.listview__item__title))
                .perform(click());

        //unselect tag
        onView(allOf(withText(testTagName), isDescendantOfA(withId(chipGroupID))))
                .perform(click());
        onView(withText("OK")).inRoot(isDialog()).perform(click());
        final Item[] itemInfo = new Item[1];
        final CountDownLatch latch = new CountDownLatch(1);

        itemRef
                .document(item.getId().toString())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            Map<String, Object> data = document.getData();
                            Item item = Item.fromFirebaseObject(data);
                            itemInfo[0] = item;
                        }
                        latch.countDown();
                    }
                });
        try {
            latch.await(); // Wait for the latch to count down to 0
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        boolean tagExists = true;
        if (itemInfo[0].getTags().size()==0){
            tagExists = false;
        }

        assertFalse(tagExists);
    }

    @Test
    public void editItemToTagList(){
        //click on item, click add tags, click back
        int listViewId = R.id.activity_item_list_item_list;
        SystemClock.sleep(2000);

        onData(Matchers.anything())
                .inAdapterView(withId(listViewId))
                .atPosition(0)
                .onChildView(withId(R.id.listview__item__title))
                .perform(click());
        //Click to add tag on tagList View
        onView(ViewMatchers.withId(R.id.add_item_fragment_add_tag_button)).perform(click());
        onView(ViewMatchers.withId(R.id.activity_tag_list_listView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        //Click on back button to go back
        onView(ViewMatchers.withId(R.id.activity_tag_list_back_button)).perform(click());
        onView(ViewMatchers.withId(R.id.add_item_fragment_add_tag_button)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void editItemToRefreshButton(){
        //click on item, click add tags, click back
        int listViewId = R.id.activity_item_list_item_list;
        int tagListViewId = R.id.activity_tag_list_listView;
        int deleteButtonId = R.id.listview_delete_tag_button;

        SystemClock.sleep(2000);

        onData(Matchers.anything())
                .inAdapterView(withId(listViewId))
                .atPosition(0)
                .onChildView(withId(R.id.listview__item__title))
                .perform(click());
        //Click to add tag on tagList View
        onView(ViewMatchers.withId(R.id.add_item_fragment_add_tag_button)).perform(click());
        onView(ViewMatchers.withId(R.id.activity_tag_list_listView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        //Delete existing tag
        Espresso.onData(Matchers.anything())
                .inAdapterView(withId(tagListViewId))
                .atPosition(0)
                .onChildView(withId(deleteButtonId))
                .perform(click());

        //Click on back button to go back
        onView(ViewMatchers.withId(R.id.activity_tag_list_back_button)).perform(click());
        onView(ViewMatchers.withId(R.id.add_item_fragment_add_tag_button)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        //Click on refresh button
        onView(ViewMatchers.withId(R.id.add_item_fragment_refresh_tags_button)).perform(click());

        //Check to see that chipGroup is empty
        onView(withId(R.id.add_item_fragment_chipGroup))
                .check((view, noViewFoundException) -> {
                    if (view instanceof ChipGroup) {
                        ChipGroup chipGroup = (ChipGroup) view;
                        int chipCount = chipGroup.getChildCount();
                        assertThat(chipCount, is(0));
                    } else {
                        throw new IllegalStateException("The asserted view is not a ChipGroup");
                    }
                });
    }


}