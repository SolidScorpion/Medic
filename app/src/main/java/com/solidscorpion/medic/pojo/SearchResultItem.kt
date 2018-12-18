package com.solidscorpion.medic.pojo

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SearchResultItem (val displayName: String, val total: Int, val items : MutableList<BaseItem>) : Parcelable