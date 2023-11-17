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

import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.StressOverflow.Item.ListActivity;
import com.example.StressOverflow.SignIn.ForgotPasswordActivity;
import com.example.StressOverflow.SignIn.SignInActivity;
import com.example.StressOverflow.SignIn.SignUpActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class TestSignInActivity {
    @Rule
    public ActivityScenarioRule<SignInActivity> scenario =
            new ActivityScenarioRule<SignInActivity>(SignInActivity.class);


    @Test
    public void testSignInFailure(){
        Intents.init();
        onView(withId(R.id.email_username_field)).perform(ViewActions.typeText("abc"));
        onView(withId(R.id.password_field)).perform(closeSoftKeyboard());
        onView(withId(R.id.password_field)).perform(ViewActions.typeText("testing"));
        onView(withId(R.id.password_field)).perform(closeSoftKeyboard());
        onView(withId(R.id.sign_in_button)).perform(click());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        onView(withId(R.id.email_username_field)).check(matches(hasErrorText("User doesn't exist")));
        Intents.release();
    }

    @Test
    public void testSignInNameSuccess() {
        Intents.init();
        onView(withId(R.id.email_username_field)).perform(ViewActions.typeText("Sagi"));
        onView(withId(R.id.email_username_field)).perform(closeSoftKeyboard());
        onView(withId(R.id.password_field)).perform(ViewActions.typeText("testing"));
        onView(withId(R.id.password_field)).perform(closeSoftKeyboard());
        onView(withId(R.id.sign_in_button)).perform(click());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        intended(hasComponent(ListActivity.class.getName()));
        Intents.release();
    }
    @Test
    public void testSignInEmailSuccess() {
        Intents.init();
        onView(withId(R.id.email_username_field)).perform(ViewActions.typeText("test@test.com"));
        onView(withId(R.id.email_username_field)).perform(closeSoftKeyboard());
        onView(withId(R.id.password_field)).perform(ViewActions.typeText("testing"));
        onView(withId(R.id.password_field)).perform(closeSoftKeyboard());
        onView(withId(R.id.sign_in_button)).perform(click());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        intended(hasComponent(ListActivity.class.getName()));
        Intents.release();
    }

    @Test
    public void testSignUpOpening() {
        Intents.init();
        onView(withId(R.id.sign_up_text)).perform(click());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        intended(hasComponent(SignUpActivity.class.getName()));
        Intents.release();
    }

    @Test
    public void testForgotPasswordOpening() {
        Intents.init();
        onView(withId(R.id.forgot_password_text)).perform(click());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        intended(hasComponent(ForgotPasswordActivity.class.getName()));
        Intents.release();
    }
}


