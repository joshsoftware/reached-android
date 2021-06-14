package com.joshsoftware.core.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.firebase.encoders.annotations.Encodable
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class Address(
        var id: String? = null,
        var name: String,
        var address: String,
        var transition: String,
        var lat: Double,
        var long: Double,
        var radius: Int = 200
): Parcelable {
    constructor(): this(null, "","","enter", 0.0, 0.0)
}