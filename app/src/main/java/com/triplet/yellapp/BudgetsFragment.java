package com.triplet.yellapp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
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
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.squareup.moshi.Moshi;
import com.triplet.yellapp.adapters.ViewPagerBudgetAdapter;
import com.triplet.yellapp.databinding.FragmentBudgetBinding;
import com.triplet.yellapp.models.BudgetCard;
import com.triplet.yellapp.models.ErrorMessage;
import com.triplet.yellapp.models.TransactionCard;
import com.triplet.yellapp.utils.ApiService;
import com.triplet.yellapp.utils.Client;
import com.triplet.yellapp.utils.SessionManager;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BudgetsFragment extends Fragment {
    FragmentBudgetBinding binding;
    BudgetCard budgetCard;

    SessionManager sessionManager;
    ApiService service;
    Moshi moshi = new Moshi.Builder().build();


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

        ViewPagerBudgetAdapter viewPagerBudgetAdapter = new ViewPagerBudgetAdapter(
                getActivity().getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, budgetCard.getTransactionsList());
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

        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        windowAttributes.gravity = Gravity.BOTTOM;
        window.setAttributes(windowAttributes);

        dialog.setCancelable(true);

        EditText content = dialog.findViewById(R.id.transactionContent);
        EditText amount = dialog.findViewById(R.id.transactionAmount);
        RadioGroup typeGroup = dialog.findViewById(R.id.typeGroup);
        MaterialButton saveBt = dialog.findViewById(R.id.transactionSaveBtn);
        MaterialButton category = dialog.findViewById(R.id.categoryTransaction);

        dialog.findViewById(R.id.makeTransactionContainer).setVisibility(View.VISIBLE);
        dialog.findViewById(R.id.categoryContainer).setVisibility(View.GONE);

        TransactionCard newTransaction = new TransactionCard();
        newTransaction.setType(0);
        typeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i == R.id.rbIncome){
                    newTransaction.setType(1);
                }
                else if(i == R.id.rbOutcome){
                    newTransaction.setType(0);
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
                    androidx.appcompat.widget.LinearLayoutCompat  categoryLayout = (androidx.appcompat.widget.LinearLayoutCompat ) dialog.findViewById(R.id.categoryContainer);
                    categoryLayout.setVisibility(View.VISIBLE);
                    dialog.findViewById(R.id.makeTransactionContainer).setVisibility(View.GONE);
                    TransactionCard type = new TransactionCard();
                    RadioGroup typeGroup = categoryLayout.findViewById(R.id.typeGroup);
                    MaterialButton saveBt = categoryLayout.findViewById(R.id.btn_save_category);
                    typeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(RadioGroup radioGroup, int i) {
                            if (i == R.id.rbEating)
                                type.setPurpose("Ăn uống");
                            else if (i == R.id.rbHangout)
                                type.setPurpose("Cà phê");
                            else if (i == R.id.rbBuying)
                                type.setPurpose("Mua sắm");
                            else if (i == R.id.rbTransport)
                                type.setPurpose("Đi lại");
                            else if (i == R.id.rbCasual)
                                type.setPurpose("Sinh hoạt hằng ngày");
                            else if (i == R.id.rbTravel)
                                type.setPurpose("Du lịch");
                        }
                    });

                    saveBt.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(type.getPurpose()==null){
                                newTransaction.setPurpose("Ăn uống");
                            }
                            else {
                                newTransaction.setPurpose(type.getPurpose());
                            }
                            dialog.findViewById(R.id.categoryContainer).setVisibility(View.GONE);
                            dialog.findViewById(R.id.makeTransactionContainer).setVisibility(View.VISIBLE);
                        }
                    });

                }
                else {
                    //openDialogIncomeCategory();
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
                    newTransaction.setAmount(Integer.parseInt(amount.getText().toString()));
                    addTransactionToServer(newTransaction, dialog);
                    dialog.dismiss();
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

    private void openDialogOutcomeCategory(Dialog dialog, TransactionCard newTransaction) {

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



        dialog.show();
    }
}



