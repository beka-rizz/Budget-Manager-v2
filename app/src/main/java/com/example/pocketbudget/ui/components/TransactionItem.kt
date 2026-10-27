package com.example.pocketbudget.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.pocketbudget.data.Category
import com.example.pocketbudget.data.Transaction
import com.example.pocketbudget.data.TransactionType
import com.example.pocketbudget.ui.formatDate
import com.example.pocketbudget.ui.formatMoney
import com.example.pocketbudget.ui.theme.ExpenseColor
import com.example.pocketbudget.ui.theme.IncomeColor
import com.example.pocketbudget.ui.theme.color

@Composable
fun CategoryIcon(
    category: Category,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(category.color.copy(alpha = 0.18f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = category.label.first().toString(),
            color = category.color,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isIncome = transaction.type == TransactionType.INCOME
    Card(onClick = onClick, modifier = modifier) {
        ListItem(
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            leadingContent = { CategoryIcon(transaction.category) },
            headlineContent = { Text(transaction.title, maxLines = 1) },
            supportingContent = {
                Text("${transaction.category.label} · ${formatDate(transaction.dateTime)}")
            },
            trailingContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = (if (isIncome) "+" else "−") + formatMoney(transaction.amount),
                        color = if (isIncome) IncomeColor else ExpenseColor,
                        fontWeight = FontWeight.SemiBold,
                    )
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete ${transaction.title}")
                    }
                }
            },
        )
    }
}
