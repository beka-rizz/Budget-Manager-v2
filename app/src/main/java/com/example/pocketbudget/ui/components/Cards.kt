package com.example.pocketbudget.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pocketbudget.domain.BudgetLevel
import com.example.pocketbudget.domain.BudgetStatus
import com.example.pocketbudget.domain.Summary
import com.example.pocketbudget.ui.formatMoney
import com.example.pocketbudget.ui.formatPercent
import com.example.pocketbudget.ui.theme.ExpenseColor
import com.example.pocketbudget.ui.theme.IncomeColor
import com.example.pocketbudget.ui.theme.WarningColor

@Composable
fun SummaryCard(
    title: String,
    summary: Summary,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(
                text = formatMoney(summary.balance),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                LabeledAmount("Income", summary.income, IncomeColor)
                LabeledAmount("Expenses", summary.expense, ExpenseColor)
            }
        }
    }
}

@Composable
fun LabeledAmount(
    label: String,
    amount: Double,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(
            text = formatMoney(amount),
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

val BudgetLevel.color: Color
    get() = when (this) {
        BudgetLevel.OK -> IncomeColor
        BudgetLevel.WARNING -> WarningColor
        BudgetLevel.EXCEEDED -> ExpenseColor
    }

@Composable
fun BudgetCard(
    status: BudgetStatus,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Monthly budget", style = MaterialTheme.typography.titleSmall)
                Text(
                    text = formatPercent(status.progress),
                    style = MaterialTheme.typography.titleSmall,
                    color = status.level.color,
                )
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { status.progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = status.level.color,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Spent ${formatMoney(status.spent)} of ${formatMoney(status.limit)}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = budgetHint(status),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

fun budgetHint(status: BudgetStatus): String = when (status.level) {
    BudgetLevel.EXCEEDED -> "Over budget by ${formatMoney(-status.remaining)}"
    else -> "You can spend ${formatMoney(status.dailyAllowance)} per day for ${status.daysLeft} days"
}
