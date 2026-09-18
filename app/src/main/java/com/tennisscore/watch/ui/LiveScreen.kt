package com.tennisscore.watch.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Text
import com.tennisscore.watch.R
import com.tennisscore.watch.engine.ScoreEngine
import com.tennisscore.watch.model.AppState
import com.tennisscore.watch.model.MatchState
import com.tennisscore.watch.model.Player

private val ACCENT = Color(0xFFE0622F)

@Composable
fun LiveScreen(
    state: AppState,
    onAddPoint: (Player) -> Unit,
    onUndo: () -> Unit,
    onSwapServe: () -> Unit,
    onNewMatch: () -> Unit
) {
    val match = state.match
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            SetDots(match = match, setsToWin = state.rules.setsToWin)

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                PlayerRow(
                    name = state.nameA.ifBlank { stringResource(R.string.player1) },
                    label = labelFor(match, forPlayerA = true),
                    isServing = match.server == Player.A,
                    onClick = { onAddPoint(Player.A) }
                )
                MidLine(state = state)
                PlayerRow(
                    name = state.nameB.ifBlank { stringResource(R.string.player2) },
                    label = labelFor(match, forPlayerA = false),
                    isServing = match.server == Player.B,
                    onClick = { onAddPoint(Player.B) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(onClick = onUndo, enabled = match.history.isNotEmpty()) { Text("↺") }
                Button(onClick = onSwapServe) { Text("⇄") }
                Button(onClick = onNewMatch) { Text("⌂") }
            }
        }

        if (match.matchOver) {
            MatchCompleteOverlay(state = state, onNewMatch = onNewMatch)
        }
    }
}

private fun labelFor(match: MatchState, forPlayerA: Boolean): String {
    val inBreaker = match.isSuperSet || match.inTiebreak
    return if (inBreaker) {
        (if (forPlayerA) match.tbA else match.tbB).toString()
    } else if (forPlayerA) {
        ScoreEngine.pointLabel(match.pointsA, match.pointsB)
    } else {
        ScoreEngine.pointLabel(match.pointsB, match.pointsA)
    }
}

@Composable
private fun PlayerRow(name: String, label: String, isServing: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(if (isServing) ACCENT else Color.Transparent)
            )
            Text(text = name.uppercase(), fontSize = 12.sp)
        }
        Text(text = label, fontSize = 46.sp)
    }
}

@Composable
private fun MidLine(state: AppState) {
    val match = state.match
    val label = when {
        match.isSuperSet -> stringResource(R.string.super_tiebreak_live)
        match.inTiebreak -> stringResource(R.string.tiebreak_live)
        else -> "${stringResource(R.string.games_label)} ${match.gamesA} – ${match.gamesB}"
    }
    val side = if ((if (match.isSuperSet || match.inTiebreak) match.tbA + match.tbB else match.pointsA + match.pointsB) % 2 == 0) {
        stringResource(R.string.serve_right)
    } else {
        stringResource(R.string.serve_left)
    }
    Text(text = "$label · $side", fontSize = 9.sp)
}

@Composable
private fun SetDots(match: MatchState, setsToWin: Int) {
    val dotsCount = setsToWin * 2 - 1
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        for (i in 0 until dotsCount) {
            val color = when {
                i < match.completedSets.size -> {
                    val set = match.completedSets[i]
                    if (set.a > set.b) ACCENT else Color.White
                }
                i == match.completedSets.size && !match.matchOver -> Color.Transparent
                else -> Color.Transparent
            }
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
private fun MatchCompleteOverlay(state: AppState, onNewMatch: () -> Unit) {
    val winnerName = when (state.match.winner) {
        Player.A -> state.nameA.ifBlank { stringResource(R.string.player1) }
        Player.B -> state.nameB.ifBlank { stringResource(R.string.player2) }
        null -> ""
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.92f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = stringResource(R.string.match_complete), fontSize = 10.sp)
            Text(text = "$winnerName ${stringResource(R.string.wins)}", fontSize = 22.sp, color = ACCENT)
            Button(onClick = onNewMatch) { Text(stringResource(R.string.new_match_btn)) }
        }
    }
}
