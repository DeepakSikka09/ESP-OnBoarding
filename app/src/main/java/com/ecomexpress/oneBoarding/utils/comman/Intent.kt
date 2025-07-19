package com.ecomexpress.oneBoarding.utils.comman

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.ecomexpress.oneBoarding.R
import com.ecomexpress.oneBoarding.ui.onBoard.activity.SignUpActivity

//Here we use inline ,noninline and extension function function with  reified
inline fun <reified T : Any> createIntent(context: Context) = Intent(context, T::class.java)

//extension function
inline fun <reified T : Any> Context.launchNewActivity(noinline bundle: Intent.() -> Unit = {}) {
    val intent = createIntent<T>(this)
    intent.bundle()
    startActivity(intent)
    (this as Activity).overridePendingTransition(R.anim.slide_in_right,
        R.anim.slide_out_left)
}
