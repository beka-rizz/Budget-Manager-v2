package com.example.pocketbudget.domain

import com.example.pocketbudget.data.Category
import com.example.pocketbudget.data.Transaction
import com.example.pocketbudget.data.TransactionType
import java.time.LocalDate
import java.time.YearMonth

enum class Period(val label: String) {
    WEEK("7 days"),
    MONTH("This month"),
    ALL("All time"),
}

data class Summary(
    val income: Double,
    val expense: Double,
) {
    val balance: Double get() = income - expense
}

data class CategoryShare(
    val category: Category,
    val amount: Double,
    val share: Float,
)

enum class BudgetLevel { OK, WARNING, EXCEEDED }

data class BudgetStatus(
    val limit: Double,
    val spent: Double,
    val daysLeft: Int,
) {
    val remaining: Double get() = limit - spent
    val progress: Float get() = if (limit > 0) (spent / limit).toFloat() else 0f
    val dailyAllowance: Double get() = if (remaining > 0 && daysLeft > 0) remaining / daysLeft else 0.0
    val level: BudgetLevel
        get() = when {
            progress >= 1f -> BudgetLevel.EXCEEDED
            progress >= WARNING_THRESHOLD -> BudgetLevel.WARNING
            else -> BudgetLevel.OK
        }

    companion object {
        const val WARNING_THRESHOLD = 0.8f
    }
}

data class ValidationResult(
    val title: String,
    val amount: Double?,
    val titleError: String?,
    val amountError: String?,
) {
    val isValid: Boolean get() = titleError == null && amountError == null && amount != null
}

/** Pure business rules. No Android or Compose dependencies, so it is easy to unit test. */
object BudgetCalculator {
    private const val MAX_AMOUNT = 99_999_999.99
    private const val MAX_TITLE_LENGTH = 40

    fun filter(
        transactions: List<Transaction>,
        query: String,
        category: Category?,
    ): List<Transaction> {
        val cleanQuery = query.trim()
        return transactions
            .filter { cleanQuery.isEmpty() || it.title.contains(cleanQuery, ignoreCase = true) }
            .filter { category == null || it.category == category }
            .sortedByDescending { it.dateTime }
    }

    fun summarize(transactions: List<Transaction>): Summary {
        val (income, expense) = transactions.partition { it.type == TransactionType.INCOME }
        return Summary(
            income = income.sumOf { it.amount },
            expense = expense.sumOf { it.amount },
        )
    }

    fun inPeriod(
        transactions: List<Transaction>,
        period: Period,
        today: LocalDate = LocalDate.now(),
    ): List<Transaction> = when (period) {
        Period.WEEK -> {
            val from = today.minusDays(6)
            transactions.filter { !it.dateTime.toLocalDate().isBefore(from) }
        }
        Period.MONTH -> {
            val month = YearMonth.from(today)
            transactions.filter { YearMonth.from(it.dateTime) == month }
        }
        Period.ALL -> transactions
    }

    fun expensesByCategory(transactions: List<Transaction>): List<CategoryShare> {
        val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
        val total = expenses.sumOf { it.amount }
        if (total <= 0.0) return emptyList()
        return expenses
            .groupBy { it.category }
            .map { (category, items) ->
                val amount = items.sumOf { it.amount }
                CategoryShare(category, amount, (amount / total).toFloat())
            }
            .sortedByDescending { it.amount }
    }

    fun budgetStatus(
        transactions: List<Transaction>,
        monthlyLimit: Double?,
        today: LocalDate = LocalDate.now(),
    ): BudgetStatus? {
        if (monthlyLimit == null || monthlyLimit <= 0.0) return null
        val spent = summarize(inPeriod(transactions, Period.MONTH, today)).expense
        val daysLeft = today.lengthOfMonth() - today.dayOfMonth + 1
        return BudgetStatus(limit = monthlyLimit, spent = spent, daysLeft = daysLeft)
    }

    fun validate(title: String, amountText: String): ValidationResult {
        val cleanTitle = title.trim()
        val titleError = when {
            cleanTitle.isEmpty() -> "Enter a title"
            cleanTitle.length > MAX_TITLE_LENGTH -> "Keep the title under $MAX_TITLE_LENGTH characters"
            else -> null
        }
        val amount = parseAmount(amountText)
        val amountError = if (amount == null) "Enter an amount greater than zero" else null
        return ValidationResult(cleanTitle, amount, titleError, amountError)
    }

    fun parseAmount(text: String): Double? {
        val value = text.replace(',', '.').toDoubleOrNull() ?: return null
        return value.takeIf { it > 0.0 && it <= MAX_AMOUNT }
    }

    /** Keeps only digits and one decimal separator with at most two decimals. */
    fun sanitizeAmountInput(raw: String): String {
        val result = StringBuilder()
        var dotSeen = false
        var decimals = 0
        for (ch in raw.replace(',', '.')) {
            when {
                ch.isDigit() && !dotSeen && result.length < 9 -> result.append(ch)
                ch.isDigit() && dotSeen && decimals < 2 -> {
                    result.append(ch)
                    decimals++
                }
                ch == '.' && !dotSeen -> {
                    if (result.isEmpty()) result.append('0')
                    result.append('.')
                    dotSeen = true
                }
            }
        }
        return result.toString()
    }
}
