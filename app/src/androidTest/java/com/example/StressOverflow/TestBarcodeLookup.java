package com.example.StressOverflow;

import static android.content.ContentValues.TAG;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.pressBack;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSubstring;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.StressOverflow.Item.Item;
import com.example.StressOverflow.Item.ListActivity;
import com.example.StressOverflow.SignIn.SignUpActivity;
import com.example.StressOverflow.Tag.Tag;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.GregorianCalendar;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class TestBarcodeLookup {
    @Before
    public void setUp() {
        AppGlobals.getInstance().setOwnerName("testUser");
    }
    @Rule
    public ActivityScenarioRule<ListActivity> scenario =
            new ActivityScenarioRule<ListActivity>(ListActivity.class);

    @Test
    public void testSagi() {
        onView(withId(R.id.activity__item__list__edit__item__button)).perform(click());
        SystemClock.sleep(2000);
        onView(withId(R.id.add__item__fragment__edit__serial)).
                perform(scrollTo());
        onView(withId(R.id.add__item__fragment__edit__serial)).perform(closeSoftKeyboard());
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
        onView(withId(R.id.add__item__fragment__edit__make)).check(ViewAssertions.matches(withText("")));
        onView(withId(R.id.add__item__fragment__edit__model)).check(ViewAssertions.matches(withText("")));
    }
}
