package com.nevoit.cresto.feature.settings.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object AppLocaleManager {
    const val SYSTEM = ""
    const val ENGLISH = "en"
    const val SIMPLIFIED_CHINESE = "zh-CN"

    @Suppress("UNUSED_PARAMETER")
    fun getLanguageTag(context: Context): String {
        return AppCompatDelegate.getApplicationLocales().toLanguageTags()
    }

    @Suppress("UNUSED_PARAMETER")
    fun setLanguageTag(context: Context, tag: String) {
        val locales = if (tag == SYSTEM) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(tag)
        }
        AppCompatDelegate.setApplicationLocales(locales)
    }
}
