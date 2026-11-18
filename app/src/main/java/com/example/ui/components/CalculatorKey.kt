package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalCalculatorKeyColors
import com.example.ui.theme.SpaceGroteskFontFamily

enum class CalculatorKeyStyle {
    NUMERIC,
    UTILITY,
    OPERATOR,
    SCIENTIFIC,
    SCIENTIFIC_ACTIVE,
    EQUALS,
    CLEAR,
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalculatorKeyButton(
    label: String,
    testTag: String,
    style: CalculatorKeyStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescriptionText: String = label,
    icon: ImageVector? = null,
    onLongClick: (() -> Unit)? = null,
    cornerRadius: Dp = 20.dp,
    fontSize: TextUnit = when (style) {
        CalculatorKeyStyle.SCIENTIFIC,
        CalculatorKeyStyle.SCIENTIFIC_ACTIVE -> 14.sp
        CalculatorKeyStyle.OPERATOR,
        CalculatorKeyStyle.EQUALS -> 26.sp
        CalculatorKeyStyle.UTILITY,
        CalculatorKeyStyle.CLEAR -> 20.sp
        CalculatorKeyStyle.NUMERIC -> 25.sp
    },
) {
    val keyColors = LocalCalculatorKeyColors.current
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 700f),
        label = "key_scale",
    )

    val containerColor: Color
    val contentColor: Color
    val borderStroke: BorderStroke?

    when (style) {
        CalculatorKeyStyle.NUMERIC -> {
            containerColor = keyColors.numericKey
            contentColor = keyColors.numericKeyText
            borderStroke = BorderStroke(1.dp, keyColors.borderSubtle.copy(alpha = 0.45f))
        }
        CalculatorKeyStyle.UTILITY -> {
            containerColor = keyColors.utilityKey
            contentColor = keyColors.utilityKeyText
            borderStroke = null
        }
        CalculatorKeyStyle.OPERATOR -> {
            containerColor = keyColors.operatorKey
            contentColor = keyColors.operatorKeyText
            borderStroke = null
        }
        CalculatorKeyStyle.SCIENTIFIC -> {
            containerColor = keyColors.scientificKey
            contentColor = keyColors.scientificKeyText
            borderStroke = BorderStroke(1.dp, keyColors.borderSubtle.copy(alpha = 0.35f))
        }
        CalculatorKeyStyle.SCIENTIFIC_ACTIVE -> {
            containerColor = MaterialTheme.colorScheme.primaryContainer
            contentColor = MaterialTheme.colorScheme.primary
            borderStroke = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
        }
        CalculatorKeyStyle.EQUALS -> {
            containerColor = keyColors.equalsKey
            contentColor = keyColors.equalsKeyText
            borderStroke = null
        }
        CalculatorKeyStyle.CLEAR -> {
            containerColor = keyColors.clearKey
            contentColor = keyColors.clearKeyText
            borderStroke = null
        }
    }

    val shape = RoundedCornerShape(cornerRadius)
    val fontFamily = when (style) {
        CalculatorKeyStyle.SCIENTIFIC,
        CalculatorKeyStyle.SCIENTIFIC_ACTIVE -> JetBrainsMonoFontFamily
        else -> SpaceGroteskFontFamily
    }

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(containerColor, shape)
            .then(
                if (borderStroke != null) {
                    Modifier.border(borderStroke, shape)
                } else {
                    Modifier
                }
            )
            .combinedClickable(
                interactionSource = interactionSource,
                indication = ripple(color = contentColor.copy(alpha = 0.25f)),
                role = Role.Button,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                },
                onLongClick = if (onLongClick != null) {
                    {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongClick()
                    }
                } else {
                    null
                },
            )
            .semantics {
                contentDescription = contentDescriptionText
            }
            .testTag(testTag)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.fillMaxSize(0.42f),
            )
        } else {
            Text(
                text = label,
                color = contentColor,
                fontFamily = fontFamily,
                fontWeight = when (style) {
                    CalculatorKeyStyle.OPERATOR,
                    CalculatorKeyStyle.EQUALS,
                    CalculatorKeyStyle.CLEAR -> FontWeight.SemiBold
                    else -> FontWeight.Medium
                },
                fontSize = fontSize,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip,
            )
        }
    }
}
