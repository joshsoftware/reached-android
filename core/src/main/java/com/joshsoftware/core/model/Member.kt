package com.joshsoftware.core.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Member(
    var id: String? = null,
    var name: String? = null,
    var profileUrl: String? = null,
    var lat: Double? = null,
    var long: Double? = null
): Parcelable