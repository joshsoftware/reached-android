package com.joshsoftware.core.model

import androidx.annotation.Keep

@Keep
enum class NotificationType(var key: String) {
    LEAVE("leave_group"),
    GEOFENCE("geofence"),
    SOS("sos"),
    JOIN_GROUP("join_group"),
    REMOVED_MEMBER("removed_member"),
    GROUP_DELETE("deleted_group")
}