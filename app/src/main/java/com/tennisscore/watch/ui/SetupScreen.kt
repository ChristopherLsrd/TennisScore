package com.tennisscore.watch.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Text
import com.tennisscore.watch.R
import com.tennisscore.watch.model.AppState
import com.tennisscore.watch.model.Player
import com.tennisscore.watch.model.TiebreakRule

@Composable
fun SetupScreen(
    state: AppState,
    onNameAChange: (String) -> Unit,
    onNameBChange: (String) -> Unit,
    onPickServer: (Player) -> Unit,
    onOpenSettings: () -> Unit,
    onStart: () -> Unit
) {
    val ds = rememberDesignScale()
    Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(start = ds.dp(56), end = ds.dp(56), top = ds.dp(26), bottom = ds.dp(22)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ds.dp(9))
        ) {
            Caption(stringResource(R.string.new_match), size = 13f, letterSpacing = 2.5f, alpha = 0.4f, weight = FontWeight.SemiBold)

            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(ds.dp(7))) {
                NameField(state.nameA, stringResource(R.string.player1), onNameAChange)
                Caption("VS", size = 13f, letterSpacing = 1.3f, alpha = 0.3f)
                NameField(state.nameB, stringResource(R.string.player2), onNameBChange)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(ds.dp(5))) {
                Caption(stringResource(R.string.serving_first), size = 12f, letterSpacing = 2f, alpha = 0.35f)
                Row(horizontalArrangement = Arrangement.spacedBy(ds.dp(8).coerceAtLeast(6.dp))) {
                    Chip("P1", selected = state.match.server == Player.A, onClick = { onPickServer(Player.A) }, fontSize = 12f, hPad = 14f, vPad = 6f, radius = 15f)
                    Chip("P2", selected = state.match.server == Player.B, onClick = { onPickServer(Player.B) }, fontSize = 12f, hPad = 14f, vPad = 6f, radius = 15f)
                }
            }

            Row(modifier = Modifier.widthIn(max = 170.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(ds.dp(8).coerceAtLeast(6.dp))) {
                CircleButton("⚙", onClick = onOpenSettings, size = 27f, glyphSize = 14f, background = 0.14f)
                Text(
                    text = settingsSummary(state),
                    fontSize = ds.sp(12f),
                    letterSpacing = ds.spacing(0.6f),
                    color = muted(0.45f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }

            Box(modifier = Modifier.padding(top = ds.dp(4))) {
                AccentButton(stringResource(R.string.start_match), onClick = onStart, fontSize = 14f, hPad = 29f, vPad = 12f, radius = 20f)
            }
        }
    }
}

@Composable
private fun settingsSummary(state: AppState): String {
    val rule = when (state.rules.tiebreakRule) {
        TiebreakRule.ADVANTAGE -> stringResource(R.string.advantage)
        TiebreakRule.SUPER_TIEBREAK -> stringResource(R.string.super_tiebreak)
        TiebreakRule.TIEBREAK -> stringResource(R.string.tiebreak_at)
    }
    return "${state.rules.setsToWin} ${stringResource(R.string.sets_unit)} · " +
        "${state.rules.gamesPerSet} ${stringResource(R.string.games_unit)} · $rule"
}

@Composable
private fun NameField(value: String, placeholder: String, onValueChange: (String) -> Unit) {
    val ds = rememberDesignScale()
    val underline = muted(0.2f)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        cursorBrush = SolidColor(Color.White),
        textStyle = TextStyle(
            color = Color.White,
            fontSize = ds.sp(15f),
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        ),
        modifier = Modifier
            .width(ds.dp(155).coerceAtLeast(96.dp))
            .drawBehind {
                val stroke = 1.dp.toPx()
                drawLine(underline, start = androidx.compose.ui.geometry.Offset(0f, size.height - stroke / 2), end = androidx.compose.ui.geometry.Offset(size.width, size.height - stroke / 2), strokeWidth = stroke)
            },
        decorationBox = { inner ->
            Box(modifier = Modifier.padding(vertical = ds.dp(4).coerceAtLeast(3.dp)), contentAlignment = Alignment.Center) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        fontSize = ds.sp(15f),
                        fontWeight = FontWeight.SemiBold,
                        color = muted(0.3f),
                        textAlign = TextAlign.Center
                    )
                }
                inner()
            }
        }
    )
}
