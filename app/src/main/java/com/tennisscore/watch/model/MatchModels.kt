package com.tennisscore.watch.model

import kotlinx.serialization.Serializable

@Serializable
enum class Player { A, B }

@Serializable
enum class TiebreakRule { ADVANTAGE, TIEBREAK, SUPER_TIEBREAK }

@Serializable
enum class Screen { SETUP, SETTINGS, LIVE }

@Serializable
enum class AppLanguage { FR, EN }

@Serializable
data class SetResult(
    val a: Int,
    val b: Int,
    val isTiebreak: Boolean = false,
    val isSuperTiebreak: Boolean = false
)

@Serializable
data class MatchRules(
    val setsToWin: Int = 2,
    val gamesPerSet: Int = 6,
    val tiebreakRule: TiebreakRule = TiebreakRule.TIEBREAK
)

@Serializable
data class MatchSnapshot(
    val pointsA: Int,
    val pointsB: Int,
    val gamesA: Int,
    val gamesB: Int,
    val setsA: Int,
    val setsB: Int,
    val completedSets: List<SetResult>,
    val server: Player,
    val matchOver: Boolean,
    val winner: Player?,
    val tbA: Int,
    val tbB: Int,
    val inTiebreak: Boolean,
    val isSuperSet: Boolean
)

@Serializable
data class MatchState(
    val pointsA: Int = 0,
    val pointsB: Int = 0,
    val gamesA: Int = 0,
    val gamesB: Int = 0,
    val setsA: Int = 0,
    val setsB: Int = 0,
    val completedSets: List<SetResult> = emptyList(),
    val server: Player = Player.A,
    val matchOver: Boolean = false,
    val winner: Player? = null,
    val tbA: Int = 0,
    val tbB: Int = 0,
    val inTiebreak: Boolean = false,
    val isSuperSet: Boolean = false,
    val history: List<MatchSnapshot> = emptyList()
) {
    fun toSnapshot() = MatchSnapshot(
        pointsA, pointsB, gamesA, gamesB, setsA, setsB, completedSets,
        server, matchOver, winner, tbA, tbB, inTiebreak, isSuperSet
    )

    /** Resets score/progress for a new match, keeping who serves first. */
    fun reset(): MatchState = MatchState(server = server)
}

@Serializable
data class AppState(
    val screen: Screen = Screen.SETUP,
    val nameA: String = "",
    val nameB: String = "",
    val rules: MatchRules = MatchRules(),
    val language: AppLanguage = AppLanguage.FR,
    val match: MatchState = MatchState()
)
