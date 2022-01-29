package com.triplet.yellapp;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.triplet.yellapp.adapters.TransactionAdapter;
import com.triplet.yellapp.databinding.FragmentBudgetBinding;
import com.triplet.yellapp.databinding.FragmentBudgetHistoryBinding;
import com.triplet.yellapp.models.BudgetCard;
import com.triplet.yellapp.models.TransactionCard;
import com.triplet.yellapp.utils.SessionManager;

import java.util.List;

public class HistoryBudgetFragment extends Fragment {
    List<TransactionCard> transactionCardList;
    FragmentBudgetHistoryBinding binding;
    TransactionAdapter transactionAdapter;
    SessionManager sessionManager;

    public HistoryBudgetFragment(List<TransactionCard> transactionCardList) {
        this.transactionCardList = transactionCardList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBudgetHistoryBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        sessionManager = SessionManager.getInstance(getActivity().
                getSharedPreferences(getResources().getString(R.string.yell_sp), Context.MODE_PRIVATE));


        transactionAdapter = new TransactionAdapter(getContext(), sessionManager);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        transactionAdapter.setData(this.transactionCardList);

        binding.recycleViewTransaction.setLayoutManager(layoutManager);
        binding.recycleViewTransaction.setAdapter(transactionAdapter);

        return view;
    }
}
