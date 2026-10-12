package org.nexlibre.orbita

import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.lifecycle.ViewModelProvider
import org.nexlibre.orbita.core.LauncherState

@RunWith(AndroidJUnit4::class)
class LauncherSmokeTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    @Test fun homeOnboardingSearchGesturesAndAppearance() {
        @Suppress("DEPRECATION")
        val homes = ui.activity.packageManager.queryIntentActivities(Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_HOME).addCategory(Intent.CATEGORY_DEFAULT),0)
        assertTrue(homes.any { it.activityInfo.packageName == ui.activity.packageName })
        ui.waitUntil(10_000) { ui.onAllNodesWithText("Comenzar").fetchSemanticsNodes().isNotEmpty() }
        lateinit var state: LauncherState
        ui.runOnUiThread { state = ViewModelProvider(ui.activity)[LauncherState::class.java] }
        ui.onNodeWithText("Comenzar").performClick()
        ui.waitUntil(10_000) { !state.snapshot.welcome }
        ui.onNodeWithTag("gestureHandle").performTouchInput { swipeUp() }
        ui.onNodeWithText("Buscar aplicaciones").assertIsDisplayed()
        ui.onNodeWithText("Buscar aplicaciones").performTextInput("no-such-app-81937")
        ui.onNodeWithText("Sin coincidencias.").assertIsDisplayed()
        ui.onNodeWithTag("gestureHandle").performTouchInput { swipeDown() }
        ui.onNodeWithText("Aplicaciones").assertIsDisplayed()
        ui.onNodeWithContentDescription("Ajustes").performClick()
        ui.onNodeWithText("Oscuro").performClick()
        ui.onNodeWithText("Guardar").performClick()
        ui.waitUntil(10_000) { state.snapshot.appearance.theme == "dark" }
        ui.onNodeWithContentDescription("Ajustes").performClick()
        ui.onNodeWithText("Oscuro").assertIsSelected()
        ui.onNodeWithText("Claro").performClick()
        ui.onNodeWithText("Descartar").performClick()
        ui.onNodeWithContentDescription("Ajustes").performClick()
        ui.onNodeWithText("Oscuro").assertIsSelected()
        ui.onNodeWithText("Descartar").performClick()
        ui.waitUntil(10_000) { state.snapshot.apps.map { it.label }.distinct().size >= 2 }
        val apps = state.snapshot.apps.distinctBy { it.label }.take(2)
        ui.onNodeWithText("Aplicaciones").performClick()
        ui.onNodeWithText("Buscar aplicaciones").performTextInput(apps[0].label)
        ui.onAllNodesWithContentDescription("Opciones de ${apps[0].label}").onFirst().performClick()
        ui.onNodeWithText("Añadir al escritorio").performClick()
        ui.waitUntil(10_000) { state.snapshot.layout.items.size == 1 }
        ui.onNodeWithText("Escritorio").performClick()
        ui.onNodeWithContentDescription("Abrir ${apps[0].label}").assertHasClickAction().performTouchInput { longClick() }
        ui.onNodeWithText("Crear carpeta").performClick()
        ui.onNodeWithText("Nombre").performTextReplacement("Prueba QA")
        ui.onNodeWithText("Guardar").performClick()
        ui.waitUntil(10_000) { state.snapshot.layout.items.single().folder }
        ui.onNodeWithContentDescription("Abrir carpeta Prueba QA").performClick()
        ui.onNodeWithText("Añadir aplicaciones").performClick()
        ui.onNodeWithText(apps[1].label).performClick()
        ui.waitUntil(10_000) { state.snapshot.layout.items.single().members.size == 2 }
        ui.onNodeWithText("Listo").performClick()
        ui.onNodeWithContentDescription("Bajar ${apps[0].label}").performClick()
        ui.waitUntil(10_000) { state.snapshot.layout.items.single().members.first() == apps[1].key }
        ui.onNodeWithText("Cerrar").performClick()
        ui.onNodeWithContentDescription("Abrir carpeta Prueba QA").performTouchInput { longClick() }
        ui.onNodeWithText("Renombrar carpeta").performClick()
        ui.onNodeWithText("Nombre").performTextReplacement("Carpeta QA")
        ui.onNodeWithText("Guardar").performClick()
        ui.waitUntil(10_000) { state.snapshot.layout.items.single().title == "Carpeta QA" }
        ui.onNodeWithContentDescription("Abrir carpeta Carpeta QA").performTouchInput { longClick() }
        ui.onNodeWithText("Mover").performClick()
        ui.onNodeWithContentDescription("Mover a posición 12").performClick()
        ui.waitUntil(10_000) { state.snapshot.layout.items.single().slot == 11 }
        ui.onNodeWithContentDescription("Deshacer último cambio").performClick()
        ui.waitUntil(10_000) { state.snapshot.layout.items.single().slot == 0 }
        ui.onNodeWithContentDescription("Abrir carpeta Carpeta QA").performClick()
        ui.onNodeWithContentDescription("Sacar ${apps[0].label} al escritorio").performClick()
        ui.waitUntil(10_000) { state.snapshot.layout.items.size == 2 }
        ui.onNodeWithText("Cerrar").performClick()
        ui.onNodeWithContentDescription("Abrir ${apps[0].label}").performTouchInput { longClick() }
        ui.onNodeWithText("Quitar acceso").performClick()
        ui.waitUntil(10_000) { state.snapshot.layout.items.size == 1 }
        assertTrue(state.snapshot.apps.any { it.key == apps[0].key })
    }
}
