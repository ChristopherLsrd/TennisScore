package com.tennisscore.watch.engine

import com.tennisscore.watch.model.MatchRules
import com.tennisscore.watch.model.MatchState
import com.tennisscore.watch.model.Player
import com.tennisscore.watch.model.SetResult
import com.tennisscore.watch.model.TiebreakRule

object ScoreEngine {

    fun pointLabel(mine: Int, theirs: Int): String {
        if (mine >= 3 && theirs >= 3) {
            return when {
                mine == theirs -> "40"
                mine > theirs -> "AD"
                else -> "40"
            }
        }
        val labels = listOf("0", "15", "30", "40")
        return labels[minOf(mine, 3)]
    }

    private fun wonBy(mine: Int, theirs: Int, target: Int) = mine >= target && mine - theirs >= 2

    fun addPoint(state: MatchState, player: Player, rules: MatchRules): MatchState {
        if (state.matchOver) return state
        val snapshot = state.toSnapshot()

        var pa = state.pointsA
        var pb = state.pointsB
        var tbA = state.tbA
        var tbB = state.tbB
        var gamesA = state.gamesA
        var gamesB = state.gamesB
        var setsA = state.setsA
        var setsB = state.setsB
        var completedSets = state.completedSets
        var server = state.server
        var matchOver = false
        var winner: Player? = null
        var inTiebreak = state.inTiebreak
        var isSuperSet = state.isSuperSet

        fun finishSet(setWinner: Player, entry: SetResult) {
            completedSets = completedSets + entry
            if (setWinner == Player.A) setsA++ else setsB++
            gamesA = 0; gamesB = 0; tbA = 0; tbB = 0; inTiebreak = false
            if (setsA >= rules.setsToWin) {
                matchOver = true; winner = Player.A
            } else if (setsB >= rules.setsToWin) {
                matchOver = true; winner = Player.B
            }
            isSuperSet = !matchOver &&
                rules.tiebreakRule == TiebreakRule.SUPER_TIEBREAK &&
                (setsA + setsB) == (rules.setsToWin * 2 - 2)
        }

        when {
            isSuperSet -> {
                if (player == Player.A) tbA++ else tbB++
                if (wonBy(tbA, tbB, 10) || wonBy(tbB, tbA, 10)) {
                    finishSet(
                        if (tbA > tbB) Player.A else Player.B,
                        SetResult(tbA, tbB, isSuperTiebreak = true)
                    )
                }
            }
            inTiebreak -> {
                if (player == Player.A) tbA++ else tbB++
                if (wonBy(tbA, tbB, 7) || wonBy(tbB, tbA, 7)) {
                    val gameWinner = if (tbA > tbB) Player.A else Player.B
                    if (gameWinner == Player.A) gamesA++ else gamesB++
                    server = if (server == Player.A) Player.B else Player.A
                    finishSet(
                        if (gamesA > gamesB) Player.A else Player.B,
                        SetResult(gamesA, gamesB, isTiebreak = true)
                    )
                }
            }
            else -> {
                if (player == Player.A) pa++ else pb++
                if (wonBy(pa, pb, 4) || wonBy(pb, pa, 4)) {
                    val gameWinner = if (pa > pb) Player.A else Player.B
                    pa = 0; pb = 0
                    if (gameWinner == Player.A) gamesA++ else gamesB++
                    server = if (server == Player.A) Player.B else Player.A
                    if (rules.tiebreakRule != TiebreakRule.ADVANTAGE &&
                        gamesA == rules.gamesPerSet && gamesB == rules.gamesPerSet
                    ) {
                        inTiebreak = true; tbA = 0; tbB = 0
                    } else if (wonBy(gamesA, gamesB, rules.gamesPerSet) || wonBy(gamesB, gamesA, rules.gamesPerSet)) {
                        finishSet(if (gamesA > gamesB) Player.A else Player.B, SetResult(gamesA, gamesB))
                    }
                }
            }
        }

        return state.copy(
            pointsA = pa, pointsB = pb, gamesA = gamesA, gamesB = gamesB,
            setsA = setsA, setsB = setsB, completedSets = completedSets,
            server = server, matchOver = matchOver, winner = winner,
            tbA = tbA, tbB = tbB, inTiebreak = inTiebreak, isSuperSet = isSuperSet,
            history = state.history + snapshot
        )
    }

    fun undo(state: MatchState): MatchState {
        if (state.history.isEmpty()) return state
        val last = state.history.last()
        return state.copy(
            pointsA = last.pointsA,
            pointsB = last.pointsB,
            gamesA = last.gamesA,
            gamesB = last.gamesB,
            setsA = last.setsA,
            setsB = last.setsB,
            completedSets = last.completedSets,
            server = last.server,
            matchOver = last.matchOver,
            winner = last.winner,
            tbA = last.tbA,
            tbB = last.tbB,
            inTiebreak = last.inTiebreak,
            isSuperSet = last.isSuperSet,
            history = state.history.dropLast(1)
        )
    }
}
