package com.notabene.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.Dispatchers
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
        val context = compose.activity.applicationContext
        compose.runOnIdle {
            runBlocking(Dispatchers.IO) { NotaBeneDatabase.get(context).clearAllTables() }
        }
    }

    @Test
    fun firstCollectionExplainsHowToKeepAnItem() {
        waitForText("NEW TODO ITEM")
        compose.onNodeWithText("Your first task starts above. Write it, then tap KEEP ITEM.", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("+").assertIsDisplayed()
    }

    @Test
    fun settingsCabinetKeepsDataAndHelpEasyToFind() {
        compose.onNodeWithContentDescription("Settings").performClick()
        compose.onNodeWithText("THE CABINET").assertIsDisplayed()
        compose.onNodeWithText("EXPORT XLSX").assertIsDisplayed()
        compose.onNodeWithText("IMPORT XLSX").assertIsDisplayed()
        compose.onNodeWithText("HOW IT WORKS").performScrollTo().performClick()
        compose.onNodeWithText("Tap + beside the tabs to add a collection. Hold a tab to rename or delete it.")
            .performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("ABOUT & SUPPORT").performScrollTo().performClick()
        compose.onNodeWithText("SEND FEEDBACK").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("RECOMMEND NOTA BENE").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun aNewCollectionOpensItsOwnEntryForm() {
        waitForText("NEW TODO ITEM")
        compose.onNodeWithText("+").performClick()
        compose.onNodeWithText("NEW COLLECTION").assertIsDisplayed()
        compose.onNode(hasText("Name") and hasSetTextAction()).performTextInput("Journal")
        compose.onNodeWithText("LOG").performClick()
        compose.onNodeWithText("CREATE").performClick()
        waitForText("NEW LOG ITEM")
        compose.onAllNodesWithText("JOURNAL")[0].assertIsDisplayed()
    }

    @Test
    fun repeatHistoryToggleShowsItsStateAndSurvivesRotation() {
        createRepeatItem()
        compose.onNodeWithText("Show History").performScrollTo().performClick()
        compose.onNodeWithText("No completion events logged yet").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Show History").performScrollTo().assertIsDisplayed()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("No completion events logged yet").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Show History").performScrollTo().performClick()
        compose.onNodeWithText("Show History").assertIsDisplayed()
        compose.onNodeWithText("No completion events logged yet").assertDoesNotExist()
    }

    @Test
    fun repeatLogCanBeUndoneAndHistoryCanBeCorrected() {
        createRepeatItem()
        compose.onNodeWithText("LOG").performScrollTo().performClick()
        waitForText("UNDO")
        compose.onNodeWithText("UNDO").performScrollTo().performClick()
        waitForText("Not logged yet")
        compose.onNodeWithText("Show History").performScrollTo().performClick()
        compose.onNodeWithText("ADD PAST EVENT").performScrollTo().performClick()
        compose.onNodeWithText("Add past event").assertIsDisplayed()
        compose.onNodeWithText("SAVE").performClick()
        waitForText("EDIT")
        compose.onNodeWithText("EDIT").performScrollTo().performClick()
        compose.onNodeWithText("Correct recorded time").assertIsDisplayed()
        compose.onNodeWithText("SAVE").performClick()
        compose.onNodeWithText("DELETE").performScrollTo().performClick()
        compose.onNodeWithText("Delete this occurrence?").assertIsDisplayed()
        compose.onNode(hasText("DELETE") and hasAnyAncestor(isDialog())).performClick()
        waitForText("No completion events logged yet")
        compose.onNodeWithText("Not logged yet").performScrollTo().assertIsDisplayed()
    }

    private fun createRepeatItem() {
        waitForText("NEW TODO ITEM")
        compose.onNodeWithText("+").performClick()
        compose.onNode(hasText("Name") and hasSetTextAction()).performTextInput("Routines")
        compose.onNodeWithText("REPEAT").performClick()
        compose.onNodeWithText("CREATE").performClick()
        waitForText("NEW REPEAT ITEM")
        compose.onNode(hasText("What do you want to do?") and hasSetTextAction())
            .performTextInput("Water the fern")
        compose.onNodeWithText("KEEP ITEM").performScrollTo().performClick()
        waitForText("Show History")
    }

    @Test
    fun unfinishedTodoSurvivesActivityRecreation() {
        waitForText("NEW TODO ITEM")
        compose.onNode(hasText("What do you want to do?") and hasSetTextAction())
            .performTextInput("Survive rotation")

        compose.activityRule.scenario.recreate()

        compose.onNode(hasText("Survive rotation") and hasSetTextAction()).assertIsDisplayed()
    }

    private fun waitForText(text: String) {
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
