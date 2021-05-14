package com.joshsoftware.core.model

data class LeaveRequestData (
    var from: RequestParam? = null,
    var to: String? = null,
    var group: RequestParam? = null,
    var fromId: String? = null,
    var requestId: String? = null
)
