package com.example.StressOverflow;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.withChild;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.instanceOf;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class FilterTest {

    @Rule
    public ActivityScenarioRule<ListActivity> scenario =
            new ActivityScenarioRule<ListActivity>(ListActivity.class);

    @Test
    public void testKeywordFilter() throws InterruptedException {
        onView(withId(R.id.activity__item__list__filter__item__button)).perform(click());

        onView(withId(R.id.filter__dialog__keywords__textedit)).perform(typeText("luggage"));
        onView(withId(R.id.filter__dialog__keywords__textedit)).perform(pressImeActionButton());
        Thread.sleep(1000);
        onView(withId(R.id.filter__dialog__back__btn)).inRoot(isDialog()).perform(click());

        onData(is(instanceOf(Item.class)))
            .inAdapterView(withId(R.id.activity__item__list__item__list)).atPosition(0)
            .onChildView(withId(R.id.listview__item__title))
            .check(matches(withText("Samsonite Winfield 2 Fashions 2-Piece Hardside Luggage Set")));
    }

    @Test
    public void testMakeFilter() throws InterruptedException {
        onView(withId(R.id.activity__item__list__filter__item__button)).perform(click());

        onView(allOf(withId(R.id.filter__dialog__makes__chipgroup), withChild(withText("56844")))).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.filter__dialog__back__btn)).inRoot(isDialog()).perform(click());

        onData(is(instanceOf(Item.class)))
                .inAdapterView(withId(R.id.activity__item__list__item__list)).atPosition(0)
                .onChildView(withId(R.id.listview__item__title))
                .check(matches(withText("Samsonite Winfield 2 Fashions 2-Piece Hardside Luggage Set")));
    }

    @Test
    public void testTagFilter() throws InterruptedException {
        onView(withId(R.id.activity__item__list__filter__item__button)).perform(click());

        onView(allOf(withId(R.id.filter__dialog__makes__chipgroup), withChild(withText("luggage")))).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.filter__dialog__back__btn)).inRoot(isDialog()).perform(click());

        onData(is(instanceOf(Item.class)))
                .inAdapterView(withId(R.id.activity__item__list__item__list)).atPosition(0)
                .onChildView(withId(R.id.listview__item__title))
                .check(matches(withText("Samsonite Winfield 2 Fashions 2-Piece Hardside Luggage Set")));
    }
}
