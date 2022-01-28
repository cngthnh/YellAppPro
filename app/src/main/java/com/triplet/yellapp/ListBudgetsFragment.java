package com.triplet.yellapp;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.moshi.Moshi;
import com.triplet.yellapp.adapters.BudgetsAdapter;
import com.triplet.yellapp.databinding.FragmentBudgetListBinding;
import com.triplet.yellapp.models.BudgetCard;
import com.triplet.yellapp.models.DashboardCard;
import com.triplet.yellapp.models.ErrorMessage;
import com.triplet.yellapp.models.UserAccount;
import com.triplet.yellapp.utils.ApiService;
import com.triplet.yellapp.utils.Client;
import com.triplet.yellapp.utils.SessionManager;


import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListBudgetsFragment extends Fragment {
    FragmentBudgetListBinding binding;
    BudgetsAdapter budgetsAdapter = null;
    List<BudgetCard> list;
    ApiService service;
    SessionManager sessionManager;

    public ListBudgetsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        sessionManager = SessionManager.getInstance(getActivity().
                getSharedPreferences(getResources().getString(R.string.yell_sp), Context.MODE_PRIVATE));
        // Inflate the layout for this fragment
        binding = FragmentBudgetListBinding.inflate(inflater, container, false );
        View view = binding.getRoot();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        binding.recycleView.setLayoutManager(layoutManager);
        binding.recycleView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                // scroll xuống
                if (dy > 0) binding.fabListBudgets.hide();
                else binding.fabListBudgets.show();
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        budgetsAdapter = new BudgetsAdapter(getContext(), sessionManager);
        list = new ArrayList<>();
        getListBudgetsFromServer();

        budgetsAdapter.setData(list);
        budgetsAdapter.notifyDataSetChanged();
        binding.recycleView.setVisibility(View.VISIBLE);
        binding.recycleView.setAdapter(budgetsAdapter);

        binding.backListBudgets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getActivity() != null){
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });


        return view;
    }

    private void getListBudgetsFromServer() {
        if (sessionManager.getToken()!=null) {
            service = Client.createServiceWithAuth(ApiService.class, sessionManager);
            Call<UserAccount> call;
            call = service.getUserProfile("compact");
            call.enqueue(new Callback<UserAccount>() {
                @Override
                public void onResponse(Call<UserAccount> call, Response<UserAccount> response) {
                    Log.w("YellBudgetGet", "onResponse: " + response);
                    if (response.isSuccessful()) {
                        getListBudget(response.body().getBudgets());
                    } else {
                        ErrorMessage apiError = ErrorMessage.convertErrors(response.errorBody());
                        Toast.makeText(getActivity(), apiError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<UserAccount> call, Throwable t) {
                    Log.w("YellBudgetFromUser", "onFailure: " + t.getMessage() );
                }
            });
        }
    }
    private void getListBudget(List<String> budget){
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);
        Call<BudgetCard> call;
        for (int i = 0; i < budget.size(); i++)
        {
            call = service.getBudget (budget.get(i), "full");
            call.enqueue(new Callback<BudgetCard>() {
                @Override
                public void onResponse(Call<BudgetCard> call, Response<BudgetCard> response) {
                    Log.w("YellGetBudget", "onResponse: " + response);
                    if (response.isSuccessful()) {
                        list.add(response.body());
                        budgetsAdapter.notifyDataSetChanged();
                    }
                }
                @Override
                public void onFailure(Call<BudgetCard> call, Throwable t) {
                    Log.w("YellGetBudget", "onFailure: " + t.getMessage() );
                }
            });

        }

    }

}