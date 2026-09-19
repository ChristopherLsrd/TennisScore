package com.tennisscore.watch.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Text
import com.tennisscore.watch.R
import com.tennisscore.watch.model.AppLanguage
import com.tennisscore.watch.model.AppState
import com.tennisscore.watch.model.TiebreakRule

@Composable
fun SettingsScreen(
    state: AppState,
    onSetsDelta: (Int) -> Unit,
    onGamesDelta: (Int) -> Unit,
    onPickRule: (TiebreakRule) -> Unit,
    onPickLanguage: (AppLanguage) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    val ds = rememberDesignScale()
    Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(start = ds.dp(47), end = ds.dp(47), top = ds.dp(14), bottom = ds.dp(30)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ds.dp(7))
        ) {
            Caption(stringResource(R.string.settings_title), size = 13f, letterSpacing = 2.5f, alpha = 0.4f, weight = FontWeight.SemiBold)

            Row(horizontalArrangement = Arrangement.spacedBy(ds.dp(18).coerceAtLeast(8.dp))) {
                Stepper(stringResource(R.string.sets_to_win), state.rules.setsToWin, onSetsDelta)
                Stepper(stringResource(R.string.games_per_set), state.rules.gamesPerSet, onGamesDelta)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(ds.dp(8))) {
                Caption(stringResource(R.string.set_tiebreak), size = 12f, letterSpacing = 2f, alpha = 0.35f)
                Row(
                    // The face is widest at this height, so the three chips may extend past the column padding.
                    modifier = Modifier.wrapContentWidth(Alignment.CenterHorizontally, unbounded = true),
                    horizontalArrangement = Arrangement.spacedBy(ds.dp(6).coerceAtLeast(4.dp))
                ) {
                    RuleChip(R.string.rule_chip_advantage, TiebreakRule.ADVANTAGE, state, onPickRule)
                    RuleChip(R.string.rule_chip_tiebreak, TiebreakRule.TIEBREAK, state, onPickRule)
                    RuleChip(R.string.rule_chip_super_tiebreak, TiebreakRule.SUPER_TIEBREAK, state, onPickRule)
                }
            }

            // Caption and chips share a row (unlike the design) so Cancel/Save fit on a 206dp round face.
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(ds.dp(8).coerceAtLeast(4.dp))) {
                Caption(stringResource(R.string.language), size = 12f, letterSpacing = 2f, alpha = 0.35f)
                Chip("FR", selected = state.language == AppLanguage.FR, onClick = { onPickLanguage(AppLanguage.FR) }, fontSize = 13f, hPad = 16f, vPad = 7f, radius = 16f)
                Chip("EN", selected = state.language == AppLanguage.EN, onClick = { onPickLanguage(AppLanguage.EN) }, fontSize = 13f, hPad = 16f, vPad = 7f, radius = 16f)
            }

            // Intrinsic-size row + equal weights + fillMaxHeight: both buttons match the larger one in width and height.
            Row(
                modifier = Modifier.padding(top = ds.dp(2)).width(IntrinsicSize.Max).height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(ds.dp(10).coerceAtLeast(6.dp))
            ) {
                OutlineButton(stringResource(R.string.cancel), onClick = onCancel, fontSize = 14f, hPad = 22f, vPad = 11f, radius = 20f, modifier = Modifier.weight(1f).fillMaxHeight())
                AccentButton(stringResource(R.string.save), onClick = onSave, fontSize = 14f, hPad = 22f, vPad = 11f, radius = 20f, modifier = Modifier.weight(1f).fillMaxHeight())
            }
        }
    }
}

@Composable
private fun RuleChip(labelRes: Int, rule: TiebreakRule, state: AppState, onPickRule: (TiebreakRule) -> Unit) {
    Chip(
        text = stringResource(labelRes),
        selected = state.rules.tiebreakRule == rule,
        onClick = { onPickRule(rule) },
        fontSize = 9.5f,
        hPad = 9f,
        vPad = 6f,
        radius = 12f,
        minHPad = 6.dp
    )
}

@Composable
private fun Stepper(label: String, value: Int, onDelta: (Int) -> Unit) {
    val ds = rememberDesignScale()
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(ds.dp(4))) {
        Caption(label, size = 11f, letterSpacing = 1.3f, alpha = 0.35f)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(ds.dp(8).coerceAtLeast(4.dp))) {
            CircleButton("−", onClick = { onDelta(-1) }, size = 24f, glyphSize = 14f, background = 0.1f, minSize = 22.dp)
            Text(
                text = "$value",
                fontFamily = ScoreFont,
                fontWeight = FontWeight.Bold,
                fontSize = ds.sp(19f),
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(min = ds.dp(17).coerceAtLeast(12.dp))
            )
            CircleButton("+", onClick = { onDelta(1) }, size = 24f, glyphSize = 14f, background = 0.1f, minSize = 22.dp)
        }
    }
}
