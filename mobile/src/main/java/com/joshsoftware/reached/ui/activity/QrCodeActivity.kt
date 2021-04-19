package com.joshsoftware.reached.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.ktx.shortLinkAsync
import com.google.zxing.BarcodeFormat
import com.joshsoftware.core.model.Group
import com.joshsoftware.reached.databinding.ActivityQrCodeBinding
import com.journeyapps.barcodescanner.BarcodeEncoder
import timber.log.Timber

const val INTENT_GROUP_ID = "INTENT_GROUP_ID"
const val INTENT_GROUP = "INTENT_GROUP"
const val INTENT_GROUP_NAME = "INTENT_GROUP_NAME"
class QrCodeActivity : AppCompatActivity() {
    lateinit var binding: ActivityQrCodeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrCodeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        intent.extras?.getParcelable<Group>(INTENT_GROUP)?.let { group ->
            group.id?.let { gId ->
                generateQrCode(gId)
                binding.viewListButton.setOnClickListener {
                    startGroupMemberActivity(gId)
                }
                binding.inviteButton.setOnClickListener {
                    group.name?.let { name -> createAndShareDynamicLink(gId, groupName = name) }
                }
            }

        }
    }

    private fun startGroupMemberActivity(groupId: String) {
        val intent = Intent(this, GroupMemberActivity::class.java)
        intent.putExtra(INTENT_GROUP_ID, groupId)
        startActivity(intent)
    }

    private fun generateQrCode(id: String) {
        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder
                .encodeBitmap(id, BarcodeFormat.QR_CODE, 400, 400)
                binding.qrCodeImageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun createAndShareDynamicLink(groupId: String, groupName: String) {
        val url = Uri.Builder()
                .scheme("https")
                .authority("google.com")
                .appendQueryParameter("groupId", groupId)
                .appendQueryParameter("groupName", groupName)
                .build()
        val shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(url)
                .setDomainUriPrefix("https://reached1.page.link")
                .setAndroidParameters(DynamicLink.AndroidParameters.Builder().build())
                .buildShortDynamicLink()
        shortLinkTask.addOnCompleteListener {
            if(it.isSuccessful) {
                val i = Intent(Intent.ACTION_SEND)
                i.type = "text/plain"
                i.putExtra(Intent.EXTRA_SUBJECT, "Reached - kids and kin's are safe")
                var strShareMessage = "\n${groupName}\n"
                strShareMessage += it.result.shortLink
                i.putExtra(Intent.EXTRA_TEXT, strShareMessage)
                startActivity(Intent.createChooser(i, "Share via"))
            }
        }


    }
}