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
    fun firstCollectionExplainsHowToKeepAnItem() {
        waitForText("NEW TODO ITEM")
        compose.onNodeWithText("Your first task starts above. Write it, then tap KEEP ITEM.").assertIsDisplayed()
        compose.onNodeWithContentDescription("Add a collection").assertIsDisplayed()
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
        compose.onNodeWithContentDescription("Add a collection").performClick()
        compose.onNodeWithText("NEW COLLECTION").assertIsDisplayed()
        compose.onNode(hasText("Name") and hasSetTextAction()).performTextInput("Journal")
        compose.onNodeWithText("LOG").performClick()
        compose.onNodeWithText("CREATE").performClick()
        waitForText("NEW LOG ITEM")
        compose.onNodeWithText("JOURNAL").assertIsDisplayed()
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
