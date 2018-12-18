package com.solidscorpion.medic.pojo

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SearchResults (val data : MutableList<SearchResultItem> = mutableListOf()) : Parcelable