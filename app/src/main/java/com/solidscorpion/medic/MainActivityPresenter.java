package com.solidscorpion.medic;

import android.annotation.SuppressLint;

import com.solidscorpion.medic.api.Api;
import com.solidscorpion.medic.api.Provider;
import com.solidscorpion.medic.pojo.ModelMenuItem;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivityPresenter implements MainActivityContract.Presenter {

    private Retrofit retrofit;
    private Api api;
    private MainActivityContract.View view;

    public MainActivityPresenter(MainActivityContract.View view) {
        retrofit = new Retrofit.Builder()
                .baseUrl("https://dev.medic.co.il/")
                .client(Provider.INSTANCE.getUnsafeOkHttpClient().build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        api = retrofit.create(Api.class);
        this.view = view;
    }

    @SuppressLint("CheckResult")
    public void loadMenuItems() {
        api.getMenuItems().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<ModelMenuItem>>() {
                    @Override
                    public void accept(List<ModelMenuItem> items) throws Exception {
                        if (view != null) view.onMenuItemsLoaded(items);
                    }
                });
    }

}
