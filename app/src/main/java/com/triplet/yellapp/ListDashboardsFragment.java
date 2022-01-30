package com.triplet.yellapp;

import android.content.Context;
import android.os.Bundle;
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

        /*
        dashboardViewModel = new ViewModelProvider(this.getActivity(), new DashboardViewModelFactory(list)).get(DashboardViewModel.class);
        dashboardViewModel.getListDashboardLiveData().observe(this.getActivity(), new Observer<List<DashboardCard>>() {
            @Override
            public void onChanged(List<DashboardCard> dashboardCards) {
                dashboardsAdapter = new DashboardsAdapter(getContext(), sessionManager);
                dashboardsAdapter.setData(dashboardCards);
                binding.recycleView.setAdapter(dashboardsAdapter);
            }
        });

         */

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

        return view;
    }

    private void addDashboardToServer(DashboardCard dashboardCard, View view) {
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);
        Call<DashboardCard> call;
        String json = moshi.adapter(DashboardCard.class).toJson(dashboardCard);
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), json);
        call = service.addDashboard(requestBody);
        call.enqueue(new Callback<DashboardCard>() {
            @Override
            public void onResponse(Call<DashboardCard> call, Response<DashboardCard> response) {
                Log.w("YellCreateDashboard", "onResponse: " + response);
                if (response.isSuccessful()) {
                    String id = response.body().getDashboard_id();
                    getDb(id, view);
                } else {
                    if (response.code() == 401) {
                        ErrorMessage apiError = ErrorMessage.convertErrors(response.errorBody());
                        Toast.makeText(getContext(), apiError.getMessage(), Toast.LENGTH_LONG).show();
                    }

                }
            }
            @Override
            public void onFailure(Call<DashboardCard> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi khi kết nối với server", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addList(DashboardCard dashboardCard) {
        //dashboardViewModel.addDashboard(dashboardCard);
    }


    private void getListDashboardFromServer() {
        if (sessionManager.getToken()!=null) {
            service = Client.createServiceWithAuth(ApiService.class, sessionManager);
            Call<UserAccount> call;
            call = service.getUserProfile("compact");
            call.enqueue(new Callback<UserAccount>() {
                @Override
                public void onResponse(Call<UserAccount> call, Response<UserAccount> response) {
                    Log.w("YellGetListDashboard", "onResponse: " + response);
                    if (response.isSuccessful()) {
                        List<String> dashboards = response.body().getDashboards();
                        getListDashboard(dashboards);
                    }
                }

                @Override
                public void onFailure(Call<UserAccount> call, Throwable t) {
                    Log.w("YellGetListDashboard", "onFailure: " + t.getMessage() );
                }
            });
        }
    }

    private void getListDashboard(List<String> dashboards) {
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);
        Call<DashboardCard> call;
        for (int i = 0; i < dashboards.size(); i++)
        {
            call = service.getDashboard(dashboards.get(i), "full");
            call.enqueue(new Callback<DashboardCard>() {
                @Override
                public void onResponse(Call<DashboardCard> call, Response<DashboardCard> response) {
                    Log.w("YellGetDashboard", "onResponse: " + response);
                    if (response.isSuccessful()) {
                        list.add(response.body());
                        dashboardsAdapter.notifyDataSetChanged();
                    }
                }
                @Override
                public void onFailure(Call<DashboardCard> call, Throwable t) {
                    Log.w("YellGetDashboard", "onFailure: " + t.getMessage() );
                }
            });

        }
    }

    private void getDb(String id, View view) {
        Call<DashboardCard> call;
        call = service.getDashboard(id, "full");
        call.enqueue(new Callback<DashboardCard>() {
            @Override
            public void onResponse(Call<DashboardCard> call, Response<DashboardCard> response) {
                Log.w("YellGetDashboard", "onResponse: " + response);
                if (response.isSuccessful()) {
                    AppCompatActivity activity = (AppCompatActivity) view.getContext();
                    DashboardFragment dashboardFragment = new DashboardFragment(response.body());
                    activity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragmentContainer,dashboardFragment)
                            .addToBackStack(null).commit();
                    list.add(response.body());
                    dashboardsAdapter.notifyDataSetChanged();

                }
            }
            @Override
            public void onFailure(Call<DashboardCard> call, Throwable t) {
                Log.w("YellGetDashboard", "onFailure: " + t.getMessage() );
            }
        });
    }
}