package com.example.pocketbudget.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.pocketbudget.data.Category

val Green40 = Color(0xFF0F6E56)
val Green80 = Color(0xFF7FD8B8)
val GreenContainerLight = Color(0xFFCDEFE2)
val GreenContainerDark = Color(0xFF00513E)
val Slate40 = Color(0xFF4A635A)
val Slate80 = Color(0xFFB1CCC1)

val IncomeColor = Color(0xFF2E9D6A)
val ExpenseColor = Color(0xFFD9534F)
val WarningColor = Color(0xFFE6A23C)

val Category.color: Color
    get() = when (this) {
        Category.FOOD -> Color(0xFFEF8A3C)
        Category.TRANSPORT -> Color(0xFF3C8DEF)
        Category.SHOPPING -> Color(0xFFB45AD6)
        Category.BILLS -> Color(0xFFE05A6B)
        Category.FUN -> Color(0xFFF2C230)
        Category.STUDY -> Color(0xFF4B6CD1)
        Category.SALARY -> Color(0xFF2E9D6A)
        Category.OTHER -> Color(0xFF8A8F98)
    }
