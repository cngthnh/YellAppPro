package com.triplet.yellapp.adapters;

import static android.content.Context.MODE_PRIVATE;

import android.app.Application;
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
import android.widget.Toast;

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
import com.triplet.yellapp.models.DashboardPermission;
import com.triplet.yellapp.models.InfoMessage;
import com.triplet.yellapp.models.UserAccount;
import com.triplet.yellapp.repository.DashboardRepository;
import com.triplet.yellapp.utils.ApiService;
import com.triplet.yellapp.utils.Client;
import com.triplet.yellapp.utils.SessionManager;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardsAdapter extends RecyclerView.Adapter<DashboardsAdapter.DashboardsViewHolder>{

    private Context mContext = null;
    private List<DashboardCard> mListDashboard;
    private ViewBinderHelper viewBinderHelper = new ViewBinderHelper();
    SessionManager sessionManager;
    ApiService service;
    String uid;
    Moshi moshi = new Moshi.Builder().build();
    DashboardRepository repository;

    public DashboardsAdapter(Context mContext, SessionManager sessionManager) {
        this.mContext = mContext;
        this.sessionManager = sessionManager;
        this.uid =  mContext.getSharedPreferences(mContext.getResources().getString(R.string.yell_sp), MODE_PRIVATE)
                .getString("uid","n");
        this.repository = new DashboardRepository((Application) mContext.getApplicationContext());
    }

    public void setData(List<DashboardCard> mListDashboard) {
        this.mListDashboard = mListDashboard;
        notifyDataSetChanged();
    }

    public void addDashboardCard(DashboardCard dashboardCard) {
        mListDashboard.add(mListDashboard.size(),dashboardCard);
        notifyItemInserted(mListDashboard.size()+1);
    }

    public void removeDashboardCard(int position) {
        mListDashboard.remove(position);
        notifyItemRemoved(position);
    }

    @NonNull
    @Override
    public DashboardsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dashboard, parent, false);
        return new DashboardsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DashboardsViewHolder holder, int position) {
        DashboardCard dashboardCard = mListDashboard.get(position);
        if(dashboardCard == null){
            return;
        }

        holder.nameDashboard.setText(dashboardCard.getName());
        viewBinderHelper.bind(holder.swipeRevealLayout, String.valueOf(1));
        holder.deleteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String permission = getPermission(dashboardCard);
                if (permission.equals("admin")) {
                    openDialogDeleteDashboard(holder,dashboardCard);
                    return;
                }
                else {
                    Toast.makeText(mContext, "Bạn không có quyền thực hiện chức năng này", Toast.LENGTH_LONG).show();
                }
            }
        });

        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppCompatActivity activity = (AppCompatActivity) view.getContext();

                DashboardFragment dashboardFragment = new DashboardFragment(dashboardCard);
                activity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer,dashboardFragment, "DASHBOARD").addToBackStack(null).commit();
            }
        });

    }

    private String getPermission(DashboardCard dashboardCard) {
        List<DashboardPermission> permissions = dashboardCard.getUsers();
        for (int i = 0;i<permissions.size();i++) {
            if (uid.equals(permissions.get(i).getUid()))
                return permissions.get(i).getRole();
        }
        return null;
    }

    private void openDialogDeleteDashboard(DashboardsViewHolder holder, DashboardCard dashboardCard) {
        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_delete_dashboard);

        Window window = dialog.getWindow();
        if(window == null){
            return;
        }

        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        windowAttributes.gravity = Gravity.CENTER;
        window.setAttributes(windowAttributes);

        dialog.setCancelable(true);

        TextView title = dialog.findViewById(R.id.title_delete_db);
        TextView deleteBt = dialog.findViewById(R.id.delete_db);
        TextView cancelDeleteBt = dialog.findViewById(R.id.cancel_delete_db);


        String elementS = "Bạn có chắc là muốn xoá bảng ";
        String s = elementS + dashboardCard.getName() + " không?";

        Spannable spannable = new SpannableString(s);
        spannable.setSpan(new ForegroundColorSpan(Color.rgb(255,152,0)), elementS.length(), elementS.length() + dashboardCard.getName().length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        title.setText(spannable);

        deleteBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                repository.deleteDashboard(dashboardCard);
                removeDashboardCard(holder.getLayoutPosition());
                dialog.dismiss();
            }
        });

        cancelDeleteBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void deleteDashboardFromServer(DashboardCard dashboardCard) {
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);
        Call<InfoMessage> call;

        String json = moshi.adapter(DashboardCard.class).toJson(dashboardCard);
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), json);

        call = service.deleteDashboard(requestBody);
        call.enqueue(new Callback<InfoMessage>() {
            @Override
            public void onResponse(Call<InfoMessage> call, Response<InfoMessage> response) {
                Log.w("YellDeleteDashboard", "onResponse: " + response);
            }

            @Override
            public void onFailure(Call<InfoMessage> call, Throwable t) {
                Log.w("YellDeleteDashboard", "onFailure: " + t.getMessage() );
            }
        });
    }

    private void checkPermission(DashboardCard dashboardCard, DashboardsViewHolder holder) {
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);
        Call<UserAccount> call;
        call = service.getUserProfile("compact");
        call.enqueue(new Callback<UserAccount>() {
            @Override
            public void onResponse(Call<UserAccount> call, Response<UserAccount> response) {
                Log.w("YellGetListDashboard", "onResponse: " + response);
                if (response.isSuccessful()) {
                    String uid = response.body().getId();
                    for(int i = 0; i < dashboardCard.getUsers().size(); i++){
                        if(uid.equals(dashboardCard.getUsers().get(i).getUid())){

                            if(dashboardCard.getUsers().get(i).getRole().equals("admin"))
                            {
                                openDialogDeleteDashboard(holder, dashboardCard);
                                return;
                            }
                            else {
                                Toast.makeText(mContext, "Bạn không có quyền thực hiện chức năng này", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<UserAccount> call, Throwable t) {
                Log.w("YellGetListDashboard", "onFailure: " + t.getMessage() );
            }
        });
    }

    @Override
    public int getItemCount() {
        if(mListDashboard != null){
            return mListDashboard.size();
        }
        return 0;
    }

    public class DashboardsViewHolder extends RecyclerView.ViewHolder{
        private ImageView cover;
        private TextView nameDashboard;
        private TextView label;
        private SwipeRevealLayout swipeRevealLayout;
        private CardView deleteLayout;
        private CardView itemLayout;

        public DashboardsViewHolder(@NonNull View itemView) {
            super(itemView);
            cover = itemView.findViewById(R.id.cover_image);
            nameDashboard = itemView.findViewById((R.id.name_db_item));
            label = itemView.findViewById(R.id.label);
            swipeRevealLayout = itemView.findViewById(R.id.swipe);
            deleteLayout = itemView.findViewById(R.id.delete_layout);
            itemLayout = itemView.findViewById(R.id.item_layout);
        }
    }
}
