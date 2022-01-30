package com.triplet.yellapp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.telecom.ConnectionService;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.squareup.moshi.Moshi;
import com.triplet.yellapp.adapters.ViewPagerBudgetAdapter;
import com.triplet.yellapp.databinding.FragmentBudgetBinding;
import com.triplet.yellapp.models.BudgetCard;
import com.triplet.yellapp.models.DashboardCard;
import com.triplet.yellapp.models.DashboardPermission;
import com.triplet.yellapp.models.ErrorMessage;
import com.triplet.yellapp.models.TransactionCard;
import com.triplet.yellapp.models.YellTask;
import com.triplet.yellapp.utils.ApiService;
import com.triplet.yellapp.utils.Client;
import com.triplet.yellapp.utils.SessionManager;
import com.triplet.yellapp.viewmodels.BudgetViewModel;
import com.triplet.yellapp.viewmodels.DashboardViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BudgetsFragment extends Fragment {
    FragmentBudgetBinding binding;
    BudgetCard budgetCard;
    BudgetViewModel budgetViewModel;
    SessionManager sessionManager;
    ApiService service;
    Moshi moshi = new Moshi.Builder().build();
    LoadingDialog loadingDialog;
    List<TransactionCard> transactionCards;
    ViewPagerBudgetAdapter viewPagerBudgetAdapter;


    public BudgetsFragment(BudgetCard budgetCard, SessionManager sessionManager) {
        this.budgetCard = budgetCard;
        this.sessionManager = sessionManager;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        budgetViewModel = new ViewModelProvider(this).get(BudgetViewModel.class);
        budgetViewModel.init();
        loadingDialog = new LoadingDialog(getActivity());

        budgetViewModel.getBudgetCardLiveData().observe(this, new Observer<BudgetCard>() {
            @Override
            public void onChanged(BudgetCard budget) {
                List<TransactionCard> temp = budget.getTransactionsList();
                transactionCards = new ArrayList<>();
                for (int i = 0; i<temp.size();i++) {
                    transactionCards.add(temp.get(i));
                }
                budgetCard.setBalance(budget.getBalance());
                if (getActivity() != null) {
                    if (loadingDialog != null)
                        loadingDialog.dismissDialog();
                    budgetCard = budget;
                    bindingData();
                }
            }
        });
    }

    private void bindingData() {
        binding.budgetName.setText(budgetCard.getName());
        binding.budgetBalance.setText(String.valueOf(budgetCard.getBalance()));
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentBudgetBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        if (!budgetViewModel.getBudget(budgetCard.getId()))
            loadingDialog.startLoadingDialog();
        bindingData();
        binding.backBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });

        viewPagerBudgetAdapter = new ViewPagerBudgetAdapter(
                getActivity().getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, budgetViewModel);
        binding.viewpager.setAdapter(viewPagerBudgetAdapter);

        binding.tablayout.setupWithViewPager(binding.viewpager);

        binding.fabBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialogCreateNewTransaction();
            }
        });

        return view;
    }

    private void openDialogCreateNewTransaction() {
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_new_transaction);

        Window window = dialog.getWindow();
        if(window == null){
            return;
        }

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(window.getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.BOTTOM;
        lp.windowAnimations = R.style.DialogAnimation;

        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        window.setAttributes(lp);

        dialog.setCancelable(true);

        EditText content = dialog.findViewById(R.id.transactionContent);
        EditText amount = dialog.findViewById(R.id.transactionAmount);
        RadioGroup typeGroup = dialog.findViewById(R.id.typeGroup);
        MaterialButton saveBt = dialog.findViewById(R.id.transactionSaveBtn);
        MaterialButton category = dialog.findViewById(R.id.categoryTransaction);

        dialog.findViewById(R.id.outcomeCategoryPicker).setVisibility(View.GONE);
        dialog.findViewById(R.id.incomeCategoryPicker).setVisibility(View.GONE);
        dialog.findViewById(R.id.makeTransactionContainer).setVisibility(View.VISIBLE);

        TransactionCard newTransaction = new TransactionCard();
        newTransaction.setType(0);
        typeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.rbIncome){
                    newTransaction.setType(1);
                    newTransaction.setPurpose(getResources().getString(R.string.savings));
                    category.setIcon(getResources().getDrawable(R.drawable.ic_savings));
                    category.setText(R.string.savings);
                }
                else if(i == R.id.rbOutcome){
                    newTransaction.setType(0);
                    newTransaction.setPurpose(getResources().getString(R.string.eating));
                    category.setIcon(getResources().getDrawable(R.drawable.ic_pizza));
                    category.setText(R.string.eating);
                }
            }
        });

        category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(newTransaction.getType() == null){
                    Toast.makeText(getContext(), "Chưa chọn loại danh mục", Toast.LENGTH_LONG).show();
                }
                else if(newTransaction.getType() == 0)
                {
                    androidx.appcompat.widget.LinearLayoutCompat  categoryLayout = (androidx.appcompat.widget.LinearLayoutCompat ) dialog.findViewById(R.id.outcomeCategoryPicker);
                    dialog.findViewById(R.id.makeTransactionContainer).setVisibility(View.GONE);
                    dialog.findViewById(R.id.incomeCategoryPicker).setVisibility(View.GONE);
                    categoryLayout.setVisibility(View.VISIBLE);
                    TransactionCard type = new TransactionCard();
                    RadioGroup categoryGroup = categoryLayout.findViewById(R.id.outcomeCategoryGroup);
                    MaterialButton saveBt = categoryLayout.findViewById(R.id.btn_save_category);
                    categoryGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(RadioGroup radioGroup, int i) {
                            if (i == R.id.rbEating) {
                                type.setPurpose(getResources().getString(R.string.eating));
                                category.setIcon(getResources().getDrawable(R.drawable.ic_pizza));
                                category.setText(R.string.eating);
                            }
                            else if (i == R.id.rbHangout) {
                                type.setPurpose(getResources().getString(R.string.hangout));
                                category.setIcon(getResources().getDrawable(R.drawable.ic_coffee));
                                category.setText(R.string.hangout);
                            }
                            else if (i == R.id.rbBuying) {
                                type.setPurpose(getResources().getString(R.string.shopping));
                                category.setIcon(getResources().getDrawable(R.drawable.ic_basket));
                                category.setText(R.string.shopping);
                            }
                            else if (i == R.id.rbTransport) {
                                type.setPurpose(getResources().getString(R.string.transport));
                                category.setIcon(getResources().getDrawable(R.drawable.ic_car_alt));
                                category.setText(R.string.transport);
                            }
                            else if (i == R.id.rbCasual) {
                                type.setPurpose(getResources().getString(R.string.daily));
                                category.setIcon(getResources().getDrawable(R.drawable.ic_home_line));
                                category.setText(R.string.daily);
                            }
                            else if (i == R.id.rbTravel) {
                                type.setPurpose(getResources().getString(R.string.travel));
                                category.setIcon(getResources().getDrawable(R.drawable.ic_plane));
                                category.setText(R.string.travel);
                            }
                        }
                    });

                    saveBt.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(type.getPurpose()==null){
                                newTransaction.setPurpose(getResources().getString(R.string.eating));
                            }
                            else {
                                newTransaction.setPurpose(type.getPurpose());
                            }
                            dialog.findViewById(R.id.outcomeCategoryPicker).setVisibility(View.GONE);
                            dialog.findViewById(R.id.makeTransactionContainer).setVisibility(View.VISIBLE);
                        }
                    });

                }
                else {
                    androidx.appcompat.widget.LinearLayoutCompat  categoryLayout = (androidx.appcompat.widget.LinearLayoutCompat ) dialog.findViewById(R.id.incomeCategoryPicker);
                    dialog.findViewById(R.id.makeTransactionContainer).setVisibility(View.GONE);
                    dialog.findViewById(R.id.outcomeCategoryPicker).setVisibility(View.GONE);
                    categoryLayout.setVisibility(View.VISIBLE);
                    TransactionCard type = new TransactionCard();
                    RadioGroup categoryGroup = categoryLayout.findViewById(R.id.incomeCategoryGroup);
                    MaterialButton saveBt = categoryLayout.findViewById(R.id.btn_save_category);
                    categoryGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(RadioGroup radioGroup, int i) {
                            if (i == R.id.rbSalary) {
                                type.setPurpose(getResources().getString(R.string.salary));
                                category.setIcon(getResources().getDrawable(R.drawable.ic_salary));
                                category.setText(R.string.salary);
                            }
                            else if (i == R.id.rbSavings) {
                                type.setPurpose(getResources().getString(R.string.savings));
                                category.setIcon(getResources().getDrawable(R.drawable.ic_savings));
                                category.setText(R.string.savings);
                            }
                            else if (i == R.id.rbDealing) {
                                type.setPurpose(getResources().getString(R.string.dealing));
                                category.setIcon(getResources().getDrawable(R.drawable.ic_dealing));
                                category.setText(R.string.dealing);
                            }
                            else if (i == R.id.rbInterest) {
                                type.setPurpose(getResources().getString(R.string.interest));
                                category.setIcon(getResources().getDrawable(R.drawable.ic_interest));
                                category.setText(R.string.interest);
                            }
                        }
                    });

                    saveBt.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(type.getPurpose()==null){
                                newTransaction.setPurpose(getResources().getString(R.string.savings));
                            }
                            else {
                                newTransaction.setPurpose(type.getPurpose());
                            }
                            dialog.findViewById(R.id.incomeCategoryPicker).setVisibility(View.GONE);
                            dialog.findViewById(R.id.makeTransactionContainer).setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        });



        saveBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (content.getText().toString().equals("") || amount.getText().toString().equals("")){

                    Toast.makeText(getContext(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_LONG).show();
                }
                else if(newTransaction.getPurpose() == null)
                {
                    Toast.makeText(getContext(), "Vui lòng chọn danh mục", Toast.LENGTH_LONG).show();
                }
                else {
                    newTransaction.setBudget_id(budgetCard.getId());
                    newTransaction.setContent(content.getText().toString());
                    if(newTransaction.getType() == 0)
                        newTransaction.setAmount(-Integer.parseInt(amount.getText().toString()));
                    else
                        newTransaction.setAmount(Integer.parseInt(amount.getText().toString()));

                    if(Math.abs(newTransaction.getAmount()) > budgetCard.balance)
                        Toast.makeText(getContext(), "Số dư còn lại của tài khoản không đủ để thực hiện giao dịch này",
                                Toast.LENGTH_LONG).show();
                    else {
                        budgetViewModel.addTransaction(newTransaction);
                        dialog.dismiss();
                    }
                    //addTransactionToServer(newTransaction, dialog);

                }
            }
        });




        dialog.show();
    }

    private void addTransactionToServer(TransactionCard newTransaction, Dialog dialog) {
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);
        Call<TransactionCard> call;
        String transID;

        String json = moshi.adapter(TransactionCard.class).toJson(newTransaction);
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), json);
        call = service.addTransaction(requestBody);
        call.enqueue(new Callback<TransactionCard>() {
            @Override
            public void onResponse(Call<TransactionCard> call, Response<TransactionCard> response) {
                Log.w("YellCreateTransaction", "onResponse: " + response);
                if (response.isSuccessful()) {
                    Toast.makeText(getActivity(), "Tạo thành công!", Toast.LENGTH_LONG).show();
                    newTransaction.setTran_id(response.body().getTran_id());
                    Log.e("ID", newTransaction.getTran_id());
                    budgetCard.getTransactionsList().add(newTransaction);

                } else {
                    {
                        ErrorMessage apiError = ErrorMessage.convertErrors(response.errorBody());
                        Toast.makeText(getActivity(), "Tạo thất bại! " + apiError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
            @Override
            public void onFailure(Call<TransactionCard> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi khi kết nối với server", Toast.LENGTH_LONG).show();
            }
        });
    }
}



