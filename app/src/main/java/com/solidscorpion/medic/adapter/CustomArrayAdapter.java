package com.solidscorpion.medic.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.solidscorpion.medic.R;
import com.solidscorpion.medic.pojo.BaseItem;

import java.util.List;

public class CustomArrayAdapter extends ArrayAdapter<String> {

    private final LayoutInflater mInflater;
    private final Context mContext;
    private final List<BaseItem> items;
    private final int mResource;

    public CustomArrayAdapter(@NonNull Context context, @LayoutRes int resource,
                              @NonNull List objects) {
        super(context, resource, 0, objects);

        mContext = context;
        mInflater = LayoutInflater.from(context);
        mResource = resource;
        items = objects;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView,
                                @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    @Override
    public @NonNull View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    private View createItemView(int position, View convertView, ViewGroup parent){
        final View view = mInflater.inflate(mResource, parent, false);

        TextView title = (TextView) view.findViewById(R.id.title2);

        BaseItem item = items.get(position);
        title.setText(item.getText());
        if (item.getText().contains("view all")){
            title.setTextColor(Color.parseColor("#2f90fb"));
        } else if (item.getUrl().equals("")){
            title.setTextColor(Color.parseColor("#000000"));
        }
        return view;
    }
}