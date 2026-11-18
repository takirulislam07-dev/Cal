package com.example.calculator

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CalculatorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    private var nextHistoryId = 1L

    fun onDigit(digit: String) {
        updateExpression { state ->
            val base = if (state.justEvaluated) "" else state.rawExpression
            val currentSegment = trailingNumberSegment(base)
            val nextExpr = when {
                base.endsWith(")") || base.endsWith("π") || base.endsWith("e") ||
                    base.endsWith("%") || base.endsWith("!") -> "$base×$digit"
                currentSegment == "0" && digit == "0" -> base
                currentSegment == "0" && digit != "0" -> base.dropLast(1) + digit
                else -> base + digit
            }
            nextExpr
        }
    }

    fun onDecimal() {
        updateExpression { state ->
            val base = if (state.justEvaluated) "" else state.rawExpression
            val currentSegment = trailingNumberSegment(base)
            if (currentSegment.contains(".")) {
                base
            } else if (base.isEmpty() || isBinaryOperator(base.last()) || base.last() == '(') {
                "${base}0."
            } else if (base.last() == ')' || base.last() == 'π' || base.last() == 'e' ||
                base.last() == '%' || base.last() == '!'
            ) {
                "${base}×0."
            } else {
                "$base."
            }
        }
    }

    fun onOperator(op: String) {
        updateExpression { state ->
            val base = state.rawExpression
            if (base.isEmpty()) {
                if (op == "−") "−" else ""
            } else if (base == "−" && op != "−") {
                ""
            } else {
                val last = base.last()
                when {
                    last == '(' -> if (op == "−") "$base−" else base
                    isBinaryOperator(last) -> {
                        if (op == "−" && (last == '×' || last == '÷' || last == '^')) {
                            "$base−"
                        } else if (base.length >= 2 && isBinaryOperator(base[base.length - 2]) && last == '−') {
                            base.dropLast(2) + op
                        } else {
                            base.dropLast(1) + op
                        }
                    }
                    else -> "$base$op"
                }
            }
        }
    }

    fun onParenthesisSmart() {
        updateExpression { state ->
            val base = if (state.justEvaluated) "" else state.rawExpression
            if (base.isEmpty()) {
                "("
            } else {
                val openCount = base.count { it == '(' } - base.count { it == ')' }
                val last = base.last()
                val canClose = openCount > 0 && !isBinaryOperator(last) && last != '('
                when {
                    canClose -> "$base)"
                    last.isDigit() || last == ')' || last == 'π' || last == 'e' ||
                        last == '%' || last == '!' -> "$base×("
                    else -> "$base("
                }
            }
        }
    }

    fun onOpenParen() {
        updateExpression { state ->
            val base = if (state.justEvaluated) "" else state.rawExpression
            if (base.isNotEmpty() && (base.last().isDigit() || base.last() == ')' ||
                    base.last() == 'π' || base.last() == 'e' || base.last() == '%' || base.last() == '!')
            ) {
                "$base×("
            } else {
                "$base("
            }
        }
    }

    fun onCloseParen() {
        updateExpression { state ->
            val base = state.rawExpression
            val openCount = base.count { it == '(' } - base.count { it == ')' }
            if (openCount > 0 && base.isNotEmpty() && !isBinaryOperator(base.last()) && base.last() != '(') {
                "$base)"
            } else {
                base
            }
        }
    }

    fun onPercent() {
        updateExpression { state ->
            val base = state.rawExpression
            if (base.isEmpty()) return@updateExpression ""
            val last = base.last()
            if (last.isDigit() || last == ')' || last == 'π' || last == 'e') {
                "$base%"
            } else {
                base
            }
        }
    }

    fun onFactorial() {
        updateExpression { state ->
            val base = state.rawExpression
            if (base.isEmpty()) return@updateExpression ""
            val last = base.last()
            if (last.isDigit() || last == ')') {
                "$base!"
            } else {
                base
            }
        }
    }

    fun onFunction(funcPrefix: String) {
        updateExpression { state ->
            val base = if (state.justEvaluated && state.rawExpression.isNotEmpty()) {
                "$funcPrefix(${state.rawExpression})"
            } else {
                val expr = state.rawExpression
                if (expr.isNotEmpty() && (expr.last().isDigit() || expr.last() == ')' ||
                        expr.last() == 'π' || expr.last() == 'e' || expr.last() == '%' || expr.last() == '!')
                ) {
                    "$expr×$funcPrefix("
                } else {
                    "$expr$funcPrefix("
                }
            }
            base
        }
    }

    fun onConstant(symbol: String) {
        updateExpression { state ->
            val base = if (state.justEvaluated) "" else state.rawExpression
            if (base.isNotEmpty() && (base.last().isDigit() || base.last() == ')' ||
                    base.last() == 'π' || base.last() == 'e' || base.last() == '%' || base.last() == '!')
            ) {
                "$base×$symbol"
            } else {
                "$base$symbol"
            }
        }
    }

    fun onPowerBase(prefix: String) {
        updateExpression { state ->
            val base = if (state.justEvaluated) "" else state.rawExpression
            if (base.isNotEmpty() && (base.last().isDigit() || base.last() == ')' ||
                    base.last() == 'π' || base.last() == 'e' || base.last() == '%' || base.last() == '!')
            ) {
                "$base×$prefix"
            } else {
                "$base$prefix"
            }
        }
    }

    fun onSquare() {
        updateExpression { state ->
            val base = state.rawExpression
            if (base.isEmpty()) return@updateExpression ""
            val last = base.last()
            if (last.isDigit() || last == ')' || last == 'π' || last == 'e') {
                "$base^2"
            } else {
                base
            }
        }
    }

    fun onReciprocal() {
        updateExpression { state ->
            val base = state.rawExpression
            if (base.isEmpty()) return@updateExpression "1÷("
            if (state.justEvaluated) {
                "1÷(${base})"
            } else {
                val last = base.last()
                if (last.isDigit() || last == ')' || last == 'π' || last == 'e') {
                    "$base^(−1)"
                } else {
                    "${base}1÷("
                }
            }
        }
    }

    fun onSignToggle() {
        updateExpression { state ->
            val base = state.rawExpression
            if (base.isEmpty()) return@updateExpression "(−"
            val match = Regex("""\(\−([0-9.πe]+)\)$""").find(base)
            if (match != null) {
                base.removeRange(match.range) + match.groupValues[1]
            } else if (base.startsWith("−") && base.drop(1).all { it.isDigit() || it == '.' }) {
                base.drop(1)
            } else if (base.all { it.isDigit() || it == '.' }) {
                "−$base"
            } else {
                val numMatch = Regex("""([0-9.πe]+)$""").find(base)
                if (numMatch != null) {
                    base.removeRange(numMatch.range) + "(−${numMatch.groupValues[1]})"
                } else if (base.endsWith("(−")) {
                    base.dropLast(2)
                } else if (isBinaryOperator(base.last()) || base.last() == '(') {
                    "$base(−"
                } else {
                    base
                }
            }
        }
    }

    fun onBackspace() {
        updateExpression { state ->
            val base = state.rawExpression
            if (base.isEmpty()) return@updateExpression ""
            if (state.justEvaluated) return@updateExpression ""

            val functionPrefixes = listOf(
                "sin⁻¹(", "cos⁻¹(", "tan⁻¹(",
                "sin(", "cos(", "tan(",
                "asin(", "acos(", "atan(",
                "pow10(", "sqrt(", "exp(",
                "log(", "abs(", "ln(", "√("
            )
            val matchedPrefix = functionPrefixes.firstOrNull { base.endsWith(it) }
            if (matchedPrefix != null) {
                base.dropLast(matchedPrefix.length)
            } else {
                base.dropLast(1)
            }
        }
    }

    fun onAllClear() {
        _uiState.update { state ->
            state.copy(
                rawExpression = "",
                formattedExpression = "0",
                livePreview = "",
                hasOperatorInExpression = false,
                justEvaluated = false,
                error = null,
            )
        }
    }

    fun onEquals() {
        val current = _uiState.value
        if (current.rawExpression.isBlank()) return

        when (val outcome = CalculatorEngine.evaluate(current.rawExpression, current.angleMode)) {
            is EvalSuccess -> {
                val displayExpr = CalculatorEngine.formatExpressionForDisplay(current.rawExpression)
                val shouldRecordHistory = CalculatorEngine.containsOperation(current.rawExpression) &&
                    displayExpr != outcome.formattedValue

                val updatedHistory = if (shouldRecordHistory) {
                    val entry = HistoryEntry(
                        id = nextHistoryId++,
                        rawExpression = current.rawExpression,
                        displayExpression = displayExpr,
                        rawResult = outcome.rawValue.replace('-', '−'),
                        formattedResult = outcome.formattedValue,
                    )
                    listOf(entry) + current.history
                } else {
                    current.history
                }

                val nextRaw = outcome.rawValue.replace('-', '−')
                _uiState.update {
                    it.copy(
                        rawExpression = nextRaw,
                        formattedExpression = outcome.formattedValue,
                        livePreview = "",
                        hasOperatorInExpression = false,
                        justEvaluated = true,
                        error = null,
                        history = updatedHistory,
                    )
                }
            }
            is EvalFailure -> {
                _uiState.update {
                    it.copy(
                        error = outcome.error,
                        livePreview = "",
                    )
                }
            }
            EvalEmpty -> Unit
        }
    }

    fun onToggleAngleMode() {
        _uiState.update { state ->
            val nextMode = if (state.angleMode == AngleMode.DEG) AngleMode.RAD else AngleMode.DEG
            val preview = computeLivePreview(state.rawExpression, nextMode)
            state.copy(angleMode = nextMode, livePreview = preview)
        }
    }

    fun onToggleScientific() {
        _uiState.update { it.copy(isScientificExpanded = !it.isScientificExpanded) }
    }

    fun onToggleInverse() {
        _uiState.update { it.copy(isInverseMode = !it.isInverseMode) }
    }

    fun onToggleTheme(currentlyDark: Boolean) {
        _uiState.update { it.copy(darkThemeOverride = !currentlyDark) }
    }

    fun onSelectHistoryResult(entry: HistoryEntry) {
        updateExpression { state ->
            val base = if (state.justEvaluated) "" else state.rawExpression
            val valueToInsert = entry.rawResult
            if (base.isEmpty()) {
                valueToInsert
            } else if (base.last().isDigit() || base.last() == ')' ||
                base.last() == 'π' || base.last() == 'e' || base.last() == '%' || base.last() == '!'
            ) {
                "$base×$valueToInsert"
            } else {
                "$base$valueToInsert"
            }
        }
    }

    fun onRecallHistoryExpression(entry: HistoryEntry) {
        _uiState.update { state ->
            val preview = computeLivePreview(entry.rawExpression, state.angleMode)
            state.copy(
                rawExpression = entry.rawExpression,
                formattedExpression = CalculatorEngine.formatExpressionForDisplay(entry.rawExpression),
                livePreview = preview,
                hasOperatorInExpression = CalculatorEngine.containsOperation(entry.rawExpression),
                justEvaluated = false,
                error = null,
            )
        }
    }

    fun onDeleteHistoryItem(id: Long) {
        _uiState.update { state ->
            state.copy(history = state.history.filterNot { it.id == id })
        }
    }

    fun onClearHistory() {
        _uiState.update { it.copy(history = emptyList()) }
    }

    private fun updateExpression(transform: (CalculatorUiState) -> String) {
        _uiState.update { state ->
            val nextRaw = transform(state)
            val nextFormatted = CalculatorEngine.formatExpressionForDisplay(nextRaw)
            val hasOp = CalculatorEngine.containsOperation(nextRaw)
            val preview = if (hasOp) computeLivePreview(nextRaw, state.angleMode) else ""
            state.copy(
                rawExpression = nextRaw,
                formattedExpression = nextFormatted,
                livePreview = preview,
                hasOperatorInExpression = hasOp,
                justEvaluated = false,
                error = null,
            )
        }
    }

    private fun computeLivePreview(rawExpression: String, angleMode: AngleMode): String {
        if (rawExpression.isBlank() || !CalculatorEngine.containsOperation(rawExpression)) return ""
        return when (val outcome = CalculatorEngine.evaluate(rawExpression, angleMode)) {
            is EvalSuccess -> outcome.formattedValue
            else -> ""
        }
    }

    private fun trailingNumberSegment(expr: String): String {
        val sb = StringBuilder()
        for (i in expr.lastIndex downTo 0) {
            val c = expr[i]
            if (c.isDigit() || c == '.') {
                sb.append(c)
            } else {
                break
            }
        }
        return sb.reverse().toString()
    }

    private fun isBinaryOperator(ch: Char): Boolean {
        return ch == '+' || ch == '-' || ch == '−' || ch == '*' || ch == '×' || ch == '/' || ch == '÷' || ch == '^'
    }
}
