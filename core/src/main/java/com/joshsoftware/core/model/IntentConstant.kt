package com.joshsoftware.core.model

enum class IntentConstant {
    REQUEST_ID,
    MEMBER_ID,
    GROUP,
    MEMBER,
    GROUP_ID,
    MESSAGE,
    ADDRESS,
    INVITE_LINK_GROUP,
}
enum class RequestCodes(val code: Int) {
    PICK_LOCATION(1)
}