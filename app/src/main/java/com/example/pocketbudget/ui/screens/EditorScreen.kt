package com.example.pocketbudget.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.pocketbudget.data.Category
import com.example.pocketbudget.data.Transaction
import com.example.pocketbudget.data.TransactionType
import com.example.pocketbudget.domain.ValidationResult
import com.example.pocketbudget.ui.components.AmountField
import com.example.pocketbudget.ui.components.CategoryChipRow
import com.example.pocketbudget.ui.components.SectionTitle
import com.example.pocketbudget.ui.components.SegmentedSelector
import com.example.pocketbudget.ui.toInputText

/**
 * Add / edit form. Field values live in rememberSaveable, so typed text survives rotation;
 * validation and saving are delegated to the ViewModel through [onSave].
 */
@Composable
fun EditorScreen(
    transaction: Transaction?,
    onSave: (title: String, amountText: String, type: TransactionType, category: Category) -> ValidationResult,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var title by rememberSaveable { mutableStateOf(transaction?.title.orEmpty()) }
    var amountText by rememberSaveable { mutableStateOf(transaction?.amount?.toInputText().orEmpty()) }
    var type by rememberSaveable { mutableStateOf(transaction?.type ?: TransactionType.EXPENSE) }
    var category by rememberSaveable { mutableStateOf(transaction?.category ?: Category.FOOD) }
    var titleError by rememberSaveable { mutableStateOf<String?>(null) }
    var amountError by rememberSaveable { mutableStateOf<String?>(null) }

    fun save() {
        val result = onSave(title, amountText, type, category)
        titleError = result.titleError
        amountError = result.amountError
        if (result.isValid) onSaved()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SegmentedSelector(
            options = TransactionType.entries,
            selected = type,
            label = { it.label },
            onSelect = { selected ->
                type = selected
                if (selected == TransactionType.INCOME && transaction == null) category = Category.SALARY
            },
        )

        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
                titleError = null
            },
            label = { Text("Title") },
            singleLine = true,
            isError = titleError != null,
            supportingText = titleError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        AmountField(
            value = amountText,
            onValueChange = {
                amountText = it
                amountError = null
            },
            label = "Amount",
            error = amountError,
            onDone = ::save,
        )

        SectionTitle("Category")
        CategoryChipRow(
            selected = category,
            onSelect = { selected -> selected?.let { category = it } },
        )

        Button(onClick = ::save, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text(if (transaction == null) "Add transaction" else "Save changes")
        }
    }
}
