package com.tennisscore.watch.data

import com.tennisscore.watch.model.AppLanguage
import com.tennisscore.watch.model.AppState
import com.tennisscore.watch.model.MatchRules
import com.tennisscore.watch.model.MatchState
import com.tennisscore.watch.model.Player
import com.tennisscore.watch.model.Screen
import com.tennisscore.watch.model.SetResult
import com.tennisscore.watch.model.TiebreakRule
import org.junit.Assert.assertEquals
import org.junit.Test

class AppStateJsonTest {

    @Test
    fun `round-trips a default AppState`() {
        val state = AppState()
        val json = AppStateJson.encode(state)
        assertEquals(state, AppStateJson.decode(json))
    }

    @Test
    fun `round-trips an AppState with an in-progress match and history`() {
        val state = AppState(
            screen = Screen.LIVE,
            nameA = "Alice",
            nameB = "Bob",
            rules = MatchRules(setsToWin = 3, gamesPerSet = 4, tiebreakRule = TiebreakRule.SUPER_TIEBREAK),
            language = AppLanguage.EN,
            match = MatchState(
                pointsA = 2, pointsB = 1, gamesA = 3, gamesB = 2, setsA = 1, setsB = 0,
                completedSets = listOf(SetResult(6, 4)),
                server = Player.B,
                history = listOf(
                    com.tennisscore.watch.model.MatchSnapshot(
                        1, 1, 3, 2, 1, 0, listOf(SetResult(6, 4)), Player.A, false, null, 0, 0, false, false
                    )
                )
            )
        )
        val json = AppStateJson.encode(state)
        assertEquals(state, AppStateJson.decode(json))
    }

    @Test(expected = Exception::class)
    fun `decoding garbage throws rather than silently returning a default`() {
        AppStateJson.decode("not valid json")
    }
}
