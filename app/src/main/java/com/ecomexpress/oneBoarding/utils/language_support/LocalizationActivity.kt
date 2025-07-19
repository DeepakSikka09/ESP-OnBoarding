package com.ecomexpress.oneBoarding.utils.language_support

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.util.*

open class LocalizationActivity : AppCompatActivity(), OnLocaleChangedListener {
    private val localizationDelegate = LocalizationDelegate(this)
    public override fun onCreate(savedInstanceState: Bundle?) {
        localizationDelegate.addOnLocaleChengedListener(this)
        localizationDelegate.onCreate(savedInstanceState)
        super.onCreate(savedInstanceState)
    }

    public override fun onResume() {
        super.onResume()
        localizationDelegate.onResume()
    }

    fun setLanguage(locale: Locale?) {
        localizationDelegate.setLanguage(locale!!)
    }

    fun setDefaultLanguage(language: String?) {
        localizationDelegate.setDefaultLanguage(language)
    }

    fun setDefaultLanguage(locale: Locale?) {
        localizationDelegate.setDefaultLanguage(locale!!)
    }

    var language: String?
        get() = localizationDelegate.language
        set(language) {
            localizationDelegate.language = language!!
        }
    val locale: Locale
        get() = localizationDelegate.locale

    // Just override method locale change event
    override fun onBeforeLocaleChanged() {}
    override fun onAfterLocaleChanged() {}
}