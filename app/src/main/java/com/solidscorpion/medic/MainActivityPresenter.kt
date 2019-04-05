package com.solidscorpion.medic

import android.annotation.SuppressLint
import android.util.Log
import com.solidscorpion.medic.api.Api
import com.solidscorpion.medic.pojo.BaseItem
import com.solidscorpion.medic.pojo.ModelMenuItem
import com.solidscorpion.medic.pojo.SearchResultItem
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import androidx.core.content.ContextCompat.startActivity
import android.content.Intent
import android.net.Uri


class MainActivityPresenter(
    private val view: MainActivityContract.View,
    private val api: Api
) :
    MainActivityContract.Presenter {
    private val TAG = MainActivityPresenter::class.java.simpleName
    private val disposables = CompositeDisposable()
    private var username: String = ""
    @SuppressLint("CheckResult")
    override fun loadMenuItems(logged: Boolean) {
        Single.zip(api.getMenuItems().subscribeOn(Schedulers.io()),
            api.getFooterMenuItems().subscribeOn(Schedulers.io()),
            BiFunction<List<ModelMenuItem>, List<ModelMenuItem>, List<ModelMenuItem>> { t1, t2 ->
                var font = ModelMenuItem.HEADER_FONT
                val separator = ModelMenuItem("1", "", font)
                val mutableListOf = mutableListOf<ModelMenuItem>()
                if (logged) {
                    mutableListOf.add(ModelMenuItem("000", username, font))
                } else {
                    mutableListOf.add(ModelMenuItem("", "", font))

                }
                mutableListOf.add(separator)
                for (model in t1) model.font = font
                mutableListOf.addAll(t1)

                font = ModelMenuItem.BOTTOM_FONT
                val footerSeparator = ModelMenuItem("2", "", font)
                mutableListOf.add(footerSeparator)
                for (model in t2) model.font = font
                mutableListOf.addAll(t2)
                mutableListOf.add(ModelMenuItem("3", "", font))

                font = ModelMenuItem.COPYRIGHT_FONT
                mutableListOf.add(ModelMenuItem("11", "", font))
                mutableListOf
            })
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { items ->
                    view.onMenuItemsLoaded(items)
                }, {
                    Log.e(TAG, it.message)
                    it.printStackTrace()
                })
    }

    override fun onStop() {
        disposables.clear()
    }

    override fun performSearch(text: CharSequence, delay: Long) {
        disposables.clear()
        disposables.add(
            Single.timer(delay, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map { view.showProgress() }
                .flatMap { api.performSearch(text.toString()).subscribeOn(Schedulers.io()) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    val results = parseResults(it.searchResults)
                    view.showResults(results)
                    view.hideProgress()
                }, {
                    view.hideProgress()
                    Log.e(TAG, it.message)
                    it.printStackTrace()
                })
        )
    }

    private fun parseResults(searchResults: MutableList<Map<String, SearchResultItem>>): List<BaseItem> {
        val parsedResult = ArrayList<BaseItem>()
        for (result in searchResults) {
            result.forEach { (s: String, searchResultItem: SearchResultItem) ->
                parsedResult.add(BaseItem("Divider", "", 0))
                parsedResult.add(BaseItem(searchResultItem.displayName, "", 0))
                parsedResult.addAll(searchResultItem.items)
            }
        }
        return parsedResult
    }

    fun validateToken(token: String) {
        disposables.add(api.validateToken(token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.data.status == 200) {
                    getUser(token)
                }
            }, {
                Log.e(TAG, it.message)
                it.printStackTrace()
            })
        )
    }

    fun getUser(token: String) {
        disposables.add(api.userInfo(token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                username = it.data.user_login
                view.userLogged()
            }, {
                Log.e(TAG, it.message)
                it.printStackTrace()
            })
        )
    }
}
