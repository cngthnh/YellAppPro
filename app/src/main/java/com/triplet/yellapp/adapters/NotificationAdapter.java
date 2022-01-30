package com.triplet.yellapp.adapters;

import android.content.Context;
import android.icu.util.TimeZone;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.moshi.Moshi;
import com.triplet.yellapp.R;
import com.triplet.yellapp.models.InfoMessage;
import com.triplet.yellapp.models.Notification;
import com.triplet.yellapp.utils.ApiService;
import com.triplet.yellapp.utils.Client;
import com.triplet.yellapp.utils.SessionManager;
import com.triplet.yellapp.viewmodels.UserViewModel;

import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>{

    private List<Notification> mListNotification;
    private FragmentActivity activity;
    private UserViewModel userViewModel;

    public NotificationAdapter(FragmentActivity activity, UserViewModel viewModel) {
        this.activity = activity;
        this.userViewModel = viewModel;
        userViewModel.getNotificationLiveData().observe(activity, new Observer<Notification>() {
            @Override
            public void onChanged(Notification notification) {
                notifyDataSetChanged();
            }
        });
    }

    public void setData(List<Notification> mListNotification) {
        Collections.reverse(mListNotification);
        this.mListNotification = mListNotification;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationAdapter.NotificationViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = mListNotification.get(position);

        holder.message.setText(notification.getMessage());

        holder.timeNotification.setText(serverTime2MobileTime(notification.getCreatedAt()));

        if(notification.getRole() == null){
            holder.confirmed.setVisibility(View.GONE);
            holder.unconfirmed.setVisibility(View.GONE);
        }
        else{
            holder.unconfirmed.setVisibility(View.VISIBLE);
            holder.confirmButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    userViewModel.acceptNotify(notification);
                }
            });

            holder.cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    userViewModel.reject(notification);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if(mListNotification != null){
            return mListNotification.size();
        }
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private String serverTime2MobileTime(String time) {
        android.icu.text.SimpleDateFormat currentFormat = new android.icu.text.SimpleDateFormat("HH:mm  dd/MM/yyyy");
        currentFormat.setTimeZone(TimeZone.getTimeZone("GMT+7:00"));
        android.icu.text.SimpleDateFormat isoFormat = new android.icu.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date date = isoFormat.parse(time);
            return currentFormat.format(date);
        } catch (ParseException e) {
            Log.e("TimeParseError", "Time Parse Error");
            return null;
        }
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder{

        TextView message;
        TextView timeNotification;
        TextView confirmed;
        LinearLayout unconfirmed;
        CardView confirmButton;
        CardView cancelButton;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.message);
            timeNotification = itemView.findViewById(R.id.time_noti);
            confirmed = itemView.findViewById(R.id.confirm);
            unconfirmed = itemView.findViewById(R.id.unconfirm);
            confirmButton = itemView.findViewById(R.id.confirm_bt);
            cancelButton = itemView.findViewById(R.id.cancel_bt);
        }
    }
}
