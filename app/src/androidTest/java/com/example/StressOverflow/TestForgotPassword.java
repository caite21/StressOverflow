package com.example.StressOverflow;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.StressOverflow.SignIn.ForgotPasswordActivity;
import com.example.StressOverflow.SignIn.SignInActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class TestForgotPassword {
    @Rule
    public ActivityScenarioRule<ForgotPasswordActivity> scenario =
            new ActivityScenarioRule<ForgotPasswordActivity>(ForgotPasswordActivity.class);
    @Test
    public void testSuccessSending(){
        Intents.init();
        onView(withId(R.id.email_field)).perform(ViewActions.typeText("test@test.com"));
        onView(withId(R.id.password_field)).perform(closeSoftKeyboard());
        onView(withId(R.id.reset_password_button)).perform(click());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        intended(hasComponent(SignInActivity.class.getName()));
        Intents.release();
    }
}
