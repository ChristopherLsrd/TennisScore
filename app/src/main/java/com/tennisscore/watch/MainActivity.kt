package com.tennisscore.watch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.tennisscore.watch.ui.TennisWatchApp
import com.tennisscore.watch.viewmodel.MatchViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<com.tennisscore.watch.viewmodel.MatchViewModel> {
        MatchViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TennisWatchApp(viewModel = viewModel)
        }
    }
}
