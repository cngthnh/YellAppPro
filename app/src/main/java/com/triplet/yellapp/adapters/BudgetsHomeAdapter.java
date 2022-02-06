package com.triplet.yellapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.triplet.yellapp.BudgetsFragment;
import com.triplet.yellapp.R;
import com.triplet.yellapp.models.BudgetCard;
import com.triplet.yellapp.utils.SessionManager;
import com.triplet.yellapp.viewmodels.UserViewModel;

import java.util.List;

public class BudgetsHomeAdapter extends RecyclerView.Adapter<BudgetsHomeAdapter.BudgetsHomeViewHolder>{
    private Context mContext = null;
    private List<BudgetCard> mListBudget;
    SessionManager sessionManager;
    UserViewModel userViewModel;

    public BudgetsHomeAdapter(Context mContext, SessionManager sessionManager, UserViewModel userViewModel) {
        this.mContext = mContext;
        this.sessionManager = sessionManager;
        this.userViewModel = userViewModel;
    }

    public void setData(List<BudgetCard> mListBudget) {
        this.mListBudget = mListBudget;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BudgetsHomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget_home, parent, false);
        return new BudgetsHomeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetsHomeViewHolder holder, int position) {
        BudgetCard budgetCard = mListBudget.get(position);
        if(budgetCard == null){
            return;
        }
        holder.nameBudget.setText(budgetCard.getName());
        String s = String.valueOf(budgetCard.getBalance())+" vnđ";
        holder.balance.setText(s);
        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                BudgetsFragment budgetFragment = new BudgetsFragment(budgetCard, sessionManager, userViewModel);
                activity.getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right,
                                R.anim.slide_in_right, R.anim.slide_out_left)
                        .replace(R.id.fragmentContainer,budgetFragment, "BUDGET")
                        .addToBackStack(null).commit();
            }
        });

        setProgressbar(holder, budgetCard);
    }

    private void setProgressbar(BudgetsHomeViewHolder holder, BudgetCard budgetCard) {
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

    @Override
    public int getItemCount() {
        if(mListBudget != null){
            if(mListBudget.size() > 5)
                return 5;
            return mListBudget.size();
        }
        return 0;
    }

    public class BudgetsHomeViewHolder extends RecyclerView.ViewHolder{
        private TextView nameBudget;
        private TextView balance;
        private CardView itemLayout;
        private ProgressBar progressBar;

        public BudgetsHomeViewHolder(@NonNull View itemView) {
            super(itemView);
            nameBudget = itemView.findViewById((R.id.name_bg_home_item));
            balance = itemView.findViewById(R.id.balance_bg_home);
            itemLayout = itemView.findViewById(R.id.item_budget_home);
            progressBar = itemView.findViewById(R.id.progressBar_bg_home);
        }
    }
}
