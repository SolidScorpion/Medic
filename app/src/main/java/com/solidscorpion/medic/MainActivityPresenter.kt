package com.solidscorpion.medic

import android.annotation.SuppressLint
import com.solidscorpion.medic.api.Api
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class MainActivityPresenter(private val view: MainActivityContract.View, private val api: Api) :
    MainActivityContract.Presenter {

    @SuppressLint("CheckResult")
    override fun loadMenuItems() {
        api.getMenuItems().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { items -> view.onMenuItemsLoaded(items) }
    }

}
