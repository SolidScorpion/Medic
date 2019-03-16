package com.solidscorpion.medic.pojo

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TokenResponse(val code: String,
                         val data: Code): Parcelable