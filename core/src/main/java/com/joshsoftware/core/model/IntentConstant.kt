package com.joshsoftware.core.model

enum class IntentConstant {
    REQUEST_ID,
    MEMBER_ID,
    MEMBER,
    GROUP_ID,
    MESSAGE,
    ADDRESS,
}
enum class RequestCodes(val code: Int) {
    PICK_LOCATION(1)
}