package com.solidscorpion.medic.pojo

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ModelMenuItem(val title: String,
                         val link: String,
                         val isDiscontinued: Int? = 0,
                         var font : Int = HEADER_FONT) : Parcelable {
    companion object {
        val HEADER_FONT = 1
        val BOTTOM_FONT = 2
        val COPYRIGHT_FONT = 3
    }
}



