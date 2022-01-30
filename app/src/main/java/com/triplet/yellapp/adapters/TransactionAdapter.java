package com.triplet.yellapp.adapters;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
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
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.triplet.yellapp.R;
import com.triplet.yellapp.models.TransactionCard;
import com.triplet.yellapp.utils.SessionManager;
import com.triplet.yellapp.viewmodels.BudgetViewModel;

import java.util.ArrayList;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>{

    FragmentActivity activity;
    private List<TransactionCard> mListTransaction;
    private final ViewBinderHelper viewBinderHelper = new ViewBinderHelper();
    SessionManager sessionManager;
    BudgetViewModel budgetViewModel;
    int balance;

    public TransactionAdapter(FragmentActivity activity, SessionManager sessionManager, BudgetViewModel budgetViewModel) {
        this.activity = activity;
        this.sessionManager = sessionManager;
        this.budgetViewModel = budgetViewModel;
    }

    public void setData(List<TransactionCard> mListTransaction, int balance) {
        this.mListTransaction = mListTransaction;
        this.balance = balance;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        TransactionCard transactionCard = mListTransaction.get(position);
        if(transactionCard == null){
            return;
        }

        holder.nameTrans.setText(transactionCard.getPurpose());
        holder.content.setText(transactionCard.getContent());
        holder.amount.setText(String.valueOf(transactionCard.getAmount()));

        if(transactionCard.getPurpose().equals("Ăn uống")) {
            holder.categoryImg.setImageResource(R.drawable.ic_pizza);
            holder.categoryImg.setColorFilter(Color.rgb(255, 152, 0));
            holder.amount.setTextColor(Color.rgb(255,152,0));
        }
        else if(transactionCard.getPurpose().equals("Mua sắm")) {
            holder.categoryImg.setImageResource(R.drawable.ic_basket);
            holder.categoryImg.setColorFilter(Color.rgb(255, 152, 0));
            holder.amount.setTextColor(Color.rgb(255,152,0));
        }
        else if(transactionCard.getPurpose().equals("Sinh hoạt hằng ngày")) {
            holder.categoryImg.setImageResource(R.drawable.ic_home_line);
            holder.categoryImg.setColorFilter(Color.rgb(255, 152, 0));
            holder.amount.setTextColor(Color.rgb(255,152,0));
        }
        else if(transactionCard.getPurpose().equals("Cà phê")) {
            holder.categoryImg.setImageResource(R.drawable.ic_coffee);
            holder.categoryImg.setColorFilter(Color.rgb(255, 152, 0));
            holder.amount.setTextColor(Color.rgb(255,152,0));
        }
        else if(transactionCard.getPurpose().equals("Di chuyển")) {
            holder.categoryImg.setImageResource(R.drawable.ic_car_alt);
            holder.categoryImg.setColorFilter(Color.rgb(255, 152, 0));
            holder.amount.setTextColor(Color.rgb(255,152,0));
        }
        else if(transactionCard.getPurpose().equals("Du lịch")) {
            holder.categoryImg.setImageResource(R.drawable.ic_plane);
            holder.categoryImg.setColorFilter(Color.rgb(255, 152, 0));
            holder.amount.setTextColor(Color.rgb(255,152,0));
        }
        else if(transactionCard.getPurpose().equals("Lương tháng")) {
            holder.categoryImg.setImageResource(R.drawable.ic_salary);
            holder.categoryImg.setColorFilter(Color.rgb(4, 69, 173));
            holder.amount.setTextColor(Color.rgb(4,69,173));
        }else if(transactionCard.getPurpose().equals("Tiết kiệm")) {
            holder.categoryImg.setImageResource(R.drawable.ic_savings);
            holder.categoryImg.setColorFilter(Color.rgb(4, 69, 173));
            holder.amount.setTextColor(Color.rgb(4,69,173));
        }else if(transactionCard.getPurpose().equals("Bán đồ cũ")) {
            holder.categoryImg.setImageResource(R.drawable.ic_dealing);
            holder.categoryImg.setColorFilter(Color.rgb(4, 69, 173));
            holder.amount.setTextColor(Color.rgb(4,69,173));
        }else if(transactionCard.getPurpose().equals("Tiền lời")) {
            holder.categoryImg.setImageResource(R.drawable.ic_interest);
            holder.categoryImg.setColorFilter(Color.rgb(4, 69, 173));
            holder.amount.setTextColor(Color.rgb(4,69,173));
        }


        viewBinderHelper.bind(holder.swipeRevealLayout, String.valueOf(1));
        holder.deleteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(balance > transactionCard.getAmount())
                    openDialogDeleteTransaction(holder, transactionCard);
                else
                    Toast.makeText(activity, "Không thể xoá giao dịch lớn hơn số dư hiện tại", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void openDialogDeleteTransaction(TransactionViewHolder holder, TransactionCard transactionCard) {
        final Dialog dialog = new Dialog(activity);
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
        TextView deleteTs = dialog.findViewById(R.id.delete_db);
        TextView cancelDeleteTs = dialog.findViewById(R.id.cancel_delete_db);


        String elementS = "Bạn có chắc là muốn xoá giao dịch ";
        String s = elementS + transactionCard.getContent() + " không?";

        Spannable spannable = new SpannableString(s);
        spannable.setSpan(new ForegroundColorSpan(Color.rgb(255,152,0)), elementS.length(), elementS.length() + transactionCard.getContent().length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        title.setText(spannable);

        deleteTs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                budgetViewModel.deleteTransaction(transactionCard);
                dialog.dismiss();
            }
        });

        cancelDeleteTs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    @Override
    public int getItemCount() {
        if(mListTransaction != null){
            return mListTransaction.size();
        }
        return 0;
    }

    public void filterList(List<TransactionCard> filteredList) {
        mListTransaction = filteredList;
        notifyDataSetChanged();
    }

    public class TransactionViewHolder extends RecyclerView.ViewHolder{
        private ImageView categoryImg;
        private AppCompatTextView nameTrans;
        private AppCompatTextView content;
        private TextView date;
        private TextView amount;
        private SwipeRevealLayout swipeRevealLayout;
        private CardView deleteLayout;
        private CardView itemLayout;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryImg = itemView.findViewById((R.id.categoryImg));
            nameTrans = itemView.findViewById((R.id.transactionNameItem));
            content = itemView.findViewById((R.id.contentTransaction));
            amount = itemView.findViewById((R.id.amount));
            date = itemView.findViewById((R.id.dateTransaction));
            swipeRevealLayout = itemView.findViewById(R.id.swipeTransaction);
            deleteLayout = itemView.findViewById(R.id.deleteTransactionItem);
            itemLayout = itemView.findViewById(R.id.item_layout_transaction);
        }
    }
}
