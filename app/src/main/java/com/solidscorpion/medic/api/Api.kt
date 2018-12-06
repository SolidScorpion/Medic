package com.solidscorpion.medic.api

import com.solidscorpion.medic.pojo.ModelMenuItem

import io.reactivex.Single
import retrofit2.http.GET

interface Api {
    @GET("wp-content/themes/rgb-mobile/app/menu.json")
    fun getMenuItems(): Single<List<ModelMenuItem>>
    @GET("wp-content/themes/rgb-mobile/app/footer-menu.json")
    fun getFooterMenuItems() : Single<List<ModelMenuItem>>
}
