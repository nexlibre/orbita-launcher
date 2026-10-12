package org.nexlibre.orbita.core

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val Application.store by preferencesDataStore("orbita_core_v1")
data class LauncherSnapshot(
    val apps: List<AppRecord> = emptyList(),
    val icons: Map<String, ImageBitmap> = emptyMap(),
    val home: List<String> = emptyList(),
    val dock: List<String> = emptyList(),
    val labels: Boolean = true,
    val welcome: Boolean = true,
    val ready: Boolean = false,
    val error: String? = null
)

class LauncherState(application: Application) : AndroidViewModel(application) {
    var snapshot by androidx.compose.runtime.mutableStateOf(LauncherSnapshot())
        private set
    private val homeKey = stringPreferencesKey("home")
    private val dockKey = stringPreferencesKey("dock")
    private val labelsKey = booleanPreferencesKey("labels")
    private val welcomeKey = booleanPreferencesKey("onboarded")
    private var inventoryJob: Job? = null

    init {
        viewModelScope.launch {
            try {
                application.store.data.collect { prefs ->
                    fun decode(key: Preferences.Key<String>): List<String> {
                        val raw = prefs[key].orEmpty()
                        val keys = raw.split('\n').filter { it.isNotEmpty() }
                        require(keys.size <= 1000 && keys.all { ComponentName.unflattenFromString(it) != null })
                        return keys.distinct()
                    }
                    snapshot = snapshot.copy(home = decode(homeKey), dock = decode(dockKey),
                        labels = prefs[labelsKey] ?: true, welcome = !(prefs[welcomeKey] ?: false), ready = true)
                }
            } catch (error: Exception) {
                if (error is kotlinx.coroutines.CancellationException) throw error
                snapshot = snapshot.copy(ready = false, error = "No se pudo leer la disposición. Los datos se conservan; no se guardarán cambios.")
            }
        }
        refresh()
    }

    fun refresh() {
        inventoryJob?.cancel()
        inventoryJob = viewModelScope.launch {
            try {
                val inventory = withContext(Dispatchers.IO) {
                    val pm = getApplication<Application>().packageManager
                    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
                    val icons = mutableMapOf<String, ImageBitmap>()
                    @Suppress("DEPRECATION")
                    val apps = pm.queryIntentActivities(intent, 0).mapNotNull { item ->
                        if (item.activityInfo.packageName == getApplication<Application>().packageName) return@mapNotNull null
                        val key = ComponentName(item.activityInfo.packageName, item.activityInfo.name).flattenToString()
                        try {
                            val drawable = item.loadIcon(pm)
                            val bitmap = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888)
                            drawable.setBounds(0, 0, 96, 96)
                            drawable.draw(Canvas(bitmap))
                            icons[key] = bitmap.asImageBitmap()
                        } catch (_: RuntimeException) { /* Text remains an accessible fallback. */ }
                        AppRecord(key, item.loadLabel(pm).toString())
                    }
                    visibleApps(apps, "") to icons.toMap()
                }
                snapshot = snapshot.copy(apps = inventory.first, icons = inventory.second)
            } catch (error: Exception) {
                if (error is kotlinx.coroutines.CancellationException) throw error
                snapshot = snapshot.copy(error = "No se pudo actualizar la lista de aplicaciones. Intenta de nuevo.")
            }
        }
    }

    fun pin(key: String, dock: Boolean) = save {
        val pref = if (dock) dockKey else homeKey
        val current = it[pref].orEmpty().split('\n').filter(String::isNotEmpty)
        if (key in current) it[pref] = current.filterNot { entry -> entry == key }.joinToString("\n")
        else if (!dock || current.size < 5) it[pref] = (current + key).joinToString("\n")
        else snapshot = snapshot.copy(error = "El dock admite cinco favoritos en este incremento.")
    }

    fun saveLabels(show: Boolean) = save { it[labelsKey] = show }
    fun finishWelcome() = save { it[welcomeKey] = true }
    private fun save(change: (MutablePreferences) -> Unit) {
        if (!snapshot.ready) return
        viewModelScope.launch {
            try { getApplication<Application>().store.edit { change(it) } }
            catch (error: Exception) {
                if (error is kotlinx.coroutines.CancellationException) throw error
                snapshot = snapshot.copy(error = "No se guardó el cambio. La disposición anterior se conserva.")
            }
        }
    }

    fun open(app: AppRecord) {
        val component = ComponentName.unflattenFromString(app.key) ?: return
        try {
            getApplication<Application>().startActivity(Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER).setComponent(component).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: RuntimeException) {
            snapshot = snapshot.copy(error = "Esta aplicación no está disponible.")
            refresh()
        }
    }
}
