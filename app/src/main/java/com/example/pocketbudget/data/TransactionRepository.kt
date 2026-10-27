package com.example.pocketbudget.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * In-memory source of truth for transactions and the monthly budget limit.
 * It can be replaced with a Room database later without changing the UI.
 */
class TransactionRepository(
    initialTransactions: List<Transaction> = sampleTransactions(),
    initialMonthlyLimit: Double? = 200_000.0,
) {
    private val _transactions = MutableStateFlow(initialTransactions)
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _monthlyLimit = MutableStateFlow(initialMonthlyLimit)
    val monthlyLimit: StateFlow<Double?> = _monthlyLimit.asStateFlow()

    fun getById(id: Long): Transaction? = _transactions.value.firstOrNull { it.id == id }

    fun nextId(): Long = (_transactions.value.maxOfOrNull { it.id } ?: 0L) + 1L

    fun upsert(transaction: Transaction) {
        _transactions.update { list ->
            if (list.any { it.id == transaction.id }) {
                list.map { if (it.id == transaction.id) transaction else it }
            } else {
                list + transaction
            }
        }
    }

    fun delete(id: Long): Transaction? {
        val removed = getById(id) ?: return null
        _transactions.update { list -> list.filterNot { it.id == id } }
        return removed
    }

    fun setMonthlyLimit(limit: Double?) {
        _monthlyLimit.value = limit
    }
}
