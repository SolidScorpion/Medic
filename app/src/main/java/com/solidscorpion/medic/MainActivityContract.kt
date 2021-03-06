package com.solidscorpion.medic

import com.solidscorpion.medic.pojo.BaseItem
import com.solidscorpion.medic.pojo.ModelMenuItem

interface MainActivityContract {

    interface View {
        fun onMenuItemsLoaded(items: List<ModelMenuItem>)
        fun showProgress()
        fun hideProgress()
        fun showResults(results: List<BaseItem>)
    }

    interface Presenter {
        fun loadMenuItems()
        fun performSearch(text: CharSequence, delay: Long)
        fun onStop()
    }
}
