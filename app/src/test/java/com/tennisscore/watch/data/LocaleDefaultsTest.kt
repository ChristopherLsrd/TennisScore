package com.tennisscore.watch.data

import com.tennisscore.watch.model.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class LocaleDefaultsTest {

    @Test
    fun `French system locale defaults to FR`() {
        assertEquals(AppLanguage.FR, systemDefaultLanguage(Locale.FRENCH))
    }

    @Test
    fun `non-French system locale defaults to EN`() {
        assertEquals(AppLanguage.EN, systemDefaultLanguage(Locale.US))
        assertEquals(AppLanguage.EN, systemDefaultLanguage(Locale.GERMANY))
    }
}
