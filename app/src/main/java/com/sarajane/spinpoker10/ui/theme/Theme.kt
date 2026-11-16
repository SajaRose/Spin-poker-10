package com.sarajane.spinpoker10.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CasinoColors = darkColorScheme(
    primary = Gold,
    onPrimary = CasinoBackground,
    secondary = GoldDark,
    background = CasinoBackground,
    surface = CasinoSurface,
    onSurface = TextLight
)

/** Thème casino sombre unique : le jeu est toujours rendu en ambiance de nuit. */
@Composable
fun SpinPokerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CasinoColors,
        content = content
    )
}
