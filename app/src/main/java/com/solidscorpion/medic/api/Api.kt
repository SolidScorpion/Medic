package com.solidscorpion.medic.api

import com.solidscorpion.medic.pojo.ModelMenuItem

import io.reactivex.Observable
import retrofit2.http.GET

interface Api {
    @GET("wp-content/themes/rgb-mobile/app/menu.json")
    fun getMenuItems(): Observable<List<ModelMenuItem>>
}
