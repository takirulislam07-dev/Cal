package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class CalculatorKeyColors(
    val backgroundGlow: Color,
    val displaySurface: Color,
    val numericKey: Color,
    val numericKeyText: Color,
    val utilityKey: Color,
    val utilityKeyText: Color,
    val operatorKey: Color,
    val operatorKeyText: Color,
    val scientificKey: Color,
    val scientificKeyText: Color,
    val equalsKey: Color,
    val equalsKeyText: Color,
    val clearKey: Color,
    val clearKeyText: Color,
    val borderSubtle: Color,
    val previewText: Color,
)

private val LightKeyColors = CalculatorKeyColors(
    backgroundGlow = LightBackgroundAccent,
    displaySurface = LightSurface,
    numericKey = LightKeyNumeric,
    numericKeyText = LightTextPrimary,
    utilityKey = LightKeyUtility,
    utilityKeyText = LightTextPrimary,
    operatorKey = SignalOrangeContainerLight,
    operatorKeyText = SignalOrange,
    scientificKey = LightKeyScientific,
    scientificKeyText = LightTextSecondary,
    equalsKey = SignalOrange,
    equalsKeyText = CrispWhite,
    clearKey = SignalOrangeContainerLight,
    clearKeyText = SignalOrange,
    borderSubtle = LightDivider,
    previewText = LightTextSecondary,
)

private val DarkKeyColors = CalculatorKeyColors(
    backgroundGlow = DarkBackgroundAccent,
    displaySurface = DarkSurface,
    numericKey = DarkKeyNumeric,
    numericKeyText = DarkTextPrimary,
    utilityKey = DarkKeyUtility,
    utilityKeyText = DarkTextPrimary,
    operatorKey = SignalOrangeContainerDark,
    operatorKeyText = SignalOrangeDark,
    scientificKey = DarkKeyScientific,
    scientificKeyText = DarkTextSecondary,
    equalsKey = SignalOrangeDark,
    equalsKeyText = CrispWhite,
    clearKey = SignalOrangeContainerDark,
    clearKeyText = SignalOrangeDark,
    borderSubtle = DarkDivider,
    previewText = DarkTextSecondary,
)

val LocalCalculatorKeyColors = staticCompositionLocalOf { LightKeyColors }

private val LightColorScheme = lightColorScheme(
    primary = SignalOrange,
    onPrimary = CrispWhite,
    primaryContainer = SignalOrangeContainerLight,
    onPrimaryContainer = SignalOrange,
    secondary = DeepCobalt,
    onSecondary = CrispWhite,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = LightTextSecondary,
    outline = LightDivider,
    outlineVariant = LightDivider,
    error = ErrorCoralLight,
    onError = CrispWhite,
)

private val DarkColorScheme = darkColorScheme(
    primary = SignalOrangeDark,
    onPrimary = CrispWhite,
    primaryContainer = SignalOrangeContainerDark,
    onPrimaryContainer = SignalOrangeDark,
    secondary = SoftCobaltDark,
    onSecondary = DarkBackground,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkDivider,
    outlineVariant = DarkDivider,
    error = ErrorCoralDark,
    onError = DarkBackground,
)

val CalculatorShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
)

@Composable
fun CalculatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val keyColors = if (darkTheme) DarkKeyColors else LightKeyColors

    CompositionLocalProvider(LocalCalculatorKeyColors provides keyColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = CalculatorTypography,
            shapes = CalculatorShapes,
            content = content,
        )
    }
}
