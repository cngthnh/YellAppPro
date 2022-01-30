package com.triplet.yellapp;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.triplet.yellapp.adapters.TransactionAdapter;
import com.triplet.yellapp.databinding.FragmentBudgetBinding;
import com.triplet.yellapp.databinding.FragmentBudgetHistoryBinding;
import com.triplet.yellapp.models.BudgetCard;
import com.triplet.yellapp.models.TransactionCard;
import com.triplet.yellapp.utils.SessionManager;
import com.triplet.yellapp.viewmodels.BudgetViewModel;

import java.util.ArrayList;
import java.util.List;

public class HistoryBudgetFragment extends Fragment {
    List<TransactionCard> transactionCardList;
    FragmentBudgetHistoryBinding binding;
    TransactionAdapter transactionAdapter;
    SessionManager sessionManager;
    BudgetViewModel budgetViewModel;
    LoadingDialog loadingDialog;

    public HistoryBudgetFragment(BudgetViewModel budgetViewModel) {
        this.budgetViewModel = budgetViewModel;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadingDialog = new LoadingDialog(getActivity());
        sessionManager = SessionManager.getInstance(getActivity().
                getSharedPreferences(getResources().getString(R.string.yell_sp), Context.MODE_PRIVATE));
        budgetViewModel.getBudgetCardLiveData().observe(this, new Observer<BudgetCard>() {
            @Override
            public void onChanged(BudgetCard budget) {
                List<TransactionCard> temp = budget.getTransactionsList();
                transactionCardList = new ArrayList<>();
                for (int i = 0; i<temp.size();i++) {
                    transactionCardList.add(temp.get(i));
                }

                if (getActivity() != null) {
                    if (loadingDialog != null)
                        loadingDialog.dismissDialog();
                    bindingData();
                }
            }
        });
        transactionAdapter = new TransactionAdapter(getActivity(), sessionManager, budgetViewModel);
    }

    private void bindingData() {
        transactionAdapter.setData(transactionCardList);
        transactionAdapter.notifyDataSetChanged();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBudgetHistoryBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        bindingData();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        binding.recycleViewTransaction.setLayoutManager(layoutManager);
        binding.recycleViewTransaction.setAdapter(transactionAdapter);

        return view;
    }
}
