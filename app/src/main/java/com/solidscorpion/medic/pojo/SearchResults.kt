package com.solidscorpion.medic.pojo

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SearchResults (val searchResults : MutableList<SearchResultItem> = mutableListOf()) : Parcelable