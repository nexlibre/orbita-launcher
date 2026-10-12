package org.nexlibre.orbita.feature

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.nexlibre.orbita.core.*

@Composable
fun Orbita(state: LauncherState, homeRequest: Int) {
    val s = state.snapshot
    var drawer by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var options by remember { mutableStateOf<AppRecord?>(null) }
    var settings by rememberSaveable { mutableStateOf(false) }
    var draftLabels by remember { mutableStateOf(s.labels) }
    val context = LocalContext.current
    LaunchedEffect(homeRequest) { drawer = false; query = ""; options = null; settings = false }
    BackHandler { if (options != null) options = null else if (settings) settings = false else { drawer = false; query = "" } }
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("ÓRBITA", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(vertical = 18.dp))
                    TextButton(onClick = { draftLabels = s.labels; settings = true }) { Text("Ajustes") }
                }
                s.error?.let { Text(it, color = MaterialTheme.colorScheme.error); TextButton(onClick = state::refresh) { Text("Actualizar aplicaciones") } }
                if (drawer) {
                    OutlinedTextField(value = query, onValueChange = { query = it }, label = { Text("Buscar aplicaciones") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    val results = remember(s.apps, query) { visibleApps(s.apps, query) }
                    if (results.isEmpty()) Text(if (query.isBlank()) "No hay aplicaciones disponibles." else "Sin coincidencias.", modifier = Modifier.padding(20.dp))
                    LazyColumn(Modifier.weight(1f)) {
                        items(results, key = { it.key }) { app ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                TextButton(onClick = { state.open(app) }, modifier = Modifier.weight(1f)) {
                                    s.icons[app.key]?.let { Image(it, contentDescription = null, modifier = Modifier.size(42.dp)) }
                                    Spacer(Modifier.width(12.dp))
                                    Text(app.label, modifier = Modifier.weight(1f))
                                }
                                TextButton(onClick = { options = app }) { Text("Opciones de ${app.label}") }
                            }
                        }
                    }
                } else {
                    if (s.home.isEmpty()) Text("Tu universo. Tus reglas.\nAbre Aplicaciones para añadir accesos.", modifier = Modifier.padding(vertical = 16.dp))
                    LazyVerticalGrid(columns = GridCells.Fixed(5), modifier = Modifier.weight(1f)) {
                        items(pinnedApps(s.home, s.apps), key = { it.key }) { app ->
                            Column {
                                TextButton(onClick = { state.open(app) }) {
                                    Column {
                                        s.icons[app.key]?.let { Image(it, contentDescription = "Abrir ${app.label}", modifier = Modifier.size(44.dp)) }
                                        if (s.labels || s.icons[app.key] == null) Text(app.label)
                                    }
                                }
                                TextButton(onClick = { options = app }) { Text("Editar ${app.label}", style = MaterialTheme.typography.labelSmall) }
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        pinnedApps(s.dock, s.apps).forEach { app ->
                            TextButton(onClick = { state.open(app) }, modifier = Modifier.weight(1f)) {
                                s.icons[app.key]?.let { Image(it, contentDescription = "Abrir ${app.label}", modifier = Modifier.size(44.dp)) } ?: Text(app.label)
                            }
                        }
                    }
                }
                Button(onClick = { drawer = !drawer; query = "" }, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) { Text(if (drawer) "Escritorio" else "Aplicaciones") }
            }
        }
        options?.let { app ->
            AlertDialog(onDismissRequest = { options = null }, title = { Text(app.label) }, text = {
                Column {
                    TextButton(enabled = s.ready, onClick = { state.pin(app.key, false); options = null }) { Text(if (app.key in s.home) "Quitar del escritorio" else "Añadir al escritorio") }
                    TextButton(enabled = s.ready, onClick = { state.pin(app.key, true); options = null }) { Text(if (app.key in s.dock) "Quitar del dock" else "Añadir al dock") }
                }
            }, confirmButton = { TextButton(onClick = { options = null }) { Text("Cerrar") } })
        }
        if (settings) AlertDialog(onDismissRequest = { settings = false }, title = { Text("Essential · Automático") }, text = {
            Column {
                Row { Checkbox(checked = draftLabels, onCheckedChange = { draftLabels = it }); Text("Mostrar etiquetas") }
                Text(if (draftLabels) "Vista previa: ◯ Cámara" else "Vista previa: ◯")
                TextButton(onClick = { context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS)) }) { Text("Elegir aplicación de inicio") }
                TextButton(onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }) { Text("Accesibilidad de Android") }
            }
        }, confirmButton = { TextButton(enabled = s.ready, onClick = { state.saveLabels(draftLabels); settings = false }) { Text("Guardar") } }, dismissButton = { TextButton(onClick = { settings = false }) { Text("Descartar") } })
        if (s.ready && s.welcome) AlertDialog(onDismissRequest = {}, title = { Text("Bienvenido a ÓRBITA") }, text = {
            Text("Un inicio local y sin cuentas. Usa Aplicaciones y sus opciones para añadir accesos. En Ajustes puedes elegir ÓRBITA como inicio y abrir Accesibilidad. Versión de desarrollo: faltan funciones de Alfa 0.1.")
        }, confirmButton = { TextButton(onClick = state::finishWelcome) { Text("Comenzar") } })
    }
}
