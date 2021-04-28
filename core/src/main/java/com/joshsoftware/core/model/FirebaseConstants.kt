package com.joshsoftware.core.model

import androidx.annotation.Keep

@Keep
object FirebaseConstants {
    // Analytics
    object EVENTS {
        const val USER_SIGNED_IN = "user_signed_in"
    }
    // Analytics
    object PARAM_KEY {
        const val CATEGORY = "category"
    }
    //Crashlytics
    object LOGS {
        const val GOOGLE_SIGN_IN_CLICKED = "google sign in clicked"
    }
}