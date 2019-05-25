package com.example.a17280.whether.adapter;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.a17280.whether.Interface.MyCityBack;
import com.example.a17280.whether.R;
import com.example.a17280.whether.entity.City;

import java.util.List;

/**
 * Created by 17280 on 2019/5/15.
 */

public class MyCityAdapter extends RecyclerView.Adapter<MyCityAdapter.ViewHolder> {
    private List<City> mMyCityList;
    protected MyCityBack mMyCityBack;


    public MyCityAdapter(List<City> list,MyCityBack mycityBack){
        mMyCityList = list;
        mMyCityBack = mycityBack;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView textView ;
        View cityView;

        public ViewHolder(View view){
            super(view);
            cityView = view;
            textView = (TextView) view.findViewById(R.id.text_view_my_city_recycler_item);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_city_item,parent,false);
        final ViewHolder holder = new ViewHolder(view);

        holder.cityView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                City city = mMyCityList.get(position);
                mMyCityBack.onFinish(city.getName());
            }
        });
        holder.cityView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int position = holder.getAdapterPosition();
                mMyCityBack.onLongFinish(position , v);
                return true;
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder , int position){
        City city = mMyCityList.get(position);
        holder.textView.setText(city.getName());
    }

    public void removeItem(int pos){
        mMyCityList.remove(pos);
        notifyItemRemoved(pos);
    }

    public void del(final View view , final int position) {

        final float f = view.getX();
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX", 0, 1500);
        animator.setDuration(1000);
        animator.start();
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mMyCityList.remove(position);
                notifyDataSetChanged();
                view.setX(f);

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }


    @Override
    public int getItemCount(){
        return mMyCityList.size();
    }

}
