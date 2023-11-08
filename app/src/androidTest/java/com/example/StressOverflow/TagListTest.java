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

import android.view.View;

import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class TagListTest {
    @Rule
    public ActivityScenarioRule<TagList> scenario =
            new ActivityScenarioRule<TagList>(TagList.class);

    @Test
    public void addTagList() throws InterruptedException {
        onView(ViewMatchers.withId(R.id.addTag_button)).perform(click());
        onView(withText("Add Tag")).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(ViewMatchers.withId(R.id.addTagTextView)).inRoot(isDialog()).perform(typeText("luggage"));
        onView(withText("OK")).inRoot(isDialog()).perform(click());

        onView(withId(R.id.addTagTextView)).check(doesNotExist());

    }


}
