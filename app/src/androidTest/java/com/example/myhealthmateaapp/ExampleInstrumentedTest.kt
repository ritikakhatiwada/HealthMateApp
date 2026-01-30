package com.example.myhealthmateaapp

import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginInstrumentedTest {

    @Before
    fun setup() {
        Intents.init()
        launch(LoginActivity::class.java)
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun login_withValidCredentials_opensMainActivity() {

        // Type email
        onView(withId(R.id.etEmail))
            .perform(typeText("test@gmail.com"), closeSoftKeyboard())

        // Type password
        onView(withId(R.id.etPassword))
            .perform(typeText("123456"), closeSoftKeyboard())

        // Click login
        onView(withId(R.id.btnLogin))
            .perform(click())

        // Verify MainActivity is opened
        Intents.intended(hasComponent(MainActivity::class.java.name))
    }
}
