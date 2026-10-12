package org.nexlibre.orbita
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
@RunWith(AndroidJUnit4::class)
class LargeTextTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    @Test fun essentialControlsRemainReachableAtTwoHundredPercent() {
        ui.onNodeWithText("Aplicaciones").assertIsDisplayed().assertHasClickAction().performClick()
        ui.onNodeWithText("Buscar aplicaciones").assertIsDisplayed().performTextInput("no-result-7391")
        ui.waitUntil(10_000) { ui.onAllNodesWithText("Sin coincidencias.").fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithText("Sin coincidencias.").assertIsDisplayed()
        ui.onNodeWithText("Escritorio").assertIsDisplayed().performClick()
        ui.onNodeWithContentDescription("Ajustes").assertIsDisplayed().performClick()
        ui.onNodeWithText("Mostrar etiquetas").assertHasClickAction()
        ui.onNodeWithText("Accesibilidad de Android").performScrollTo().assertIsDisplayed().assertHasClickAction()
        ui.onNodeWithText("Descartar").assertIsDisplayed().performClick()
    }
}
