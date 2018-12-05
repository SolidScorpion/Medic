package com.solidscorpion.medic;

import com.solidscorpion.medic.pojo.ModelMenuItem;

import java.util.List;

public interface MainActivityContract {

    interface View {
        void onMenuItemsLoaded(List<ModelMenuItem> items);
    }

    interface Presenter {
        void loadMenuItems();
    }
}
