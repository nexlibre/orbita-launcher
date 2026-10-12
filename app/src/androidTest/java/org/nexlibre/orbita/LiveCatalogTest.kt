package org.nexlibre.orbita

import android.app.Application
import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Real package broadcasts and Android accessibility actions, without a simulated catalog. */
@RunWith(AndroidJUnit4::class)
class LiveCatalogTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val automation get() = instrumentation.uiAutomation
    private val fixture = "org.nexlibre.orbita.fixture/org.nexlibre.orbita.fixture.FixtureActivity"
    private fun shell(command: String): String = automation.executeShellCommand(command).use { descriptor ->
        android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor).bufferedReader().use { it.readText() }
    }
    private fun waitFor(condition: () -> Boolean) = runBlocking { withTimeout(20_000) { while (!condition()) delay(100) } }
    private fun nodes(): List<AccessibilityNodeInfo> {
        val result=mutableListOf<AccessibilityNodeInfo>()
        val root=automation.rootInActiveWindow ?: return result
        val queue=ArrayDeque<AccessibilityNodeInfo>();queue.add(root)
        while(queue.isNotEmpty()) { val node=queue.removeFirst();result.add(node);repeat(node.childCount) { node.getChild(it)?.let(queue::addLast) } }
        return result
    }
    private fun click(text: String? = null, description: String? = null) {
        var node:AccessibilityNodeInfo?=null
        waitFor { node=nodes().firstOrNull { if (description!=null) it.contentDescription?.toString()==description else it.text?.toString()==text };node!=null }
        while(node!=null && !node!!.isClickable) node=node!!.parent
        assertNotNull("Accessible clickable control: $text / $description",node)
        assertTrue(node!!.performAction(AccessibilityNodeInfo.ACTION_CLICK))
    }
    private fun home() { shell("input keyevent KEYCODE_HOME");waitFor { nodes().any { it.contentDescription?.toString()=="Ajustes" } } }
    private fun fixtureVisible() { waitFor { nodes().any { it.packageName?.toString()=="org.nexlibre.orbita.fixture" } } }
    @Test fun liveInstallLaunchFromDrawerAndDesktopUninstallPreservesLayout() {
        val app=instrumentation.targetContext.applicationContext as Application
        val repository=org.nexlibre.orbita.core.LauncherRepositories.get(app)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            lateinit var state:org.nexlibre.orbita.core.LauncherState
            scenario.onActivity { state=ViewModelProvider(it)[org.nexlibre.orbita.core.LauncherState::class.java] }
            waitFor { state.snapshot.ready && state.snapshot.apps.isNotEmpty() }
            val original=state.snapshot.layout
            assertTrue(state.snapshot.apps.none { it.key==fixture })
            assertTrue(shell("pm install -r /data/local/tmp/orbita-fixture.apk").contains("Success"))
            // The Activity is already visible: receiving PACKAGE_ADDED must update this same ViewModel.
            waitFor { state.snapshot.apps.any { it.key==fixture } }
            val label=state.snapshot.apps.first { it.key==fixture }.label
            click(text="Aplicaciones")
            var edit:AccessibilityNodeInfo?=null
            waitFor { edit=nodes().firstOrNull { it.isEditable };edit!=null }
            assertTrue(edit!!.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,label)
            }))
            click(text=label);fixtureVisible()
            home()
            runBlocking { repository.change { it.add(fixture,0) } }
            waitFor { state.snapshot.layout.items.any { !it.folder && it.members==listOf(fixture) } }
            click(description="Abrir $label");fixtureVisible()
            home()
            runBlocking { repository.recover(original);repository.saved.first() }
            waitFor { state.snapshot.layout==original }
            assertTrue(shell("pm uninstall org.nexlibre.orbita.fixture").contains("Success"))
            waitFor { state.snapshot.apps.none { it.key==fixture } }
            assertEquals(original,state.snapshot.layout)
        }
    }
}
