package com.joshsoftware.core.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class Member(
    var id: String? = null,
    var name: String? = null,
    var profileUrl: String? = null,
    var lastUpdated: String? = null,
    var lastKnownAddress: String? = null,
    var sosState: Boolean = false,
    var address: MutableMap<String, Address> = mutableMapOf(),
    var lat: Double? = null,
    var long: Double? = null
): Parcelable