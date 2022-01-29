package com.triplet.yellapp;

import static java.lang.Math.abs;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.triplet.yellapp.adapters.DashboardsHomeAdapter;
import com.triplet.yellapp.databinding.FragmentHomeBinding;
import com.triplet.yellapp.models.DashboardCard;
import com.triplet.yellapp.models.UserAccountFull;
import com.triplet.yellapp.models.YellTask;
import com.triplet.yellapp.repository.YellTaskRepository;
import com.triplet.yellapp.utils.ApiService;
import com.triplet.yellapp.utils.SessionManager;
import com.triplet.yellapp.viewmodels.UserViewModel;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.Sort;

public class HomeFragment extends Fragment {

    FragmentHomeBinding binding;
    DashboardsHomeAdapter dashboardsHomeAdapter = null;
    UserViewModel userViewModel;
    LoadingDialog loadingDialog;
    UserAccountFull user;
    Realm realm;
    SessionManager sessionManager;
    YellTask nearDeadlineTask;
    YellTaskRepository taskRepo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        loadingDialog = new LoadingDialog(getActivity());
        sessionManager = SessionManager.getInstance(getActivity().
                getSharedPreferences(getResources().getString(R.string.yell_sp), Context.MODE_PRIVATE));
        dashboardsHomeAdapter = new DashboardsHomeAdapter(getContext(), sessionManager);
        taskRepo = new YellTaskRepository(getActivity().getApplication());
        realm = Realm.getDefaultInstance();
        userViewModel.init();
        userViewModel.getYellUserLiveData().observe(this, new Observer<UserAccountFull>() {
            @Override
            public void onChanged(UserAccountFull userAccountFull) {
                if (loadingDialog != null)
                    loadingDialog.dismissDialog();
                user = userAccountFull;
                bindingData();

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        if(!userViewModel.getUser())
            loadingDialog.startLoadingDialog();
        binding.avatarImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                AccountFragment accountFragment = new AccountFragment();
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, accountFragment
                        , "ACCOUNT").addToBackStack(null).commit();
            }
        });

        binding.notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                NotificationFragment notificationFragment = new NotificationFragment();
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, notificationFragment
                        , "NOTIFICATION").addToBackStack(null).commit();
            }
        });
        binding.viewAllDashboardsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                ListDashboardsFragment dashboardsFragment = new ListDashboardsFragment(userViewModel);
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, dashboardsFragment
                        , "LIST_DASHBOARD").addToBackStack(null).commit();
            }
        });

        binding.viewAllBudgetsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCompatActivity activity = (AppCompatActivity) view.getContext();

                ListBudgetsFragment budgetsFragment = new ListBudgetsFragment();
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, budgetsFragment
                        , "LIST_BUDGET").addToBackStack(null).commit();
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        binding.dashboardPreviewList.setLayoutManager(layoutManager);
        binding.dashboardPreviewList.setVisibility(View.VISIBLE);
        binding.dashboardPreviewList.setAdapter(dashboardsHomeAdapter);

        return view;
    }

    private String time2DurationString(String deadlineString)
    {
        String result = "";
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date deadline = null;
        Date now = null;

        try {
            deadline = df.parse(deadlineString);
            now = df.parse(df.format(new Date()));
        } catch (ParseException e) {
            return "Không thể xác định thời gian";
        }

        if (deadline == null || now == null) return "Không thể xác định thời gian";

        int count = 0;

        if (now.compareTo(deadline) > 0)
            result += "Trễ hạn ";
        else
            result += "Còn lại ";

        long timespan = abs(now.getTime() - deadline.getTime());

        long diff = TimeUnit.DAYS.convert(timespan, TimeUnit.MILLISECONDS);
        if (diff > 0) {
            count += 1;
            result += String.valueOf(diff);
            result += " ngày ";
            timespan -= TimeUnit.MILLISECONDS.convert(diff, TimeUnit.DAYS);
        }

        diff = TimeUnit.HOURS.convert(timespan, TimeUnit.MILLISECONDS);
        if (diff > 0) {
            count += 1;
            result += String.valueOf(diff);
            result += " giờ ";
            if (count == 2) return result;
            timespan -= TimeUnit.MILLISECONDS.convert(diff, TimeUnit.HOURS);
        }

        diff = TimeUnit.MINUTES.convert(timespan, TimeUnit.MILLISECONDS);
        if (diff > 0) {
            count += 1;
            result += String.valueOf(diff);
            result += " phút ";
            if (count == 2) return result;
            timespan -= TimeUnit.MILLISECONDS.convert(diff, TimeUnit.MINUTES);
        }

        diff = TimeUnit.SECONDS.convert(timespan, TimeUnit.MILLISECONDS);
        if (diff > 0) {
            count += 1;
            result += String.valueOf(diff);
            result += " giây ";
            if (count == 2) return result;
        }
        return result;
    }

    private void bindNearestDeadlineTask() {
        YellTask realmTaskObj = realm
                .where(YellTask.class)
                .equalTo("status", 1)
                .isNotNull("end_time")
                .isNotEmpty("end_time")
                .sort("end_time", Sort.ASCENDING)
                .findFirst();
        if (realmTaskObj != null) {
            nearDeadlineTask = realm.copyFromRealm(realmTaskObj);
            binding.highlightTaskTitle.setText(nearDeadlineTask.name);
            binding.highlightTaskDuration.setText(time2DurationString(nearDeadlineTask.end_time));
        } else {
            binding.highlightTaskTitle.setText("Không có công việc nào gần đến hạn");
            binding.highlightTaskDuration.setText("Bạn có thời gian rảnh");
        }
    }

    private void bindingData () {
        dashboardsHomeAdapter.setData(user.getDashboards());
        binding.accountTitle.setText("Hi, "+user.getName());
        bindNearestDeadlineTask();
        binding.highLightTaskCompleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nearDeadlineTask.status = 2;
                ((ImageButton) view).setImageResource(R.drawable.ic_check_circle_filled);
                ((ImageButton) view).setImageResource(R.drawable.ic_check_circle_filled);
                ((ImageButton) view).setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.orange)));
                taskRepo.patchTask(nearDeadlineTask);
                bindNearestDeadlineTask();
            }
        });
    }

}