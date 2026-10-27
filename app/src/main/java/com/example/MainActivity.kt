package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.CallSaverViewModel
import com.example.ui.MainScreen
import com.example.ui.theme.AutoContactSaverTheme

class MainActivity : ComponentActivity() {

    private val viewModel: CallSaverViewModel by viewModels {
        val app = application as AutoContactSaverApp
        CallSaverViewModel.Factory(
            repository = app.repository,
            appSettings = app.appSettings,
            context = applicationContext
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AutoContactSaverTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
