package com.triplet.yellapp.adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.triplet.yellapp.HistoryBudgetFragment;
import com.triplet.yellapp.StatisticBudgetFragment;
import com.triplet.yellapp.models.TransactionCard;
import com.triplet.yellapp.viewmodels.BudgetViewModel;

import java.util.List;

public class ViewPagerBudgetAdapter extends FragmentStatePagerAdapter {

    BudgetViewModel budgetViewModel;

    public ViewPagerBudgetAdapter(@NonNull FragmentManager fm, int behavior, BudgetViewModel budgetViewModel) {
        super(fm, behavior);
        this.budgetViewModel = budgetViewModel;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (position == 1) {
            return new StatisticBudgetFragment(budgetViewModel);
        }
        return new HistoryBudgetFragment(budgetViewModel);
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
