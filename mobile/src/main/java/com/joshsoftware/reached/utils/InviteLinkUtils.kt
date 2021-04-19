package com.joshsoftware.reached.utils

import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat.startActivity
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks

const val IOS_BUNDLE_ID = "com.joshsoftware.app.reached"
class InviteLinkUtils {

    fun getInviteLinkFor(groupId: String, groupName: String, onLinkCreate: (Uri?) -> Unit) {
            val url = Uri.Builder()
                    .scheme("https")
                    .authority("google.com")
                    .appendQueryParameter("groupId", groupId)
                    .appendQueryParameter("groupName", groupName)
                    .build()

            val shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                    .setLink(url)
                    .setDomainUriPrefix("https://reached1.page.link")
                    .setIosParameters(getIosParameters())
                    .setAndroidParameters(DynamicLink.AndroidParameters.Builder().build())
                    .buildShortDynamicLink()

            shortLinkTask.addOnCompleteListener {
                if(it.isSuccessful) {
                   onLinkCreate(it.result.shortLink)
                }
            }
    }

    fun getIosParameters(): DynamicLink.IosParameters {
        return DynamicLink.IosParameters.Builder(IOS_BUNDLE_ID)
                .setAppStoreId("1561609913")
                .setMinimumVersion("1.0")
                .build()
    }
}