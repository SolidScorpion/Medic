package com.solidscorpion.medic.api;

import com.solidscorpion.medic.pojo.ModelMenuItem;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;

public interface Api {
    @GET("wp-content/themes/rgb-mobile/app/menu.json")
    Observable<List<ModelMenuItem>> getMenuItems();
}
