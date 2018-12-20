package com.solidscorpion.medic.pojo

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BaseItem(val text: String, @Json(name = "URL") val url: String, val isDiscontinued : Int?) : Parcelable