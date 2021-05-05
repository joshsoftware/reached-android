package com.joshsoftware.core.util

import androidx.annotation.Keep

@Keep
enum class FirebaseDatabaseKey(var key: String) {
    GROUPS ("groups"),
    USERS ("users"),
    SOS ("sos")
}