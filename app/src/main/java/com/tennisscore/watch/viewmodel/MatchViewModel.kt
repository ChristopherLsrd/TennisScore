package com.tennisscore.watch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tennisscore.watch.data.AppCompatLocaleApplier
import com.tennisscore.watch.data.LocaleApplier
import com.tennisscore.watch.data.MatchRepository
import com.tennisscore.watch.engine.ScoreEngine
import com.tennisscore.watch.model.AppLanguage
import com.tennisscore.watch.model.AppState
import com.tennisscore.watch.model.MatchRules
import com.tennisscore.watch.model.Player
import com.tennisscore.watch.model.Screen
import com.tennisscore.watch.model.TiebreakRule
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private data class SettingsDraft(val rules: MatchRules, val language: AppLanguage)

class MatchViewModel(
    private val repository: MatchRepository,
    private val saveDebounceMs: Long = 300L,
    private val localeApplier: LocaleApplier = AppCompatLocaleApplier
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppState())
    val uiState: StateFlow<AppState> = _uiState.asStateFlow()

    private var settingsDraft: SettingsDraft? = null
    private var saveJob: Job? = null

    init {
        viewModelScope.launch {
            val restored = repository.load()
            val resolved = if (restored.screen == Screen.LIVE && !restored.match.matchOver) {
                restored
            } else {
                restored.copy(screen = Screen.SETUP)
            }
            _uiState.value = resolved
            localeApplier.apply(resolved.language)
        }
    }

    private fun update(block: (AppState) -> AppState) {
        _uiState.update(block)
        schedulePersist()
    }

    private fun schedulePersist() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(saveDebounceMs)
            repository.save(_uiState.value)
        }
    }

    fun setNameA(value: String) = update { it.copy(nameA = value) }
    fun setNameB(value: String) = update { it.copy(nameB = value) }
    fun setServerChoice(player: Player) = update { it.copy(match = it.match.copy(server = player)) }

    fun openSettings() {
        settingsDraft = SettingsDraft(_uiState.value.rules, _uiState.value.language)
        _uiState.update { it.copy(screen = Screen.SETTINGS) }
    }

    fun cancelSettings() {
        val draft = settingsDraft
        update {
            it.copy(
                screen = Screen.SETUP,
                rules = draft?.rules ?: it.rules,
                language = draft?.language ?: it.language
            )
        }
    }

    fun saveSettings() {
        update { it.copy(screen = Screen.SETUP) }
        localeApplier.apply(_uiState.value.language)
    }

    fun incSets(delta: Int) = update {
        it.copy(rules = it.rules.copy(setsToWin = (it.rules.setsToWin + delta).coerceIn(1, 3)))
    }

    fun incGames(delta: Int) = update {
        it.copy(rules = it.rules.copy(gamesPerSet = (it.rules.gamesPerSet + delta).coerceIn(3, 9)))
    }

    fun setTiebreakRule(rule: TiebreakRule) = update { it.copy(rules = it.rules.copy(tiebreakRule = rule)) }
    fun setLanguage(language: AppLanguage) = update { it.copy(language = language) }

    fun startMatch() = update {
        it.copy(screen = Screen.LIVE, match = if (it.match.matchOver) it.match.reset() else it.match)
    }
    fun newMatch() = update { it.copy(screen = Screen.SETUP, match = it.match.reset()) }
    fun swapServe() = update {
        it.copy(match = it.match.copy(server = if (it.match.server == Player.A) Player.B else Player.A))
    }

    fun addPoint(player: Player) = update { it.copy(match = ScoreEngine.addPoint(it.match, player, it.rules)) }
    fun undo() = update { it.copy(match = ScoreEngine.undo(it.match)) }
}
