package com.example.pocketbudget.data

import java.time.LocalDateTime

enum class TransactionType(val label: String) {
    EXPENSE("Expense"),
    INCOME("Income"),
}

enum class Category(val label: String) {
    FOOD("Food"),
    TRANSPORT("Transport"),
    SHOPPING("Shopping"),
    BILLS("Bills"),
    FUN("Fun"),
    STUDY("Study"),
    SALARY("Salary"),
    OTHER("Other"),
}

data class Transaction(
    val id: Long,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: Category,
    val dateTime: LocalDateTime,
)

fun sampleTransactions(now: LocalDateTime = LocalDateTime.now()): List<Transaction> {
    fun daysAgo(days: Long, hour: Int): LocalDateTime =
        now.minusDays(days).withHour(hour).withMinute(0).withSecond(0).withNano(0)

    return listOf(
        Transaction(1, "Scholarship", 45_000.0, TransactionType.INCOME, Category.SALARY, daysAgo(0, 9)),
        Transaction(2, "Magnum groceries", 8_640.0, TransactionType.EXPENSE, Category.FOOD, daysAgo(0, 18)),
        Transaction(3, "Coffee with friends", 1_800.0, TransactionType.EXPENSE, Category.FOOD, daysAgo(1, 16)),
        Transaction(4, "Metro card", 3_200.0, TransactionType.EXPENSE, Category.TRANSPORT, daysAgo(1, 8)),
        Transaction(5, "Room rent", 80_000.0, TransactionType.EXPENSE, Category.BILLS, daysAgo(2, 12)),
        Transaction(6, "Part-time shift", 60_000.0, TransactionType.INCOME, Category.SALARY, daysAgo(3, 19)),
        Transaction(7, "Cinema", 3_500.0, TransactionType.EXPENSE, Category.FUN, daysAgo(4, 20)),
        Transaction(8, "Textbook", 6_500.0, TransactionType.EXPENSE, Category.STUDY, daysAgo(5, 14)),
        Transaction(9, "Winter jacket", 24_900.0, TransactionType.EXPENSE, Category.SHOPPING, daysAgo(9, 15)),
        Transaction(10, "Phone bill", 2_990.0, TransactionType.EXPENSE, Category.BILLS, daysAgo(12, 11)),
        Transaction(11, "Family transfer", 150_000.0, TransactionType.INCOME, Category.OTHER, daysAgo(20, 10)),
        Transaction(12, "Taxi home", 2_400.0, TransactionType.EXPENSE, Category.TRANSPORT, daysAgo(35, 23)),
    )
}
