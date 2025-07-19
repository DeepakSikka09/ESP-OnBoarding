package com.ecomexpress.oneBoarding.utils.language_support

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import com.ecomexpress.oneBoarding.utils.language_support.LanguageSetting.defaultLanguage
import com.ecomexpress.oneBoarding.utils.language_support.LanguageSetting.getLocale
import com.ecomexpress.oneBoarding.utils.language_support.LanguageSetting.setLanguage
import java.util.*

class LocalizationDelegate(private val activity: Activity) {
    // Boolean flag to check that activity was recreated from locale changed.
    private var isLocalizationChanged = false

    // Prepare default language.
    private var currentLanguage = defaultLanguage
    private val localeChangedListeners: MutableList<OnLocaleChangedListener> = ArrayList()
    fun addOnLocaleChengedListener(onLocaleChangedListener: OnLocaleChangedListener) {
        localeChangedListeners.add(onLocaleChangedListener)
    }

    fun onCreate(savedInstanceState: Bundle?) {
        setupLanguage()
        checkBeforeLocaleChanging()
    }

    // Provide method to set application language by locale.
    fun setLanguage(locale: Locale) {
        language = locale.language
    }

    fun setDefaultLanguage(language: String?) {
        defaultLanguage = language
    }

    fun setDefaultLanguage(locale: Locale) {
        defaultLanguage = locale.language
    }

    // Provide method to set application language by country name.
    // Get current language
    var language: String
        get() = LanguageSetting.language
        set(language) {
            if (!isDuplicatedLanguageSetting(language)) {
                setLanguage(activity, language)
                notifyLanguageChanged()
            }
        }

    // Get current locale
    val locale: Locale
        get() = getLocale(activity)

    // Check that bundle come from locale change.
    // If yes, bundle will obe remove and set boolean flag to "true".
    private fun checkBeforeLocaleChanging() {
        val isLocalizationChanged =
            activity.intent.getBooleanExtra(KEY_ACTIVIY_LOCALE_CHANGED, false)
        if (isLocalizationChanged) {
            this.isLocalizationChanged = true
            activity.intent.removeExtra(KEY_ACTIVIY_LOCALE_CHANGED)
        }
    }

    // Setup language to locale and language preference.
    // This method will called before onCreate.
    private fun setupLanguage() {
        val locale = getLocale(activity)
        setupLocale(locale)
        currentLanguage = locale.language
        setLanguage(activity, locale.language)
    }

    // Set locale configuration.
    private fun setupLocale(locale: Locale) {
        updateLocaleConfiguration(activity, locale)
    }

    private fun updateLocaleConfiguration(context: Context, locale: Locale) {
        val config = context.resources.configuration
        config.locale = locale
        val dm = context.resources.displayMetrics
        context.resources.updateConfiguration(config, dm)
    }

    // Avoid duplicated setup
    private fun isDuplicatedLanguageSetting(language: String): Boolean {
        return language.lowercase(Locale.getDefault()) == LanguageSetting.language
    }

    // Let's take it change! (Using recreate method that available on API 11 or more.
    private fun notifyLanguageChanged() {
        for (changedListener in localeChangedListeners) {
            changedListener.onBeforeLocaleChanged()
        }
        activity.intent.putExtra(KEY_ACTIVIY_LOCALE_CHANGED, true)
//        activity.recreate()
    }

    // If activity is run to backstack. So we have to check if this activity is resume working.
    fun onResume() {
        Handler().post {
            checkLocaleChange()
            checkAfterLocaleChanging()
        }
    }

    // Check if locale has change while this activity was run to backstack.
    private fun checkLocaleChange() {
        if (LanguageSetting.language.lowercase(Locale.getDefault()) != currentLanguage.lowercase(
                Locale.getDefault()
            )
        ) {
//            activity.recreate()
        }
    }

    // Call override method if local is really changed
    private fun checkAfterLocaleChanging() {
        if (isLocalizationChanged) {
            for (listener in localeChangedListeners) {
                listener.onAfterLocaleChanged()
            }
            isLocalizationChanged = false
        }
    }

    companion object {
        private const val KEY_ACTIVIY_LOCALE_CHANGED = "activity_locale_changed"
    }
}