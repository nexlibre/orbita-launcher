package org.nexlibre.orbita.core

val THEMES = listOf("auto", "light", "dark", "amoled", "pixel", "liquid")
val MOTIONS = listOf("soft", "balanced", "weighty")

data class Appearance(
    val labels: Boolean = true, val reducedMotion: Boolean = false,
    val haptics: Boolean = true, val theme: String = "auto",
    val iconSize: Int = 44, val monochrome: Boolean = false,
    val clock: Boolean = false, val clockSize: Int = 48,
    val clockDate: Boolean = true, val searchBar: Boolean = false,
    val motion: String = "balanced", val refreshHz: Int = 0
) {
    fun checked(): Appearance {
        require(theme in THEMES && motion in MOTIONS)
        require(iconSize in 32..64 && clockSize in 32..72 && refreshHz in 0..144)
        return this
    }
}

/** A window preference, never a promise to override panel/OS/battery limits. */
fun supportedRefreshRates(rates: List<Float>): List<Int> = rates
    .filter { it.isFinite() && it >= 1f && it <= 144.5f }
    .map { kotlin.math.round(it).toInt() }.filter { it in 1..144 }.distinct().sorted()

fun refreshPreference(requested: Int, rates: List<Float>): Float =
    if (requested == 0) 0f else rates.firstOrNull {
        it.isFinite() && it <= 144.5f && kotlin.math.abs(it - requested) < 0.6f
    } ?: 0f
