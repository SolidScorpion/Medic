package com.solidscorpion.medic

import com.solidscorpion.medic.pojo.ModelMenuItem

interface MainActivityContract {

    interface View {
        fun onMenuItemsLoaded(items: List<ModelMenuItem>)
    }

    interface Presenter {
        fun loadMenuItems()
        fun performSearch(text: CharSequence)
        fun onStop()
    }
}
