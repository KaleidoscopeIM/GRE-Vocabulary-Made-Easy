package com.curiousfreaks.greword;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;


/**
 * Created by gasaini on 3/24/2018.
 */

public class imageRecyclerAdapter extends RecyclerView.Adapter<imageRecyclerAdapter.imageViewHolder> implements View.OnClickListener{

    public List<imageDefinition> imagesList;
    imageClickInterface imgInterfaceListner;

    public interface imageClickInterface{
        void OnImageClick(List<imageDefinition> imagesList);
    }
    public void setImageClickListner(imageClickInterface lstn)
    {
        this.imgInterfaceListner=lstn;
    }


    public imageRecyclerAdapter(List<imageDefinition> l)
    {
        imagesList=l;
    }
    @Override
    public imageRecyclerAdapter.imageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.one_image, parent, false);
        return (new imageViewHolder(v));
    }

    @Override
    public void onBindViewHolder(imageViewHolder holder, int position) {
        imageDefinition oneImage=imagesList.get(position);
        //Long id=oneImage.getId();
        Bitmap bitmap=oneImage.getBitmap();
        holder.image.setImageBitmap(getScaledBitmap(bitmap,620,420));
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgInterfaceListner.OnImageClick(imagesList);
            }
        });
        //holder.imageId.setText(id.toString());
    }

    @Override
    public int getItemCount() {
        return imagesList.size();
    }

    @Override
    public void onClick(View view) {
        Log.d(MainActivity.TAG,"clicl on image items");
    }

    public static class imageViewHolder extends RecyclerView.ViewHolder{
        ImageView image;
        TextView imageId;
        public imageViewHolder(View view)
        {
            super(view);
            image=view.findViewById(R.id.oneImage);
            //imageId=view.findViewById(R.id.imageId);
        }

    }
    public Bitmap getScaledBitmap(Bitmap originalBitmap, float scaledWidth, float scaledHeight)
    {
        Bitmap scaledBitmap=null;
        if(originalBitmap!=null) {
            int originalWidth = originalBitmap.getWidth();
            int originalHeight = originalBitmap.getHeight();
            float heightRatio=scaledHeight/originalHeight;

            int finalWidth=(int)Math.floor(originalWidth*heightRatio);
            int finalHeight=(int)Math.floor(originalHeight*heightRatio);

            scaledBitmap=Bitmap.createScaledBitmap(originalBitmap,finalWidth,finalHeight,true);


        }

        return scaledBitmap;
    }
}
