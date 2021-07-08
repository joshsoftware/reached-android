package com.joshsoftware.core.util

object StringUtils {
    fun capitalizeInitialsName(name: String): String {
        val fullname = name.split(" ")
        var tempName = StringBuilder()
        fullname.forEach {
            tempName.append("${it.capitalize()} ")
        }
        return tempName.trim().toString()
    }
}

