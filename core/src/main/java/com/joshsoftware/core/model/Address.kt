package com.joshsoftware.core.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Address(
    var name: String,
    var lat: Double,
    var long: Double,
    var radius: Int? = null
): Parcelable