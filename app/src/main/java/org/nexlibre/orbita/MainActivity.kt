package org.nexlibre.orbita

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.toArgb
import org.nexlibre.orbita.feature.themePalette
import android.hardware.display.DisplayManager
import android.os.Handler
import android.os.Looper
import org.nexlibre.orbita.core.refreshPreference
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import org.nexlibre.orbita.core.LauncherState
import org.nexlibre.orbita.feature.Orbita

class MainActivity : ComponentActivity() {
    private lateinit var state: LauncherState
    val launcherState: LauncherState get() = state
    private var registered = false
    private var rates by mutableStateOf(emptyList<Float>())
    private val displays by lazy { getSystemService(DisplayManager::class.java) }
    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) { updateRates() }
        override fun onDisplayRemoved(displayId: Int) { updateRates() }
        override fun onDisplayChanged(displayId: Int) { updateRates() }
    }
    @Suppress("DEPRECATION")
    private fun updateRates() {
        val display = windowManager.defaultDisplay
        val current = display.mode
        rates = display.supportedModes.filter {
            it.physicalWidth == current.physicalWidth && it.physicalHeight == current.physicalHeight
        }.map { it.refreshRate }
    }
    private var homeRequest by mutableIntStateOf(0)
    private val packages = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) { state.refresh() }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        state = (application as OrbitaApplication).launcherState
        setContent {
            SideEffect {
                val attributes = window.attributes
                val preferred = refreshPreference(state.snapshot.appearance.refreshHz, rates)
                if (attributes.preferredRefreshRate != preferred) {
                    attributes.preferredRefreshRate = preferred
                    window.attributes = attributes
                }
                val systemDark = (resources.configuration.uiMode and 0x30) == 0x20
                val palette = themePalette(state.snapshot.appearance.theme, systemDark)
                @Suppress("DEPRECATION")
                run { window.statusBarColor = palette.background.toArgb(); window.navigationBarColor = palette.background.toArgb() }
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    val dark = state.snapshot.appearance.theme in listOf("dark", "amoled", "liquid") ||
                        (state.snapshot.appearance.theme == "auto" && (resources.configuration.uiMode and 0x30) == 0x20)
                    isAppearanceLightStatusBars = !dark; isAppearanceLightNavigationBars = !dark
                }
            }
            Orbita(state, homeRequest, rates)
        }
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.hasCategory(Intent.CATEGORY_HOME)) homeRequest++
    }
    override fun onStart() {
        super.onStart()
        updateRates()
        displays.registerDisplayListener(displayListener, Handler(Looper.getMainLooper()))
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        if (Build.VERSION.SDK_INT >= 33) registerReceiver(packages, filter, Context.RECEIVER_NOT_EXPORTED)
        else {
            @Suppress("DEPRECATION")
            registerReceiver(packages, filter)
        }
        registered = true
        state.refresh()
    }
    override fun onStop() {
        displays.unregisterDisplayListener(displayListener)
        if (registered) { unregisterReceiver(packages); registered = false }
        super.onStop()
    }
}
