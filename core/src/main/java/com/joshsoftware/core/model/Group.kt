package com.joshsoftware.core.model

data class Group(
    var id: String? = null,
    var members: HashMap<String, Member> = hashMapOf(),
    var created_by: String? = null,
    var name: String
)