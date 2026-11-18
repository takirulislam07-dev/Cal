package com.example.calculator

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

object CalculatorEngine {

    private val HIGH_PRECISION = MathContext(34, RoundingMode.HALF_UP)
    private val DISPLAY_PRECISION = MathContext(12, RoundingMode.HALF_UP)
    private val ZERO_EPSILON = BigDecimal("1E-14")
    private val MAX_SCIENTIFIC_THRESHOLD = BigDecimal("1E12")
    private val MIN_SCIENTIFIC_THRESHOLD = BigDecimal("1E-9")

    private data class EvalValue(
        val value: BigDecimal,
        val isDirectPercent: Boolean = false,
    )

    private sealed interface Token {
        data class Number(val value: BigDecimal) : Token
        data class Constant(val symbol: String) : Token
        data class Function(val name: String) : Token
        data class Operator(val symbol: Char) : Token
        data object LeftParen : Token
        data object RightParen : Token
        data object Percent : Token
        data object Factorial : Token
    }

    fun evaluate(rawExpression: String, angleMode: AngleMode): EvalOutcome {
        val trimmed = rawExpression.trim()
        if (trimmed.isEmpty()) return EvalEmpty

        val normalized = balanceParentheses(trimmed)
        val tokens = try {
            tokenize(normalized)
        } catch (_: SyntaxException) {
            return EvalFailure(CalculatorErrorType.SYNTAX)
        }

        if (tokens.isEmpty()) return EvalEmpty
        val withImplicitMul = insertImplicitMultiplication(tokens)

        return try {
            val parser = Parser(withImplicitMul, angleMode)
            val result = parser.parse()
            if (!parser.isAtEnd()) {
                EvalFailure(CalculatorErrorType.SYNTAX)
            } else {
                val cleaned = cleanTinyValue(result.value)
                val rawFormatted = formatRaw(cleaned)
                val displayFormatted = formatDisplay(cleaned)
                EvalSuccess(
                    rawValue = rawFormatted,
                    formattedValue = displayFormatted,
                )
            }
        } catch (_: DivideByZeroException) {
            EvalFailure(CalculatorErrorType.DIVIDE_BY_ZERO)
        } catch (_: DomainException) {
            EvalFailure(CalculatorErrorType.DOMAIN)
        } catch (_: OverflowException) {
            EvalFailure(CalculatorErrorType.OVERFLOW)
        } catch (_: SyntaxException) {
            EvalFailure(CalculatorErrorType.SYNTAX)
        } catch (_: ArithmeticException) {
            EvalFailure(CalculatorErrorType.OVERFLOW)
        }
    }

    fun containsOperation(rawExpression: String): Boolean {
        if (rawExpression.isBlank()) return false
        val opChars = setOf('+', '-', '−', '*', '×', '/', '÷', '^', '%', '!', '√', 'π', 'e')
        if (rawExpression.any { it in opChars }) return true
        return rawExpression.any { it.isLetter() }
    }

    fun formatExpressionForDisplay(rawExpression: String): String {
        if (rawExpression.isEmpty()) return "0"
        val sb = StringBuilder(rawExpression.length + 8)
        var i = 0
        while (i < rawExpression.length) {
            val c = rawExpression[i]
            if (c.isDigit() || c == '.') {
                val start = i
                var hasDecimal = false
                var hasExponent = false
                while (i < rawExpression.length) {
                    val ch = rawExpression[i]
                    if (ch.isDigit()) {
                        i++
                    } else if (ch == '.' && !hasDecimal && !hasExponent) {
                        hasDecimal = true
                        i++
                    } else if ((ch == 'E' || ch == 'e') && i + 1 < rawExpression.length &&
                        (rawExpression[i + 1].isDigit() || rawExpression[i + 1] == '-' || rawExpression[i + 1] == '+')
                    ) {
                        // Scientific notation literal like 1.5E12 or 1.5E-4
                        hasExponent = true
                        i++
                        if (i < rawExpression.length && (rawExpression[i] == '-' || rawExpression[i] == '+')) {
                            i++
                        }
                    } else {
                        break
                    }
                }
                val numToken = rawExpression.substring(start, i)
                sb.append(if (hasExponent) numToken else groupNumberLiteral(numToken))
            } else {
                sb.append(
                    when (c) {
                        '*' -> '×'
                        '/' -> '÷'
                        '-' -> '−'
                        else -> c
                    }
                )
                i++
            }
        }
        return sb.toString()
    }

    private fun groupNumberLiteral(literal: String): String {
        val dotIndex = literal.indexOf('.')
        val intPart = if (dotIndex >= 0) literal.substring(0, dotIndex) else literal
        val fracPart = if (dotIndex >= 0) literal.substring(dotIndex) else ""
        if (intPart.length <= 3) return literal
        val groupedInt = buildString {
            for (idx in intPart.indices) {
                append(intPart[idx])
                val remaining = intPart.length - 1 - idx
                if (remaining > 0 && remaining % 3 == 0) {
                    append(',')
                }
            }
        }
        return groupedInt + fracPart
    }

    private fun balanceParentheses(expr: String): String {
        var openCount = 0
        for (ch in expr) {
            when (ch) {
                '(' -> openCount++
                ')' -> if (openCount > 0) openCount--
            }
        }
        if (openCount == 0) return expr
        return expr + ")".repeat(openCount)
    }

    private fun tokenize(expr: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        while (i < expr.length) {
            val ch = expr[i]
            when {
                ch.isWhitespace() -> i++
                ch.isDigit() || ch == '.' -> {
                    val start = i
                    var hasDot = false
                    var hasExp = false
                    while (i < expr.length) {
                        val c = expr[i]
                        if (c.isDigit()) {
                            i++
                        } else if (c == '.' && !hasDot && !hasExp) {
                            hasDot = true
                            i++
                        } else if (c == 'E' && !hasExp && i + 1 < expr.length &&
                            (expr[i + 1].isDigit() || expr[i + 1] == '-' || expr[i + 1] == '+')
                        ) {
                            hasExp = true
                            i++
                            if (i < expr.length && (expr[i] == '-' || expr[i] == '+')) {
                                i++
                            }
                        } else {
                            break
                        }
                    }
                    val numStr = expr.substring(start, i)
                    if (numStr == ".") throw SyntaxException()
                    val parsed = numStr.toBigDecimalOrNull() ?: throw SyntaxException()
                    tokens.add(Token.Number(parsed))
                }
                ch == 'π' -> {
                    tokens.add(Token.Constant("π"))
                    i++
                }
                ch == '√' -> {
                    tokens.add(Token.Function("sqrt"))
                    i++
                }
                ch.isLetter() -> {
                    val start = i
                    while (i < expr.length && (expr[i].isLetter() || expr[i] == '⁻' || expr[i] == '¹')) {
                        i++
                    }
                    val word = expr.substring(start, i)
                    when (word) {
                        "sin", "cos", "tan", "asin", "acos", "atan",
                        "sin⁻¹", "cos⁻¹", "tan⁻¹",
                        "ln", "log", "sqrt", "abs", "exp" -> tokens.add(Token.Function(word))
                        "e" -> tokens.add(Token.Constant("e"))
                        "pi" -> tokens.add(Token.Constant("π"))
                        else -> throw SyntaxException()
                    }
                }
                ch == '(' -> {
                    tokens.add(Token.LeftParen)
                    i++
                }
                ch == ')' -> {
                    tokens.add(Token.RightParen)
                    i++
                }
                ch == '%' -> {
                    tokens.add(Token.Percent)
                    i++
                }
                ch == '!' -> {
                    tokens.add(Token.Factorial)
                    i++
                }
                ch == '+' -> {
                    tokens.add(Token.Operator('+'))
                    i++
                }
                ch == '-' || ch == '−' -> {
                    tokens.add(Token.Operator('-'))
                    i++
                }
                ch == '*' || ch == '×' -> {
                    tokens.add(Token.Operator('*'))
                    i++
                }
                ch == '/' || ch == '÷' -> {
                    tokens.add(Token.Operator('/'))
                    i++
                }
                ch == '^' -> {
                    tokens.add(Token.Operator('^'))
                    i++
                }
                else -> throw SyntaxException()
            }
        }
        return tokens
    }

    private fun insertImplicitMultiplication(tokens: List<Token>): List<Token> {
        if (tokens.size < 2) return tokens
        val out = ArrayList<Token>(tokens.size + 4)
        for (idx in tokens.indices) {
            val current = tokens[idx]
            if (idx > 0) {
                val prev = tokens[idx - 1]
                val prevEndsValue = prev is Token.Number ||
                    prev is Token.Constant ||
                    prev is Token.RightParen ||
                    prev is Token.Percent ||
                    prev is Token.Factorial

                val currStartsValue = current is Token.Number ||
                    current is Token.Constant ||
                    current is Token.Function ||
                    current is Token.LeftParen

                // Avoid inserting * between two numbers (never occurs unless whitespace separated)
                if (prevEndsValue && currStartsValue) {
                    out.add(Token.Operator('*'))
                }
            }
            out.add(current)
        }
        return out
    }

    private class Parser(
        private val tokens: List<Token>,
        private val angleMode: AngleMode,
    ) {
        private var pos = 0

        fun isAtEnd(): Boolean = pos >= tokens.size

        private fun peek(): Token? = tokens.getOrNull(pos)

        private fun advance(): Token = tokens[pos++]

        fun parse(): EvalValue = parseExpression()

        private fun parseExpression(): EvalValue {
            var left = parseTerm()
            while (true) {
                val token = peek()
                if (token is Token.Operator && (token.symbol == '+' || token.symbol == '-')) {
                    advance()
                    val right = parseTerm()
                    val combined = if (right.isDirectPercent) {
                        val delta = left.value.multiply(right.value, HIGH_PRECISION)
                        if (token.symbol == '+') {
                            left.value.add(delta, HIGH_PRECISION)
                        } else {
                            left.value.subtract(delta, HIGH_PRECISION)
                        }
                    } else {
                        if (token.symbol == '+') {
                            left.value.add(right.value, HIGH_PRECISION)
                        } else {
                            left.value.subtract(right.value, HIGH_PRECISION)
                        }
                    }
                    left = EvalValue(combined, isDirectPercent = false)
                } else {
                    break
                }
            }
            return left
        }

        private fun parseTerm(): EvalValue {
            var left = parsePower()
            while (true) {
                val token = peek()
                if (token is Token.Operator && (token.symbol == '*' || token.symbol == '/')) {
                    advance()
                    val right = parsePower()
                    val combined = if (token.symbol == '*') {
                        left.value.multiply(right.value, HIGH_PRECISION)
                    } else {
                        if (right.value.compareTo(BigDecimal.ZERO) == 0) {
                            throw DivideByZeroException()
                        }
                        left.value.divide(right.value, HIGH_PRECISION)
                    }
                    left = EvalValue(combined, isDirectPercent = false)
                } else {
                    break
                }
            }
            return left
        }

        private fun parsePower(): EvalValue {
            val base = parseUnary()
            val token = peek()
            if (token is Token.Operator && token.symbol == '^') {
                advance()
                val exponent = parsePower() // Right-associative
                val result = computePower(base.value, exponent.value)
                return EvalValue(result, isDirectPercent = false)
            }
            return base
        }

        private fun parseUnary(): EvalValue {
            val token = peek()
            if (token is Token.Operator && (token.symbol == '-' || token.symbol == '+')) {
                advance()
                val inner = parseUnary()
                return if (token.symbol == '-') {
                    EvalValue(inner.value.negate(), isDirectPercent = inner.isDirectPercent)
                } else {
                    inner
                }
            }
            return parsePostfix()
        }

        private fun parsePostfix(): EvalValue {
            var current = parsePrimary()
            while (true) {
                when (peek()) {
                    is Token.Percent -> {
                        advance()
                        val divided = current.value.divide(BigDecimal(100), HIGH_PRECISION)
                        current = EvalValue(divided, isDirectPercent = true)
                    }
                    is Token.Factorial -> {
                        advance()
                        val fact = computeFactorial(current.value)
                        current = EvalValue(fact, isDirectPercent = false)
                    }
                    else -> break
                }
            }
            return current
        }

        private fun parsePrimary(): EvalValue {
            val token = peek() ?: throw SyntaxException()
            return when (token) {
                is Token.Number -> {
                    advance()
                    EvalValue(token.value)
                }
                is Token.Constant -> {
                    advance()
                    val value = when (token.symbol) {
                        "π" -> BigDecimal(PI.toString())
                        "e" -> BigDecimal(kotlin.math.E.toString())
                        else -> throw SyntaxException()
                    }
                    EvalValue(value)
                }
                is Token.LeftParen -> {
                    advance()
                    val inner = parseExpression()
                    val closing = peek()
                    if (closing !is Token.RightParen) throw SyntaxException()
                    advance()
                    EvalValue(inner.value, isDirectPercent = false)
                }
                is Token.Function -> {
                    advance()
                    val arg = if (peek() is Token.LeftParen) {
                        advance()
                        val inner = parseExpression()
                        if (peek() !is Token.RightParen) throw SyntaxException()
                        advance()
                        inner.value
                    } else {
                        parseUnary().value
                    }
                    EvalValue(applyFunction(token.name, arg))
                }
                else -> throw SyntaxException()
            }
        }

        private fun applyFunction(name: String, arg: BigDecimal): BigDecimal {
            val d = arg.toDouble()
            if (d.isNaN() || d.isInfinite()) throw OverflowException()

            val resultDouble = when (name) {
                "sin" -> {
                    val rad = if (angleMode == AngleMode.DEG) Math.toRadians(d) else d
                    sin(rad)
                }
                "cos" -> {
                    val rad = if (angleMode == AngleMode.DEG) Math.toRadians(d) else d
                    cos(rad)
                }
                "tan" -> {
                    if (angleMode == AngleMode.DEG) {
                        val mod = ((d % 180.0) + 180.0) % 180.0
                        if (abs(mod - 90.0) < 1e-12) throw DomainException()
                        tan(Math.toRadians(d))
                    } else {
                        val cosVal = cos(d)
                        if (abs(cosVal) < 1e-15) throw DomainException()
                        tan(d)
                    }
                }
                "asin", "sin⁻¹" -> {
                    if (d < -1.0 || d > 1.0) throw DomainException()
                    val rad = asin(d)
                    if (angleMode == AngleMode.DEG) Math.toDegrees(rad) else rad
                }
                "acos", "cos⁻¹" -> {
                    if (d < -1.0 || d > 1.0) throw DomainException()
                    val rad = acos(d)
                    if (angleMode == AngleMode.DEG) Math.toDegrees(rad) else rad
                }
                "atan", "tan⁻¹" -> {
                    val rad = atan(d)
                    if (angleMode == AngleMode.DEG) Math.toDegrees(rad) else rad
                }
                "ln" -> {
                    if (d <= 0.0) throw DomainException()
                    ln(d)
                }
                "log" -> {
                    if (d <= 0.0) throw DomainException()
                    log10(d)
                }
                "sqrt" -> {
                    if (arg.compareTo(BigDecimal.ZERO) < 0) throw DomainException()
                    return sqrtBigDecimal(arg)
                }
                "abs" -> return arg.abs()
                "exp" -> exp(d)
                "pow10" -> 10.0.pow(d)
                else -> throw SyntaxException()
            }

            if (resultDouble.isNaN()) throw DomainException()
            if (resultDouble.isInfinite()) throw OverflowException()
            return BigDecimal.valueOf(resultDouble)
        }

        private fun sqrtBigDecimal(value: BigDecimal): BigDecimal {
            if (value.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO
            // Use Double initial guess and 4 Newton-Raphson iterations in DECIMAL128
            val doubleSqrt = sqrt(value.toDouble())
            if (doubleSqrt.isInfinite() || doubleSqrt.isNaN()) {
                return BigDecimal.valueOf(doubleSqrt)
            }
            var x = BigDecimal.valueOf(doubleSqrt)
            val two = BigDecimal(2)
            repeat(5) {
                x = x.add(value.divide(x, HIGH_PRECISION), HIGH_PRECISION)
                    .divide(two, HIGH_PRECISION)
            }
            return x
        }

        private fun computePower(base: BigDecimal, exponent: BigDecimal): BigDecimal {
            // Exact integer exponent path when exponent is a small integer
            val strippedExp = exponent.stripTrailingZeros()
            if (strippedExp.scale() <= 0) {
                val intExp = try {
                    strippedExp.intValueExact()
                } catch (_: ArithmeticException) {
                    null
                }
                if (intExp != null && intExp in -1000..1000) {
                    if (base.compareTo(BigDecimal.ZERO) == 0 && intExp < 0) {
                        throw DivideByZeroException()
                    }
                    return if (intExp >= 0) {
                        base.pow(intExp, HIGH_PRECISION)
                    } else {
                        BigDecimal.ONE.divide(base.pow(-intExp, HIGH_PRECISION), HIGH_PRECISION)
                    }
                }
            }

            val b = base.toDouble()
            val e = exponent.toDouble()
            if (b == 0.0 && e < 0.0) throw DivideByZeroException()
            if (b < 0.0 && e % 1.0 != 0.0) throw DomainException()
            val res = b.pow(e)
            if (res.isNaN()) throw DomainException()
            if (res.isInfinite()) throw OverflowException()
            return BigDecimal.valueOf(res)
        }

        private fun computeFactorial(value: BigDecimal): BigDecimal {
            val stripped = value.stripTrailingZeros()
            if (stripped.scale() > 0 || stripped.compareTo(BigDecimal.ZERO) < 0) {
                throw DomainException()
            }
            val n = try {
                stripped.intValueExact()
            } catch (_: ArithmeticException) {
                throw OverflowException()
            }
            if (n > 200) throw OverflowException()
            var acc = BigDecimal.ONE
            for (k in 2..n) {
                acc = acc.multiply(BigDecimal(k), HIGH_PRECISION)
            }
            return acc
        }
    }

    private fun cleanTinyValue(value: BigDecimal): BigDecimal {
        val rounded = value.round(DISPLAY_PRECISION)
        return if (rounded.abs() < ZERO_EPSILON && value.abs() < ZERO_EPSILON) {
            BigDecimal.ZERO
        } else {
            rounded.stripTrailingZeros().let {
                if (it.compareTo(BigDecimal.ZERO) == 0) BigDecimal.ZERO else it
            }
        }
    }

    private fun formatRaw(value: BigDecimal): String {
        if (value.compareTo(BigDecimal.ZERO) == 0) return "0"
        val absVal = value.abs()
        return if (absVal >= MAX_SCIENTIFIC_THRESHOLD || absVal < MIN_SCIENTIFIC_THRESHOLD) {
            value.round(MathContext(10, RoundingMode.HALF_UP))
                .stripTrailingZeros()
                .toString()
                .replace("+", "")
        } else {
            value.stripTrailingZeros().toPlainString()
        }
    }

    private fun formatDisplay(value: BigDecimal): String {
        val raw = formatRaw(value)
        if (raw.contains('E') || raw.contains('e')) return raw
        val isNegative = raw.startsWith('-')
        val unsigned = if (isNegative) raw.substring(1) else raw
        val grouped = groupNumberLiteral(unsigned)
        return if (isNegative) "−$grouped" else grouped
    }

    private class SyntaxException : RuntimeException()
    private class DivideByZeroException : RuntimeException()
    private class DomainException : RuntimeException()
    private class OverflowException : RuntimeException()
}
