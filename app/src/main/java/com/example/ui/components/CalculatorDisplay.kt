package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.rounded.Functions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.calculator.AngleMode
import com.example.calculator.CalculatorErrorType
import com.example.calculator.CalculatorUiState
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalCalculatorKeyColors
import com.example.ui.theme.SpaceGroteskFontFamily

@Composable
fun CalculatorDisplay(
    uiState: CalculatorUiState,
    isDarkTheme: Boolean,
    showHistoryButton: Boolean,
    showScientificToggle: Boolean,
    compactVertical: Boolean,
    onToggleAngleMode: () -> Unit,
    onToggleScientific: () -> Unit,
    onOpenHistory: () -> Unit,
    onToggleTheme: () -> Unit,
    onCopyResult: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyColors = LocalCalculatorKeyColors.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val errorColor = MaterialTheme.colorScheme.error

    val exprScrollState = rememberScrollState()
    LaunchedEffect(uiState.formattedExpression) {
        exprScrollState.animateScrollTo(exprScrollState.maxValue)
    }

    val expressionFontSize = remember(uiState.formattedExpression, compactVertical) {
        val len = uiState.formattedExpression.length
        when {
            compactVertical -> when {
                len <= 10 -> 38.sp
                len <= 16 -> 30.sp
                else -> 24.sp
            }
            len <= 9 -> 56.sp
            len <= 14 -> 46.sp
            len <= 20 -> 38.sp
            else -> 30.sp
        }
    }

    val highlightedExpression = remember(
        uiState.formattedExpression,
        primaryColor,
        onSurface,
        onSurfaceVariant,
    ) {
        buildHighlightedExpression(
            expression = uiState.formattedExpression,
            operatorColor = primaryColor,
            parenColor = onSurfaceVariant,
            textColor = onSurface,
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(keyColors.displaySurface)
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.06f),
                            Color.Transparent,
                        ),
                        center = Offset(size.width * 0.88f, size.height * 0.25f),
                        radius = size.maxDimension * 0.75f,
                    )
                )
            }
            .padding(horizontal = 20.dp, vertical = if (compactVertical) 8.dp else 14.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Top Utility Strip
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(
                    onClick = onToggleAngleMode,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f),
                    modifier = Modifier.testTag("angle_mode_button"),
                ) {
                    Text(
                        text = if (uiState.angleMode == AngleMode.DEG) {
                            stringResource(R.string.mode_deg)
                        } else {
                            stringResource(R.string.mode_rad)
                        },
                        color = primaryColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }

                if (showScientificToggle) {
                    val active = uiState.isScientificExpanded
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (active) primaryColor else keyColors.utilityKey.copy(alpha = 0.7f)
                            )
                            .clickable(onClick = onToggleScientific)
                            .testTag("scientific_toggle_button")
                            .padding(horizontal = 11.dp, vertical = 6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Functions,
                            contentDescription = stringResource(R.string.toggle_scientific),
                            tint = if (active) MaterialTheme.colorScheme.onPrimary else onSurfaceVariant,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = stringResource(R.string.sci_label),
                            color = if (active) MaterialTheme.colorScheme.onPrimary else onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                if (showHistoryButton) {
                    Box {
                        IconButton(
                            onClick = onOpenHistory,
                            modifier = Modifier.testTag("history_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.History,
                                contentDescription = stringResource(R.string.open_history),
                                tint = onSurfaceVariant,
                            )
                        }
                        if (uiState.history.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 10.dp, end = 10.dp)
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(primaryColor)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier.testTag("theme_toggle_button"),
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                        contentDescription = stringResource(R.string.toggle_theme),
                        tint = onSurfaceVariant,
                    )
                }
            }
        }

        // Expression & Live Result Area
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(exprScrollState),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Text(
                    text = highlightedExpression,
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = expressionFontSize,
                    lineHeight = expressionFontSize * 1.15f,
                    letterSpacing = (-1).sp,
                    maxLines = 1,
                    softWrap = false,
                    textAlign = TextAlign.End,
                    modifier = Modifier.testTag("display_expression"),
                )
            }

            Spacer(modifier = Modifier.height(if (compactVertical) 4.dp else 10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                AnimatedContent(
                    targetState = Triple(uiState.error, uiState.livePreview, uiState.justEvaluated),
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "display_secondary",
                ) { (error, preview, justEvaluated) ->
                    when {
                        error != null -> {
                            val errorMsg = when (error) {
                                CalculatorErrorType.DIVIDE_BY_ZERO -> stringResource(R.string.error_divide_by_zero)
                                CalculatorErrorType.DOMAIN -> stringResource(R.string.error_domain)
                                CalculatorErrorType.OVERFLOW -> stringResource(R.string.error_overflow)
                                CalculatorErrorType.SYNTAX -> stringResource(R.string.error_syntax)
                            }
                            Text(
                                text = errorMsg,
                                color = errorColor,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.testTag("display_error"),
                            )
                        }
                        preview.isNotEmpty() -> {
                            Text(
                                text = "= $preview",
                                color = keyColors.previewText,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = if (compactVertical) 18.sp else 22.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.testTag("display_preview"),
                            )
                        }
                        justEvaluated && uiState.rawExpression.isNotEmpty() -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        1.dp,
                                        keyColors.borderSubtle.copy(alpha = 0.6f),
                                        RoundedCornerShape(10.dp),
                                    )
                                    .clickable(onClick = onCopyResult)
                                    .testTag("copy_result_button")
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ContentCopy,
                                    contentDescription = stringResource(R.string.copy_result),
                                    tint = onSurfaceVariant,
                                    modifier = Modifier.size(14.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.copy_result),
                                    color = onSurfaceVariant,
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            }
                        }
                        else -> {
                            Spacer(modifier = Modifier.height(1.dp))
                        }
                    }
                }
            }

            if (!compactVertical) {
                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(
                    color = keyColors.borderSubtle.copy(alpha = 0.45f),
                    thickness = 1.dp,
                )
            }
        }
    }
}

private fun buildHighlightedExpression(
    expression: String,
    operatorColor: Color,
    parenColor: Color,
    textColor: Color,
): AnnotatedString {
    val operators = setOf('+', '−', '×', '÷', '^', '%', '!')
    val parens = setOf('(', ')')
    return buildAnnotatedString {
        append(expression)
        expression.forEachIndexed { index, ch ->
            when {
                ch in operators -> addStyle(
                    style = SpanStyle(color = operatorColor),
                    start = index,
                    end = index + 1,
                )
                ch in parens -> addStyle(
                    style = SpanStyle(color = parenColor),
                    start = index,
                    end = index + 1,
                )
                else -> addStyle(
                    style = SpanStyle(color = textColor),
                    start = index,
                    end = index + 1,
                )
            }
        }
    }
}
