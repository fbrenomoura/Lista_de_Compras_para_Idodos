package com.breno.listadecomprasparaidosos;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.ArrayList;

public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.ViewHolder> {

    ArrayList<String> data;
    ArrayList<URL>    image;
    Context           context;

    public RecycleAdapter(Context context, ArrayList<String> data, ArrayList<URL> image) {
        this.data    = data;
        this.image   = image;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View           view           = layoutInflater.inflate(R.layout.recyclerview_row, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.itemTV.setText(data.get(holder.getAdapterPosition()));
        System.out.println("URL: " + image.get(holder.getAdapterPosition()).toString());
        /*Picasso.get()
                .load(image.get(holder.getAdapterPosition()).toString())
                .resize(1200,600)
                .noFade()
                .into(holder.imageView);*/
        Glide.with(context).load(image.get(holder.getAdapterPosition()).toString()).into(holder.imageView);

        //SHORT CLICK TO MARK AS PURCHASED
        holder.imageView.setOnClickListener(v -> {
            ColorMatrix            matrix = new ColorMatrix();
            ColorMatrixColorFilter filter;
            if(holder.bought){
                matrix.setSaturation(1);
                filter = new ColorMatrixColorFilter(matrix);
                holder.imageView.setColorFilter(filter);
                holder.itemTV.setPaintFlags(holder.itemTV.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                holder.bought = false;
            }
            else{
                matrix.setSaturation(0);
                filter = new ColorMatrixColorFilter(matrix);
                holder.imageView.setColorFilter(filter);
                holder.itemTV.setPaintFlags(holder.itemTV.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.bought = true;
                Toast.makeText(context, context.getResources().getString(R.string.purchased) + " " + data.get(holder.getAdapterPosition()).toUpperCase(), Toast.LENGTH_SHORT).show();
            }
            });

        //LONG CLICK TO EDIT QUANTITY
        holder.imageView.setOnLongClickListener(v -> {
            if (holder.editingVisible) {
                holder.fabAddItem.hide();
                holder.fabRemoveItem.hide();
                holder.editingVisible = false;
            } else {
                holder.fabAddItem.show();
                holder.fabRemoveItem.show();
                holder.editingVisible = true;
            }
            return true;
        });

        //ADD 1 QUANTITY
        holder.fabAddItem.setOnClickListener(v -> {
            int temp = Integer.parseInt(data.get(holder.getAdapterPosition()).split(" ")[0]) + 1;
            MainActivity.speechAsText.set(holder.getAdapterPosition()/2, String.valueOf(temp));
            data.set(holder.getAdapterPosition(), temp + " " + data.get(holder.getAdapterPosition()).split(" ")[1]);
            holder.itemTV.setText(data.get(holder.getAdapterPosition()));
        });

        //REMOVE 1 QUANTITY
        holder.fabRemoveItem.setOnClickListener(v -> {
            int temp = Integer.parseInt(data.get(holder.getAdapterPosition()).split(" ")[0]) - 1;

            if(temp > 0) {
                data.set(holder.getAdapterPosition(), temp + " " + data.get(holder.getAdapterPosition()).split(" ")[1]);
                MainActivity.speechAsText.set(holder.getAdapterPosition()/2, String.valueOf(temp));
                holder.itemTV.setText(data.get(holder.getAdapterPosition()));
            }
            else{
                Toast.makeText(context, context.getResources().getString(R.string.swipeToRemove), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView             itemTV;
        ImageView            imageView;
        FloatingActionButton fabAddItem, fabRemoveItem;
        boolean              bought         = false;
        boolean              editingVisible = false;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemTV        = itemView.findViewById(R.id.itemRvTv);
            imageView     = itemView.findViewById(R.id.imageViewRv);
            fabAddItem    = itemView.findViewById(R.id.fabAddQtd);
            fabRemoveItem = itemView.findViewById(R.id.fabRemoveQtd);
        }
    }

}
