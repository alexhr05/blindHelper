package com.example.saveimageinstorage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
//import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder>{
    ArrayList<Image> arrayList;
    Context context;
    private OnItemClickListner onItemClickListner;

    public ImageAdapter(Context context, ArrayList<Image> arrayList ){
        this.arrayList = arrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_main,parent,false);
        //View rowView = LayoutInflater.from(parent.getContext()).inflate(R.layout.listcontentstorechat,parent,false);
        //ViewHolder viewHolder = new ViewHolder(rowView);

        //return viewHolder;
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.title.setText(arrayList.get(position).getTitle());
        holder.size.setText(getSize(arrayList.get(position).getSize()));
        Glide.with(context).load(arrayList.get(position).getPath()).placeholder(R.drawable.ic_launcher_background).into(holder.imageView);
        holder.itemView.setOnClickListener(v -> onItemClickListner.onClick(v,arrayList.get(position).getPath()));
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }
    public static String getSize(long size){
        if(size<0){
            return "0";
        }

        double d = (double) size;
        int log10 = (int) (Math.log10(d)/Math.log10(1024.0d));
        StringBuilder stringBuilder = new StringBuilder();
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.#");
        double power = Math.pow(1024.0d,log10);
        stringBuilder.append(decimalFormat.format(d/power));
        stringBuilder.append(" ");
        stringBuilder.append(new String[] {"B","KB","MB","GB","TB"}[log10]);
        return  stringBuilder.toString();

    }



    public static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView title,size;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageItem);
            title = itemView.findViewById(R.id.itemTitle);
            size = itemView.findViewById(R.id.itemSize);



        }
    }

    public void setOnItemClickListner(OnItemClickListner onItemClickListener){
        this.onItemClickListner = onItemClickListener;
    }
    public interface OnItemClickListner{
        void onClick(View view, String path);
    }

}
