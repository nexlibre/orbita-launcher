package org.nexlibre.orbita.feature

import android.content.Intent
import android.provider.Settings
import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.pager.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.nexlibre.orbita.core.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Orbita(state: LauncherState, homeRequest: Int) {
    val s = state.snapshot
    var drawer by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var options by remember { mutableStateOf<AppRecord?>(null) }
    var selected by remember { mutableStateOf<String?>(null) }
    var folder by remember { mutableStateOf<String?>(null) }
    var folderAdd by remember { mutableStateOf<String?>(null) }
    var moving by remember { mutableStateOf<String?>(null) }
    var naming by remember { mutableStateOf<String?>(null) }
    var nameDraft by remember { mutableStateOf("") }
    var settings by rememberSaveable { mutableStateOf(false) }
    var draft by remember { mutableStateOf(s.appearance) }
    var resetConfirm by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val pager = rememberPagerState { s.layout.pages }
    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri -> uri?.let(state::export) }
    val keyboard = LocalSoftwareKeyboardController.current
    val systemReduced = remember { Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f }
    val reduced = s.appearance.reducedMotion || systemReduced
    fun closeDrawer() { drawer = false; query = ""; keyboard?.hide() }
    fun haptic() { if (s.appearance.haptics) view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS) }
    fun launcherSettings(action: String) {
        try { context.startActivity(Intent(action)) }
        catch (_: RuntimeException) { context.startActivity(Intent(Settings.ACTION_SETTINGS)) }
    }
    LaunchedEffect(homeRequest) { closeDrawer(); options = null; selected = null; folder = null; settings = false; moving = null; naming = null; folderAdd = null }
    LaunchedEffect(s.layout.pages) { if (pager.currentPage >= s.layout.pages) pager.scrollToPage(s.layout.pages - 1) }
    BackHandler {
        when {
            naming != null -> naming = null
            moving != null -> moving = null
            options != null -> options = null
            selected != null -> selected = null
            folderAdd != null -> folderAdd = null
            folder != null -> folder = null
            settings -> settings = false
            else -> closeDrawer()
        }
    }
    val dark = when(s.appearance.theme) { "light" -> false; "dark" -> true; else -> isSystemInDarkTheme() }
    val palette = if (dark) darkColorScheme(primary = Color(0xFFAFDFA1), background = Color(0xFF101813), surface = Color(0xFF17231D))
        else lightColorScheme(primary = Color(0xFF315E39), background = Color(0xFFF5F8F2), surface = Color(0xFFEDF3E9))
    MaterialTheme(colorScheme = palette) {
        Surface(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 14.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("ÓRBITA", style = MaterialTheme.typography.headlineSmall)
                    Row {
                        if (s.canUndo) IconButton(onClick = { state.undo() }) { Icon(Icons.Default.Refresh, "Deshacer último cambio") }
                        IconButton(onClick = { draft = s.appearance; settings = true }) { Icon(Icons.Default.Settings, "Ajustes") }
                    }
                }
                if (s.recovery) {
                    Text("Los datos requieren recuperación. Se conserva la disposición original; la edición está bloqueada.", color = palette.error)
                    TextButton(onClick = { settings = true }) { Text("Recuperar datos") }
                }
                s.error?.let { message -> Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(message, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                    IconButton(onClick = state::dismissError) { Icon(Icons.Default.Close, "Cerrar aviso") }
                } }
                AnimatedContent(targetState = drawer, modifier = Modifier.weight(1f), label = "drawer", transitionSpec = {
                    fadeIn(tween(if (reduced) 0 else 140)) togetherWith fadeOut(tween(if (reduced) 0 else 100))
                }) { showDrawer -> Column(Modifier.fillMaxSize()) {
                if (showDrawer) {
                    OutlinedTextField(query, { query = it }, label = { Text("Buscar aplicaciones") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    val results by produceState<List<AppRecord>?>(initialValue = null, key1 = s.apps, key2 = query) {
                        value = null
                        value = withContext(Dispatchers.Default) { visibleApps(s.apps, query) }
                    }
                    if (results?.isEmpty() == true) Text(if (query.isBlank()) "No hay aplicaciones disponibles." else "Sin coincidencias.", Modifier.padding(20.dp))
                    LazyColumn(Modifier.weight(1f)) {
                        items(results.orEmpty(), key = { it.key }) { app -> AppRow(app, s, { state.open(app) }, { options = app }) }
                    }
                } else {
                    if (s.layout.items.isEmpty()) Text("Tu universo. Tus reglas.\nAbre Aplicaciones para añadir accesos.", Modifier.padding(vertical = 12.dp), style = MaterialTheme.typography.bodyMedium)
                    HorizontalPager(pager, Modifier.weight(1f), userScrollEnabled = !reduced) { page ->
                        LazyVerticalGrid(GridCells.Fixed(5), Modifier.fillMaxSize()) {
                            items(PAGE_CELLS, key = { "$page:$it" }) { slot ->
                                val item = s.layout.items.firstOrNull { !it.dock && it.page == page && it.slot == slot }
                                if (item == null) Spacer(Modifier.height(88.dp)) else Tile(item, s, {
                                    if (item.folder) folder = item.id else s.apps.find { it.key == item.members.first() }?.let(state::open)
                                }, { haptic(); selected = item.id })
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        IconButton(enabled = pager.currentPage > 0, onClick = { scope.launch { if (reduced) pager.scrollToPage(pager.currentPage - 1) else pager.animateScrollToPage(pager.currentPage - 1) } }) { Icon(Icons.Default.ArrowBack, "Página anterior") }
                        Text("Página ${pager.currentPage + 1} de ${s.layout.pages}", style = MaterialTheme.typography.labelMedium)
                        IconButton(enabled = pager.currentPage < s.layout.pages - 1, onClick = { scope.launch { if (reduced) pager.scrollToPage(pager.currentPage + 1) else pager.animateScrollToPage(pager.currentPage + 1) } }) { Icon(Icons.Default.ArrowForward, "Página siguiente") }
                    }
                    Row(Modifier.fillMaxWidth()) {
                        repeat(5) { slot -> Box(Modifier.weight(1f)) {
                            val item = s.layout.items.firstOrNull { it.dock && it.slot == slot }
                            if (item == null) Spacer(Modifier.height(64.dp)) else Tile(item, s, {
                                if (item.folder) folder = item.id else s.apps.find { it.key == item.members.first() }?.let(state::open)
                            }, { haptic(); selected = item.id }, dock = true)
                        } }
                    }
                }
                } }
                // Only this dedicated area handles vertical gestures. Drawer scrolling and system edges remain free.
                val threshold = with(LocalDensity.current) { 24.dp.toPx() }
                Box(Modifier.fillMaxWidth().heightIn(min = 64.dp).semantics { testTag = "gestureHandle" }
                    .pointerInput(drawer) {
                        var distance = 0f
                        detectVerticalDragGestures(onDragStart = { distance = 0f }, onDragCancel = { distance = 0f },
                            onVerticalDrag = { change, delta -> distance += delta; change.consume() }, onDragEnd = {
                                if (distance < -threshold) drawer = true
                                if (distance > threshold) closeDrawer()
                            })
                    }, contentAlignment = Alignment.Center) {
                    Button(onClick = { if (drawer) closeDrawer() else drawer = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (drawer) "Escritorio" else "Aplicaciones")
                    }
                }
            }
        }
        options?.let { app -> AlertDialog(onDismissRequest = { options = null }, title = { Text(app.label) }, text = {
            Column {
                Action("Añadir al escritorio", s.ready && !s.recovery) { state.edit { it.add(app.key, pager.currentPage) }; options = null }
                Action("Añadir al dock", s.ready && !s.recovery) { state.edit { it.add(app.key, dock = true) }; options = null }
                s.layout.items.filter { it.folder }.forEach { target -> Action("Añadir a ${target.title}", !s.recovery) { state.edit { it.addToFolder(target.id, app.key) }; options = null } }
            }
        }, confirmButton = { Action("Cerrar") { options = null } }) }
        selected?.let { id -> s.layout.items.find { it.id == id }?.let { item ->
            val title = if (item.folder) item.title else s.apps.find { it.key == item.members.first() }?.label ?: "Aplicación no disponible"
            AlertDialog(onDismissRequest = { selected = null }, title = { Text(title) }, text = { Column {
                Action("Mover", !s.recovery) { moving = id; selected = null }
                Action(if (item.folder) "Renombrar carpeta" else "Crear carpeta", !s.recovery) { naming = id; nameDraft = if (item.folder) item.title else "Nueva carpeta"; selected = null }
                Action("Quitar acceso", !s.recovery) { state.edit { it.remove(id) }; selected = null }
                Text("Quitar un acceso no desinstala la aplicación.", style = MaterialTheme.typography.bodySmall)
            } }, confirmButton = { Action("Cerrar") { selected = null } })
        } }
        naming?.let { id -> AlertDialog(onDismissRequest = { naming = null }, title = { Text("Nombre de carpeta") }, text = {
            OutlinedTextField(nameDraft, { nameDraft = it.take(80) }, label = { Text("Nombre") })
        }, confirmButton = { Action("Guardar", nameDraft.isNotBlank()) { state.edit { it.folder(id, nameDraft) }; naming = null } }, dismissButton = { Action("Cancelar") { naming = null } }) }
        moving?.let { id ->
            var targetPage by remember(id) { mutableIntStateOf(pager.currentPage) }
            var targetDock by remember(id) { mutableStateOf(false) }
            AlertDialog(onDismissRequest = { moving = null }, title = { Text("Elige una posición") }, text = { Column {
                Preference("Dock", targetDock) { targetDock = it }
                if (!targetDock) Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(enabled = targetPage > 0, onClick = { targetPage-- }) { Icon(Icons.Default.ArrowBack, "Página anterior para mover") }
                    Text("Página ${targetPage + 1}")
                    IconButton(enabled = targetPage < s.layout.pages - 1, onClick = { targetPage++ }) { Icon(Icons.Default.ArrowForward, "Página siguiente para mover") }
                }
                LazyVerticalGrid(GridCells.Fixed(5), Modifier.heightIn(max = 280.dp)) {
                    items(if (targetDock) 5 else PAGE_CELLS) { slot ->
                        TextButton(onClick = { state.edit { it.move(id, targetPage, slot, targetDock) }; moving = null }) {
                            Text("${slot + 1}", modifier = Modifier.semantics { contentDescription = "Mover a posición ${slot + 1}" })
                        }
                    }
                }
                Text("Si la posición está ocupada, los accesos intercambian lugar.", style = MaterialTheme.typography.bodySmall)
            } }, confirmButton = { Action("Cancelar") { moving = null } })
        }
        folder?.let { id -> s.layout.items.find { it.id == id && it.folder }?.let { item ->
            AlertDialog(onDismissRequest = { folder = null }, title = { Text(item.title) }, text = {
                LazyColumn(Modifier.heightIn(max = 350.dp)) { items(item.members, key = { it }) { key ->
                    val app = s.apps.find { it.key == key } ?: AppRecord(key, "No disponible: ${key.substringBefore('/')}")
                    Column {
                        Action(app.label) { s.apps.find { it.key == key }?.let(state::open) }
                        Row {
                            IconButton(enabled = !s.recovery && key != item.members.first(), onClick = { state.edit { it.reorder(id, key, -1) } }) { Icon(Icons.Default.KeyboardArrowUp, "Subir ${app.label}") }
                            IconButton(enabled = !s.recovery && key != item.members.last(), onClick = { state.edit { it.reorder(id, key, 1) } }) { Icon(Icons.Default.KeyboardArrowDown, "Bajar ${app.label}") }
                            IconButton(enabled = !s.recovery, onClick = { state.edit { it.add(key, pager.currentPage) } }) { Icon(Icons.Default.ArrowForward, "Sacar ${app.label} al escritorio") }
                            IconButton(enabled = !s.recovery, onClick = { state.edit { it.without(key) } }) { Icon(Icons.Default.Close, "Quitar ${app.label} de carpeta") }
                        }
                    }
                } }
            }, confirmButton = { Action("Cerrar") { folder = null } }, dismissButton = { Action("Añadir aplicaciones", !s.recovery) { folderAdd = id; folder = null } })
        } }
        folderAdd?.let { id -> AlertDialog(onDismissRequest = { folderAdd = null }, title = { Text("Añadir a carpeta") }, text = {
            LazyColumn(Modifier.heightIn(max = 350.dp)) { items(s.apps, key = { it.key }) { app ->
                Action(app.label) { state.edit { it.addToFolder(id, app.key) } }
            } }
        }, confirmButton = { Action("Listo") { folderAdd = null; folder = id } }) }
        if (settings) AlertDialog(onDismissRequest = { settings = false }, title = { Text("Essential · Ajustes") }, text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Preference("Mostrar etiquetas", draft.labels) { draft = draft.copy(labels = it) }
                Preference("Reducir movimiento", draft.reducedMotion) { draft = draft.copy(reducedMotion = it) }
                Preference("Vibración sutil", draft.haptics) { draft = draft.copy(haptics = it) }
                Text("Apariencia")
                FlowRow { listOf("auto" to "Auto", "light" to "Claro", "dark" to "Oscuro").forEach { (key, label) ->
                    TextButton(onClick = { draft = draft.copy(theme = key) }, modifier = Modifier.semantics { this.selected = draft.theme == key }) { Text(label) }
                } }
                val previewDark = draft.theme == "dark" || (draft.theme == "auto" && isSystemInDarkTheme())
                Surface(color = if (previewDark) Color(0xFF17231D) else Color(0xFFEDF3E9), contentColor = if (previewDark) Color.White else Color.Black) {
                    Text(if (draft.labels) "Vista previa: ◯ Cámara" else "Vista previa: ◯", Modifier.padding(12.dp))
                }
                Action("Añadir página", s.layout.pages < MAX_PAGES && !s.recovery) { state.edit { it.addPage() } }
                Action("Quitar última página vacía", s.layout.pages > 1 && !s.recovery) { state.edit { it.removeLastPage() } }
                Action("Elegir aplicación de inicio") { launcherSettings(Settings.ACTION_HOME_SETTINGS) }
                Action("Accesibilidad de Android") { launcherSettings(Settings.ACTION_ACCESSIBILITY_SETTINGS) }
                Action("Exportar respaldo") { exporter.launch("ORBITA-respaldo.zip") }
                if (s.recovery) {
                    Action("Recuperar copia válida", s.backup != null) { state.recover(true); settings = false }
                    Action("Restaurar inicio vacío", s.exported) { resetConfirm = true }
                    if (!s.exported) Text("Exporta primero los datos originales para habilitar la restauración.")
                }
                Text("ÓRBITA 0.1.0-alpha.1 · Essential", style = MaterialTheme.typography.bodySmall)
            }
        }, confirmButton = { Action("Guardar", s.ready && !s.recovery) { state.saveAppearance(draft); settings = false } }, dismissButton = { Action("Descartar") { settings = false } })
        if (resetConfirm) AlertDialog(onDismissRequest = { resetConfirm = false }, title = { Text("Restaurar inicio vacío") }, text = { Text("Se quitarán los accesos y carpetas de la disposición activa. El respaldo exportado y los archivos originales se conservan.") }, confirmButton = { Action("Restaurar") { state.recover(false); resetConfirm = false; settings = false } }, dismissButton = { Action("Cancelar") { resetConfirm = false } })
        if (s.ready && s.welcome && !s.recovery) AlertDialog(onDismissRequest = {}, title = { Text("Bienvenido a ÓRBITA") }, text = { Column {
            Text("Sin cuentas y sin red. Añade aplicaciones desde el cajón; mantén un acceso pulsado para moverlo o crear una carpeta. Desliza la barra inferior hacia arriba para abrir el cajón y hacia abajo para cerrarlo. Todos los gestos tienen botones alternativos.")
            Action("Elegir aplicación de inicio") { launcherSettings(Settings.ACTION_HOME_SETTINGS) }
            Action("Accesibilidad de Android") { launcherSettings(Settings.ACTION_ACCESSIBILITY_SETTINGS) }
        } }, confirmButton = { Action("Comenzar") { state.finishWelcome() } })
    }
}

@Composable
private fun Action(label: String, enabled: Boolean = true, action: () -> Unit) { TextButton(onClick = action, enabled = enabled) { Text(label) } }
@Composable
private fun Preference(label: String, checked: Boolean, change: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().toggleable(checked, role = Role.Checkbox, onValueChange = change), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked, onCheckedChange = null); Text(label, Modifier.weight(1f))
    }
}
@Composable
private fun AppRow(app: AppRecord, snapshot: LauncherSnapshot, open: () -> Unit, options: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = open, modifier = Modifier.weight(1f)) {
            snapshot.icons[app.key]?.let { Image(it, null, Modifier.size(42.dp)) }
            Spacer(Modifier.width(12.dp)); Text(app.label, Modifier.weight(1f))
        }
        IconButton(onClick = options) { Icon(Icons.Default.MoreVert, "Opciones de ${app.label}") }
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Tile(item: Placement, snapshot: LauncherSnapshot, open: () -> Unit, options: () -> Unit, dock: Boolean = false) {
    val app = snapshot.apps.find { it.key == item.members.first() }
    val name = if (item.folder) item.title else app?.label ?: "No disponible"
    Column(Modifier.fillMaxWidth().heightIn(min = if (dock) 64.dp else 88.dp)
        .combinedClickable(onClick = open, onLongClick = options).semantics(mergeDescendants = true) {
            contentDescription = if (item.folder) "Abrir carpeta $name" else "Abrir $name"
            role = Role.Button
            customActions = listOf(CustomAccessibilityAction("Opciones de $name") { options(); true })
        }.padding(vertical = 8.dp, horizontal = 2.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        if (item.folder) {
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.secondaryContainer) {
                Icon(Icons.Default.List, null, Modifier.padding(10.dp).size(26.dp))
            }
        } else snapshot.icons[item.members.first()]?.let { Image(it, null, Modifier.size(44.dp)) }
            ?: Icon(Icons.Default.Info, null, Modifier.size(44.dp))
        if (snapshot.appearance.labels) Text(name, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall)
    }
}
