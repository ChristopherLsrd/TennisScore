package com.tennisscore.watch.viewmodel

import com.tennisscore.watch.data.LocaleApplier
import com.tennisscore.watch.data.MatchRepository
import com.tennisscore.watch.model.AppLanguage
import com.tennisscore.watch.model.AppState
import com.tennisscore.watch.model.Player
import com.tennisscore.watch.model.Screen
import com.tennisscore.watch.model.TiebreakRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FakeMatchRepository(private var stored: AppState = AppState()) : MatchRepository {
    var saveCount = 0
        private set

    override suspend fun load(): AppState = stored

    override suspend fun save(state: AppState) {
        stored = state
        saveCount++
    }
}

class FakeLocaleApplier : LocaleApplier {
    var lastApplied: AppLanguage? = null
    override fun apply(language: AppLanguage) {
        lastApplied = language
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MatchViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `starts on Setup with a fresh repository`() = runTest {
        val vm = MatchViewModel(FakeMatchRepository(), saveDebounceMs = 0)
        assertEquals(Screen.SETUP, vm.uiState.value.screen)
    }

    @Test
    fun `restores an in-progress unfinished match directly to Live`() = runTest {
        val saved = AppState(screen = Screen.LIVE, match = com.tennisscore.watch.model.MatchState(pointsA = 2))
        val vm = MatchViewModel(FakeMatchRepository(saved), saveDebounceMs = 0)
        assertEquals(Screen.LIVE, vm.uiState.value.screen)
        assertEquals(2, vm.uiState.value.match.pointsA)
    }

    @Test
    fun `restoring a completed match forces Setup instead of Live`() = runTest {
        val saved = AppState(
            screen = Screen.LIVE,
            match = com.tennisscore.watch.model.MatchState(matchOver = true, winner = Player.A)
        )
        val vm = MatchViewModel(FakeMatchRepository(saved), saveDebounceMs = 0)
        assertEquals(Screen.SETUP, vm.uiState.value.screen)
    }

    @Test
    fun `startMatch moves to Live`() = runTest {
        val vm = MatchViewModel(FakeMatchRepository(), saveDebounceMs = 0)
        vm.startMatch()
        assertEquals(Screen.LIVE, vm.uiState.value.screen)
    }

    @Test
    fun `addPoint delegates to ScoreEngine`() = runTest {
        val vm = MatchViewModel(FakeMatchRepository(), saveDebounceMs = 0)
        vm.startMatch()
        vm.addPoint(Player.A)
        assertEquals(1, vm.uiState.value.match.pointsA)
    }

    @Test
    fun `undo delegates to ScoreEngine`() = runTest {
        val vm = MatchViewModel(FakeMatchRepository(), saveDebounceMs = 0)
        vm.startMatch()
        vm.addPoint(Player.A)
        vm.undo()
        assertEquals(0, vm.uiState.value.match.pointsA)
    }

    @Test
    fun `newMatch resets the score but keeps the server`() = runTest {
        val vm = MatchViewModel(FakeMatchRepository(), saveDebounceMs = 0)
        vm.setServerChoice(Player.B)
        vm.startMatch()
        vm.addPoint(Player.A)
        vm.newMatch()
        assertEquals(0, vm.uiState.value.match.pointsA)
        assertEquals(Player.B, vm.uiState.value.match.server)
        assertEquals(Screen.SETUP, vm.uiState.value.screen)
    }

    @Test
    fun `cancelSettings reverts rules and language changed while in Settings`() = runTest {
        val vm = MatchViewModel(FakeMatchRepository(), saveDebounceMs = 0)
        vm.openSettings()
        vm.incSets(1)
        vm.setLanguage(AppLanguage.EN)
        vm.cancelSettings()
        assertEquals(2, vm.uiState.value.rules.setsToWin)
        assertEquals(AppLanguage.FR, vm.uiState.value.language)
        assertEquals(Screen.SETUP, vm.uiState.value.screen)
    }

    @Test
    fun `saveSettings keeps changes made in Settings`() = runTest {
        val vm = MatchViewModel(FakeMatchRepository(), saveDebounceMs = 0)
        vm.openSettings()
        vm.incGames(1)
        vm.setTiebreakRule(TiebreakRule.SUPER_TIEBREAK)
        vm.saveSettings()
        assertEquals(7, vm.uiState.value.rules.gamesPerSet)
        assertEquals(TiebreakRule.SUPER_TIEBREAK, vm.uiState.value.rules.tiebreakRule)
        assertEquals(Screen.SETUP, vm.uiState.value.screen)
    }

    @Test
    fun `incSets and incGames are clamped to their bounds`() = runTest {
        val vm = MatchViewModel(FakeMatchRepository(), saveDebounceMs = 0)
        repeat(10) { vm.incSets(1) }
        assertEquals(3, vm.uiState.value.rules.setsToWin)
        repeat(10) { vm.incSets(-1) }
        assertEquals(1, vm.uiState.value.rules.setsToWin)
        repeat(10) { vm.incGames(1) }
        assertEquals(9, vm.uiState.value.rules.gamesPerSet)
        repeat(10) { vm.incGames(-1) }
        assertEquals(3, vm.uiState.value.rules.gamesPerSet)
    }

    @Test
    fun `saveSettings applies the chosen language`() = runTest {
        val localeApplier = FakeLocaleApplier()
        val vm = MatchViewModel(FakeMatchRepository(), saveDebounceMs = 0, localeApplier = localeApplier)
        vm.openSettings()
        vm.setLanguage(AppLanguage.EN)
        vm.saveSettings()
        assertEquals(AppLanguage.EN, localeApplier.lastApplied)
    }

    @Test
    fun `restoring a saved state applies its persisted language on startup`() = runTest {
        val saved = AppState(language = AppLanguage.EN)
        val localeApplier = FakeLocaleApplier()
        val vm = MatchViewModel(FakeMatchRepository(saved), saveDebounceMs = 0, localeApplier = localeApplier)
        assertEquals(AppLanguage.EN, localeApplier.lastApplied)
    }

    @Test
    fun `startMatch resets a finished match instead of reopening the complete overlay`() = runTest {
        val saved = AppState(
            match = com.tennisscore.watch.model.MatchState(matchOver = true, winner = Player.A, setsA = 2)
        )
        val vm = MatchViewModel(FakeMatchRepository(saved), saveDebounceMs = 0)
        vm.startMatch()
        assertEquals(Screen.LIVE, vm.uiState.value.screen)
        assertEquals(false, vm.uiState.value.match.matchOver)
        assertEquals(0, vm.uiState.value.match.setsA)
    }
}
