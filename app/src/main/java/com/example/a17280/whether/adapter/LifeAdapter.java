package com.example.a17280.whether.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.a17280.whether.R;
import com.example.a17280.whether.entity.City;
import com.example.a17280.whether.entity.Life;
import com.example.a17280.whether.entity.Whether;

import java.util.List;

/**
 * Created by 17280 on 2019/5/9.
 *
 */

public class LifeAdapter extends RecyclerView.Adapter<LifeAdapter.ViewHolder> {


    private List<Life> mLifeList;

    public LifeAdapter(List<Life> lifeList ){
        mLifeList = lifeList;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView lifeImage;
        TextView modelLifeText;
        TextView lifeText;

        public ViewHolder(View view){
            super(view);
            lifeImage = (ImageView) view.findViewById(R.id.image_view_life_item);
            modelLifeText = (TextView) view.findViewById(R.id.text_view_model_life_item);
            lifeText = (TextView) view.findViewById(R.id.text_view_life_item);
        }
    }

    @Override
    public LifeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.life_item,parent,false);
        LifeAdapter.ViewHolder holder = new LifeAdapter.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(LifeAdapter.ViewHolder holder , int position){
        Life life = mLifeList.get(position);
        holder.modelLifeText.setText(life.getString_model_life());
        holder.lifeImage.setImageResource(life.getImageView_life());
        holder.lifeText.setText(life.getString_life());

    }

    @Override
    public int getItemCount(){
        return mLifeList.size();
    }





}
