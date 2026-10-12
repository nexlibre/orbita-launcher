package org.nexlibre.orbita.core

import org.junit.Assert.*
import org.junit.Test
import java.util.Locale

class CatalogTest {
    @Test fun searchIgnoresAccentsAndCase() {
        assertEquals(listOf("a"), visibleApps(listOf(AppRecord("a", "Cámara")), " CAMARA ").map { it.key })
    }
    @Test fun duplicateNamesHaveDeterministicOrderAndComponentsAreUnique() {
        val apps = listOf(AppRecord("b", "App"), AppRecord("a", "App"), AppRecord("a", "App"))
        assertEquals(listOf("a", "b"), visibleApps(apps, "", Locale.US).map { it.key })
    }
    @Test fun unavailablePinsDoNotReassignPositionsOrDuplicateApps() {
        val keys = listOf("missing", "b", "a", "b")
        assertEquals(listOf("b", "a"), pinnedApps(keys, listOf(AppRecord("a", "A"), AppRecord("b", "B"))).map { it.key })
        assertEquals("missing", keys.first())
    }
    @Test fun manyAppsAndEmptyResults() {
        val apps = (0..999).map { AppRecord("$it", "App $it") }
        assertEquals(1000, visibleApps(apps, "").size)
        assertTrue(visibleApps(apps, "absent").isEmpty())
    }
}
