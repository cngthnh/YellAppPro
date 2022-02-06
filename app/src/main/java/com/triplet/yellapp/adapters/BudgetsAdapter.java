package com.triplet.yellapp.adapters;

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
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.squareup.moshi.Moshi;
import com.triplet.yellapp.BudgetsFragment;
import com.triplet.yellapp.R;
import com.triplet.yellapp.models.BudgetCard;
import com.triplet.yellapp.models.InfoMessage;
import com.triplet.yellapp.repository.BudgetRepository;
import com.triplet.yellapp.utils.ApiService;
import com.triplet.yellapp.utils.Client;
import com.triplet.yellapp.utils.SessionManager;
import com.triplet.yellapp.viewmodels.UserViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BudgetsAdapter extends RecyclerView.Adapter<BudgetsAdapter.BudgetsViewHolder>{

    private Context mContext = null;
    private List<BudgetCard> mListBudget;
    private ViewBinderHelper viewBinderHelper = new ViewBinderHelper();
    SessionManager sessionManager;
    ApiService service;
    Moshi moshi = new Moshi.Builder().build();
    BudgetRepository repository;
    UserViewModel userViewModel;

    public BudgetsAdapter(Context mContext, SessionManager sessionManager, UserViewModel userViewModel) {
        this.mContext = mContext;
        this.sessionManager = sessionManager;
        this.repository = new BudgetRepository((Application) mContext.getApplicationContext());
        this.userViewModel = userViewModel;
    }

    public void setData(List<BudgetCard> mListBudget) {
        this.mListBudget = mListBudget;
        notifyDataSetChanged();
    }

    public void removeBudgetCard(int position) {
        mListBudget.remove(position);
        notifyItemRemoved(position);
    }

    @NonNull
    @Override
    public BudgetsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget, parent, false);
        return new BudgetsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetsViewHolder holder, int position) {
        BudgetCard budgetCard = mListBudget.get(position);
        int progress;
        if(budgetCard == null){
            return;
        }
        holder.budgetName.setText(budgetCard.getName());
        holder.balance.setText(String.valueOf(budgetCard.balance));

        viewBinderHelper.bind(holder.swipeRevealLayout, String.valueOf(1));
        holder.deleteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialogDeleteBudget(holder, budgetCard);
            }
        });


        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                BudgetsFragment budgetsFragment = new BudgetsFragment(budgetCard, sessionManager, userViewModel);
                activity.getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right,
                                R.anim.slide_in_right, R.anim.slide_out_left)
                        .replace(R.id.fragmentContainer,budgetsFragment, "BUDGET")
                        .addToBackStack(null).commit();
            }
        });

        setProgressbar(holder, budgetCard);





    }

    private void setProgressbar(BudgetsViewHolder holder, BudgetCard budgetCard) {
        int totalOutcome = 0;
        int total;
        int percentage;


        if(budgetCard.getTransactionsList() == null)
        {
            return;
        }

        for(int i = 0; i < budgetCard.getTransactionsList().size(); i++)
        {
            if(budgetCard.getTransactionsList().get(i).getAmount() < 0)
                totalOutcome -= budgetCard.getTransactionsList().get(i).amount;
        }

        total = budgetCard.getBalance() + totalOutcome;


        if(budgetCard.getType() != 0)
        {
            percentage = (budgetCard.balance * 100)/ budgetCard.threshold;
            if(percentage > 100)
                percentage = 100;
        }
        else{
            if(total == 0)
                percentage = 0;
            else
                percentage = (totalOutcome * 100) /total;
        }

        holder.progressBar.setProgress(percentage);
    }

    private void openDialogDeleteBudget(BudgetsViewHolder holder, BudgetCard budgetCard) {
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


        String elementS = "Bạn có chắc là muốn xoá sổ tay ";
        String s = elementS + budgetCard.getName() + " không?";

        Spannable spannable = new SpannableString(s);
        spannable.setSpan(new ForegroundColorSpan(Color.rgb(255,152,0)), elementS.length(), elementS.length() + budgetCard.getName().length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        title.setText(spannable);

        deleteBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                repository.deleteBudget(budgetCard);
                removeBudgetCard(holder.getLayoutPosition());
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

    private void deleteBudgetFromServer(BudgetCard budgetCard) {
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);
        Call<InfoMessage> call;

        String json = moshi.adapter(BudgetCard.class).toJson(budgetCard);
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), json);

        call = service.deleteBudgets(requestBody);

        call.enqueue(new Callback<InfoMessage>() {
            @Override
            public void onResponse(Call<InfoMessage> call, Response<InfoMessage> response) {
                Log.w("YellDeleteBudget", "onResponse: " + response);
            }

            @Override
            public void onFailure(Call<InfoMessage> call, Throwable t) {
                Log.w("YellDeleteBudget", "onFailure: " + t.getMessage() );
            }
        });
    }

    @Override
    public int getItemCount() {
        if(mListBudget != null){
            return mListBudget.size();
        }
        return 0;
    }

    public void filterList(List<BudgetCard> filteredList) {
        mListBudget = filteredList;
        notifyDataSetChanged();
    }

    public class BudgetsViewHolder extends RecyclerView.ViewHolder{
        private TextView budgetName;
        private TextView balance;
        private SwipeRevealLayout swipeRevealLayout;
        private CardView deleteLayout;
        private CardView itemLayout;
        private ProgressBar progressBar;

        public BudgetsViewHolder(@NonNull View itemView) {
            super(itemView);
            budgetName = itemView.findViewById((R.id.budgetNameItem));
            swipeRevealLayout = itemView.findViewById(R.id.swipeBudget);
            balance=itemView.findViewById(R.id.budgetBalanceItem);
            deleteLayout = itemView.findViewById(R.id.deleteBudgetItem);
            itemLayout = itemView.findViewById(R.id.item_layout_budget);
            progressBar = itemView.findViewById(R.id.progressBarBudget);
        }
    }
}
