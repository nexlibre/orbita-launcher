package org.nexlibre.orbita.core

import java.text.Collator
import java.text.Normalizer
import java.util.Locale

data class AppRecord(val key: String, val label: String)

/** Deterministic ties prevent duplicate labels from changing position. */
fun visibleApps(apps: List<AppRecord>, query: String, locale: Locale = Locale.getDefault()): List<AppRecord> {
    fun normalized(value: String) = Normalizer.normalize(value, Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "").lowercase(Locale.ROOT)
    val needle = normalized(query.trim())
    val collator = Collator.getInstance(locale)
    return apps.distinctBy { it.key }.filter { normalized(it.label).contains(needle) }
        .sortedWith { a, b -> collator.compare(a.label, b.label).takeIf { it != 0 } ?: a.key.compareTo(b.key) }
}

fun pinnedApps(keys: List<String>, apps: List<AppRecord>): List<AppRecord> {
    val index = apps.associateBy { it.key }
    return keys.distinct().mapNotNull(index::get)
}
