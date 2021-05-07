package com.joshsoftware.core.model

data class Request<T>(
    var type: RequestType = RequestType.OTHER,
    var data: T? = null
)