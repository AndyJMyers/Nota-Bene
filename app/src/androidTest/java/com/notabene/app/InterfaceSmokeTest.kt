package com.notabene.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InterfaceSmokeTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun clearRecords() {
        compose.runOnIdle {
            runBlocking { NotaBeneDatabase.get(compose.activity).clearAllTables() }
        }
    }

    @Test
    fun allFiveGospelInstrumentsOpenTheirWorkingPanels() {
        compose.onNodeWithText("MEDS").performClick()
        compose.onNodeWithText("NEW MEDICATION SCHEDULE").assertIsDisplayed()

        compose.onNodeWithText("SOMA").performClick()
        compose.onNodeWithText("NEW SOMA RECORD").assertIsDisplayed()

        compose.onNodeWithText("TASK").performClick()
        compose.onNodeWithText("NEW TASK").assertIsDisplayed()

        compose.onNodeWithText("ASK").performClick()
        compose.onNodeWithText("NEW QUESTION / TASK").assertIsDisplayed()

        compose.onNodeWithText("SPEND").performClick()
        compose.onNodeWithText("CAPTURE / REVIEW").assertIsDisplayed()
    }

    @Test
    fun settingsKeepsAdministrativeControlsOneLevelDown() {
        compose.onNodeWithContentDescription("Settings").performClick()
        compose.onNodeWithText("SETTINGS").assertIsDisplayed()
        compose.onNodeWithText("EXPORT XLSX").assertIsDisplayed()
        compose.onNodeWithText("PRIVACY & SAFETY").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun MEDSLogButtonRemainsAvailableForASecondSameDayEntry() {
        compose.onNodeWithText("MEDS").performClick()
        compose.onNode(hasText("Medication") and hasSetTextAction()).performTextInput("Example")
        compose.onNode(hasText("Dosage") and hasSetTextAction()).performTextInput("one")
        compose.onNode(hasText("Doses left") and hasSetTextAction()).performTextInput("10")
        compose.onNodeWithText("ADD").performClick()
        waitForText("0/1")

        compose.onNodeWithText("LOG TAKEN").performScrollTo().performClick()
        waitForText("1/1")
        compose.onNodeWithText("LOG TAKEN").performScrollTo().performClick()
        waitForText("2/1")
    }

    @Test
    fun unfinishedTaskSurvivesActivityRecreation() {
        compose.onNodeWithText("TASK").performClick()
        compose.onNode(hasText("What needs doing?") and hasSetTextAction()).performTextInput("Survive rotation")

        compose.activityRule.scenario.recreate()

        compose.onNode(hasText("Survive rotation") and hasSetTextAction()).assertIsDisplayed()
    }

    private fun waitForText(text: String) {
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
