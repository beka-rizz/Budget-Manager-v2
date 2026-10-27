package com.example.pocketbudget.ui.navigation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pocketbudget.ui.BudgetViewModel
import com.example.pocketbudget.ui.screens.BudgetScreen
import com.example.pocketbudget.ui.screens.EditorScreen
import com.example.pocketbudget.ui.screens.StatisticsScreen
import com.example.pocketbudget.ui.screens.TransactionsScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PocketBudgetApp(
    viewModel: BudgetViewModel = viewModel(),
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val currentTab = TopLevelDestination.entries.firstOrNull { it.route == currentRoute }
    val isNewTransaction = backStackEntry?.arguments?.getLong(Routes.ARG_ID, Routes.NEW_ID) == Routes.NEW_ID

    fun showMessage(message: String) {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        when {
                            currentTab != null -> currentTab.label
                            isNewTransaction -> "New transaction"
                            else -> "Edit transaction"
                        },
                    )
                },
                navigationIcon = {
                    if (currentTab == null) {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (currentTab != null) {
                NavigationBar {
                    TopLevelDestination.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = destination == currentTab,
                            onClick = { navController.navigateToTab(destination) },
                            icon = { Icon(painterResource(destination.icon), contentDescription = null) },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (currentTab == TopLevelDestination.TRANSACTIONS) {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate(Routes.editor()) },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Add") },
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.TRANSACTIONS,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            composable(Routes.TRANSACTIONS) {
                TransactionsScreen(
                    state = uiState,
                    searchQuery = searchQuery,
                    onSearchQueryChange = viewModel::onSearchQueryChange,
                    onCategorySelected = viewModel::onCategorySelected,
                    onClearFilters = viewModel::clearFilters,
                    onTransactionClick = { id -> navController.navigate(Routes.editor(id)) },
                    onDeleteTransaction = { id ->
                        val removed = viewModel.deleteTransaction(id) ?: return@TransactionsScreen
                        scope.launch {
                            snackbarHostState.currentSnackbarData?.dismiss()
                            val result = snackbarHostState.showSnackbar(
                                message = "\"${removed.title}\" deleted",
                                actionLabel = "Undo",
                                duration = SnackbarDuration.Long,
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                viewModel.restoreTransaction(removed)
                            }
                        }
                    },
                    onBudgetClick = { navController.navigateToTab(TopLevelDestination.BUDGET) },
                )
            }

            composable(Routes.STATISTICS) {
                StatisticsScreen(transactions = uiState.allTransactions)
            }

            composable(Routes.BUDGET) {
                BudgetScreen(
                    monthlyLimit = uiState.monthlyLimit,
                    status = uiState.budget,
                    onSaveLimit = { text ->
                        viewModel.setMonthlyLimit(text).also { error ->
                            if (error == null) showMessage(if (text.isBlank()) "Limit removed" else "Limit saved")
                        }
                    },
                )
            }

            composable(
                route = Routes.EDITOR,
                arguments = listOf(
                    navArgument(Routes.ARG_ID) {
                        type = NavType.LongType
                        defaultValue = Routes.NEW_ID
                    },
                ),
            ) { entry ->
                val id = entry.arguments?.getLong(Routes.ARG_ID, Routes.NEW_ID) ?: Routes.NEW_ID
                val transaction = remember(id) { viewModel.getTransaction(id) }
                EditorScreen(
                    transaction = transaction,
                    onSave = { title, amount, type, category ->
                        viewModel.saveTransaction(transaction?.id, title, amount, type, category)
                    },
                    onSaved = {
                        navController.popBackStack()
                        showMessage(if (transaction == null) "Transaction added" else "Changes saved")
                    },
                )
            }
        }
    }
}

private fun NavHostController.navigateToTab(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
