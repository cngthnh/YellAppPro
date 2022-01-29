package com.triplet.yellapp.adapters;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.squareup.moshi.Moshi;
import com.triplet.yellapp.DashboardFragment;
import com.triplet.yellapp.R;
import com.triplet.yellapp.models.DashboardCard;
import com.triplet.yellapp.models.InfoMessage;
import com.triplet.yellapp.models.UserAccount;
import com.triplet.yellapp.utils.ApiService;
import com.triplet.yellapp.utils.Client;
import com.triplet.yellapp.utils.SessionManager;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardsHomeAdapter extends RecyclerView.Adapter<DashboardsHomeAdapter.DashboardsHomeViewHolder>{

    private Context mContext = null;
    private List<DashboardCard> mListDashboard;
    SessionManager sessionManager;

    public DashboardsHomeAdapter(Context mContext, SessionManager sessionManager) {
        this.mContext = mContext;
        this.sessionManager = sessionManager;
    }

    public void setData(List<DashboardCard> mListDashboard) {
        this.mListDashboard = mListDashboard;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DashboardsHomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dashboard_home, parent, false);
        return new DashboardsHomeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DashboardsHomeViewHolder holder, int position) {
        DashboardCard dashboardCard = mListDashboard.get(position);
        if(dashboardCard == null){
            return;
        }
        holder.nameDashboard.setText(dashboardCard.getName());

        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                DashboardFragment dashboardFragment = new DashboardFragment(dashboardCard, sessionManager);
                activity.getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right,
                                R.anim.slide_in_right, R.anim.slide_out_left)
                        .replace(R.id.fragmentContainer, dashboardFragment, "DASHBOARD").addToBackStack(null).commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        if(mListDashboard != null){
            if(mListDashboard.size() > 5)
                return 5;
            return mListDashboard.size();
        }
        return 0;
    }

    public class DashboardsHomeViewHolder extends RecyclerView.ViewHolder{
        private TextView nameDashboard;
        private TextView label;
        private CardView itemLayout;

        public DashboardsHomeViewHolder(@NonNull View itemView) {
            super(itemView);
            nameDashboard = itemView.findViewById((R.id.name_db_home_item));
            label = itemView.findViewById(R.id.label_db_home);
            itemLayout = itemView.findViewById(R.id.item_layout_home);
        }
    }
}
