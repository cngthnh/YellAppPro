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
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.squareup.moshi.Moshi;
import com.triplet.yellapp.R;
import com.triplet.yellapp.models.BudgetCard;
import com.triplet.yellapp.models.InfoMessage;
import com.triplet.yellapp.models.TransactionCard;
import com.triplet.yellapp.utils.ApiService;
import com.triplet.yellapp.utils.Client;
import com.triplet.yellapp.utils.SessionManager;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>{

    private Context mContext = null;
    private List<TransactionCard> mListTransaction;
    private ViewBinderHelper viewBinderHelper = new ViewBinderHelper();
    SessionManager sessionManager;
    ApiService service;
    Moshi moshi = new Moshi.Builder().build();

    public TransactionAdapter(Context mContext, SessionManager sessionManager) {
        this.mContext = mContext;
        this.sessionManager = sessionManager;
    }

    public void setData(List<TransactionCard> mListTransaction) {
        this.mListTransaction = mListTransaction;
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

        if(transactionCard.getPurpose().equals("Ăn uống"))
            holder.categoryImg.setImageResource(R.drawable.ic_pizza);
        else if(transactionCard.getPurpose().equals("Mua sắm"))
            holder.categoryImg.setImageResource(R.drawable.ic_basket);
        else if(transactionCard.getPurpose().equals("Sinh hoạt hằng ngày"))
            holder.categoryImg.setImageResource(R.drawable.ic_home_line);
        else if(transactionCard.getPurpose().equals("Cà phê"))
            holder.categoryImg .setImageResource(R.drawable.ic_coffee);
        else if(transactionCard.getPurpose().equals("Di chuyển"))
            holder.categoryImg.setImageResource(R.drawable.ic_car_alt);
        else if(transactionCard.getPurpose().equals("Du lịch"))
            holder.categoryImg.setImageResource(R.drawable.ic_plane);


        viewBinderHelper.bind(holder.swipeRevealLayout, String.valueOf(1));
        holder.deleteLayout.setOnClickListener(view -> openDialogDeleteTransaction(holder, transactionCard));
    }

    private void openDialogDeleteTransaction(TransactionViewHolder holder, TransactionCard transactionCard) {
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
        TextView deleteTs = dialog.findViewById(R.id.delete_db);
        TextView cancelDeleteTs = dialog.findViewById(R.id.cancel_delete_db);


        String elementS = "Bạn có chắc là muốn xoá giao dịch";
        String s = elementS + transactionCard.getContent() + " không?";

        Spannable spannable = new SpannableString(s);
        spannable.setSpan(new ForegroundColorSpan(Color.rgb(255,152,0)), elementS.length(), elementS.length() + transactionCard.getContent().length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        title.setText(spannable);

        deleteTs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteTransactionFromServer(transactionCard);
                mListTransaction.remove(holder.getAdapterPosition());
                notifyItemRemoved(holder.getAdapterPosition());
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

    private void deleteTransactionFromServer(TransactionCard transactionCard) {
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);
        Call<InfoMessage> call;

        String json = moshi.adapter(TransactionCard.class).toJson(transactionCard);
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), json);

        call = service.deleteTransaction(requestBody);
        call.enqueue(new Callback<InfoMessage>() {
            @Override
            public void onResponse(Call<InfoMessage> call, Response<InfoMessage> response) {
                Log.w("YellDeleteTransaction", "onResponse: " + response);
            }

            @Override
            public void onFailure(Call<InfoMessage> call, Throwable t) {
                Log.w("YellDeleteTransaction", "onFailure: " + t.getMessage() );
            }
        });
    }

    @Override
    public int getItemCount() {
        if(mListTransaction != null){
            return mListTransaction.size();
        }
        return 0;
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
