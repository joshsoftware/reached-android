package com.joshsoftware.core.model

data class User (
    val name: String? = null,
    var email: String? = null,
    var groups: HashMap<String, Boolean> = hashMapOf()
)