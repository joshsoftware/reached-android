package com.joshsoftware.core.util

import com.joshsoftware.core.model.Member

class ConversionUtil {

    fun getMemberListFromMap(map: HashMap<String, Member>): ArrayList<Member> {
        val result = arrayListOf<Member>()
        map.forEach { (s, member) ->
            member.id = s
            result.add(member)
        }
        return result
    }
}