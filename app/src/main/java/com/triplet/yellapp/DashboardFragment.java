package com.triplet.yellapp;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.moshi.Moshi;
import com.triplet.yellapp.adapters.DashboardsAdapter;
import com.triplet.yellapp.adapters.TaskAdapter;
import com.triplet.yellapp.adapters.UsersAdapter;
import com.triplet.yellapp.adapters.UsersDetailAdapter;
import com.triplet.yellapp.databinding.FragmentDashboardBinding;
import com.triplet.yellapp.models.DashboardCard;
import com.triplet.yellapp.models.DashboardPermission;
import com.triplet.yellapp.models.ErrorMessage;
import com.triplet.yellapp.models.InfoMessage;
import com.triplet.yellapp.models.UserAccount;
import com.triplet.yellapp.models.YellTask;
import com.triplet.yellapp.utils.ApiService;
import com.triplet.yellapp.utils.Client;
import com.triplet.yellapp.utils.MySpannable;
import com.triplet.yellapp.utils.SessionManager;
import com.triplet.yellapp.viewmodels.DashboardViewModel;
import com.triplet.yellapp.viewmodels.YellTaskViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment {
    FragmentDashboardBinding binding;
    DashboardCard dashboardCard;
    UsersAdapter usersAdapter = null;
    UsersDetailAdapter usersDetailAdapter = null;
    List<String> usernames;
    List<YellTask> yellTasks;
    SessionManager sessionManager;
    LoadingDialog loadingDialog;
    ApiService service;
    DashboardViewModel dashboardViewModel;
    String uid;
    Moshi moshi = new Moshi.Builder().build();
    TaskAdapter yellTaskAdapter;


    public DashboardFragment(DashboardCard dashboardCard, SessionManager sessionManager) {
        this.dashboardCard = dashboardCard;
        this.sessionManager = sessionManager;
        yellTasks = new ArrayList<>();
        usernames = new ArrayList<>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.uid =  getContext().getSharedPreferences(getContext().getResources().getString(R.string.yell_sp), MODE_PRIVATE)
                .getString("uid","n");
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        dashboardViewModel.init();
        loadingDialog = new LoadingDialog(getActivity());
        dashboardViewModel.getDashboardCardLiveData().observe(this, new Observer<DashboardCard>() {
            @Override
            public void onChanged(DashboardCard dashboard) {
                List<YellTask> temp = dashboard.getTasks();
                for (int i = 0; i<temp.size();i++) {
                    if (temp.get(i).getParent_id() == null) {
                        yellTasks.add(temp.get(i));
                    }
                }
                List<DashboardPermission> temp1 = dashboard.getUsers();
                for (int i = 0;i< temp1.size();i++) {
                    usernames.add(temp1.get(i).getUid().subSequence(0,1).toString().toUpperCase(Locale.ROOT));
                }
                if (getActivity() != null) {
                   if (loadingDialog != null)
                       loadingDialog.dismissDialog();
                   dashboardCard = dashboard;
                   bindingData();
               }
            }
        });
        usersAdapter = new UsersAdapter(getContext());
        yellTaskAdapter = new TaskAdapter(getActivity());
    }

    private void bindingData() {
        binding.edtNameDb.setText(dashboardCard.getName());
        //TODO: Binding Label
        binding.tvDescriptionDb.setText(dashboardCard.getDescription());
        yellTaskAdapter.setYellTaskArrayList(yellTasks);
        usersAdapter.setData(dashboardCard.getUsers());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDashboardBinding.inflate(inflater, container, false );
        View view = binding.getRoot();
        if (!dashboardViewModel.getDashboard(dashboardCard.getId()))
            loadingDialog.startLoadingDialog();
        bindingData();
        if(dashboardCard.getDescription()!=null){
            binding.tvDescriptionDb.setText(dashboardCard.getDescription());
            binding.edtDescriptionDb.setText(dashboardCard.getDescription());
        }

        binding.backDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getActivity()!= null){
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });

        binding.deleteInDb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getPermission(dashboardCard).equals("admin")) {
                    openDialogDeleteDashboard(dashboardCard);
                }
                else {
                    Toast.makeText(getContext(), "Bạn không có quyền thực hiện chức năng này", Toast.LENGTH_LONG).show();
                }
            }
        });

        binding.editDb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getPermission(dashboardCard).equals("admin")) {
                    editDashboard();
                }
                else {
                    Toast.makeText(getContext(), "Bạn không có quyền thực hiện chức năng này", Toast.LENGTH_LONG).show();
                }
            }
        });

        binding.cancelEditDb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.edtNameDb.setText(dashboardCard.getName());
                binding.completeEditDb.setVisibility(View.GONE);
                binding.cancelEditDb.setVisibility(View.GONE);
                binding.edtDescriptionDb.setVisibility(View.GONE);
                binding.editDb.setVisibility(View.VISIBLE);
                binding.deleteInDb.setVisibility(View.VISIBLE);
                binding.backDashboard.setVisibility(View.VISIBLE);
                binding.tvDescriptionDb.setVisibility(View.VISIBLE);
                String dsc = binding.tvDescriptionDb.getTag().toString();
                binding.edtDescriptionDb.setText(dsc);
                try {
                    InputMethodManager inputManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                }
                catch (Exception ex){
                    Log.e(TAG, ex.toString());
                }

                binding.edtNameDb.setFocusable(false);
            }
        });

        binding.completeEditDb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.completeEditDb.setVisibility(View.GONE);
                binding.cancelEditDb.setVisibility(View.GONE);
                binding.edtDescriptionDb.setVisibility(View.GONE);
                binding.editDb.setVisibility(View.VISIBLE);
                binding.deleteInDb.setVisibility(View.VISIBLE);
                binding.backDashboard.setVisibility(View.VISIBLE);
                binding.tvDescriptionDb.setVisibility(View.VISIBLE);
                String dsc = binding.edtDescriptionDb.getText().toString();
                String name = binding.edtNameDb.getText().toString();
                if(name.length() == 0)
                {
                    binding.edtNameDb.setText(dashboardCard.getName());
                    Toast.makeText(getActivity(), "Tên bảng công việc phải có ít nhất 1 kí tự", Toast.LENGTH_LONG).show();
                }
                else {
                    dashboardCard.setName(name);
                }
                dashboardCard.setDescription(dsc);
                dashboardViewModel.editDashboard(dashboardCard);

                binding.tvDescriptionDb.setText(dsc);
                binding.tvDescriptionDb.setTag(null);
                makeTextViewResizable(binding.tvDescriptionDb, 3, "...Xem thêm", true);
                try {
                    InputMethodManager inputManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                }
                catch (Exception ex){
                    Log.e(TAG, ex.toString());
                }
                binding.edtNameDb.setFocusable(false);


            }
        });
        makeTextViewResizable(binding.tvDescriptionDb, 3, "...Xem thêm", true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(),  LinearLayoutManager.HORIZONTAL, false);
        binding.listUsers.setLayoutManager(layoutManager);

        binding.listUsers.setVisibility(View.VISIBLE);
        binding.listUsers.setAdapter(usersAdapter);

        binding.editUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getPermission(dashboardCard).equals("admin"))
                    dashboardViewModel.editDashboard(dashboardCard);
                else
                    Toast.makeText(getContext(), "Bạn không có quyền thực hiện chức năng này", Toast.LENGTH_LONG).show();
            }
        });



        //getListTaskFromServer();
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(getActivity());
        binding.listTasks.setLayoutManager(layoutManager2);
        binding.listTasks.setAdapter(yellTaskAdapter);

        binding.listTasks.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                // scroll xuống
                if (dy > 0) binding.fabDashboard.hide();
                else binding.fabDashboard.show();
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        binding.fabDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getPermission(dashboardCard).equals("admin"))
                    dashboardViewModel.editDashboard(dashboardCard);
                else
                    Toast.makeText(getContext(), "Bạn không có quyền thực hiện chức năng này", Toast.LENGTH_LONG).show();
            }
        });

        return view;
    }

    private void checkDeletePermission() {
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);
        Call<UserAccount> call;
        call = service.getUserProfile("compact");
        call.enqueue(new Callback<UserAccount>() {
            @Override
            public void onResponse(Call<UserAccount> call, Response<UserAccount> response) {
                Log.w("YellGetListDashboard", "onResponse: " + response);
                if (response.isSuccessful()) {
                    String uid = response.body().getId();
                    for(int i = 0; i < dashboardCard.getUsers().size(); i++){
                        if(uid.equals(dashboardCard.getUsers().get(i).getUid())){

                            if(dashboardCard.getUsers().get(i).getRole().equals("admin"))
                            {
                                openDialogDeleteDashboard(dashboardCard);
                                return;
                            }
                            else {
                                Toast.makeText(getContext(), "Bạn không có quyền thực hiện chức năng này", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<UserAccount> call, Throwable t) {
                Log.w("YellGetListDashboard", "onFailure: " + t.getMessage() );
            }
        });
    }

    private void checkEditPermission(int code) {
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);
        Call<UserAccount> call;
        call = service.getUserProfile("compact");
        call.enqueue(new Callback<UserAccount>() {
            @Override
            public void onResponse(Call<UserAccount> call, Response<UserAccount> response) {
                Log.w("YellGetListDashboard", "onResponse: " + response);
                if (response.isSuccessful()) {
                    String uid = response.body().getId();
                    for(int i = 0; i < dashboardCard.getUsers().size(); i++){
                        if(uid.equals(dashboardCard.getUsers().get(i).getUid())){
                            if(dashboardCard.getUsers().get(i).getRole().equals("viewer"))
                            {
                                if(code == 3)
                                    openDialogListShareDashboard(2);
                                else
                                    Toast.makeText(getContext(), "Bạn không có quyền thực hiện chức năng này", Toast.LENGTH_LONG).show();
                            }
                            else {
                                if(code == 1)
                                    editDashboard();
                                else if(code == 2)
                                    addTask();
                                else if(code == 3)
                                {
                                    openDialogListShareDashboard(1);
                                }
                                return;
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<UserAccount> call, Throwable t) {
                Log.w("YellGetListDashboard", "onFailure: " + t.getMessage() );
            }
        });
    }

    private void addTask() {
        YellTask yell = new YellTask(dashboardCard.getId(),"Untitled");
        addTaskToServer(yell);
    }

    private void editDashboard() {
        binding.edtNameDb.setFocusableInTouchMode(true);
        binding.edtNameDb.setFocusable(true);
        binding.editDb.setVisibility(View.GONE);
        binding.deleteInDb.setVisibility(View.GONE);
        binding.backDashboard.setVisibility(View.GONE);
        binding.tvDescriptionDb.setVisibility(View.GONE);
        binding.completeEditDb.setVisibility(View.VISIBLE);
        binding.cancelEditDb.setVisibility(View.VISIBLE);
        binding.edtDescriptionDb.setVisibility(View.VISIBLE);
    }

    private void getListTaskFromServer() {
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);
        Call<DashboardCard> call;

        call = service.getDashboard(dashboardCard.getId(), "full");
        call.enqueue(new Callback<DashboardCard>() {
            @Override
            public void onResponse(Call<DashboardCard> call, Response<DashboardCard> response) {
                Log.w("YellGetDashboard", "onResponse: " + response);
                if (response.isSuccessful()) {
                    dashboardCard = response.body();
                    if(dashboardCard.getTasks() == null)
                        return;
                    for(int i = 0; i < dashboardCard.getTasks().size(); i++){
                        yellTaskAdapter.addYellTask(dashboardCard.getTasks().get(i));
                    }
                }
            }
            @Override
            public void onFailure(Call<DashboardCard> call, Throwable t) {
                Log.w("YellGetDashboard", "onFailure: " + t.getMessage() );
            }
        });
    }

    private void setDashboard(int i, YellTask body) {
        dashboardCard.getTasks().set(i ,body);
        yellTaskAdapter.notifyDataSetChanged();
    }

    private void addTaskToServer(YellTask yellTask) {
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);
        Call<YellTask> call;

        RequestBody requestBody = taskToJson(yellTask);

        call = service.addTask(null, requestBody);
        call.enqueue(new Callback<YellTask>() {
            @Override
            public void onResponse(Call<YellTask> call, Response<YellTask> response) {

                Log.w("YellTaskCreate", "onResponse: " + response);

                if (response.isSuccessful()) {
                    yellTask.setTask_id(response.body().getTask_id());
                    yellTaskAdapter.addYellTask(yellTask);
                    yellTaskAdapter.notifyDataSetChanged();
                }
                else {
                    if (response.code() == 401) {
                        ErrorMessage apiError = ErrorMessage.convertErrors(response.errorBody());
                        Toast.makeText(getContext(), apiError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    // TODO
                }

            }

            @Override
            public void onFailure(Call<YellTask> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi khi kết nối với server", Toast.LENGTH_LONG).show();
                // TODO:
            }
        });
    }

    private RequestBody taskToJson(YellTask currentYellTask) {
        String jsonYellTask = moshi.adapter(YellTask.class).toJson(currentYellTask);
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), jsonYellTask);
        return requestBody;
    }

    private void openDialogListShareDashboard(int i) {
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_share);

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
        AppCompatImageButton invite = dialog.findViewById(R.id.invite);
        TextView email = dialog.findViewById(R.id.uid);
        RecyclerView listUser = dialog.findViewById(R.id.userList);

        invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(i == 1){
                    PopupMenu popupMenu = new PopupMenu(getContext(), invite);
                    popupMenu.getMenuInflater().inflate(R.menu.permission_menu, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            String userId = email.getText().toString();
                            String role = null;
                            switch (menuItem.getItemId()){
                                case R.id.menu_edit:
                                    role = "editor";
                                    break;
                                case R.id.menu_view:
                                    role = "viewer";
                                    break;
                            }
                            if(role != null && userId.length() > 0)
                            {
                                DashboardPermission dbPermission = new DashboardPermission(dashboardCard.getId(), userId, role);
                                inviteSoToDashboard(dbPermission);
                            }
                            return false;
                        }
                    });
                    popupMenu.show();
                }
                else {
                    Toast.makeText(getContext(), "Bạn không có quyền thực hiện chức năng này", Toast.LENGTH_LONG).show();
                }
            }
        });

        LinearLayoutManager layoutManager1 = new LinearLayoutManager(getActivity());
        listUser.setLayoutManager(layoutManager1);
        usersDetailAdapter = new UsersDetailAdapter(getContext());

        List<DashboardPermission> listUserDetail = dashboardCard.getUsers();

        usersDetailAdapter.setData(listUserDetail);
        usersDetailAdapter.notifyDataSetChanged();
        listUser.setVisibility(View.VISIBLE);
        listUser.setAdapter(usersDetailAdapter);

        dialog.show();
    }

    private void inviteSoToDashboard(DashboardPermission dbPermission) {
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);
        Call<InfoMessage> call;

        String json = moshi.adapter(DashboardPermission.class).toJson(dbPermission);
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), json);

        call = service.inviteSoToDashboard(requestBody);
        call.enqueue(new Callback<InfoMessage>() {
            @Override
            public void onResponse(Call<InfoMessage> call, Response<InfoMessage> response) {
                Log.w("YellInviteSoToDashboard", "onResponse: " + response);
                if(!response.isSuccessful())
                {
                    if (response.code() == 404) {
                        Toast.makeText(getContext(), "User id này không tồn tại", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<InfoMessage> call, Throwable t) {
                Log.w("YellInviteSoToDashboard", "onFailure: " + t.getMessage() );
            }
        });
    }

    private void editDashboardOnServer() {
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);
        Call<InfoMessage> call;

        String json = moshi.adapter(DashboardCard.class).toJson(dashboardCard);
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), json);

        call = service.editDashboard(requestBody);
        call.enqueue(new Callback<InfoMessage>() {
            @Override
            public void onResponse(Call<InfoMessage> call, Response<InfoMessage> response) {
                Log.w("YellEditDashboard", "onResponse: " + response);
            }

            @Override
            public void onFailure(Call<InfoMessage> call, Throwable t) {
                Log.w("YellEditDashboard", "onFailure: " + t.getMessage() );
            }
        });
    }

    private void getListUserNamesFromServer() {
        usernames.add("BC");
        usernames.add("TH");
        usernames.add("TT");
    }

    private void openDialogDeleteDashboard(DashboardCard dashboardCard) {
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_delete_dashboard);

        Window window = dialog.getWindow();
        if(window == null){
            return;
        }

        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        windowAttributes.gravity = Gravity.CENTER;
        window.setAttributes(windowAttributes);

        dialog.setCancelable(true);

        TextView title = dialog.findViewById(R.id.title_delete_db);
        TextView deleteBt = dialog.findViewById(R.id.delete_db);
        TextView cancelDeleteBt = dialog.findViewById(R.id.cancel_delete_db);


        String elementS = "Bạn có chắc là muốn xoá bảng ";
        String s = elementS + dashboardCard.getName() + " không?";

        Spannable spannable = new SpannableString(s);
        spannable.setSpan(new ForegroundColorSpan(Color.rgb(255,152,0)), elementS.length(), elementS.length() + dashboardCard.getName().length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        title.setText(spannable);

        deleteBt.setOnClickListener(view -> {
            dashboardViewModel.deleteDashboard(dashboardCard);
            getActivity().getSupportFragmentManager().popBackStack();
            dialog.dismiss();
        });

        cancelDeleteBt.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }

    private void deleteDashboardFromServer(DashboardCard dashboardCard) {
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);
        Call<InfoMessage> call;

        String json = moshi.adapter(DashboardCard.class).toJson(dashboardCard);
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), json);

        call = service.deleteDashboard(requestBody);
        call.enqueue(new Callback<InfoMessage>() {
            @Override
            public void onResponse(Call<InfoMessage> call, Response<InfoMessage> response) {
                Log.w("YellDeleteDashboard", "onResponse: " + response);
                if(getActivity() != null){
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }

            @Override
            public void onFailure(Call<InfoMessage> call, Throwable t) {
                Log.w("YellDeleteDashboard", "onFailure: " + t.getMessage() );
            }
        });
    }

    public static void makeTextViewResizable(final TextView tv, final int maxLine, final String expandText, final boolean viewMore) {

        if (tv.getTag() == null) {
            tv.setTag(tv.getText());
        }
        ViewTreeObserver vto = tv.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            //@SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                String text;
                int lineEndIndex;
                ViewTreeObserver obs = tv.getViewTreeObserver();
                obs.removeOnGlobalLayoutListener(this);

                if (maxLine == 0) {
                    lineEndIndex = tv.getLayout().getLineEnd(0);
                    text = tv.getText().subSequence(0, lineEndIndex - expandText.length() + 1) + " " + expandText;
                } else if (maxLine > 0 && tv.getLineCount() >= maxLine) {
                    lineEndIndex = tv.getLayout().getLineEnd(maxLine - 1);
                    text = tv.getText().subSequence(0, lineEndIndex - expandText.length() + 1) + " " + expandText;
                } else {
                    lineEndIndex = tv.getLayout().getLineEnd(tv.getLayout().getLineCount() - 1);
                    text = tv.getText().subSequence(0, lineEndIndex) + " " + expandText;
                }
                
                if(tv.getLineCount() > 3){
                    tv.setText(text);
                    tv.setMovementMethod(LinkMovementMethod.getInstance());

                    tv.setText(addClickablePartTextViewResizable(new SpannableString(tv.getText().toString()), tv, lineEndIndex, expandText,
                            viewMore), TextView.BufferType.SPANNABLE);
                }
            }
        });
    }

    private static SpannableStringBuilder addClickablePartTextViewResizable(SpannableString strSpanned, TextView tv,
                                                                            final int maxLine, final String spanableText, final boolean viewMore) {
        String str = strSpanned.toString();
        SpannableStringBuilder ssb = new SpannableStringBuilder(strSpanned);
        if (str.contains(spanableText)) {
            ssb.setSpan(new MySpannable(false) {

                @Override
                public void onClick(View widget) {
                    tv.setLayoutParams(tv.getLayoutParams());
                    tv.setText(tv.getTag().toString(), TextView.BufferType.SPANNABLE);
                    tv.invalidate();
                    if (viewMore) {
                        makeTextViewResizable(tv, -1, "...Thu gọn", false);
                    } else {
                        makeTextViewResizable(tv, 3, "...Xem thêm", true);
                    }

                }
            }, str.indexOf(spanableText), str.indexOf(spanableText) + spanableText.length(), 0);

        }
        return ssb;
    }
    private String getPermission(DashboardCard dashboardCard) {
        List<DashboardPermission> permissions = dashboardCard.getUsers();
        for (int i = 0;i<permissions.size();i++) {
            if (uid.equals(permissions.get(i).getUid()))
                return permissions.get(i).getRole();
        }
        return "n";
    }
}