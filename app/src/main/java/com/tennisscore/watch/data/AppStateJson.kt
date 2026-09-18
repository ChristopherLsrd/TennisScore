package com.tennisscore.watch.data

import com.tennisscore.watch.model.AppState
import kotlinx.serialization.json.Json

object AppStateJson {
    private val json = Json { ignoreUnknownKeys = true }

    fun encode(state: AppState): String = json.encodeToString(AppState.serializer(), state)

    fun decode(raw: String): AppState = json.decodeFromString(AppState.serializer(), raw)
}
