package com.triplet.yellapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.triplet.yellapp.R;
import com.triplet.yellapp.models.DashboardPermission;

import java.util.List;

public class UsersDetailAdapter extends RecyclerView.Adapter<UsersDetailAdapter.UsersDetailViewHolder> {
    private Context mContext = null;
    private List<DashboardPermission> mListUser;

    public UsersDetailAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setData(List<DashboardPermission> mListUser) {
        this.mListUser = mListUser;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UsersDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_detail, parent, false);
        return new UsersDetailAdapter.UsersDetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersDetailViewHolder holder, int position) {
        DashboardPermission dashboardPermission = mListUser.get(position);
        if(dashboardPermission == null){
            return;
        }
        holder.userId.setText(dashboardPermission.getUid() + "(" + dashboardPermission.getRole() + ")");

    }

    @Override
    public int getItemCount() {
        if(mListUser != null){
            return mListUser.size();
        }
        return 0;
    }

    public class UsersDetailViewHolder extends RecyclerView.ViewHolder{

        AppCompatTextView userId;
        ImageView editPermission;
        ImageView deletePermission;
        public UsersDetailViewHolder(@NonNull View itemView) {
            super(itemView);
            userId = itemView.findViewById(R.id.user_id);
            editPermission = itemView.findViewById(R.id.edit_permission);
            deletePermission = itemView.findViewById(R.id.delete_permission);
        }
    }

}
