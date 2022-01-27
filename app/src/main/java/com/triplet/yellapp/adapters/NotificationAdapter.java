package com.triplet.yellapp.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.moshi.Moshi;
import com.triplet.yellapp.R;
import com.triplet.yellapp.models.DashboardPermission;
import com.triplet.yellapp.models.InfoMessage;
import com.triplet.yellapp.models.Notification;
import com.triplet.yellapp.utils.ApiService;
import com.triplet.yellapp.utils.Client;
import com.triplet.yellapp.utils.SessionManager;

import java.text.ParseException;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>{

    private Context mContext = null;
    private List<Notification> mListNotification;

    Moshi moshi = new Moshi.Builder().build();
    SessionManager sessionManager;
    ApiService service;

    public NotificationAdapter(Context mContext, SessionManager sessionManager) {
        this.mContext = mContext;
        this.sessionManager = sessionManager;
    }

    public void setData(List<Notification> mListNotification) {
        this.mListNotification = mListNotification;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationAdapter.NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = mListNotification.get(position);

        holder.message.setText(notification.getMessage());

        try {
            Date date = new SimpleDateFormat("dd/mm/yyyy HH:mm:ss").parse(notification.getCreatedAt());
            Log.e("Date", date.toString());
        } catch (ParseException e) {
            Log.e("Date", e.getMessage());
        }

        if(notification.getRole() == null){
            holder.confirmed.setVisibility(View.GONE);
            holder.unconfirm.setVisibility(View.GONE);
        }
        else{
            holder.unconfirm.setVisibility(View.VISIBLE);
            holder.confirmButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    confirmInvited(notification);
                }
            });

            holder.cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    rejectInvited(notification);
                }
            });
        }
    }

    private void rejectInvited(Notification notification) {
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);
        Call<InfoMessage> call;

        String json = moshi.adapter(Notification.class).toJson(notification);
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), json);

        call = service.rejectInvited(requestBody);
        call.enqueue(new Callback<InfoMessage>() {
            @Override
            public void onResponse(Call<InfoMessage> call, Response<InfoMessage> response) {
                Log.w("YellReject", "onResponse: " + response);
                if(response.isSuccessful()){
                    notification.setRole(null);
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<InfoMessage> call, Throwable t) {
                Toast.makeText(mContext, "Lỗi khi kết nối với server", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void confirmInvited(Notification notification) {
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);
        Call<InfoMessage> call;

        String json = moshi.adapter(Notification.class).toJson(notification);
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), json);

        call = service.confirmInvited(requestBody);
        call.enqueue(new Callback<InfoMessage>() {
            @Override
            public void onResponse(Call<InfoMessage> call, Response<InfoMessage> response) {
                Log.w("YellConfirm", "onResponse: " + response);
                if(response.isSuccessful()){
                    notification.setRole(null);
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<InfoMessage> call, Throwable t) {
                Toast.makeText(mContext, "Lỗi khi kết nối với server", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        if(mListNotification != null){
            return mListNotification.size();
        }
        return 0;
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder{

        TextView message;
        TextView timeNotification;
        TextView confirmed;
        LinearLayout unconfirm;
        CardView confirmButton;
        CardView cancelButton;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.message);
            timeNotification = itemView.findViewById(R.id.time_noti);
            confirmed = itemView.findViewById(R.id.confirm);
            unconfirm = itemView.findViewById(R.id.unconfirm);
            confirmButton = itemView.findViewById(R.id.confirm_bt);
            cancelButton = itemView.findViewById(R.id.cancel_bt);
        }
    }
}
