package com.triplet.yellapp.adapters;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.squareup.moshi.Moshi;
import com.triplet.yellapp.R;
import com.triplet.yellapp.models.CategoryStatistic;
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

public class TransactionStatisticAdapter extends RecyclerView.Adapter<TransactionStatisticAdapter.TransactionStatisticViewHolder>{
    private Context mContext = null;
    private List<CategoryStatistic> mListCategoryStatistic;

    SessionManager sessionManager;


    public TransactionStatisticAdapter(Context mContext, SessionManager sessionManager) {
        this.mContext = mContext;
        this.sessionManager = sessionManager;
    }

    public void setData(List<CategoryStatistic> mListCategoryStatistic) {
        this.mListCategoryStatistic = mListCategoryStatistic;
    }

    @NonNull
    @Override
    public TransactionStatisticViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction_statistic, parent, false);
        return new TransactionStatisticViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionStatisticViewHolder holder, int position) {
        CategoryStatistic categoryStatistic = mListCategoryStatistic.get(position);
        if(categoryStatistic == null){
            return;
        }

        if(categoryStatistic.getPurpose().equals("Ăn uống"))
            holder.categoryImg.setImageResource(R.drawable.ic_pizza);
        else if(categoryStatistic.getPurpose().equals("Mua sắm"))
            holder.categoryImg.setImageResource(R.drawable.ic_basket);
        else if(categoryStatistic.getPurpose().equals("Sinh hoạt hằng ngày"))
            holder.categoryImg.setImageResource(R.drawable.ic_home_line);
        else if(categoryStatistic.getPurpose().equals("Cà phê"))
            holder.categoryImg .setImageResource(R.drawable.ic_coffee);
        else if(categoryStatistic.getPurpose().equals("Di chuyển"))
            holder.categoryImg.setImageResource(R.drawable.ic_car_alt);
        else if(categoryStatistic.getPurpose().equals("Du lịch"))
            holder.categoryImg.setImageResource(R.drawable.ic_plane);
        else if(categoryStatistic.getPurpose().equals("Lương tháng")) {
            holder.categoryImg.setImageResource(R.drawable.ic_salary);
            holder.categoryImg.setColorFilter(Color.rgb(4, 69, 173));
            //holder.percentage.setProgressDrawable();
        }else if(categoryStatistic.getPurpose().equals("Tiết kiệm")) {
            holder.categoryImg.setImageResource(R.drawable.ic_savings);
            holder.categoryImg.setColorFilter(Color.rgb(4, 69, 173));

        }else if(categoryStatistic.getPurpose().equals("Bán đồ cũ")) {
            holder.categoryImg.setImageResource(R.drawable.ic_dealing);
            holder.categoryImg.setColorFilter(Color.rgb(4, 69, 173));
        }else if(categoryStatistic.getPurpose().equals("Tiền lời")) {
            holder.categoryImg.setImageResource(R.drawable.ic_interest);
            holder.categoryImg.setColorFilter(Color.rgb(4, 69, 173));
        }

        holder.purpose.setText(categoryStatistic.purpose);
        holder.total.setText(String.valueOf(categoryStatistic.getAmount()) + "vnđ");

        int percentage = (Math.abs(categoryStatistic.getAmount())* 100) / categoryStatistic.getTotal();
        holder.percentageText.setText(String.valueOf(percentage) + "%");
        holder.percentage.setProgress(percentage);

    }

    @Override
    public int getItemCount() {
        if(mListCategoryStatistic != null){
            return mListCategoryStatistic.size();
        }
        return 0;
    }

    public class TransactionStatisticViewHolder extends RecyclerView.ViewHolder{
        private ImageView categoryImg;
        private AppCompatTextView purpose;
        private AppCompatTextView total;
        private ProgressBar percentage;
        private TextView percentageText;
        public TransactionStatisticViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryImg = itemView.findViewById((R.id.category_img_statistic));
            purpose = itemView.findViewById((R.id.transactionTypeItem));
            total = itemView.findViewById((R.id.total_amount_type_transaction));
            percentage = itemView.findViewById(R.id.circularPercentageStatistic);
            percentageText = itemView.findViewById(R.id.percentageCategory);
        }
    }
}
