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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import org.nexlibre.orbita.core.LauncherState
import org.nexlibre.orbita.feature.Orbita

class MainActivity : ComponentActivity() {
    private lateinit var state: LauncherState
    private var registered = false
    private var homeRequest by mutableIntStateOf(0)
    private val packages = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) { state.refresh() }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        state = ViewModelProvider(this)[LauncherState::class.java]
        setContent { Orbita(state, homeRequest) }
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.hasCategory(Intent.CATEGORY_HOME)) homeRequest++
    }
    override fun onStart() {
        super.onStart()
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
        if (registered) { unregisterReceiver(packages); registered = false }
        super.onStop()
    }
}
