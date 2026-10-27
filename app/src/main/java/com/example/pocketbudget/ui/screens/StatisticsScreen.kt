package com.example.pocketbudget.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.pocketbudget.data.Transaction
import com.example.pocketbudget.data.TransactionType
import com.example.pocketbudget.domain.BudgetCalculator
import com.example.pocketbudget.domain.Period
import com.example.pocketbudget.ui.components.CategoryIcon
import com.example.pocketbudget.ui.components.CategoryShareRow
import com.example.pocketbudget.ui.components.EmptyState
import com.example.pocketbudget.ui.components.SectionTitle
import com.example.pocketbudget.ui.components.SegmentedSelector
import com.example.pocketbudget.ui.components.SummaryCard
import com.example.pocketbudget.ui.formatDate
import com.example.pocketbudget.ui.formatMoney
import com.example.pocketbudget.ui.formatPercent

@Composable
fun StatisticsScreen(
    transactions: List<Transaction>,
    modifier: Modifier = Modifier,
) {
    // UI-only state: which period is selected. Survives rotation thanks to rememberSaveable.
    var period by rememberSaveable { mutableStateOf(Period.MONTH) }

    // Derived values are recalculated only when the data or the period changes.
    val periodTransactions = remember(transactions, period) {
        BudgetCalculator.inPeriod(transactions, period)
    }
    val summary = remember(periodTransactions) { BudgetCalculator.summarize(periodTransactions) }
    val shares = remember(periodTransactions) { BudgetCalculator.expensesByCategory(periodTransactions) }
    val biggestExpense = remember(periodTransactions) {
        periodTransactions.filter { it.type == TransactionType.EXPENSE }.maxByOrNull { it.amount }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SegmentedSelector(
                options = Period.entries,
                selected = period,
                label = { it.label },
                onSelect = { period = it },
            )
        }

        item { SummaryCard(title = "Net for ${period.label.lowercase()}", summary = summary) }

        if (summary.income > 0) {
            item {
                val savedShare = (summary.balance / summary.income).toFloat().coerceAtLeast(0f)
                Text("You saved ${formatPercent(savedShare)} of your income in this period")
            }
        }

        biggestExpense?.let { expense ->
            item { SectionTitle("Biggest expense") }
            item {
                Card {
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        leadingContent = { CategoryIcon(expense.category) },
                        headlineContent = { Text(expense.title) },
                        supportingContent = { Text(formatDate(expense.dateTime)) },
                        trailingContent = {
                            Text(formatMoney(expense.amount), style = MaterialTheme.typography.titleMedium)
                        },
                    )
                }
            }
        }

        item { SectionTitle("Spending by category") }

        if (shares.isEmpty()) {
            item { EmptyState("No expenses in this period") }
        } else {
            items(shares, key = { it.category }) { share -> CategoryShareRow(share) }
        }
    }
}
