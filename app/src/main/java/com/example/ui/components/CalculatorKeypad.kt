package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.calculator.CalculatorViewModel

@Composable
fun ScientificKeypadGrid(
    isInverseMode: Boolean,
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier,
    keySpacing: Dp = 8.dp,
    cornerRadius: Dp = 14.dp,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(keySpacing),
    ) {
        // Row 1: 2nd, (, ), π, e
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(keySpacing),
        ) {
            CalculatorKeyButton(
                label = stringResource(R.string.inv_label),
                testTag = "key_inv",
                style = if (isInverseMode) CalculatorKeyStyle.SCIENTIFIC_ACTIVE else CalculatorKeyStyle.SCIENTIFIC,
                onClick = viewModel::onToggleInverse,
                cornerRadius = cornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            CalculatorKeyButton(
                label = "(",
                testTag = "key_open_paren",
                style = CalculatorKeyStyle.SCIENTIFIC,
                onClick = viewModel::onOpenParen,
                cornerRadius = cornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            CalculatorKeyButton(
                label = ")",
                testTag = "key_close_paren",
                style = CalculatorKeyStyle.SCIENTIFIC,
                onClick = viewModel::onCloseParen,
                cornerRadius = cornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            CalculatorKeyButton(
                label = "π",
                testTag = "key_pi",
                style = CalculatorKeyStyle.SCIENTIFIC,
                onClick = { viewModel.onConstant("π") },
                cornerRadius = cornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            CalculatorKeyButton(
                label = "e",
                testTag = "key_e",
                style = CalculatorKeyStyle.SCIENTIFIC,
                onClick = { viewModel.onConstant("e") },
                cornerRadius = cornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }

        // Row 2: sin/sin⁻¹, cos/cos⁻¹, tan/tan⁻¹, ln/eˣ, log/10ˣ
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(keySpacing),
        ) {
            CalculatorKeyButton(
                label = if (isInverseMode) "sin⁻¹" else "sin",
                testTag = "key_sin",
                style = CalculatorKeyStyle.SCIENTIFIC,
                onClick = { viewModel.onFunction(if (isInverseMode) "sin⁻¹" else "sin") },
                cornerRadius = cornerRadius,
                fontSize = if (isInverseMode) 12.sp else 14.sp,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            CalculatorKeyButton(
                label = if (isInverseMode) "cos⁻¹" else "cos",
                testTag = "key_cos",
                style = CalculatorKeyStyle.SCIENTIFIC,
                onClick = { viewModel.onFunction(if (isInverseMode) "cos⁻¹" else "cos") },
                cornerRadius = cornerRadius,
                fontSize = if (isInverseMode) 12.sp else 14.sp,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            CalculatorKeyButton(
                label = if (isInverseMode) "tan⁻¹" else "tan",
                testTag = "key_tan",
                style = CalculatorKeyStyle.SCIENTIFIC,
                onClick = { viewModel.onFunction(if (isInverseMode) "tan⁻¹" else "tan") },
                cornerRadius = cornerRadius,
                fontSize = if (isInverseMode) 12.sp else 14.sp,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            CalculatorKeyButton(
                label = if (isInverseMode) "eˣ" else "ln",
                testTag = "key_ln",
                style = CalculatorKeyStyle.SCIENTIFIC,
                onClick = {
                    if (isInverseMode) viewModel.onPowerBase("e^(") else viewModel.onFunction("ln")
                },
                cornerRadius = cornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            CalculatorKeyButton(
                label = if (isInverseMode) "10ˣ" else "log",
                testTag = "key_log",
                style = CalculatorKeyStyle.SCIENTIFIC,
                onClick = {
                    if (isInverseMode) viewModel.onPowerBase("10^(") else viewModel.onFunction("log")
                },
                cornerRadius = cornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }

        // Row 3: √ / x², xʸ / 1/x, x! / |x|, ±, x² / 1/x
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(keySpacing),
        ) {
            CalculatorKeyButton(
                label = if (isInverseMode) "x²" else "√",
                testTag = "key_sqrt",
                style = CalculatorKeyStyle.SCIENTIFIC,
                onClick = {
                    if (isInverseMode) viewModel.onSquare() else viewModel.onFunction("√")
                },
                cornerRadius = cornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            CalculatorKeyButton(
                label = if (isInverseMode) "1/x" else "xʸ",
                testTag = "key_power",
                style = CalculatorKeyStyle.SCIENTIFIC,
                onClick = {
                    if (isInverseMode) viewModel.onReciprocal() else viewModel.onOperator("^")
                },
                cornerRadius = cornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            CalculatorKeyButton(
                label = if (isInverseMode) "|x|" else "x!",
                testTag = "key_factorial",
                style = CalculatorKeyStyle.SCIENTIFIC,
                onClick = {
                    if (isInverseMode) viewModel.onFunction("abs") else viewModel.onFactorial()
                },
                cornerRadius = cornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            CalculatorKeyButton(
                label = "±",
                testTag = "key_sign_toggle",
                contentDescriptionText = stringResource(R.string.cd_sign_toggle),
                style = CalculatorKeyStyle.SCIENTIFIC,
                onClick = viewModel::onSignToggle,
                cornerRadius = cornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            CalculatorKeyButton(
                label = "x²",
                testTag = "key_square_quick",
                style = CalculatorKeyStyle.SCIENTIFIC,
                onClick = viewModel::onSquare,
                cornerRadius = cornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }
    }
}

@Composable
fun StandardKeypadGrid(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier,
    keySpacing: Dp = 10.dp,
    cornerRadius: Dp = 20.dp,
    compactText: Boolean = false,
) {
    val numSize = if (compactText) 20.sp else 26.sp
    val opSize = if (compactText) 22.sp else 28.sp
    val utilSize = if (compactText) 17.sp else 20.sp

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(keySpacing),
    ) {
        // Row 1: AC, (), %, ÷
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(keySpacing),
        ) {
            CalculatorKeyButton(
                label = "AC",
                testTag = "key_ac",
                contentDescriptionText = stringResource(R.string.cd_all_clear),
                style = CalculatorKeyStyle.CLEAR,
                onClick = viewModel::onAllClear,
                cornerRadius = cornerRadius,
                fontSize = utilSize,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            CalculatorKeyButton(
                label = "( )",
                testTag = "key_parens",
                contentDescriptionText = stringResource(R.string.cd_parentheses),
                style = CalculatorKeyStyle.UTILITY,
                onClick = viewModel::onParenthesisSmart,
                cornerRadius = cornerRadius,
                fontSize = utilSize,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            CalculatorKeyButton(
                label = "%",
                testTag = "key_percent",
                contentDescriptionText = stringResource(R.string.cd_percent),
                style = CalculatorKeyStyle.UTILITY,
                onClick = viewModel::onPercent,
                cornerRadius = cornerRadius,
                fontSize = utilSize,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            CalculatorKeyButton(
                label = "÷",
                testTag = "key_divide",
                contentDescriptionText = stringResource(R.string.cd_divide),
                style = CalculatorKeyStyle.OPERATOR,
                onClick = { viewModel.onOperator("÷") },
                cornerRadius = cornerRadius,
                fontSize = opSize,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }

        // Row 2: 7, 8, 9, ×
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(keySpacing),
        ) {
            listOf("7", "8", "9").forEach { digit ->
                CalculatorKeyButton(
                    label = digit,
                    testTag = "key_$digit",
                    style = CalculatorKeyStyle.NUMERIC,
                    onClick = { viewModel.onDigit(digit) },
                    cornerRadius = cornerRadius,
                    fontSize = numSize,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
            CalculatorKeyButton(
                label = "×",
                testTag = "key_multiply",
                contentDescriptionText = stringResource(R.string.cd_multiply),
                style = CalculatorKeyStyle.OPERATOR,
                onClick = { viewModel.onOperator("×") },
                cornerRadius = cornerRadius,
                fontSize = opSize,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }

        // Row 3: 4, 5, 6, −
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(keySpacing),
        ) {
            listOf("4", "5", "6").forEach { digit ->
                CalculatorKeyButton(
                    label = digit,
                    testTag = "key_$digit",
                    style = CalculatorKeyStyle.NUMERIC,
                    onClick = { viewModel.onDigit(digit) },
                    cornerRadius = cornerRadius,
                    fontSize = numSize,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
            CalculatorKeyButton(
                label = "−",
                testTag = "key_subtract",
                contentDescriptionText = stringResource(R.string.cd_subtract),
                style = CalculatorKeyStyle.OPERATOR,
                onClick = { viewModel.onOperator("−") },
                cornerRadius = cornerRadius,
                fontSize = opSize,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }

        // Row 4: 1, 2, 3, +
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(keySpacing),
        ) {
            listOf("1", "2", "3").forEach { digit ->
                CalculatorKeyButton(
                    label = digit,
                    testTag = "key_$digit",
                    style = CalculatorKeyStyle.NUMERIC,
                    onClick = { viewModel.onDigit(digit) },
                    cornerRadius = cornerRadius,
                    fontSize = numSize,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
            CalculatorKeyButton(
                label = "+",
                testTag = "key_add",
                contentDescriptionText = stringResource(R.string.cd_add),
                style = CalculatorKeyStyle.OPERATOR,
                onClick = { viewModel.onOperator("+") },
                cornerRadius = cornerRadius,
                fontSize = opSize,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }

        // Row 5: 0, ., ⌫, =
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(keySpacing),
        ) {
            CalculatorKeyButton(
                label = "0",
                testTag = "key_0",
                style = CalculatorKeyStyle.NUMERIC,
                onClick = { viewModel.onDigit("0") },
                cornerRadius = cornerRadius,
                fontSize = numSize,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            CalculatorKeyButton(
                label = ".",
                testTag = "key_decimal",
                contentDescriptionText = stringResource(R.string.cd_decimal),
                style = CalculatorKeyStyle.NUMERIC,
                onClick = viewModel::onDecimal,
                cornerRadius = cornerRadius,
                fontSize = numSize,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            CalculatorKeyButton(
                label = "⌫",
                testTag = "key_backspace",
                contentDescriptionText = stringResource(R.string.cd_backspace),
                icon = Icons.AutoMirrored.Rounded.Backspace,
                style = CalculatorKeyStyle.UTILITY,
                onClick = viewModel::onBackspace,
                onLongClick = viewModel::onAllClear,
                cornerRadius = cornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            CalculatorKeyButton(
                label = "=",
                testTag = "key_equals",
                contentDescriptionText = stringResource(R.string.cd_equals),
                style = CalculatorKeyStyle.EQUALS,
                onClick = viewModel::onEquals,
                cornerRadius = cornerRadius,
                fontSize = opSize,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }
    }
}
