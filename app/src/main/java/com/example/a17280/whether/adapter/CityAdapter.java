package com.example.a17280.whether.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.a17280.whether.R;
import com.example.a17280.whether.entity.City;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 17280 on 2019/5/8.
 *
 */

public class CityAdapter extends ArrayAdapter<City> {
    private int resourceId;

    public CityAdapter(Context context, int textViewResourceId, List<City> objects){
        super(context,textViewResourceId,objects);
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        City city = getItem(position);
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
        }else{
            view = convertView;
        }
        TextView textView = (TextView) view.findViewById(R.id.text_view_fragment_list_item);
        textView.setText(city.getPath());
        return view;
    }
}
