package com.joshsoftware.core.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Address(
        var id: String? = null,
        var name: String,
        var lat: Double,
        var long: Double,
        var radius: Int = 0
): Parcelable {
    constructor(): this("", "", 0.0, 0.0)
}