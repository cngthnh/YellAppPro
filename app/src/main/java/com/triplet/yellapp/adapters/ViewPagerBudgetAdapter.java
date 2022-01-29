package com.triplet.yellapp.adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.triplet.yellapp.HistoryBudgetFragment;
import com.triplet.yellapp.StatisticBudgetFragment;
import com.triplet.yellapp.models.TransactionCard;

import java.util.List;

public class ViewPagerBudgetAdapter extends FragmentStatePagerAdapter {

    List<TransactionCard> transactionCardList;
    public ViewPagerBudgetAdapter(@NonNull FragmentManager fm, int behavior, List<TransactionCard> transactionCardList) {
        super(fm, behavior);
        this.transactionCardList = transactionCardList;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (position == 1) {
            return new StatisticBudgetFragment();
        }
        return new HistoryBudgetFragment(transactionCardList);
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        String title = "";
        if (position == 1) {
            title = "THỐNG KÊ";
        }
        else {
            title = "LỊCH SỬ";
        }
        return title;
    }
}
