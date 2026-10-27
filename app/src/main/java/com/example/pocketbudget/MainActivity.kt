package com.example.pocketbudget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.pocketbudget.ui.navigation.PocketBudgetApp
import com.example.pocketbudget.ui.theme.PocketBudgetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PocketBudgetTheme {
                PocketBudgetApp()
            }
        }
    }
}
