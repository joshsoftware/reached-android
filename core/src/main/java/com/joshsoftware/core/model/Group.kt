package com.joshsoftware.core.model

data class Group (
    var members: ArrayList<Member> = arrayListOf(),
    var created_by: String? = null
)