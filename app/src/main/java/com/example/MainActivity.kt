package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.ui.screens.MainAppContainer
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.EVViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Support full screen bleed under system bars
        enableEdgeToEdge()
        
        // Instantiate ViewModel with classic robust provider pattern (Hilt-independent)
        val viewModel = ViewModelProvider(
            this, 
            EVViewModel.Factory(application)
        )[EVViewModel::class.java]

        setContent {
            MyApplicationTheme {
                MainAppContainer(viewModel = viewModel)
            }
        }
    }
}
