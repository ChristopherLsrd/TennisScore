package com.tennisscore.watch

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.tennisscore.watch.ui.TennisWatchApp
import com.tennisscore.watch.viewmodel.MatchViewModelFactory

// AppCompatActivity (not ComponentActivity) so AppCompatDelegate.setApplicationLocales can apply the in-app language.
class MainActivity : AppCompatActivity() {

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
