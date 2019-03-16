package com.solidscorpion.medic.api

import com.solidscorpion.medic.pojo.ModelMenuItem
import com.solidscorpion.medic.pojo.SearchResults
import com.solidscorpion.medic.pojo.TokenResponse
import com.solidscorpion.medic.pojo.UserResponse

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface Api {
    @GET("wp-content/themes/rgb-mobile/app/menu.json")
    fun getMenuItems(): Single<List<ModelMenuItem>>
    @GET("wp-content/themes/rgb-mobile/app/footer-menu.json")
    fun getFooterMenuItems() : Single<List<ModelMenuItem>>

    @GET("wp-content/themes/rgb/ajax/rgb-ajax.php?action=rgb_search&format=json&number=3")
    fun performSearch(@Query(value = "search", encoded = true)searchString: String) : Single<SearchResults>

    @POST("wp-json/jwt-auth/v1/token/validate")
    fun validateToken(@Header("Authorization") token: String): Single<TokenResponse>

    @GET("wp-json/medic/v1/me/profile")
    fun userInfo(@Header("Authorization") token: String): Single<UserResponse>

}
