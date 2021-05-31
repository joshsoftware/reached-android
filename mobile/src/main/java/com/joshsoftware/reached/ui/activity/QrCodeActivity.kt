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
import com.joshsoftware.reached.utils.InviteLinkUtils
import com.journeyapps.barcodescanner.BarcodeEncoder
import timber.log.Timber

const val INTENT_GROUP_ID = "INTENT_GROUP_ID"
const val INTENT_GROUP = "INTENT_GROUP"
class QrCodeActivity : AppCompatActivity() {
    lateinit var binding: ActivityQrCodeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrCodeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        intent.extras?.getParcelable<Group>(INTENT_GROUP)?.let { group ->
            group.id?.let { gId ->
                binding.txtGroupName.text = group.name
                generateQrCode(gId)
                binding.skipButton.setOnClickListener {
                    startGroupMemberActivity(gId)
                }
                binding.sendInviteButton.setOnClickListener {
                    val linkUtils = InviteLinkUtils()
                    group.name?.let { name -> linkUtils.getInviteLinkFor(gId, groupName = name) { shortInvitedLink ->
                        shortInvitedLink?.let { uri ->
                            val i = Intent(Intent.ACTION_SEND)
                            i.type = "text/plain"
                            i.putExtra(Intent.EXTRA_SUBJECT, "Reached - kids and kin's are safe")
                            var strShareMessage = "\n${name}\n"
                            strShareMessage += uri
                            i.putExtra(Intent.EXTRA_TEXT, strShareMessage)
                            startActivity(Intent.createChooser(i, "Share via"))
                        }
                    }}
                }
            }

        }
    }

    private fun startGroupMemberActivity(groupId: String) {
        val intent = Intent(this, HomeActivity::class.java)
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


}