package com.solidscorpion.medic.pojo

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ModelMenuItem(val title: String, val link: String, var font : Int = HEADER_FONT) : Parcelable {
    companion object {
        val HEADER_FONT = 1
        val BOTTOM_FONT = 2
        val COPYRIGHT_FONT = 3
    }
}



