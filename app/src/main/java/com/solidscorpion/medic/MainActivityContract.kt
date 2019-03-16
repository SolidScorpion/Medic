package com.solidscorpion.medic

import android.annotation.SuppressLint
import com.solidscorpion.medic.pojo.BaseItem
import com.solidscorpion.medic.pojo.ModelMenuItem

interface MainActivityContract {

    interface View {
        fun onMenuItemsLoaded(items: List<ModelMenuItem>)
        fun showProgress()
        fun hideProgress()
        fun showResults(results: List<BaseItem>)
        fun userLogged()
    }

    interface Presenter {
        fun performSearch(text: CharSequence, delay: Long)
        fun onStop()
        fun loadMenuItems(logged: Boolean)
    }
}
