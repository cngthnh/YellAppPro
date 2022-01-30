package com.triplet.yellapp;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.triplet.yellapp.adapters.TransactionAdapter;
import com.triplet.yellapp.adapters.TransactionStatisticAdapter;
import com.triplet.yellapp.databinding.FragmentBudgetHistoryBinding;
import com.triplet.yellapp.databinding.FragmentBudgetStatisticBinding;
import com.triplet.yellapp.models.BudgetCard;
import com.triplet.yellapp.models.CategoryStatistic;
import com.triplet.yellapp.models.TransactionCard;
import com.triplet.yellapp.utils.SessionManager;
import com.triplet.yellapp.viewmodels.BudgetViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StatisticBudgetFragment extends Fragment {

    List<TransactionCard> transactionCardList;
    FragmentBudgetStatisticBinding binding;
    TransactionStatisticAdapter transactionStatisticAdapterIncome;
    TransactionStatisticAdapter transactionStatisticAdapterOutcome;
    SessionManager sessionManager;
    BudgetViewModel budgetViewModel;
    LoadingDialog loadingDialog;
    BudgetCard budgetCard;
    List<CategoryStatistic> listIncome;
    List<CategoryStatistic> listOutcome;

    public StatisticBudgetFragment(BudgetViewModel budgetViewModel) {
        this.budgetViewModel = budgetViewModel;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadingDialog = new LoadingDialog(getActivity());
        sessionManager = SessionManager.getInstance(getActivity().
                getSharedPreferences(getResources().getString(R.string.yell_sp), Context.MODE_PRIVATE));

        budgetCard = new BudgetCard();


        budgetViewModel.getBudgetCardLiveData().observe(this, new Observer<BudgetCard>() {
            @Override
            public void onChanged(BudgetCard budget) {
                List<TransactionCard> temp = budget.getTransactionsList();
                transactionCardList = new ArrayList<>();
                listIncome = new ArrayList<>();
                listOutcome = new ArrayList<>();
                for (int i = 0; i<temp.size();i++) {
                    transactionCardList.add(temp.get(i));
                }
                budgetCard.setBalance(budget.getBalance());
                budgetCard.setType(budget.getType());
                budgetCard.setThreshold(budget.getThreshold());


                getListStatistic();
                if (getActivity() != null) {
                    if (loadingDialog != null)
                        loadingDialog.dismissDialog();
                    bindingData();
                }
            }
        });
        transactionStatisticAdapterIncome = new TransactionStatisticAdapter(getContext(), sessionManager);
        transactionStatisticAdapterOutcome = new TransactionStatisticAdapter(getContext(), sessionManager);
    }

    private void getListStatistic() {

    }

    private void bindingData() {
        int totalOutcome = 0;
        int totalIncome = 0;
        int total;
        int percentageIncome;
        int percentageOutcome;

        List<Integer> dataStatistic = new ArrayList<>(Collections.nCopies(10, 0));

        if(transactionCardList == null)
        {
            return;
        }

        for(int i = 0; i < transactionCardList.size(); i++)
        {
            if(transactionCardList.get(i).getAmount() < 0)
                totalOutcome -= transactionCardList.get(i).amount;
            else
                totalIncome += transactionCardList.get(i).amount;

            if (transactionCardList.get(i).getPurpose().equals("Ăn uống"))
                dataStatistic.set(0, dataStatistic.get(0) + transactionCardList.get(i).getAmount());
            else if (transactionCardList.get(i).getPurpose().equals("Mua sắm"))
                dataStatistic.set(1, dataStatistic.get(1) + transactionCardList.get(i).getAmount());
            else if (transactionCardList.get(i).getPurpose().equals("Sinh hoạt hằng ngày"))
                dataStatistic.set(2, dataStatistic.get(2) + transactionCardList.get(i).getAmount());
            else if (transactionCardList.get(i).getPurpose().equals("Cà phê"))
                dataStatistic.set(3, dataStatistic.get(3) + transactionCardList.get(i).getAmount());
            else if (transactionCardList.get(i).getPurpose().equals("Di chuyển"))
                dataStatistic.set(4, dataStatistic.get(4) + transactionCardList.get(i).getAmount());
            else if (transactionCardList.get(i).getPurpose().equals("Du lịch"))
                dataStatistic.set(5, dataStatistic.get(5) + transactionCardList.get(i).getAmount());
            else if (transactionCardList.get(i).getPurpose().equals("Lương tháng"))
                dataStatistic.set(6, dataStatistic.get(6) + transactionCardList.get(i).getAmount());
            else if (transactionCardList.get(i).getPurpose().equals("Tiết kiệm"))
                dataStatistic.set(7, dataStatistic.get(7) + transactionCardList.get(i).getAmount());
            else if (transactionCardList.get(i).getPurpose().equals("Bán đồ cũ"))
                dataStatistic.set(8, dataStatistic.get(8) + transactionCardList.get(i).getAmount());
            else if (transactionCardList.get(i).getPurpose().equals("Tiền lời"))
                dataStatistic.set(9, dataStatistic.get(9) + transactionCardList.get(i).getAmount());
        }

        updateDataAdapter(dataStatistic, totalIncome, totalOutcome);
        total = budgetCard.getBalance() + totalOutcome;

        binding.percentageOutcome.setText(String.valueOf((totalOutcome/total)));

        if(budgetCard.getType() == 0)
        {
            binding.layoutPercentageIncome.setVisibility(View.GONE);
            binding.lineStatistic.setVisibility(View.GONE);
        }
        else
        {
            percentageIncome = (budgetCard.balance * 100)/ budgetCard.threshold;
            if(percentageIncome > 100)
                percentageIncome = 100;

            binding.circularProgressbarIncome.setProgress(percentageIncome);
            binding.percentageIncome.setText(String.valueOf(percentageIncome) + "%");

            binding.thresholdStatisticIncome.setText(String.valueOf(budgetCard.threshold));
            binding.balanceStatisticIncome.setText(String.valueOf(budgetCard.balance));
        }

        percentageOutcome = (totalOutcome * 100) /total;
        binding.circularProgressbarOutcome.setProgress(percentageOutcome);
        binding.percentageOutcome.setText(String.valueOf(percentageOutcome) + "%");

        binding.totalBudgetOutcome.setText(String.valueOf(total));
        binding.totalOutcomeStatistic.setText(String.valueOf(totalOutcome));


    }

    private void updateDataAdapter(List<Integer> dataStatistic, int totalIncome, int totalOutcome) {
        listOutcome.add(new CategoryStatistic(totalOutcome, "Ăn uống", dataStatistic.get(0), 0));
        listOutcome.add(new CategoryStatistic(totalOutcome, "Mua sắm", dataStatistic.get(1), 0));
        listOutcome.add(new CategoryStatistic(totalOutcome, "Sinh hoạt hằng ngày", dataStatistic.get(2), 0));
        listOutcome.add(new CategoryStatistic(totalOutcome, "Cà phê", dataStatistic.get(3), 0));
        listOutcome.add(new CategoryStatistic(totalOutcome, "Di chuyển", dataStatistic.get(4), 0));
        listOutcome.add(new CategoryStatistic(totalOutcome, "Du lịch", dataStatistic.get(5), 0));
        listIncome.add(new CategoryStatistic(totalIncome, "Lương tháng", dataStatistic.get(6), 1));
        listIncome.add(new CategoryStatistic(totalIncome, "Tiết kiệm", dataStatistic.get(7), 1));
        listIncome.add(new CategoryStatistic(totalIncome, "Bán đồ cũ", dataStatistic.get(8), 1));
        listIncome.add(new CategoryStatistic(totalIncome, "Tiền lời", dataStatistic.get(9), 1));

        transactionStatisticAdapterIncome.setData(listIncome);
        transactionStatisticAdapterIncome.notifyDataSetChanged();

        transactionStatisticAdapterOutcome.setData(listOutcome);
        transactionStatisticAdapterOutcome.notifyDataSetChanged();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentBudgetStatisticBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        bindingData();

        LinearLayoutManager layoutManagerOutcome = new LinearLayoutManager(getActivity());
        binding.recycleViewStatisticOutcome. setLayoutManager(layoutManagerOutcome);
        binding.recycleViewStatisticOutcome. setAdapter(transactionStatisticAdapterOutcome);

        LinearLayoutManager layoutManagerIncome = new LinearLayoutManager(getActivity());
        binding.recycleViewStatisticIncome.setLayoutManager(layoutManagerIncome);
        binding.recycleViewStatisticIncome.setAdapter(transactionStatisticAdapterIncome);

        binding.incomeBtnStatistic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.incomeLayout.setVisibility(View.VISIBLE);
                binding.outcomeLayout.setVisibility(View.GONE);
                binding.incomeBtnStatistic.setBackgroundColor(Color.rgb(4, 69,173));
                binding.incomeBtnStatistic.setTextColor(Color.rgb(255, 255, 255));

                binding.outcomeBtnStatistic.setBackgroundColor(Color.rgb(144, 144, 144));
                binding.outcomeBtnStatistic.setTextColor(Color.rgb(241, 241, 241));
            }
        });

        binding.outcomeBtnStatistic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.incomeLayout.setVisibility(View.GONE);
                binding.outcomeLayout.setVisibility(View.VISIBLE);

                binding.outcomeBtnStatistic.setBackgroundColor(Color.rgb(255,152,0));
                binding.outcomeBtnStatistic.setTextColor(Color.rgb(255, 255, 255));

                binding.incomeBtnStatistic.setBackgroundColor(Color.rgb(144, 144, 144));
                binding.incomeBtnStatistic.setTextColor(Color.rgb(241, 241, 241));
            }
        });
        return view;
    }
}
