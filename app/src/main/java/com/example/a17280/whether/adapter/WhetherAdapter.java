package com.example.a17280.whether.adapter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.a17280.whether.R;
import com.example.a17280.whether.entity.Whether;

import java.util.List;

/**
 * Created by 17280 on 2019/5/5.
 * 未来十五天天气列表的适配器，包含两个元素，一个图片一个文字
 */

public class WhetherAdapter extends RecyclerView.Adapter<WhetherAdapter.ViewHolder> {

    private List<Whether> mWhetherList;

    public WhetherAdapter(List<Whether> whetherList ){
        mWhetherList = whetherList;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView whetherImage;
        TextView whetherText;
        TextView whetherDay;
        TextView whetherTemperature;

        public ViewHolder(View view){
            super(view);
            whetherImage = (ImageView) view.findViewById(R.id.image_view_recycler_item);
            whetherText = (TextView) view.findViewById(R.id.text_view_recycler_item_whether);
            whetherDay = (TextView) view.findViewById(R.id.text_view_recycler_item_day);
            whetherTemperature = (TextView) view.findViewById(R.id.text_view_recycler_item_temperature);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.whether_item,parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder , int position){
        Whether whether = mWhetherList.get(position);
        holder.whetherImage.setImageResource(whether.getImageView_whether());
        holder.whetherText.setText(whether.getString_whether());
        holder.whetherDay.setText(whether.getString_day());
        holder.whetherTemperature.setText(whether.getString_tempareture());
    }

    @Override
    public int getItemCount(){
        return mWhetherList.size();
    }







}
