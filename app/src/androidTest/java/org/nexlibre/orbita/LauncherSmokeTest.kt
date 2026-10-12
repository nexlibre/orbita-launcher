package org.nexlibre.orbita

import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LauncherSmokeTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    @Test fun homeOnboardingSearchGesturesAndAppearance() {
        @Suppress("DEPRECATION")
        val homes = ui.activity.packageManager.queryIntentActivities(Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_HOME).addCategory(Intent.CATEGORY_DEFAULT),0)
        assertTrue(homes.any { it.activityInfo.packageName == ui.activity.packageName })
        ui.waitUntil(10_000) { ui.onAllNodesWithText("Comenzar").fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithText("Comenzar").performClick()
        ui.onNodeWithTag("gestureHandle").performTouchInput { swipeUp() }
        ui.onNodeWithText("Buscar aplicaciones").assertIsDisplayed()
        ui.onNodeWithText("Buscar aplicaciones").performTextInput("no-such-app-81937")
        ui.onNodeWithText("Sin coincidencias.").assertIsDisplayed()
        ui.onNodeWithTag("gestureHandle").performTouchInput { swipeDown() }
        ui.onNodeWithText("Aplicaciones").assertIsDisplayed()
        ui.onNodeWithContentDescription("Ajustes").performClick()
        ui.onNodeWithText("Oscuro").performClick()
        ui.onNodeWithText("Guardar").performClick()
        ui.onNodeWithContentDescription("Ajustes").performClick()
        ui.onNodeWithText("Oscuro").assertIsSelected()
        ui.onNodeWithText("Claro").performClick()
        ui.onNodeWithText("Descartar").performClick()
        ui.onNodeWithContentDescription("Ajustes").performClick()
        ui.onNodeWithText("Oscuro").assertIsSelected()
        ui.onNodeWithText("Descartar").performClick()
    }
}
