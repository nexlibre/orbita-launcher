package org.nexlibre.orbita

import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Real Activity/Compose on emulator; does not certify TalkBack or hardware performance. */
@RunWith(AndroidJUnit4::class)
class LauncherSmokeTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()

    @Test fun homeActivityOnboardingDrawerAndSettings() {
        val activity = ui.activity
        @Suppress("DEPRECATION")
        val homes = activity.packageManager.queryIntentActivities(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addCategory(Intent.CATEGORY_DEFAULT), 0)
        assertTrue(homes.any { it.activityInfo.packageName == activity.packageName })
        ui.waitUntil(10_000) { ui.onAllNodesWithText("Comenzar").fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithText("Comenzar").performClick()
        ui.onNodeWithText("Aplicaciones").performClick()
        ui.onNodeWithText("Buscar aplicaciones").performTextInput("orbita-no-such-app-81937")
        ui.onNodeWithText("Sin coincidencias.").assertIsDisplayed()
        ui.onNodeWithText("Escritorio").performClick()
        ui.onNodeWithText("Ajustes").performClick()
        ui.onNodeWithText("Essential · Automático").assertIsDisplayed()
        ui.onNodeWithText("Descartar").performClick()
        ui.onNodeWithText("Aplicaciones").assertIsDisplayed()
    }
}
