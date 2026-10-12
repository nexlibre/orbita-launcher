package org.nexlibre.orbita.core

import java.io.*
import java.util.Base64
import java.util.UUID

const val PAGE_CELLS = 25
const val MAX_PAGES = 40

data class Placement(
    val id: String = UUID.randomUUID().toString(),
    val members: List<String>,
    val title: String = "",
    val folder: Boolean = false,
    val page: Int = 0,
    val slot: Int = 0,
    val dock: Boolean = false
)

data class Layout(val pages: Int = 1, val items: List<Placement> = emptyList()) {
    fun checked(): Layout {
        require(pages in 1..MAX_PAGES && items.size <= MAX_PAGES * PAGE_CELLS + 5)
        require(items.map { it.id }.distinct().size == items.size)
        require(items.map { Triple(it.dock, if (it.dock) 0 else it.page, it.slot) }.distinct().size == items.size)
        val members = items.flatMap { it.members }
        require(members.size <= 2000 && members.distinct().size == members.size)
        items.forEach {
            require(it.id.isNotBlank() && it.id.length <= 100)
            require(it.members.isNotEmpty() && it.members.size <= 1000)
            require(it.members.all { key -> key.length <= 500 && key.matches(Regex("[\\w.]+/[\\w.$]+")) })
            require(it.title.length <= 80 && (!it.folder || it.title.isNotBlank()))
            require(it.folder || it.members.size == 1)
            require(it.page in 0 until pages && it.slot in 0 until if (it.dock) 5 else PAGE_CELLS)
        }
        return this
    }
    fun remove(id: String) = copy(items = items.filterNot { it.id == id }).checked()
    fun without(key: String): Layout = copy(items = items.mapNotNull { item ->
        val members = item.members.filterNot { it == key }
        if (members.isEmpty()) null else item.copy(members = members)
    }).checked()
    fun add(key: String, preferredPage: Int = 0, dock: Boolean = false): Layout {
        val base = without(key)
        val page = preferredPage.coerceIn(0, base.pages - 1)
        if (dock) {
            val free = (0..4).firstOrNull { slot -> base.items.none { it.dock && it.slot == slot } }
                ?: error("El dock está lleno. Mueve o quita un favorito primero.")
            return base.copy(items = base.items + Placement(members = listOf(key), slot = free, dock = true)).checked()
        }
        val order = listOf(page) + (0 until base.pages).filterNot { it == page }
        for (p in order) {
            val free = (0 until PAGE_CELLS).firstOrNull { slot -> base.items.none { !it.dock && it.page == p && it.slot == slot } }
            if (free != null) return base.copy(items = base.items + Placement(members = listOf(key), page = p, slot = free)).checked()
        }
        require(base.pages < MAX_PAGES) { "No hay espacio: máximo 40 páginas." }
        return base.copy(pages = base.pages + 1, items = base.items + Placement(members = listOf(key), page = base.pages)).checked()
    }
    fun move(id: String, page: Int, slot: Int, dock: Boolean = false): Layout {
        val source = items.first { it.id == id }
        val destination = items.firstOrNull { it.dock == dock && it.slot == slot && (dock || it.page == page) }
        return copy(items = items.map {
            when (it.id) {
                source.id -> it.copy(page = if (dock) 0 else page, slot = slot, dock = dock)
                destination?.id -> it.copy(page = source.page, slot = source.slot, dock = source.dock)
                else -> it
            }
        }).checked()
    }
    fun addToFolder(folderId: String, key: String): Layout {
        val target = items.first { it.id == folderId && it.folder }
        if (key in target.members) return this
        val base = without(key)
        return base.copy(items = base.items.map { if (it.id == folderId) it.copy(members = it.members + key) else it }).checked()
    }
    fun folder(id: String, name: String): Layout = copy(items = items.map {
        if (it.id == id) it.copy(folder = true, title = name.trim()) else it
    }).checked()
    fun reorder(folderId: String, key: String, offset: Int): Layout = copy(items = items.map {
        if (it.id != folderId) it else {
            val list = it.members.toMutableList()
            val a = list.indexOf(key)
            require(a >= 0)
            val b = (a + offset).coerceIn(0, list.lastIndex)
            val temp = list[a]; list[a] = list[b]; list[b] = temp
            it.copy(members = list)
        }
    }).checked()
    fun addPage() = copy(pages = pages + 1).checked()
    fun removeLastPage(): Layout {
        require(pages > 1 && items.none { !it.dock && it.page == pages - 1 }) { "Solo puedes quitar la última página si está vacía." }
        return copy(pages = pages - 1).checked()
    }
}

/** Bounded/versioned local codec. Unknown or corrupt input never becomes an empty layout silently. */
object LayoutCodec {
    fun encode(layout: Layout): String {
        layout.checked()
        val bytes = ByteArrayOutputStream()
        DataOutputStream(bytes).use { out ->
            out.writeInt(2); out.writeInt(layout.pages); out.writeInt(layout.items.size)
            layout.items.forEach {
                out.writeUTF(it.id); out.writeUTF(it.title); out.writeBoolean(it.folder)
                out.writeInt(it.page); out.writeInt(it.slot); out.writeBoolean(it.dock); out.writeInt(it.members.size)
                it.members.forEach(out::writeUTF)
            }
        }
        return Base64.getEncoder().encodeToString(bytes.toByteArray())
    }
    fun decode(value: String): Layout {
        require(value.length <= 2_000_000)
        DataInputStream(ByteArrayInputStream(Base64.getDecoder().decode(value))).use { input ->
            require(input.readInt() == 2) { "Versión de datos incompatible." }
            val pages = input.readInt()
            val count = input.readInt(); require(count in 0..(MAX_PAGES * PAGE_CELLS + 5))
            val items = List(count) {
                val id = input.readUTF(); val title = input.readUTF(); val folder = input.readBoolean()
                val page = input.readInt(); val slot = input.readInt(); val dock = input.readBoolean()
                val size = input.readInt(); require(size in 1..1000)
                Placement(id, List(size) { input.readUTF() }, title, folder, page, slot, dock)
            }
            require(input.available() == 0)
            return Layout(pages, items).checked()
        }
    }
    fun migrate(home: List<String>, dock: List<String>): Layout {
        var result = Layout()
        home.distinct().forEach { result = result.add(it) }
        dock.distinct().forEach { result = result.add(it, dock = true) }
        return result.checked()
    }
}
