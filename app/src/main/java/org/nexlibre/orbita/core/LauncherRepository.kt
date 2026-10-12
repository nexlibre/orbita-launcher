package org.nexlibre.orbita.core

import android.app.Application
import android.net.Uri
import android.util.AtomicFile
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

private val layoutKey = stringPreferencesKey("layout_v2")
private val legacyHome = stringPreferencesKey("home")
private val legacyDock = stringPreferencesKey("dock")
private val recoveryKey = booleanPreferencesKey("needs_recovery")
private val lastGoodKey = stringPreferencesKey("last_good_v2")
private val onboardedKey = booleanPreferencesKey("onboarded")
private val labelsKey = booleanPreferencesKey("labels")
private val reducedKey = booleanPreferencesKey("reduced_motion")
private val hapticsKey = booleanPreferencesKey("haptics")
private val themeKey = stringPreferencesKey("theme")

// One DataStore instance per file, shared across Activity/ViewModel recreation.
object LauncherRepositories {
    @Volatile private var instance: LauncherRepository? = null
    fun get(application: Application): LauncherRepository = instance ?: synchronized(this) {
        instance ?: LauncherRepository(application).also { instance = it }
    }
}

data class Appearance(val labels: Boolean = true, val reducedMotion: Boolean = false, val haptics: Boolean = true, val theme: String = "auto")
data class SavedLauncher(val layout: Layout, val appearance: Appearance, val welcome: Boolean, val recovery: Boolean = false, val backup: Layout? = null)

class LauncherRepository(private val app: Application) {
    private val file = app.preferencesDataStoreFile("orbita_core_v1")
    private val backup = AtomicFile(File(app.filesDir, "orbita-last-valid.layout"))
    private val backupLock = Any()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val store = PreferenceDataStoreFactory.create(
        corruptionHandler = ReplaceFileCorruptionHandler {
            archive()
            mutablePreferencesOf(recoveryKey to true).apply {
                validSavedBackup()?.let {
                    this[layoutKey] = LayoutCodec.encode(it.layout); this[lastGoodKey] = LayoutCodec.encode(it.layout)
                    this[labelsKey] = it.appearance.labels; this[reducedKey] = it.appearance.reducedMotion
                    this[hapticsKey] = it.appearance.haptics; this[themeKey] = it.appearance.theme; this[onboardedKey] = !it.welcome
                }
            }
        }, scope = scope, produceFile = { file }
    )
    val saved: Flow<SavedLauncher> = store.data.map { prefs ->
        val appearance = Appearance(prefs[labelsKey] ?: true, prefs[reducedKey] ?: false, prefs[hapticsKey] ?: true,
            prefs[themeKey]?.takeIf { it in listOf("auto", "light", "dark") } ?: "auto")
        val good = validBackup() ?: runCatching { prefs[lastGoodKey]?.let(LayoutCodec::decode) }.getOrNull()
        val decoded = runCatching {
            prefs[layoutKey]?.let(LayoutCodec::decode) ?: LayoutCodec.migrate(
                prefs[legacyHome].orEmpty().split('\n').filter(String::isNotBlank),
                prefs[legacyDock].orEmpty().split('\n').filter(String::isNotBlank))
        }
        SavedLauncher(decoded.getOrNull() ?: good ?: Layout(), appearance, !(prefs[onboardedKey] ?: false),
            prefs[recoveryKey] == true || decoded.isFailure, good)
    }.flowOn(Dispatchers.IO)
    suspend fun change(transform: (Layout) -> Layout): Pair<Layout, Layout> {
        var before = Layout(); var after = Layout()
        store.edit { prefs ->
            check(prefs[recoveryKey] != true) { "Recupera los datos antes de editar." }
            before = prefs[layoutKey]?.let(LayoutCodec::decode) ?: LayoutCodec.migrate(
                prefs[legacyHome].orEmpty().split('\n').filter(String::isNotBlank), prefs[legacyDock].orEmpty().split('\n').filter(String::isNotBlank))
            after = transform(before).checked()
            prefs[lastGoodKey] = LayoutCodec.encode(before)
            prefs[layoutKey] = LayoutCodec.encode(after)
        }
        // A failed secondary backup never rolls back a successful DataStore transaction.
        withContext(Dispatchers.IO) { runCatching { writeBackup(saved.first()) } }
        return before to after
    }
    suspend fun appearance(value: Appearance) {
        require(value.theme in listOf("auto", "light", "dark"))
        store.edit { it[labelsKey] = value.labels; it[reducedKey] = value.reducedMotion; it[hapticsKey] = value.haptics; it[themeKey] = value.theme }
        withContext(Dispatchers.IO) { runCatching { writeBackup(saved.first()) } }
    }
    suspend fun onboard() { store.edit { it[onboardedKey] = true }; withContext(Dispatchers.IO) { runCatching { writeBackup(saved.first()) } } }
    suspend fun recover(layout: Layout) {
        layout.checked()
        withContext(Dispatchers.IO) { archive() }
        store.edit { it[layoutKey] = LayoutCodec.encode(layout); it[lastGoodKey] = LayoutCodec.encode(layout); it[recoveryKey] = false; it[onboardedKey] = true }
        withContext(Dispatchers.IO) { writeBackup(saved.first()) }
    }
    suspend fun export(uri: Uri) = withContext(Dispatchers.IO) {
        val current = saved.first()
        val output = app.contentResolver.openOutputStream(uri) ?: error("No se pudo abrir el archivo.")
        ZipOutputStream(output).use { zip ->
            fun put(name: String, bytes: ByteArray) { zip.putNextEntry(ZipEntry(name)); zip.write(bytes); zip.closeEntry() }
            put("layout-v2.txt", LayoutCodec.encode(current.layout).toByteArray())
            put("README.txt", "Respaldo local de ÓRBITA. Apps externas deben reinstalarse. Datos originales pueden incluir preferencias dañadas; no editar durante la recuperación.".toByteArray())
            (listOf(file, backup.baseFile) + recoveryFiles()).filter(File::exists).forEach { f ->
                zip.putNextEntry(ZipEntry("original/" + f.name)); f.inputStream().use { it.copyTo(zip) }; zip.closeEntry()
            }
        }
    }
    private fun recoveryFiles() = app.filesDir.listFiles()?.filter { it.name.startsWith("orbita-recovery-") }.orEmpty()
    private fun archive() {
        if (file.exists()) file.copyTo(File(app.filesDir, "orbita-recovery-${System.currentTimeMillis()}.pb"), overwrite = false)
    }
    private fun validBackup(): Layout? = validSavedBackup()?.layout
    private fun validSavedBackup(): SavedLauncher? = synchronized(backupLock) { runCatching {
        backup.openRead().use { input ->
            require(backup.baseFile.length() <= 2_000_000)
            val lines = input.bufferedReader().readLines()
            val layout = LayoutCodec.decode(lines.first())
            val appearance = if (lines.size == 7) Appearance(lines[1].toBooleanStrict(), lines[2].toBooleanStrict(), lines[3].toBooleanStrict(), lines[4]) else Appearance()
            require(appearance.theme in listOf("auto", "light", "dark"))
            SavedLauncher(layout, appearance, if (lines.size == 7) lines[5].toBooleanStrict() else false)
        }
    }.getOrNull() }
    private fun writeBackup(saved: SavedLauncher) = synchronized(backupLock) {
        val output = backup.startWrite()
        val value = listOf(LayoutCodec.encode(saved.layout), saved.appearance.labels.toString(), saved.appearance.reducedMotion.toString(), saved.appearance.haptics.toString(), saved.appearance.theme, saved.welcome.toString(), "backup-v1").joinToString("\n")
        try { output.write(value.toByteArray()); backup.finishWrite(output) }
        catch (failure: Exception) { backup.failWrite(output); throw failure }
    }
}
