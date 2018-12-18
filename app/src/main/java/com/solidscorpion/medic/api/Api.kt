package com.solidscorpion.medic.api

import com.solidscorpion.medic.pojo.ModelMenuItem
import com.solidscorpion.medic.pojo.SearchResults

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface Api {
    @GET("wp-content/themes/rgb-mobile/app/menu.json")
    fun getMenuItems(): Single<List<ModelMenuItem>>
    @GET("wp-content/themes/rgb-mobile/app/footer-menu.json")
    fun getFooterMenuItems() : Single<List<ModelMenuItem>>

    @GET("wp-content/themes/rgb/ajax/rgb-ajax.php?action=rgb_search&format=json&number=3")
    fun performSearch(@Query(value = "search")searchString: String) : Single<SearchResults>
}
