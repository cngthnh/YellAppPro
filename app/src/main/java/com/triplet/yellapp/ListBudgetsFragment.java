package com.triplet.yellapp;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.squareup.moshi.Moshi;
import com.triplet.yellapp.adapters.BudgetsAdapter;
import com.triplet.yellapp.adapters.UsersDetailAdapter;
import com.triplet.yellapp.databinding.FragmentBudgetListBinding;
import com.triplet.yellapp.models.BudgetCard;
import com.triplet.yellapp.models.DashboardCard;
import com.triplet.yellapp.models.DashboardPermission;
import com.triplet.yellapp.models.ErrorMessage;
import com.triplet.yellapp.models.UserAccount;
import com.triplet.yellapp.utils.ApiService;
import com.triplet.yellapp.utils.Client;
import com.triplet.yellapp.utils.SessionManager;


import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListBudgetsFragment extends Fragment {
    FragmentBudgetListBinding binding;
    BudgetsAdapter budgetsAdapter = null;
    List<BudgetCard> list;
    ApiService service;
    SessionManager sessionManager;
    Moshi moshi = new Moshi.Builder().build();

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

        binding.fabListBudgets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialogCreateNewBudget();
            }
        });


        return view;
    }


    private void openDialogCreateNewBudget() {
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_new_budget);

        Window window = dialog.getWindow();
        if(window == null){
            return;
        }

        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        windowAttributes.gravity = Gravity.BOTTOM;
        window.setAttributes(windowAttributes);

        dialog.setCancelable(true);

        EditText nameBudget = dialog.findViewById(R.id.budgetNameInput);
        EditText balanceBudget = dialog.findViewById(R.id.budgetBalanceInput);

        MaterialButton saveBt = dialog.findViewById(R.id.budgetSaveBtn);
        MaterialButton typeSave = dialog.findViewById(R.id.typeSave);
        MaterialButton typeSpend = dialog.findViewById(R.id.typeSpend);
        TextView thresholdTitle = dialog.findViewById(R.id.thresholdTitle);
        EditText thresholdBudget = dialog.findViewById(R.id.thresholdInput);
        CardView thresholdCard = dialog.findViewById(R.id.thresholdCard);

        BudgetCard newBudget = new BudgetCard();

        typeSpend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                typeSpend.setBackgroundColor(Color.DKGRAY);
                typeSpend.setTextColor(Color.WHITE);
                typeSave.setBackgroundColor(Color.WHITE);
                typeSave.setTextColor(Color.BLACK);

                thresholdCard.setVisibility(View.VISIBLE);
                thresholdTitle.setText("Giới hạn chi tiêu");
                newBudget.setType(0);
            }
        });

        typeSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                typeSave.setBackgroundColor(Color.DKGRAY);
                typeSave.setTextColor(Color.WHITE);
                typeSpend.setBackgroundColor(Color.WHITE);
                typeSpend.setTextColor(Color.BLACK);
                thresholdCard.setVisibility(View.VISIBLE);
                thresholdTitle.setText("Mục tiêu tiết kiệm");
                newBudget.setType(1);
            }
        });


        saveBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (nameBudget.getText().toString().equals("") || balanceBudget.getText().toString().equals("")
                    || thresholdBudget.getText().toString().equals("")){

                    Toast.makeText(getContext(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_LONG).show();
                }

                else {
                    newBudget.setName(nameBudget.getText().toString());
                    newBudget.setBalance(Integer.parseInt(balanceBudget.getText().toString()));
                    newBudget.setThreshold(Integer.parseInt(thresholdBudget.getText().toString()));

                    addBudgetToServer(newBudget, dialog);
                }
            }
        });

        dialog.show();
    }

    private void addBudgetToServer(BudgetCard newBudget, Dialog dialog) {
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);
        Call<BudgetCard> call;

        String json = moshi.adapter(BudgetCard.class).toJson(newBudget);
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), json);

        call = service.createBudget(requestBody);
        call.enqueue(new Callback<BudgetCard>() {
            @Override
            public void onResponse(Call<BudgetCard> call, Response<BudgetCard> response) {

                Log.w("BudgetCreate", "onResponse: " + response);
                if (response.isSuccessful()) {
                    dialog.dismiss();
                    newBudget.setId(response.body().getId());
                    list.add(newBudget);
                    budgetsAdapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "Tạo thành công", Toast.LENGTH_LONG).show();
                } else {

                    ErrorMessage apiError = ErrorMessage.convertErrors(response.errorBody());
                    Toast.makeText(getActivity(), "Tạo thất bại: " + apiError.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<BudgetCard> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi khi kết nối với server", Toast.LENGTH_LONG).show();
                Log.w("YellCreateBudget", "onFailure: " + t.getMessage());
            }
        });
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