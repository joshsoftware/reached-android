package com.joshsoftware.core.model

import androidx.annotation.Keep

@Keep
enum class NotificationType(var key: String) {
    JOIN_GROUP("join_group"),
    LEAVE("leave_group"),
    SOS("sos"),
    GEOFENCE("geofence")
}