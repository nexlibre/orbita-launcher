package org.nexlibre.orbita.core

import org.junit.Assert.*
import org.junit.Test

class AppearanceTest {
    @Test fun ratesAreFiniteSupportedUniqueAndCapped() {
        assertEquals(listOf(60, 90, 120, 144), supportedRefreshRates(listOf(59.94f, 60f, 90f, 120f, 144f, 165f, Float.NaN, 0f)))
    }
    @Test fun unavailablePanelRateFallsBackWithoutForcingAnotherMode() {
        assertEquals(0f, refreshPreference(144, listOf(60f, 90f)), 0f)
        assertEquals(0f, refreshPreference(0, listOf(60f, 90f)), 0f)
        assertEquals(59.94f, refreshPreference(60, listOf(59.94f, 90f)), 0f)
    }
    @Test fun appearanceRejectsOutOfBoundsData() {
        assertTrue(runCatching { Appearance(refreshHz = 145).checked() }.isFailure)
        assertTrue(runCatching { Appearance(iconSize = 100).checked() }.isFailure)
        assertTrue(runCatching { Appearance(theme = "unknown").checked() }.isFailure)
    }
}
