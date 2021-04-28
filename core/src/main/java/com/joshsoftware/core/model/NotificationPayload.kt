package com.joshsoftware.core.model

import androidx.annotation.Keep

@Keep
data class NotificationPayload (
    var groupId: String? = null,
    var memberId: String? = null
)