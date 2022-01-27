package com.triplet.yellapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.triplet.yellapp.R;
import com.triplet.yellapp.databinding.FragmentTaskBinding;

import java.util.ArrayList;

public class IconAdapter extends RecyclerView.Adapter<IconAdapter.IconViewHolder> {

    ArrayList<Integer> resourceList;
    Context context;

    public IconAdapter(Context context) {
        this.context = context;
    }

    public void setData(ArrayList<Integer> resourceList) {
        this.resourceList = resourceList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public IconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.icon_item,parent,false);
        return new IconViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IconViewHolder holder, int position) {
        Integer resource = resourceList.get(position);
        if (resource == null)
            return;
        holder.imageItem.setImageResource(resource);
        holder.imageItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCompatImageButton taskIcon = ((Activity)context).findViewById(R.id.taskIcon);
                taskIcon.setImageResource(resource);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (resourceList != null)
            return resourceList.size();
        return 0;
    }

    public class IconViewHolder extends RecyclerView.ViewHolder {
        AppCompatImageButton imageItem;
        public IconViewHolder(@NonNull View itemView) {
            super(itemView);
            imageItem = itemView.findViewById(R.id.image_item);
        }
    }
}
