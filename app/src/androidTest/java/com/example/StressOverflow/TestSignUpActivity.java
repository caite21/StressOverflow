package com.example.StressOverflow;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.junit.Assert.assertTrue;

import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.StressOverflow.Item.ListActivity;
import com.example.StressOverflow.SignIn.SignUpActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class TestSignUpActivity {
    @Rule
    public ActivityScenarioRule<SignUpActivity> scenario =
            new ActivityScenarioRule<SignUpActivity>(SignUpActivity.class);


    @Test
    public void testSignUpFailure(){
        onView(withId(R.id.sign_up_button)).perform(click());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        onView(withId(R.id.username_field)).check(matches(hasErrorText("This field cannot be blank")));
        onView(withId(R.id.email_field)).check(matches(hasErrorText("This field cannot be blank")));
        onView(withId(R.id.password_field)).check(matches(hasErrorText("This field cannot be blank")));
        onView(withId(R.id.reenter_password_field)).check(matches(hasErrorText("This field cannot be blank")));
    }
    @Test
    public void testSignUpUsernameTaken(){
        onView(withId(R.id.username_field)).perform(ViewActions.typeText("Sagi"));
        onView(withId(R.id.password_field)).perform(closeSoftKeyboard());
        onView(withId(R.id.email_field)).perform(ViewActions.typeText("test@test.com"));
        onView(withId(R.id.password_field)).perform(closeSoftKeyboard());
        onView(withId(R.id.password_field)).perform(ViewActions.typeText("testing"));
        onView(withId(R.id.password_field)).perform(closeSoftKeyboard());
        onView(withId(R.id.reenter_password_field)).perform(ViewActions.typeText("testing"));
        onView(withId(R.id.password_field)).perform(closeSoftKeyboard());

        onView(withId(R.id.sign_up_button)).perform(click());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        onView(withId(R.id.username_field)).check(matches(hasErrorText("Username is already taken")));
    }

    @Test
    public void testSignUpSuccess() {
        Intents.init();
        onView(withId(R.id.username_field)).perform(ViewActions.typeText("Sagi2"));
        onView(withId(R.id.password_field)).perform(closeSoftKeyboard());
        onView(withId(R.id.email_field)).perform(ViewActions.typeText("test2@test.com"));
        onView(withId(R.id.password_field)).perform(closeSoftKeyboard());
        onView(withId(R.id.password_field)).perform(ViewActions.typeText("testing"));
        onView(withId(R.id.password_field)).perform(closeSoftKeyboard());
        onView(withId(R.id.reenter_password_field)).perform(ViewActions.typeText("testing"));
        onView(withId(R.id.password_field)).perform(closeSoftKeyboard());

        onView(withId(R.id.sign_up_button)).perform(click());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        intended(hasComponent(ListActivity.class.getName()));
        Intents.release();
    }

    @Test
    public void testSignUpPasswordsMismatch() {
        onView(withId(R.id.username_field)).perform(ViewActions.typeText("Sagi3"));
        onView(withId(R.id.password_field)).perform(closeSoftKeyboard());
        onView(withId(R.id.email_field)).perform(ViewActions.typeText("test3@test.com"));
        onView(withId(R.id.password_field)).perform(closeSoftKeyboard());
        onView(withId(R.id.password_field)).perform(ViewActions.typeText("testing"));
        onView(withId(R.id.password_field)).perform(closeSoftKeyboard());
        onView(withId(R.id.reenter_password_field)).perform(ViewActions.typeText("testing2"));
        onView(withId(R.id.password_field)).perform(closeSoftKeyboard());

        onView(withId(R.id.sign_up_button)).perform(click());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        onView(withId(R.id.reenter_password_field)).check(matches(hasErrorText("Password don't match!")));
    }
}
