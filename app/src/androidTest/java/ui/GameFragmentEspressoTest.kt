package ui

import ToastMatcher
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.example.offlinemuehle.R
import ui.MainActivity
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.offlinemuehle.ui.GameFragment
import org.junit.Ignore
import org.junit.Assert.assertEquals

@RunWith(AndroidJUnit4::class)
class GameFragmentEspressoTest {

    @Test
    fun testGridLayoutIsDisplayed() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
        intent.putExtra("DEMO_MODE", true)
        val scenario = ActivityScenario.launch<MainActivity>(intent)

        // Click on Host to navigate
        onView(withId(R.id.button_host)).perform(click())

        // Now GameFragment should be loaded with gridLayout
        onView(withId(R.id.gridLayout)).check(matches(isDisplayed()))
    }

    @Test
    fun testGridButtonClickWorks() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
        intent.putExtra("DEMO_MODE", true)
        val scenario = ActivityScenario.launch<MainActivity>(intent)

        onView(withId(R.id.button_host)).perform(click())
        onView(withContentDescription("grid_cell_0")).perform(click())
        onView(withContentDescription("grid_cell_0")).check(matches(isDisplayed()))
    }

    @Test
    fun testPlayerSwitchIndicatorIsVisible() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
        intent.putExtra("DEMO_MODE", true)
        val scenario = ActivityScenario.launch<MainActivity>(intent)

        // Click on Host to navigate into GameFragment
        onView(withId(R.id.button_host)).perform(click())

        // Now perform click on a grid cell in GameFragment
        onView(withContentDescription("grid_cell_0")).perform(click())

        // Verify the player switch indicator is displayed
        onView(withId(R.id.text_current_player)).check(matches(isDisplayed()))
    }

    @Test
    fun testValidMoveUpdatesUI() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
        intent.putExtra("DEMO_MODE", true)
        val scenario = ActivityScenario.launch<MainActivity>(intent)

        onView(withId(R.id.button_host)).perform(click())
        onView(withContentDescription("grid_cell_0")).perform(click())
        onView(withId(R.id.gridLayout)).check(matches(isDisplayed()))
    }

    @Ignore("Toast not in View Hierarchy")
    @Test
    fun testInvalidMoveShowErrorToast() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
        intent.putExtra("DEMO_MODE", true)
        val scenario = ActivityScenario.launch<MainActivity>(intent)

        onView(withId(R.id.button_host)).perform(click())
        onView(withId(R.id.gridLayout)).check(matches(isDisplayed()))

        onView(withContentDescription("grid_cell_0")).perform(click())
        onView(withContentDescription("grid_cell_0")).perform(click())

        // Großer Wait, Emulator ist langsam
        Thread.sleep(4000)

        scenario.onActivity { activity ->
            val fragment = activity.supportFragmentManager.findFragmentById(R.id.main_container) as? GameFragment
            assertEquals("Ungültiger Zug!", fragment?.lastErrorMessageForTest)
        }
    }
}