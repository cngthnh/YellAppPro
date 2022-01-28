package com.triplet.yellapp.adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.triplet.yellapp.FragmentHistoryBudget;
import com.triplet.yellapp.FragmentStatisticBudget;

public class ViewPagerBudgetAdapter extends FragmentStatePagerAdapter {

    public ViewPagerBudgetAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (position == 1) {
            return new FragmentStatisticBudget();
        }
        return new FragmentHistoryBudget();
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
