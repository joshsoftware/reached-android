package com.joshsoftware.reachedapp.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import com.joshsoftware.reachedapp.R
import com.joshsoftware.reachedapp.databinding.ActivityCreateGroupBinding

class CreateGroupActivity : AppCompatActivity() {
    lateinit var binding: ActivityCreateGroupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)
        binding = ActivityCreateGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            fabDone.setOnClickListener {
                if(!TextUtils.isEmpty(groupEditText.text)) {
//                    intent.putExtra()
//                    setResult()
                }
            }
        }
    }
}