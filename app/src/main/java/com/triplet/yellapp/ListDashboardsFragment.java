package com.triplet.yellapp;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.moshi.Moshi;
import com.triplet.yellapp.adapters.DashboardsAdapter;
import com.triplet.yellapp.databinding.FragmentListDashboardsBinding;
import com.triplet.yellapp.models.BudgetCard;
import com.triplet.yellapp.models.DashboardCard;
import com.triplet.yellapp.models.ErrorMessage;
import com.triplet.yellapp.models.UserAccount;
import com.triplet.yellapp.models.UserAccountFull;
import com.triplet.yellapp.utils.ApiService;
import com.triplet.yellapp.utils.Client;
import com.triplet.yellapp.utils.SessionManager;
import com.triplet.yellapp.viewmodels.UserViewModel;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListDashboardsFragment extends Fragment {
    FragmentListDashboardsBinding binding;
    DashboardsAdapter dashboardsAdapter = null;
    List<DashboardCard> list;
    SessionManager sessionManager;
    ApiService service;
    UserViewModel userViewModel;
    LoadingDialog loadingDialog;
    Moshi moshi = new Moshi.Builder().build();

    public ListDashboardsFragment(UserViewModel userViewModel) {
        this.userViewModel = userViewModel;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadingDialog = new LoadingDialog(getActivity());
        dashboardsAdapter = new DashboardsAdapter(getContext(), sessionManager);
        list = new ArrayList<>();
        userViewModel.getYellUserLiveData().observe(this, new Observer<UserAccountFull>() {
            @Override
            public void onChanged(UserAccountFull userAccountFull) {
                list = userAccountFull.getDashboards();
                if (getActivity() != null) {
                    if (loadingDialog != null)
                        loadingDialog.dismissDialog();
                    dashboardsAdapter.setData(list);
                }
            }
        });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.yellow_primary));
        binding = FragmentListDashboardsBinding.inflate(inflater, container, false );
        View view = binding.getRoot();
        if(!userViewModel.getUser())
            loadingDialog.startLoadingDialog();
        else {
            if (list.isEmpty())
                list = userViewModel.getYellUserLiveData().getValue().getDashboards();
            dashboardsAdapter.setData(list);
        }

        sessionManager = SessionManager.getInstance(getActivity().
                getSharedPreferences(getResources().getString(R.string.yell_sp), Context.MODE_PRIVATE));

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        binding.recycleView.setLayoutManager(layoutManager);
        binding.recycleView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                // scroll xuống
                if (dy > 0) binding.fabListDashboards.hide();
                else binding.fabListDashboards.show();
                super.onScrolled(recyclerView, dx, dy);
            }
        });


        binding.recycleView.setVisibility(View.VISIBLE);
        binding.recycleView.setAdapter(dashboardsAdapter);

        binding.backListDashboards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getActivity() != null){
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });

        binding.fabListDashboards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DashboardCard dashboardCard = new DashboardCard("Untitled");
                userViewModel.addDashboard(dashboardCard);
            }
        });

        binding.searchListDashboard.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                filter(editable.toString());
            }
        });


        return view;
    }

    private void filter(String toString) {
        List<DashboardCard> filteredList = new ArrayList<>();
        for(DashboardCard item: list){
            if(item.getName().toLowerCase().contains(toString.toLowerCase())){
                filteredList.add(item);
            }
        }
        dashboardsAdapter.filterList(filteredList);
    }

}