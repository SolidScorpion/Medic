package com.solidscorpion.medic.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.solidscorpion.medic.R;
import com.solidscorpion.medic.pojo.BaseItem;

import java.util.List;

public class CustomArrayAdapter extends ArrayAdapter<String> implements Filterable {

    private final LayoutInflater mInflater;
    private final Context mContext;
    private final List<BaseItem> items;

    public CustomArrayAdapter(@NonNull Context context, @NonNull List objects) {
        super(context, 0, objects);

        mContext = context;
        mInflater = LayoutInflater.from(context);
        items = objects;
    }

    public BaseItem getBaseItem(int position) {
        return items.get(position);
    }

    public void updateItems(List items){
        this.items.clear();
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
        public String getItem(int position) {
            return items.get(position).getText();
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView,
                                @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(items.get(position)) == R.layout.item_viewall || getItemViewType(items.get(position)) == R.layout.autocomplete_item;
    }

    @Override
    public @NonNull View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    private View createItemView(int position, View convertView, ViewGroup parent){
        BaseItem item = items.get(position);
        int viewType = getItemViewType(item);
        final View view = mInflater.inflate(viewType, parent, false);

        switch (viewType){
            case R.layout.item_viewall:
            case R.layout.item_title:
            case R.layout.autocomplete_item:
                TextView title = (TextView) view.findViewById(R.id.title);
                TextView tvDiscon = (TextView) view.findViewById(R.id.tvDiscon);
                title.setText(item.getText());
                if (item.isDiscontinued() != null && item.isDiscontinued() == 1){
                    title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    tvDiscon.setVisibility(View.VISIBLE);
                } else {
                    title.setPaintFlags(0);
                    tvDiscon.setVisibility(View.GONE);
                }
                break;
        }
        return view;
    }

    private int getItemViewType(BaseItem item) {
        int resource;
        if (item.getText().equals("Divider")){
            resource = R.layout.item_divider;
        }
        else if (item.getText().contains("view all")){
            resource = R.layout.item_viewall;
        }
        else if (item.getUrl().equals("")){
            resource = R.layout.item_title;
        } else {
            resource = R.layout.autocomplete_item;
        }
        return resource;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    filterResults.values = items;
                    filterResults.count = items.size();
                }
                return filterResults;
            }
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                }
                else {
                    notifyDataSetInvalidated();
                }
            }};
        return filter;
    }

}