package com.joshsoftware.core.model

data class User (
    val name: String? = null,
    var email: String? = null,
    var token: Token = Token(),
    var profileUrl: String? = null,
    var groups: HashMap<String, Boolean> = hashMapOf()
)

data class Token (
    var phone: String? = null,
    var watch: String? = null
)