package com.example.calculator

enum class AngleMode {
    DEG,
    RAD,
}

enum class CalculatorErrorType {
    DIVIDE_BY_ZERO,
    SYNTAX,
    DOMAIN,
    OVERFLOW,
}

sealed interface EvalOutcome

data class EvalSuccess(
    val rawValue: String,
    val formattedValue: String,
) : EvalOutcome

data class EvalFailure(
    val error: CalculatorErrorType,
) : EvalOutcome

data object EvalEmpty : EvalOutcome

data class HistoryEntry(
    val id: Long,
    val rawExpression: String,
    val displayExpression: String,
    val rawResult: String,
    val formattedResult: String,
)

data class CalculatorUiState(
    val rawExpression: String = "",
    val formattedExpression: String = "0",
    val livePreview: String = "",
    val hasOperatorInExpression: Boolean = false,
    val justEvaluated: Boolean = false,
    val error: CalculatorErrorType? = null,
    val angleMode: AngleMode = AngleMode.DEG,
    val isScientificExpanded: Boolean = false,
    val isInverseMode: Boolean = false,
    val darkThemeOverride: Boolean? = null,
    val history: List<HistoryEntry> = emptyList(),
)
