package com.curiousfreaks.greword;

import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gasaini on 2/24/2018.
 */

public class wordListRecyclerAdapter extends RecyclerView.Adapter<wordListRecyclerAdapter.ViewHolder> implements View.OnClickListener{

    private List<wordDefinition> completeWordList;
    private int save_item_position = 0;
    myRecyclerItemClickListner myListner;

    public wordListRecyclerAdapter()
    {

        //completeWordList=aList;
    }

    public void setWordsList(List<wordDefinition> list)
    {
        this.completeWordList = list;
    }
    public interface myRecyclerItemClickListner{
        //void onIDClicked(View view, int position, long uniqueId);
        void onWordClicked(View view, int position, long uniqueId);
        void onMeaningClicked(View view, int position, long uniqueId);
        void onStarClicked(View view, int position, long uniqueId);
        void onSpeakClicked(View view, int position, long uniqueId);
        void onMainLayoutClicked(View view, int position, long uniqueId);

    }
    public void setOnItemCLickListner(myRecyclerItemClickListner lstn)
    {
        this.myListner=lstn;
    }

    @Override
    public void onClick(View view) {
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView word,meaning;
        ImageView starImage,speakImage;
        RelativeLayout mainLayout;
        public ViewHolder(View view)
        {
            super(view);
            //id=view.findViewById(R.id.wid);
            word=view.findViewById(R.id.word);
            meaning=view.findViewById(R.id.meaning);
            starImage=view.findViewById(R.id.starImage);
            speakImage=view.findViewById(R.id.speak);
            mainLayout = view.findViewById(R.id.layout1);
        }

    }

    @Override
    public wordListRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.one_word_layout, parent, false);
        return (new ViewHolder(v));
    }

    @Override
    public void onBindViewHolder(final wordListRecyclerAdapter.ViewHolder holder, final int position) {
        wordDefinition wd = completeWordList.get(position);
        final long uniqueID = wd.getId();
        holder.meaning.setText(wd.getMeaning());
        holder.word.setText(wd.getWord());
        if ((wd.getBookmarked()).equals("YES"))
            holder.starImage.setImageResource(R.mipmap.yellow_star);
        else
            holder.starImage.setImageResource(R.mipmap.outline_star_green);

        holder.word.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myListner.onWordClicked(view, position, uniqueID);
            }
        });
        holder.meaning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myListner.onMeaningClicked(view, position, uniqueID);
            }
        });
        holder.starImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myListner.onStarClicked(view, position, uniqueID);
            }
        });
        holder.speakImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myListner.onSpeakClicked(view, position, uniqueID);
            }
        });
        holder.mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setClickItemAnimation(view);
                final View v = view;
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        myListner.onMainLayoutClicked(v,position,uniqueID);
                    }
                }, 105);

                //selectedItemPosition=position;
                //view.setBackgroundColor(Color.parseColor("#1b5e20"));
                //notifyItemChanged(selectedItemPosition);
            }
        });

        if (save_item_position<position) {
            setLIstAnimation(holder.itemView);
        }
        save_item_position = position;

        //holder.itemView.setBackgroundColor(selectedItemPosition == position ? Color.GREEN : Color.TRANSPARENT);

    }

    @Override
    public int getItemCount() {

        return completeWordList.size();
    }

    public void setFilter(List<wordDefinition> fitleredWords)
    {
        completeWordList = new ArrayList<>();
        completeWordList.addAll(fitleredWords);
        notifyDataSetChanged();

    }

    @Override
    public void onViewDetachedFromWindow(ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        //holder.itemView.clearAnimation();
    }
    public void setLIstAnimation(View view) {

        AnimationSet animationSet = new AnimationSet(true);
        TranslateAnimation trnsAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF,
                0.0f);
        trnsAnimation.setDuration(400);
        animationSet.addAnimation(trnsAnimation);

        ScaleAnimation scaleAnim = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnim.setInterpolator(new LinearInterpolator());
        scaleAnim.setDuration(100);
        animationSet.addAnimation(scaleAnim);

        view.startAnimation(animationSet);

    }
    public void setClickItemAnimation(View view) {

        ScaleAnimation scaleAnim = new ScaleAnimation(1.0f, 0.9f, 1.0f, 0.9f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnim.setDuration(100);
        view.startAnimation(scaleAnim);
    }
}
