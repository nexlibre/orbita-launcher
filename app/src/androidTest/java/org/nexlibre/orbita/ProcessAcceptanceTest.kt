package org.nexlibre.orbita

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.nexlibre.orbita.core.*

/** Methods run individually in fresh processes by ci/android-acceptance.sh. */
@RunWith(AndroidJUnit4::class)
class ProcessAcceptanceTest {
    private val app get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
    private val repository get() = LauncherRepositories.get(app)
    private val fixture = "org.nexlibre.orbita.fixture/org.nexlibre.orbita.fixture.FixtureActivity"
    private fun expected(): Layout = Layout(2, listOf(
        Placement("folder-test",listOf("com.android.settings/com.android.settings.Settings",fixture),"Pruebas",true,1,7),
        Placement("dock-test",listOf("com.android.calculator2/com.android.calculator2.Calculator"),slot=3,dock=true)))
    @Test fun seed() = runBlocking {
        repository.recover(expected()); repository.appearance(Appearance(labels=false,reducedMotion=true,haptics=false,theme="dark"));repository.onboard()
        assertEquals(expected(),repository.saved.first().layout)
    }
    @Test fun verifyAfterRestart() = runBlocking {
        val saved=repository.saved.first()
        assertEquals(expected(),saved.layout);assertFalse(saved.welcome);assertFalse(saved.recovery)
        assertEquals(Appearance(false,true,false,"dark"),saved.appearance)
    }
    @Test fun fixtureInstalledAndLaunchable() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            lateinit var state: LauncherState
            scenario.onActivity { state=ViewModelProvider(it)[LauncherState::class.java];state.refresh() }
            runBlocking { withTimeout(15_000) { while (state.snapshot.apps.none { it.key==fixture }) delay(100) } }
            scenario.onActivity { state.open(state.snapshot.apps.first { it.key==fixture }) }
            Thread.sleep(500)
            assertNull(state.snapshot.error)
        }
    }
    @Test fun fixtureAbsentLayoutPreserved() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            lateinit var state:LauncherState
            scenario.onActivity { state=ViewModelProvider(it)[LauncherState::class.java];state.refresh() }
            runBlocking { withTimeout(15_000) { while(!state.snapshot.ready || state.snapshot.apps.isEmpty()) delay(100) } }
            assertTrue(state.snapshot.apps.none { it.key==fixture })
            assertEquals(expected(),state.snapshot.layout)
        }
    }
    @Test fun corruptionIsBlockedAndOriginalArchived() = runBlocking {
        val saved=repository.saved.first()
        assertTrue(saved.recovery);assertEquals(expected(),saved.layout);assertEquals(expected(),saved.backup)
        assertTrue(runCatching { repository.change { Layout() } }.isFailure)
        assertTrue(app.filesDir.listFiles().orEmpty().any { it.name.startsWith("orbita-recovery-") && it.readText().contains("invalid-protobuf") })
    }
    @Test fun explicitRecoveryRestoresValidLayout() = runBlocking {
        val saved=repository.saved.first();assertTrue(saved.recovery)
        repository.recover(saved.backup!!)
        val restored=repository.saved.first();assertFalse(restored.recovery);assertEquals(expected(),restored.layout)
    }
}
