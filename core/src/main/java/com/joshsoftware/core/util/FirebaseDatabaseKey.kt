package com.joshsoftware.core.util

import androidx.annotation.Keep

@Keep
enum class FirebaseDatabaseKey(var key: String) {
    GROUPS ("groups"),
    USERS ("users"),
    REQUESTS ("requests"),
    MEMBERS ("members"),
    ADDRESS ("address"),
    LASTKNOWNADDRESS ("lastKnownAddress"),
    TOKEN ("token"),
    WATCH ("watch"),
    PHONE ("phone"),
    ENTERED ("entered"),
    EXIT ("entered")
}