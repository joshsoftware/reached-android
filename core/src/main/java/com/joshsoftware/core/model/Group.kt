package com.joshsoftware.core.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class Group(
    var id: String? = null,
    var members: HashMap<String, Member> = hashMapOf(),
    var created_by: String? = null,
    var name: String? = null
): Parcelable