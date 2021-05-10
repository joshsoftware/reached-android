package com.joshsoftware.core.model

data class LeaveRequestData (
    var from: RequestParam,
    var to: String,
    var group: RequestParam
)
