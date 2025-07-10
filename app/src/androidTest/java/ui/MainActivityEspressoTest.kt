package ui

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.example.offlinemuehle.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import android.Manifest
import androidx.test.core.app.ActivityScenario
import androidx.test.rule.GrantPermissionRule

@RunWith(AndroidJUnit4::class)
class MainActivityEspressoTest {

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testRoleSelectionFragmentLoads() {
        onView(withId(R.id.button_host)).check(matches(isDisplayed()))
        onView(withId(R.id.button_client)).check(matches(isDisplayed()))
    }

    @Test
    fun testHostButtonLaunchesGameFragment() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity { it.forceDemoMode = true }

        onView(withId(R.id.button_host)).perform(click())
        onView(withId(R.id.gridLayout)).check(matches(isDisplayed()))
    }

    @Test
    fun testClientButtonShowsDeviceSelection() {
        onView(withId(R.id.button_client)).perform(click())
        onView(withText("Verfügbare Geräte")).check(matches(isDisplayed()))
    }
}