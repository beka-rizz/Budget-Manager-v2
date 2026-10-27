package com.example.pocketbudget.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.example.pocketbudget.domain.BudgetLevel
import com.example.pocketbudget.domain.BudgetStatus
import com.example.pocketbudget.ui.components.AmountField
import com.example.pocketbudget.ui.components.BudgetCard
import com.example.pocketbudget.ui.components.EmptyState
import com.example.pocketbudget.ui.components.LabeledAmount
import com.example.pocketbudget.ui.components.SectionTitle
import com.example.pocketbudget.ui.components.color
import com.example.pocketbudget.ui.formatMoney
import com.example.pocketbudget.ui.toInputText

@Composable
fun BudgetScreen(
    monthlyLimit: Double?,
    status: BudgetStatus?,
    onSaveLimit: (String) -> String?,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    var limitText by rememberSaveable { mutableStateOf(monthlyLimit?.toInputText().orEmpty()) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    fun save(text: String) {
        error = onSaveLimit(text)
        if (error == null) focusManager.clearFocus()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (status == null) {
            EmptyState("Set a monthly limit to see how much you can spend per day.")
        } else {
            BudgetCard(status)
            Card(Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    LabeledAmount("Remaining", status.remaining.coerceAtLeast(0.0), status.level.color)
                    LabeledAmount("Per day", status.dailyAllowance, MaterialTheme.colorScheme.primary)
                }
            }
            Text(
                text = adviceFor(status),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SectionTitle("Monthly limit")
        AmountField(
            value = limitText,
            onValueChange = {
                limitText = it
                error = null
            },
            label = "Limit for this month",
            error = error,
            onDone = { save(limitText) },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { save(limitText) }) { Text("Save limit") }
            if (monthlyLimit != null) {
                OutlinedButton(
                    onClick = {
                        limitText = ""
                        save("")
                    },
                ) { Text("Remove limit") }
            }
        }
    }
}

private fun adviceFor(status: BudgetStatus): String = when (status.level) {
    BudgetLevel.OK -> "You are on track. Keep daily spending under ${formatMoney(status.dailyAllowance)}."
    BudgetLevel.WARNING -> "Careful: more than 80% of the budget is already spent."
    BudgetLevel.EXCEEDED -> "The budget is exceeded. Try to postpone non-essential purchases."
}
