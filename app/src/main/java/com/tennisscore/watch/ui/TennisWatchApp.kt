package com.tennisscore.watch.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.tennisscore.watch.model.Screen
import com.tennisscore.watch.viewmodel.MatchViewModel

@Composable
fun TennisWatchApp(viewModel: MatchViewModel) {
    val state by viewModel.uiState.collectAsState()

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
