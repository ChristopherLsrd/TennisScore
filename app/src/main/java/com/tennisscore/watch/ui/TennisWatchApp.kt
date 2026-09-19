package com.tennisscore.watch.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.tennisscore.watch.model.AppState
import com.tennisscore.watch.model.Screen
import com.tennisscore.watch.viewmodel.MatchViewModel

@Composable
fun TennisWatchApp(viewModel: MatchViewModel) {
    val state by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        ScreenSwitch(state = state, viewModel = viewModel)
    }
}

@Composable
private fun ScreenSwitch(state: AppState, viewModel: MatchViewModel) {
    when (state.screen) {
        Screen.SETUP -> SetupScreen(
            state = state,
            onNameAChange = viewModel::setNameA,
            onNameBChange = viewModel::setNameB,
            onPickServer = viewModel::setServerChoice,
            onOpenSettings = viewModel::openSettings,
            onStart = viewModel::startMatch
        )
        Screen.SETTINGS -> SettingsScreen(
            state = state,
            onSetsDelta = viewModel::incSets,
            onGamesDelta = viewModel::incGames,
            onPickRule = viewModel::setTiebreakRule,
            onPickLanguage = viewModel::setLanguage,
            onCancel = viewModel::cancelSettings,
            onSave = viewModel::saveSettings
        )
        Screen.LIVE -> LiveScreen(
            state = state,
            onAddPoint = viewModel::addPoint,
            onUndo = viewModel::undo,
            onSwapServe = viewModel::swapServe,
            onNewMatch = viewModel::newMatch
        )
    }
}
