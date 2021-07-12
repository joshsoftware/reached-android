package com.joshsoftware.reached.extansions

import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

fun ChipGroup.applyCheckedOnAll(isChecked: Boolean){
    if (isChecked){
        for (index in 0 until this.childCount) {
            val chip: Chip = this.getChildAt(index) as Chip
            chip.isChecked = true
        }
    }else {
        this.clearCheck()
    }
}