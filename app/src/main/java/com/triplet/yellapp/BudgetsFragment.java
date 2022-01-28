package com.triplet.yellapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.triplet.yellapp.databinding.FragmentBudgetBinding;
import com.triplet.yellapp.models.BudgetCard;
import com.triplet.yellapp.utils.ApiService;
import com.triplet.yellapp.utils.SessionManager;

public class BudgetsFragment extends Fragment {
    FragmentBudgetBinding binding;
    BudgetCard budgetCard;

    SessionManager sessionManager;
    ApiService service;


    public BudgetsFragment(BudgetCard budgetCard, SessionManager sessionManager) {
        this.budgetCard = budgetCard;
        this.sessionManager = sessionManager;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentBudgetBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.budgetName.setText(budgetCard.getName());
        binding.budgetBalance.setText(String.valueOf(budgetCard.getBalance()));


        binding.backBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });


        return view;
    }
}



