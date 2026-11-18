package com.example

import com.example.calculator.AngleMode
import com.example.calculator.CalculatorEngine
import com.example.calculator.CalculatorErrorType
import com.example.calculator.CalculatorViewModel
import com.example.calculator.EvalFailure
import com.example.calculator.EvalSuccess
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorEngineTest {

    @Test
    fun `basic arithmetic and order of operations`() {
        val result = CalculatorEngine.evaluate("2+3×4−6÷2", AngleMode.DEG)
        assertEquals(EvalSuccess("11", "11"), result)
    }

    @Test
    fun `floating point precision avoids binary drift`() {
        val result = CalculatorEngine.evaluate("0.1+0.2", AngleMode.DEG)
        assertEquals(EvalSuccess("0.3", "0.3"), result)
    }

    @Test
    fun `parentheses and implicit multiplication`() {
        val result = CalculatorEngine.evaluate("2(3+4)", AngleMode.DEG)
        assertEquals(EvalSuccess("14", "14"), result)

        val autoClosed = CalculatorEngine.evaluate("3×(2+5", AngleMode.DEG)
        assertEquals(EvalSuccess("21", "21"), autoClosed)
    }

    @Test
    fun `smart percentage calculation`() {
        assertEquals(
            EvalSuccess("110", "110"),
            CalculatorEngine.evaluate("100+10%", AngleMode.DEG),
        )
        assertEquals(
            EvalSuccess("150", "150"),
            CalculatorEngine.evaluate("200−25%", AngleMode.DEG),
        )
        assertEquals(
            EvalSuccess("10", "10"),
            CalculatorEngine.evaluate("100×10%", AngleMode.DEG),
        )
        assertEquals(
            EvalSuccess("0.5", "0.5"),
            CalculatorEngine.evaluate("50%", AngleMode.DEG),
        )
    }

    @Test
    fun `trigonometric functions in degree and radian modes`() {
        assertEquals(
            EvalSuccess("0.5", "0.5"),
            CalculatorEngine.evaluate("sin(30)", AngleMode.DEG),
        )
        assertEquals(
            EvalSuccess("0", "0"),
            CalculatorEngine.evaluate("cos(90)", AngleMode.DEG),
        )
        assertEquals(
            EvalSuccess("1", "1"),
            CalculatorEngine.evaluate("sin(π÷2)", AngleMode.RAD),
        )
        assertEquals(
            EvalSuccess("30", "30"),
            CalculatorEngine.evaluate("sin⁻¹(0.5)", AngleMode.DEG),
        )
    }

    @Test
    fun `logarithms powers roots and factorial`() {
        assertEquals(
            EvalSuccess("12", "12"),
            CalculatorEngine.evaluate("√(144)", AngleMode.DEG),
        )
        assertEquals(
            EvalSuccess("2", "2"),
            CalculatorEngine.evaluate("log(100)", AngleMode.DEG),
        )
        assertEquals(
            EvalSuccess("120", "120"),
            CalculatorEngine.evaluate("5!", AngleMode.DEG),
        )
        assertEquals(
            EvalSuccess("512", "512"),
            CalculatorEngine.evaluate("2^3^2", AngleMode.DEG),
        )
    }

    @Test
    fun `error handling for division by zero and domain errors`() {
        assertEquals(
            EvalFailure(CalculatorErrorType.DIVIDE_BY_ZERO),
            CalculatorEngine.evaluate("12÷0", AngleMode.DEG),
        )
        assertEquals(
            EvalFailure(CalculatorErrorType.DOMAIN),
            CalculatorEngine.evaluate("√(−9)", AngleMode.DEG),
        )
        assertEquals(
            EvalFailure(CalculatorErrorType.DOMAIN),
            CalculatorEngine.evaluate("ln(0)", AngleMode.DEG),
        )
        assertEquals(
            EvalFailure(CalculatorErrorType.DOMAIN),
            CalculatorEngine.evaluate("tan(90)", AngleMode.DEG),
        )
    }

    @Test
    fun `formatting large numbers with grouping`() {
        val result = CalculatorEngine.evaluate("1000×250", AngleMode.DEG)
        assertEquals(EvalSuccess("250000", "250,000"), result)
        assertEquals("1,250×12", CalculatorEngine.formatExpressionForDisplay("1250×12"))
    }

    @Test
    fun `viewmodel records history and supports result reuse`() {
        val vm = CalculatorViewModel()
        vm.onDigit("2")
        vm.onDigit("5")
        vm.onOperator("×")
        vm.onDigit("4")
        assertEquals("100", vm.uiState.value.livePreview)

        vm.onEquals()
        assertEquals("100", vm.uiState.value.formattedExpression)
        assertEquals(1, vm.uiState.value.history.size)
        assertTrue(vm.uiState.value.justEvaluated)

        vm.onOperator("+")
        vm.onDigit("5")
        vm.onEquals()
        assertEquals("105", vm.uiState.value.formattedExpression)
        assertEquals(2, vm.uiState.value.history.size)
    }
}
