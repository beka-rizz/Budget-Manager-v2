package com.example.pocketbudget.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pocketbudget.data.Category
import com.example.pocketbudget.ui.BudgetUiState
import com.example.pocketbudget.ui.components.BudgetCard
import com.example.pocketbudget.ui.components.CategoryChipRow
import com.example.pocketbudget.ui.components.EmptyState
import com.example.pocketbudget.ui.components.SectionTitle
import com.example.pocketbudget.ui.components.SummaryCard
import com.example.pocketbudget.ui.components.TransactionItem

@Composable
fun TransactionsScreen(
    state: BudgetUiState,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelected: (Category?) -> Unit,
    onClearFilters: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    onBudgetClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasFilters = searchQuery.isNotBlank() || state.selectedCategory != null

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { SummaryCard(title = "Total balance", summary = state.summary) }

        state.budget?.let { budget ->
            item { BudgetCard(status = budget, onClick = onBudgetClick) }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search by title") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        item {
            CategoryChipRow(
                selected = state.selectedCategory,
                onSelect = onCategorySelected,
                allLabel = "All",
            )
        }

        item {
            SectionTitle("Transactions (${state.visibleTransactions.size} of ${state.allTransactions.size})")
        }

        if (state.visibleTransactions.isEmpty()) {
            item {
                EmptyState(
                    message = if (hasFilters) "Nothing matches your filters" else "No transactions yet.\nTap \"Add\" to create one.",
                    actionLabel = if (hasFilters) "Clear filters" else null,
                    onAction = onClearFilters,
                )
            }
        } else {
            items(state.visibleTransactions, key = { it.id }) { transaction ->
                TransactionItem(
                    transaction = transaction,
                    onClick = { onTransactionClick(transaction.id) },
                    onDelete = { onDeleteTransaction(transaction.id) },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}
