package com.tennisscore.watch.data

import com.tennisscore.watch.model.AppLanguage
import java.util.Locale

fun systemDefaultLanguage(locale: Locale = Locale.getDefault()): AppLanguage =
    if (locale.language == Locale.FRENCH.language) AppLanguage.FR else AppLanguage.EN
