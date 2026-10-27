package com.example.pocketbudget

import com.example.pocketbudget.data.Category
import com.example.pocketbudget.data.Transaction
import com.example.pocketbudget.data.TransactionType
import com.example.pocketbudget.domain.BudgetCalculator
import com.example.pocketbudget.domain.BudgetLevel
import com.example.pocketbudget.domain.Period
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class BudgetCalculatorTest {
    private val today = LocalDate.of(2026, 10, 21)

    private fun tx(id: Long, amount: Double, type: TransactionType, category: Category, daysAgo: Long = 0) =
        Transaction(id, "Item $id", amount, type, category, LocalDateTime.of(today.minusDays(daysAgo), LocalTime.NOON))

    private val sample = listOf(
        tx(1, 100_000.0, TransactionType.INCOME, Category.SALARY),
        tx(2, 30_000.0, TransactionType.EXPENSE, Category.FOOD),
        tx(3, 10_000.0, TransactionType.EXPENSE, Category.TRANSPORT, daysAgo = 10),
        tx(4, 50_000.0, TransactionType.EXPENSE, Category.BILLS, daysAgo = 40),
    )

    @Test
    fun summarize_splitsIncomeAndExpenses() {
        val summary = BudgetCalculator.summarize(sample)
        assertEquals(100_000.0, summary.income, 0.001)
        assertEquals(90_000.0, summary.expense, 0.001)
        assertEquals(10_000.0, summary.balance, 0.001)
    }

    @Test
    fun filter_bySearchAndCategory() {
        assertEquals(listOf(2L), BudgetCalculator.filter(sample, "item 2", null).map { it.id })
        assertEquals(listOf(3L), BudgetCalculator.filter(sample, "", Category.TRANSPORT).map { it.id })
    }

    @Test
    fun inPeriod_keepsOnlyMatchingDates() {
        assertEquals(2, BudgetCalculator.inPeriod(sample, Period.WEEK, today).size)
        assertEquals(3, BudgetCalculator.inPeriod(sample, Period.MONTH, today).size)
        assertEquals(4, BudgetCalculator.inPeriod(sample, Period.ALL, today).size)
    }

    @Test
    fun budgetStatus_usesCurrentMonthExpenses() {
        val status = BudgetCalculator.budgetStatus(sample, 50_000.0, today)!!
        assertEquals(40_000.0, status.spent, 0.001)
        assertEquals(11, status.daysLeft)
        assertEquals(BudgetLevel.WARNING, status.level)
        assertNull(BudgetCalculator.budgetStatus(sample, null, today))
    }

    @Test
    fun validate_rejectsEmptyTitleAndZeroAmount() {
        assertFalse(BudgetCalculator.validate(" ", "0").isValid)
        assertTrue(BudgetCalculator.validate("Lunch", "1500").isValid)
    }

    @Test
    fun sanitizeAmountInput_keepsTwoDecimals() {
        assertEquals("12.34", BudgetCalculator.sanitizeAmountInput("12,345abc"))
        assertEquals("0.5", BudgetCalculator.sanitizeAmountInput(".5"))
    }
}
