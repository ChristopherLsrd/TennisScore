package com.tennisscore.watch.engine

import com.tennisscore.watch.model.MatchRules
import com.tennisscore.watch.model.MatchState
import com.tennisscore.watch.model.Player
import com.tennisscore.watch.model.TiebreakRule
import org.junit.Assert.assertEquals
import org.junit.Test

class ScoreEngineTest {

    private val defaultRules = MatchRules(setsToWin = 2, gamesPerSet = 6, tiebreakRule = TiebreakRule.TIEBREAK)

    @Test
    fun `pointLabel maps 0-15-30-40`() {
        assertEquals("0", ScoreEngine.pointLabel(0, 0))
        assertEquals("15", ScoreEngine.pointLabel(1, 0))
        assertEquals("30", ScoreEngine.pointLabel(2, 0))
        assertEquals("40", ScoreEngine.pointLabel(3, 0))
    }

    @Test
    fun `pointLabel returns 40 at deuce and AD for the leader`() {
        assertEquals("40", ScoreEngine.pointLabel(3, 3))
        assertEquals("AD", ScoreEngine.pointLabel(4, 3))
        assertEquals("40", ScoreEngine.pointLabel(3, 4))
    }

    @Test
    fun `winning a game from deuce via advantage increments games and swaps server`() {
        var state = MatchState(pointsA = 3, pointsB = 3, server = Player.A)
        state = ScoreEngine.addPoint(state, Player.A, defaultRules) // A takes AD
        assertEquals(4, state.pointsA)
        state = ScoreEngine.addPoint(state, Player.A, defaultRules) // A wins the game
        assertEquals(1, state.gamesA)
        assertEquals(0, state.pointsA)
        assertEquals(0, state.pointsB)
        assertEquals(Player.B, state.server)
    }

    @Test
    fun `winning a game straight to 4 points with a 2 point lead ends the game`() {
        var state = MatchState()
        repeat(4) { state = ScoreEngine.addPoint(state, Player.A, defaultRules) }
        assertEquals(1, state.gamesA)
        assertEquals(0, state.pointsA)
    }

    @Test
    fun `winning games per set with 2 game lead completes the set`() {
        var state = MatchState()
        repeat(6) { // A wins 6 games straight (0-0 -> 6-0)
            repeat(4) { state = ScoreEngine.addPoint(state, Player.A, defaultRules) }
        }
        assertEquals(1, state.setsA)
        assertEquals(0, state.gamesA)
        assertEquals(listOf(com.tennisscore.watch.model.SetResult(6, 0)), state.completedSets)
    }

    @Test
    fun `reaching gamesPerSet-gamesPerSet under TIEBREAK rule starts a tiebreak`() {
        var state = MatchState()
        // Alternate game wins to reach 6-6 without triggering a set win.
        repeat(6) {
            repeat(4) { state = ScoreEngine.addPoint(state, Player.A, defaultRules) }
            repeat(4) { state = ScoreEngine.addPoint(state, Player.B, defaultRules) }
        }
        assertEquals(6, state.gamesA)
        assertEquals(6, state.gamesB)
        assertTrueInTiebreak(state)
    }

    private fun assertTrueInTiebreak(state: MatchState) {
        org.junit.Assert.assertTrue(state.inTiebreak)
    }

    @Test
    fun `advantage rule never starts a tiebreak at gamesPerSet-gamesPerSet`() {
        val advantageRules = defaultRules.copy(tiebreakRule = TiebreakRule.ADVANTAGE)
        var state = MatchState()
        repeat(6) {
            repeat(4) { state = ScoreEngine.addPoint(state, Player.A, advantageRules) }
            repeat(4) { state = ScoreEngine.addPoint(state, Player.B, advantageRules) }
        }
        assertEquals(6, state.gamesA)
        assertEquals(6, state.gamesB)
        org.junit.Assert.assertFalse(state.inTiebreak)
    }

    @Test
    fun `winning a tiebreak 7-0 completes the set and swaps server`() {
        var state = MatchState(gamesA = 6, gamesB = 6, inTiebreak = true, server = Player.A)
        repeat(7) { state = ScoreEngine.addPoint(state, Player.A, defaultRules) }
        assertEquals(1, state.setsA)
        assertEquals(0, state.gamesA)
        assertEquals(0, state.gamesB)
        assertFalseInTiebreak(state)
        assertEquals(Player.B, state.server)
        assertEquals(listOf(com.tennisscore.watch.model.SetResult(7, 6, isTiebreak = true)), state.completedSets)
    }

    private fun assertFalseInTiebreak(state: MatchState) {
        org.junit.Assert.assertFalse(state.inTiebreak)
    }

    @Test
    fun `tiebreak requires a 2 point lead past 7`() {
        var state = MatchState(gamesA = 6, gamesB = 6, inTiebreak = true)
        repeat(7) { state = ScoreEngine.addPoint(state, Player.A, defaultRules) }
        state = ScoreEngine.addPoint(state, Player.B, defaultRules) // 7-1 -> not won yet at 7 pts each side alt; force 7-6 scenario instead
        // Rebuild a clean 7-6-then-9-7 scenario explicitly:
        var s2 = MatchState(gamesA = 6, gamesB = 6, inTiebreak = true)
        repeat(6) { s2 = ScoreEngine.addPoint(s2, Player.A, defaultRules) }
        repeat(6) { s2 = ScoreEngine.addPoint(s2, Player.B, defaultRules) }
        // 6-6 in the tiebreak: not over yet.
        org.junit.Assert.assertTrue(s2.inTiebreak)
        s2 = ScoreEngine.addPoint(s2, Player.A, defaultRules) // 7-6: still not a 2-point lead
        org.junit.Assert.assertTrue(s2.inTiebreak)
        s2 = ScoreEngine.addPoint(s2, Player.A, defaultRules) // 8-6: won
        org.junit.Assert.assertFalse(s2.inTiebreak)
        assertEquals(1, s2.setsA)
    }

    @Test
    fun `super tiebreak rule turns the deciding set into a first-to-10 tiebreak`() {
        val superRules = defaultRules.copy(setsToWin = 2, tiebreakRule = TiebreakRule.SUPER_TIEBREAK)
        var state = MatchState()
        // A wins the first set 6-0.
        repeat(6) { repeat(4) { state = ScoreEngine.addPoint(state, Player.A, superRules) } }
        assertEquals(1, state.setsA)
        // B wins the second set 6-0 -> now 1-1 in sets, the decider must be a super tiebreak.
        repeat(6) { repeat(4) { state = ScoreEngine.addPoint(state, Player.B, superRules) } }
        assertEquals(1, state.setsB)
        org.junit.Assert.assertTrue(state.isSuperSet)

        // Play the super tiebreak: A wins it 10-0.
        repeat(10) { state = ScoreEngine.addPoint(state, Player.A, superRules) }
        assertEquals(2, state.setsA)
        org.junit.Assert.assertTrue(state.matchOver)
        assertEquals(Player.A, state.winner)
        assertEquals(
            com.tennisscore.watch.model.SetResult(10, 0, isSuperTiebreak = true),
            state.completedSets.last()
        )
    }

    @Test
    fun `super tiebreak requires a 2 point lead past 10`() {
        val superRules = defaultRules.copy(setsToWin = 1, tiebreakRule = TiebreakRule.SUPER_TIEBREAK)
        // setsToWin=1 means the very first set is already the only/deciding set,
        // but SUPER_TIEBREAK only replaces a genuine decider (setsToWin*2-2 sets played),
        // so force isSuperSet directly to test the win condition in isolation.
        var state = MatchState(isSuperSet = true)
        repeat(9) { state = ScoreEngine.addPoint(state, Player.A, superRules) }
        repeat(9) { state = ScoreEngine.addPoint(state, Player.B, superRules) }
        org.junit.Assert.assertTrue(state.isSuperSet) // 9-9, not over
        state = ScoreEngine.addPoint(state, Player.A, superRules) // 10-9: no 2-point lead yet
        org.junit.Assert.assertTrue(state.isSuperSet)
        state = ScoreEngine.addPoint(state, Player.A, superRules) // 11-9: won
        org.junit.Assert.assertFalse(state.isSuperSet)
    }

    @Test
    fun `match ends once a player reaches setsToWin`() {
        val bestOfThree = defaultRules.copy(setsToWin = 2)
        var state = MatchState()
        repeat(2) { setIndex ->
            repeat(6) { repeat(4) { state = ScoreEngine.addPoint(state, Player.A, bestOfThree) } }
        }
        assertEquals(2, state.setsA)
        org.junit.Assert.assertTrue(state.matchOver)
        assertEquals(Player.A, state.winner)
    }

    @Test
    fun `addPoint is a no-op once the match is over`() {
        var state = MatchState(setsA = 2, matchOver = true, winner = Player.A)
        val result = ScoreEngine.addPoint(state, Player.B, defaultRules)
        assertEquals(state, result)
    }

    @Test
    fun `undo restores the exact previous state within a game`() {
        var state = MatchState()
        state = ScoreEngine.addPoint(state, Player.A, defaultRules) // 15-0
        val afterFirstPoint = state
        state = ScoreEngine.addPoint(state, Player.A, defaultRules) // 30-0
        state = ScoreEngine.undo(state)
        assertEquals(afterFirstPoint.pointsA, state.pointsA)
        assertEquals(afterFirstPoint.pointsB, state.pointsB)
    }

    @Test
    fun `undo restores state across a game-winning boundary`() {
        var state = MatchState()
        repeat(3) { state = ScoreEngine.addPoint(state, Player.A, defaultRules) } // 40-0
        val before4th = state
        state = ScoreEngine.addPoint(state, Player.A, defaultRules) // wins the game: gamesA=1, points reset
        assertEquals(1, state.gamesA)
        state = ScoreEngine.undo(state)
        assertEquals(before4th.gamesA, state.gamesA)
        assertEquals(before4th.pointsA, state.pointsA)
        assertEquals(before4th.server, state.server)
    }

    @Test
    fun `undo with empty history is a no-op`() {
        val state = MatchState()
        assertEquals(state, ScoreEngine.undo(state))
    }
}
