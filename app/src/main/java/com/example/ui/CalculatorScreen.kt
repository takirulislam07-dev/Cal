package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.calculator.CalculatorViewModel
import com.example.ui.components.CalculatorDisplay
import com.example.ui.components.HistoryPanel
import com.example.ui.components.ScientificKeypadGrid
import com.example.ui.components.StandardKeypadGrid
import com.example.ui.theme.LocalCalculatorKeyColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val keyColors = LocalCalculatorKeyColors.current
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showHistorySheet by remember { mutableStateOf(false) }

    val copyResult: () -> Unit = {
        val textToCopy = uiState.rawExpression.ifEmpty { uiState.formattedExpression }
        clipboardManager.setText(AnnotatedString(textToCopy))
        coroutineScope.launch {
            snackbarHostState.showSnackbar(
                context.getString(R.string.copied_to_clipboard, uiState.formattedExpression)
            )
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            val isExpandedWidth = maxWidth >= 720.dp
            val isCompactHeight = maxHeight < 500.dp

            if (isExpandedWidth && !isCompactHeight) {
                // Tablet / Foldable Unfolded Two-Pane Canonical Layout
                Row(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    HistoryPanel(
                        history = uiState.history,
                        onSelectResult = viewModel::onSelectHistoryResult,
                        onRecallExpression = viewModel::onRecallHistoryExpression,
                        onDeleteItem = viewModel::onDeleteHistoryItem,
                        onClearAll = viewModel::onClearHistory,
                        modifier = Modifier
                            .weight(0.35f)
                            .fillMaxHeight()
                            .background(keyColors.displaySurface),
                    )

                    VerticalDivider(
                        color = keyColors.borderSubtle.copy(alpha = 0.5f),
                        thickness = 1.dp,
                    )

                    Column(
                        modifier = Modifier
                            .weight(0.65f)
                            .fillMaxHeight()
                            .padding(bottom = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CalculatorDisplay(
                            uiState = uiState,
                            isDarkTheme = isDarkTheme,
                            showHistoryButton = false,
                            showScientificToggle = false,
                            compactVertical = false,
                            onToggleAngleMode = viewModel::onToggleAngleMode,
                            onToggleScientific = viewModel::onToggleScientific,
                            onOpenHistory = {},
                            onToggleTheme = { viewModel.onToggleTheme(isDarkTheme) },
                            onCopyResult = copyResult,
                            modifier = Modifier.weight(0.34f),
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Column(
                            modifier = Modifier
                                .weight(0.66f)
                                .widthIn(max = 620.dp)
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            ScientificKeypadGrid(
                                isInverseMode = uiState.isInverseMode,
                                viewModel = viewModel,
                                keySpacing = 8.dp,
                                cornerRadius = 14.dp,
                                modifier = Modifier.weight(0.36f),
                            )
                            StandardKeypadGrid(
                                viewModel = viewModel,
                                keySpacing = 10.dp,
                                cornerRadius = 20.dp,
                                modifier = Modifier.weight(0.64f),
                            )
                        }
                    }
                }
            } else if (isCompactHeight) {
                // Phone Landscape Layout: Slim Display + Side-by-Side Scientific & Standard Keypads
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 8.dp),
                ) {
                    CalculatorDisplay(
                        uiState = uiState,
                        isDarkTheme = isDarkTheme,
                        showHistoryButton = true,
                        showScientificToggle = false,
                        compactVertical = true,
                        onToggleAngleMode = viewModel::onToggleAngleMode,
                        onToggleScientific = viewModel::onToggleScientific,
                        onOpenHistory = { showHistorySheet = true },
                        onToggleTheme = { viewModel.onToggleTheme(isDarkTheme) },
                        onCopyResult = copyResult,
                        modifier = Modifier.weight(0.32f),
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .weight(0.68f)
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        ScientificKeypadGrid(
                            isInverseMode = uiState.isInverseMode,
                            viewModel = viewModel,
                            keySpacing = 6.dp,
                            cornerRadius = 12.dp,
                            modifier = Modifier
                                .weight(0.45f)
                                .fillMaxHeight(),
                        )
                        StandardKeypadGrid(
                            viewModel = viewModel,
                            keySpacing = 6.dp,
                            cornerRadius = 14.dp,
                            compactText = true,
                            modifier = Modifier
                                .weight(0.55f)
                                .fillMaxHeight(),
                        )
                    }
                }
            } else {
                // Standard Portrait Layout
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CalculatorDisplay(
                        uiState = uiState,
                        isDarkTheme = isDarkTheme,
                        showHistoryButton = true,
                        showScientificToggle = true,
                        compactVertical = false,
                        onToggleAngleMode = viewModel::onToggleAngleMode,
                        onToggleScientific = viewModel::onToggleScientific,
                        onOpenHistory = { showHistorySheet = true },
                        onToggleTheme = { viewModel.onToggleTheme(isDarkTheme) },
                        onCopyResult = copyResult,
                        modifier = Modifier.weight(if (uiState.isScientificExpanded) 0.28f else 0.36f),
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier
                            .weight(if (uiState.isScientificExpanded) 0.72f else 0.64f)
                            .widthIn(max = 560.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        AnimatedVisibility(
                            visible = uiState.isScientificExpanded,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically(),
                            modifier = Modifier.weight(0.35f, fill = false),
                        ) {
                            ScientificKeypadGrid(
                                isInverseMode = uiState.isInverseMode,
                                viewModel = viewModel,
                                keySpacing = 7.dp,
                                cornerRadius = 14.dp,
                                modifier = Modifier.height(176.dp),
                            )
                        }

                        StandardKeypadGrid(
                            viewModel = viewModel,
                            keySpacing = 10.dp,
                            cornerRadius = 22.dp,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }

    if (showHistorySheet) {
        BackHandler {
            showHistorySheet = false
        }
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showHistorySheet = false },
            sheetState = sheetState,
            containerColor = keyColors.displaySurface,
        ) {
            HistoryPanel(
                history = uiState.history,
                onSelectResult = { entry ->
                    viewModel.onSelectHistoryResult(entry)
                    showHistorySheet = false
                },
                onRecallExpression = { entry ->
                    viewModel.onRecallHistoryExpression(entry)
                    showHistorySheet = false
                },
                onDeleteItem = viewModel::onDeleteHistoryItem,
                onClearAll = viewModel::onClearHistory,
                onClose = { showHistorySheet = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(460.dp),
            )
        }
    }
}
