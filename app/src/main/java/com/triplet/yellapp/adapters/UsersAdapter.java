package com.triplet.yellapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.triplet.yellapp.R;
import com.triplet.yellapp.models.DashboardCard;
import com.triplet.yellapp.models.DashboardPermission;

import java.util.List;
import java.util.Locale;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHolder>{

    private Context mContext = null;
    private List<DashboardPermission> mListUserName;

    public UsersAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setData(List<DashboardPermission> mListUserName) {
        this.mListUserName = mListUserName;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UsersAdapter.UsersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersViewHolder holder, int position) {
        DashboardPermission userName = mListUserName.get(position);
        if(userName == null){
            return;
        }
        holder.userName.setText(userName.getUid().substring(0,1).toUpperCase(Locale.ROOT));
    }

    @Override
    public int getItemCount() {
        if(mListUserName != null){
            if(mListUserName.size() > 3)
                return 3;
            return mListUserName.size();
        }
        return 0;
    }

    public class UsersViewHolder extends RecyclerView.ViewHolder{

        TextView userName;
        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.name_user);
        }
    }
}
