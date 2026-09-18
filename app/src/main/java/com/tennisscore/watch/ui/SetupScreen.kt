package com.tennisscore.watch.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Text
import com.tennisscore.watch.R
import com.tennisscore.watch.model.AppState
import com.tennisscore.watch.model.Player

@Composable
fun SetupScreen(
    state: AppState,
    onNameAChange: (String) -> Unit,
    onNameBChange: (String) -> Unit,
    onPickServer: (Player) -> Unit,
    onOpenSettings: () -> Unit,
    onStart: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = stringResource(R.string.new_match), fontSize = 10.sp)
        Spacer(modifier = Modifier.height(10.dp))

        BasicTextField(
            value = state.nameA,
            onValueChange = onNameAChange,
            textStyle = TextStyle(fontSize = 14.sp, textAlign = TextAlign.Center)
        )
        Text(text = "VS", fontSize = 10.sp)
        BasicTextField(
            value = state.nameB,
            onValueChange = onNameBChange,
            textStyle = TextStyle(fontSize = 14.sp, textAlign = TextAlign.Center)
        )

        Spacer(modifier = Modifier.height(10.dp))
        Text(text = stringResource(R.string.serving_first), fontSize = 9.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onPickServer(Player.A) }) { Text("P1") }
            Button(onClick = { onPickServer(Player.B) }) { Text("P2") }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onOpenSettings) { Text("⚙") }
            Text(
                text = "${state.rules.setsToWin} ${stringResource(R.string.sets_unit)} · " +
                    "${state.rules.gamesPerSet} ${stringResource(R.string.games_unit)}",
                fontSize = 9.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = onStart) { Text(stringResource(R.string.start_match)) }
    }
}
