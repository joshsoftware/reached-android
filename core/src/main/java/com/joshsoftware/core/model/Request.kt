package com.joshsoftware.core.model

data class Request<T>(
    var type: String = RequestType.OTHER.name,
    var data: T? = null
)