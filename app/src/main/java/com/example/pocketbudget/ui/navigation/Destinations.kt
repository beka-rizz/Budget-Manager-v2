package com.example.pocketbudget.ui.navigation

import androidx.annotation.DrawableRes
import com.example.pocketbudget.R

object Routes {
    const val TRANSACTIONS = "transactions"
    const val STATISTICS = "statistics"
    const val BUDGET = "budget"

    const val ARG_ID = "id"
    const val NEW_ID = -1L
    const val EDITOR = "editor?$ARG_ID={$ARG_ID}"

    fun editor(id: Long = NEW_ID) = "editor?$ARG_ID=$id"
}

enum class TopLevelDestination(
    val route: String,
    val label: String,
    @param:DrawableRes val icon: Int,
) {
    TRANSACTIONS(Routes.TRANSACTIONS, "Transactions", R.drawable.ic_list),
    STATISTICS(Routes.STATISTICS, "Statistics", R.drawable.ic_chart),
    BUDGET(Routes.BUDGET, "Budget", R.drawable.ic_wallet),
}
