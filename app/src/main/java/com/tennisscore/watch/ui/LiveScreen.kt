package com.tennisscore.watch.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Text
import com.tennisscore.watch.R
import com.tennisscore.watch.engine.ScoreEngine
import com.tennisscore.watch.model.AppState
import com.tennisscore.watch.model.MatchState
import com.tennisscore.watch.model.Player

@Composable
fun LiveScreen(
    state: AppState,
    onAddPoint: (Player) -> Unit,
    onUndo: () -> Unit,
    onSwapServe: () -> Unit,
    onNewMatch: () -> Unit
) {
    val ds = rememberDesignScale()
    val match = state.match
    var confirmLeave by remember { mutableStateOf(false) }

    // Keep the watch awake while a match is being scored; release it once the match is over.
    val view = LocalView.current
    DisposableEffect(view, match.matchOver) {
        view.keepScreenOn = !match.matchOver
        onDispose { view.keepScreenOn = false }
    }
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = ds.dp(46), end = ds.dp(46), top = ds.dp(33), bottom = ds.dp(50).coerceAtLeast(22.dp)),
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
                horizontalArrangement = Arrangement.spacedBy(ds.dp(24).coerceAtLeast(8.dp), Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircleButton("↺", onClick = onUndo, size = 50f, glyphSize = 32f, background = 0.08f, minSize = 34.dp, enabled = match.history.isNotEmpty())
                CircleButton("⇄", onClick = onSwapServe, size = 50f, glyphSize = 32f, background = 0.08f, minSize = 34.dp)
                CircleButton("⌂", onClick = { confirmLeave = true }, size = 50f, glyphSize = 32f, background = 0.08f, minSize = 34.dp)
            }
        }

        if (match.matchOver) {
            MatchCompleteOverlay(state = state, onNewMatch = onNewMatch)
        } else if (confirmLeave) {
            LeaveMatchOverlay(
                onConfirm = {
                    confirmLeave = false
                    onNewMatch()
                },
                onDismiss = { confirmLeave = false }
            )
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
    val ds = rememberDesignScale()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(ds.dp(14)))
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp, vertical = ds.dp(8).coerceAtLeast(3.dp)),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.widthIn(max = ds.dp(145).coerceAtLeast(56.dp)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ds.dp(6).coerceAtLeast(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .size(ds.dp(9).coerceAtLeast(5.dp))
                    .clip(CircleShape)
                    .background(if (isServing) Accent else Color.Transparent)
            )
            Text(
                text = name.uppercase(),
                fontSize = ds.sp(15f),
                fontWeight = FontWeight.SemiBold,
                letterSpacing = ds.spacing(0.5f),
                color = muted(0.82f),
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = label,
            fontFamily = ScoreFont,
            fontWeight = FontWeight.Bold,
            fontSize = ds.sp(53f),
            color = Color.White,
            textAlign = TextAlign.End,
            modifier = Modifier.widthIn(min = ds.dp(68))
        )
    }
}

@Composable
private fun MidLine(state: AppState) {
    val ds = rememberDesignScale()
    val match = state.match
    val inBreaker = match.isSuperSet || match.inTiebreak
    val label = when {
        match.isSuperSet -> stringResource(R.string.super_tiebreak_live)
        match.inTiebreak -> stringResource(R.string.tiebreak_live)
        else -> "${stringResource(R.string.games_label)} ${match.gamesA} – ${match.gamesB}"
    }
    val played = if (inBreaker) match.tbA + match.tbB else match.pointsA + match.pointsB
    val side = if (played % 2 == 0) stringResource(R.string.serve_right) else stringResource(R.string.serve_left)

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ds.dp(8).coerceAtLeast(4.dp))
    ) {
        Box(modifier = Modifier.weight(1f).height(1.dp).background(muted(0.12f)))
        Text(
            text = "$label · $side",
            fontSize = ds.sp(12f),
            letterSpacing = ds.spacing(1.2f),
            color = muted(0.4f),
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Box(modifier = Modifier.weight(1f).height(1.dp).background(muted(0.12f)))
    }
}

@Composable
private fun SetDots(match: MatchState, setsToWin: Int) {
    val ds = rememberDesignScale()
    val dotsCount = setsToWin * 2 - 1
    Row(
        modifier = Modifier.fillMaxWidth().height(ds.dp(13).coerceAtLeast(8.dp)),
        horizontalArrangement = Arrangement.spacedBy(ds.dp(8).coerceAtLeast(4.dp), Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until dotsCount) {
            val (fill, border) = when {
                i < match.completedSets.size -> {
                    val set = match.completedSets[i]
                    val c = if (set.a > set.b) Accent else Color.White
                    c to c
                }
                i == match.completedSets.size && !match.matchOver -> Color.Transparent to Accent
                else -> Color.Transparent to muted(0.25f)
            }
            Box(
                modifier = Modifier
                    .size(ds.dp(8).coerceAtLeast(6.dp))
                    .clip(CircleShape)
                    .background(fill)
                    .border(2.dp, border, CircleShape)
            )
        }
    }
}

@Composable
private fun MatchCompleteOverlay(state: AppState, onNewMatch: () -> Unit) {
    val ds = rememberDesignScale()
    val winnerName = when (state.match.winner) {
        Player.A -> state.nameA.ifBlank { stringResource(R.string.player1) }
        Player.B -> state.nameB.ifBlank { stringResource(R.string.player2) }
        null -> ""
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.92f))
            .clickable(enabled = false, onClick = {}),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(horizontal = ds.dp(40).coerceAtLeast(24.dp)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ds.dp(14).coerceAtLeast(6.dp))
        ) {
            Caption(stringResource(R.string.match_complete), size = 13f, letterSpacing = 2.5f, alpha = 0.45f, weight = FontWeight.SemiBold)
            Text(
                text = "$winnerName ${stringResource(R.string.wins)}",
                fontFamily = ScoreFont,
                fontWeight = FontWeight.Bold,
                fontSize = ds.sp(26f),
                color = Accent,
                textAlign = TextAlign.Center
            )
            Box(modifier = Modifier.padding(top = ds.dp(7))) {
                AccentButton(stringResource(R.string.new_match_btn), onClick = onNewMatch, fontSize = 13f, hPad = 24f, vPad = 9f, radius = 18f)
            }
        }
    }
}

@Composable
private fun LeaveMatchOverlay(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val ds = rememberDesignScale()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.92f))
            .clickable(enabled = false, onClick = {}),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(horizontal = ds.dp(50).coerceAtLeast(24.dp)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ds.dp(14).coerceAtLeast(6.dp))
        ) {
            Caption(stringResource(R.string.leave_match_title), size = 15f, letterSpacing = 2f, alpha = 0.7f, weight = FontWeight.SemiBold)
            Text(
                text = stringResource(R.string.leave_match_message),
                fontSize = ds.sp(15f),
                color = muted(0.5f),
                textAlign = TextAlign.Center
            )
            // Intrinsic-size row + equal weights + fillMaxHeight: both buttons match the larger one.
            Row(
                modifier = Modifier.padding(top = ds.dp(7)).width(IntrinsicSize.Max).height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(ds.dp(10).coerceAtLeast(6.dp))
            ) {
                OutlineButton(stringResource(R.string.cancel), onClick = onDismiss, fontSize = 14f, hPad = 22f, vPad = 11f, radius = 20f, modifier = Modifier.weight(1f).fillMaxHeight())
                AccentButton(stringResource(R.string.leave), onClick = onConfirm, fontSize = 14f, hPad = 22f, vPad = 11f, radius = 20f, modifier = Modifier.weight(1f).fillMaxHeight())
            }
        }
    }
}
