package com.tennisscore.watch.data

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.tennisscore.watch.model.AppLanguage

fun interface LocaleApplier {
    fun apply(language: AppLanguage)
}

object AppCompatLocaleApplier : LocaleApplier {
    override fun apply(language: AppLanguage) {
        val tag = if (language == AppLanguage.FR) "fr" else "en"
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
    }
}
