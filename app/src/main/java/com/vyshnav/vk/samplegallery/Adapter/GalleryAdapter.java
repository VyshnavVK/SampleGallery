package com.vyshnav.vk.samplegallery.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.vyshnav.vk.samplegallery.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {
    private List<Gallery> list;
    private Context context;
    private OnClickListener onClickListener;
    private SparseBooleanArray selected_items;
    private int current_selected_idx = -1;

    public GalleryAdapter(List<Gallery> list, Context context) {
        this.list = list;
        this.context = context;
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
        selected_items = new SparseBooleanArray();
    }


    public void removeData(int position) {
        list.remove(position);
        resetCurrentIndex();
    }

    private void resetCurrentIndex() {
        current_selected_idx = -1;
    }


    public void toggleSelection(int pos) {
        current_selected_idx = pos;
        if (selected_items.get(pos, false)) {
            selected_items.delete(pos);
        } else {
            selected_items.put(pos, true);
        }
        notifyItemChanged(pos);
    }

    public void clearSelections() {
        selected_items.clear();
        notifyDataSetChanged();
    }


    public int getSelectedItemCount() {
        return selected_items.size();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(selected_items.size());
        for (int i = 0; i < selected_items.size(); i++) {
            items.add(selected_items.keyAt(i));
        }
        return items;
    }


    private void displayImage(ViewHolder holder, Gallery gallery) {
        if (gallery.getImagePath().isEmpty()) {
            holder.check.setImageDrawable(null);
        } else {
           holder.check.setImageResource(R.drawable.ic_check_circle);
        }
    }

    private void toggleCheckedIcon(ViewHolder holder, int position) {
        if (selected_items.get(position, false)) {
            holder.check.setVisibility(View.VISIBLE);
        } else {
            holder.check.setVisibility(View.GONE);
        }
        if (current_selected_idx == position) resetCurrentIndex();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.image_item,parent,false);
        return new ViewHolder(view);
    }

    @SuppressLint("CheckResult")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final Gallery gallery = list.get(position);
        Glide.with(context).load(new File(list.get(position).imagePath)).into(holder.image);


        holder.rl_1.setActivated(selected_items.get(position,false));
        holder.rl_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickListener.onGalleryItemClick(view,gallery,position);
            }
        });

        holder.rl_1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(onClickListener==null) return false;
                else {
                    onClickListener.onItemLongClick(view, gallery, position);
                }
                    return true;
            }
        });

        toggleCheckedIcon(holder, position);
        displayImage(holder, gallery);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface OnClickListener {
        void onGalleryItemClick(View view, Gallery gallery, int pos);
        void onItemLongClick(View view, Gallery gallery, int pos);
    }

        public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image,check;
        RelativeLayout rl_1;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        image = itemView.findViewById(R.id.image);
            rl_1= itemView.findViewById(R.id.rl_1);
            check = itemView.findViewById(R.id.check);
        }
    }
}
