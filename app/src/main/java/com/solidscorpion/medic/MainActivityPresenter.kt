package com.solidscorpion.medic

import android.annotation.SuppressLint
import com.solidscorpion.medic.api.Api
import com.solidscorpion.medic.pojo.ModelMenuItem
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers

class MainActivityPresenter(private val view: MainActivityContract.View, private val api: Api) :
    MainActivityContract.Presenter {

    @SuppressLint("CheckResult")
    override fun loadMenuItems() {
        Single.zip(api.getMenuItems().subscribeOn(Schedulers.io()), api.getFooterMenuItems().subscribeOn(Schedulers.io()),
            BiFunction<List<ModelMenuItem>, List<ModelMenuItem>, List<ModelMenuItem>> { t1, t2 ->
                val separator = ModelMenuItem("1", "")
                val mutableListOf = mutableListOf<ModelMenuItem>()
                mutableListOf.add(separator)
                mutableListOf.addAll(t1)
                    val footerSeparator = ModelMenuItem("2","")
                mutableListOf.add(footerSeparator)
                mutableListOf.addAll(t2)
                mutableListOf
            })
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { items -> view.onMenuItemsLoaded(items) }
    }

}
