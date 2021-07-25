package com.joshsoftware.reachedapp.utils

import android.content.Intent
import android.net.Uri
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.joshsoftware.core.model.Group
import timber.log.Timber

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

    fun handleDynamicLinks(intent: Intent, onGroupFetch: (Group) -> Unit , onFailure: () -> Unit) {
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(intent)
                .addOnSuccessListener {
                    var deepLink: Uri?= null
                    it?.let {
                        deepLink = it.link
                    }

                    deepLink?.let {
                        val id = it.getQueryParameter("groupId")
                        val name = it.getQueryParameter("groupName")
                        val group = Group(id, name = name)
                        onGroupFetch(group)
                    } ?: run {
                        onFailure()
                    }
                }.addOnFailureListener {
                    onFailure()
                    Timber.i(it)
                }
    }
}