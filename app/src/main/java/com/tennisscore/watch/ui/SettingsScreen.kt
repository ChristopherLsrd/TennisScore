package com.tennisscore.watch.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Button
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
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = stringResource(R.string.settings_title), fontSize = 10.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = stringResource(R.string.sets_to_win), fontSize = 8.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = { onSetsDelta(-1) }) { Text("-") }
                    Text(text = "${state.rules.setsToWin}", fontSize = 18.sp)
                    Button(onClick = { onSetsDelta(1) }) { Text("+") }
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = stringResource(R.string.games_per_set), fontSize = 8.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = { onGamesDelta(-1) }) { Text("-") }
                    Text(text = "${state.rules.gamesPerSet}", fontSize = 18.sp)
                    Button(onClick = { onGamesDelta(1) }) { Text("+") }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = stringResource(R.string.set_tiebreak), fontSize = 9.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Button(onClick = { onPickRule(TiebreakRule.ADVANTAGE) }) { Text(stringResource(R.string.advantage), fontSize = 9.sp) }
            Button(onClick = { onPickRule(TiebreakRule.TIEBREAK) }) { Text(stringResource(R.string.tiebreak_at), fontSize = 9.sp) }
            Button(onClick = { onPickRule(TiebreakRule.SUPER_TIEBREAK) }) { Text(stringResource(R.string.super_tiebreak), fontSize = 9.sp) }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = stringResource(R.string.language), fontSize = 9.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Button(onClick = { onPickLanguage(AppLanguage.FR) }) { Text("FR") }
            Button(onClick = { onPickLanguage(AppLanguage.EN) }) { Text("EN") }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = onCancel) { Text(stringResource(R.string.cancel)) }
            Button(onClick = onSave) { Text(stringResource(R.string.save)) }
        }
    }
}
