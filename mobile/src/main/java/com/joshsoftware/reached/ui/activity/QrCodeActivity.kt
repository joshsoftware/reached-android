package com.joshsoftware.reached.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.joshsoftware.reached.databinding.ActivityQrCodeBinding
import com.journeyapps.barcodescanner.BarcodeEncoder
import timber.log.Timber

const val INTENT_GROUP_ID = "INTENT_GROUP_ID"
class QrCodeActivity : AppCompatActivity() {
    lateinit var binding: ActivityQrCodeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrCodeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        intent.extras?.getString(INTENT_GROUP_ID)?.let { groupId ->
            generateQrCode(groupId)
            binding.viewListButton.setOnClickListener {
                startGroupMemberActivity(groupId)
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
}