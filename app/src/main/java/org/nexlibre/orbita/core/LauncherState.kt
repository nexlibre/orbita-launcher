package org.nexlibre.orbita.core

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


data class LauncherSnapshot(
    val apps: List<AppRecord> = emptyList(), val icons: Map<String, ImageBitmap> = emptyMap(),
    val layout: Layout = Layout(), val appearance: Appearance = Appearance(), val welcome: Boolean = true,
    val ready: Boolean = false, val recovery: Boolean = false, val backup: Layout? = null,
    val error: String? = null, val canUndo: Boolean = false, val exported: Boolean = false
)

class LauncherState(application: Application) : AndroidViewModel(application) {
    var snapshot by mutableStateOf(LauncherSnapshot())
        private set
    private val repository = LauncherRepositories.get(application)
    private var inventoryJob: Job? = null
    private val edits = Mutex()
    private val history = ArrayDeque<Layout>()
    init {
        viewModelScope.launch {
            try {
                repository.saved.collect { saved ->
                    snapshot = snapshot.copy(layout = saved.layout, appearance = saved.appearance,
                        welcome = saved.welcome, recovery = saved.recovery, backup = saved.backup, ready = true)
                }
            } catch (error: Exception) { report(error, "No se pudo leer la disposición. Los datos originales se conservan.") }
        }
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
                        ensureActive()
                        if (item.activityInfo.packageName == getApplication<Application>().packageName) return@mapNotNull null
                        val key = ComponentName(item.activityInfo.packageName, item.activityInfo.name).flattenToString()
                        val label = runCatching { item.loadLabel(pm).toString() }.getOrDefault(item.activityInfo.packageName)
                        runCatching {
                            val drawable = item.loadIcon(pm)
                            val bitmap = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888)
                            drawable.setBounds(0, 0, 96, 96); drawable.draw(Canvas(bitmap))
                            icons[key] = bitmap.asImageBitmap()
                        }
                        AppRecord(key, label)
                    }
                    visibleApps(apps, "") to icons.toMap()
                }
                snapshot = snapshot.copy(apps = inventory.first, icons = inventory.second)
            } catch (error: Exception) { report(error, "No se pudo actualizar el catálogo. Intenta de nuevo.") }
        }
    }
    fun edit(transform: (Layout) -> Layout) {
        if (!snapshot.ready || snapshot.recovery) return
        viewModelScope.launch {
            edits.withLock {
                try {
                    val (before, after) = repository.change(transform)
                    if (before != after) { history.addLast(before); if (history.size > 20) history.removeFirst() }
                    snapshot = snapshot.copy(canUndo = history.isNotEmpty(), error = null)
                } catch (error: Exception) { report(error, error.message ?: "No se guardó el cambio.") }
            }
        }
    }
    fun undo() = viewModelScope.launch {
        edits.withLock {
            if (history.isNotEmpty() && !snapshot.recovery) {
                try { repository.change { history.last() }; history.removeLast(); snapshot = snapshot.copy(canUndo = history.isNotEmpty()) }
                catch (error: Exception) { report(error, "No se pudo deshacer. Los datos anteriores se conservan.") }
            }
        }
    }
    fun saveAppearance(value: Appearance) = viewModelScope.launch { try { repository.appearance(value) } catch (e: Exception) { report(e, "No se guardaron los ajustes.") } }
    fun finishWelcome() = viewModelScope.launch { try { repository.onboard() } catch (e: Exception) { report(e, "No se guardó la bienvenida.") } }
    fun recover(useBackup: Boolean) = viewModelScope.launch {
        if (!useBackup && !snapshot.exported) { snapshot = snapshot.copy(error = "Exporta los datos originales antes de restaurar el inicio vacío."); return@launch }
        edits.withLock {
            try { repository.recover(if (useBackup) snapshot.backup ?: error("No hay copia válida.") else Layout()); history.clear(); snapshot = snapshot.copy(error = null, canUndo = false, exported = false) }
            catch (e: Exception) { report(e, "No se pudieron recuperar los datos.") }
        }
    }
    fun export(uri: Uri) = viewModelScope.launch {
        try { repository.export(uri); snapshot = snapshot.copy(exported = true, error = "Respaldo guardado.") }
        catch (e: Exception) { report(e, "No se pudo guardar el respaldo; los datos originales se conservan.") }
    }
    fun dismissError() { snapshot = snapshot.copy(error = null) }
    fun open(app: AppRecord) {
        val component = ComponentName.unflattenFromString(app.key) ?: return
        try { getApplication<Application>().startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            .setComponent(component).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
        catch (_: RuntimeException) { snapshot = snapshot.copy(error = "Esta aplicación no está disponible."); refresh() }
    }
    private fun report(error: Exception, message: String) {
        if (error is CancellationException) throw error
        snapshot = snapshot.copy(error = message)
    }
}
