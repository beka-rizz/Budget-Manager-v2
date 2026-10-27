package com.example.pocketbudget.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocketbudget.data.Category
import com.example.pocketbudget.data.Transaction
import com.example.pocketbudget.data.TransactionRepository
import com.example.pocketbudget.data.TransactionType
import com.example.pocketbudget.domain.BudgetCalculator
import com.example.pocketbudget.domain.BudgetStatus
import com.example.pocketbudget.domain.Summary
import com.example.pocketbudget.domain.ValidationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDateTime

data class BudgetUiState(
    val allTransactions: List<Transaction> = emptyList(),
    val visibleTransactions: List<Transaction> = emptyList(),
    val selectedCategory: Category? = null,
    val summary: Summary = Summary(income = 0.0, expense = 0.0),
    val monthlyLimit: Double? = null,
    val budget: BudgetStatus? = null,
)

/**
 * Shared by all screens (scoped to the Activity), so it survives configuration changes
 * and every screen sees the same data.
 */
class BudgetViewModel(
    private val repository: TransactionRepository = TransactionRepository(),
) : ViewModel() {

    // Kept separate from uiState so the search TextField is updated synchronously.
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val selectedCategory = MutableStateFlow<Category?>(null)

    val uiState: StateFlow<BudgetUiState> = combine(
        repository.transactions,
        repository.monthlyLimit,
        _searchQuery,
        selectedCategory,
        ::buildState,
    )
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = buildState(
                repository.transactions.value,
                repository.monthlyLimit.value,
                _searchQuery.value,
                selectedCategory.value,
            ),
        )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: Category?) {
        selectedCategory.value = category
    }

    fun clearFilters() {
        _searchQuery.value = ""
        selectedCategory.value = null
    }

    fun getTransaction(id: Long): Transaction? = repository.getById(id)

    /** Returns the validation result; the transaction is saved only when it is valid. */
    fun saveTransaction(
        id: Long?,
        title: String,
        amountText: String,
        type: TransactionType,
        category: Category,
    ): ValidationResult {
        val validation = BudgetCalculator.validate(title, amountText)
        val amount = validation.amount
        if (!validation.isValid || amount == null) return validation

        val existing = id?.let { repository.getById(it) }
        repository.upsert(
            Transaction(
                id = existing?.id ?: repository.nextId(),
                title = validation.title,
                amount = amount,
                type = type,
                category = category,
                dateTime = existing?.dateTime ?: LocalDateTime.now(),
            ),
        )
        return validation
    }

    fun deleteTransaction(id: Long): Transaction? = repository.delete(id)

    fun restoreTransaction(transaction: Transaction) = repository.upsert(transaction)

    /** Returns an error message, or null when the limit was saved. Empty text removes the limit. */
    fun setMonthlyLimit(text: String): String? {
        if (text.isBlank()) {
            repository.setMonthlyLimit(null)
            return null
        }
        val limit = BudgetCalculator.parseAmount(text) ?: return "Enter a positive amount"
        repository.setMonthlyLimit(limit)
        return null
    }

    private fun buildState(
        transactions: List<Transaction>,
        monthlyLimit: Double?,
        query: String,
        category: Category?,
    ) = BudgetUiState(
        allTransactions = transactions,
        visibleTransactions = BudgetCalculator.filter(transactions, query, category),
        selectedCategory = category,
        summary = BudgetCalculator.summarize(transactions),
        monthlyLimit = monthlyLimit,
        budget = BudgetCalculator.budgetStatus(transactions, monthlyLimit),
    )
}
