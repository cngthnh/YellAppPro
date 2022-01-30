package com.triplet.yellapp;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.icu.text.SimpleDateFormat;
import android.icu.util.TimeZone;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.triplet.yellapp.adapters.TaskAdapter;
import com.triplet.yellapp.databinding.FragmentTaskBinding;
import com.triplet.yellapp.models.DashboardCard;
import com.triplet.yellapp.models.DashboardPermission;
import com.triplet.yellapp.models.YellTask;
import com.triplet.yellapp.viewmodels.DashboardViewModel;
import com.triplet.yellapp.viewmodels.YellTaskViewModel;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TaskFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TaskFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "taskName";
    private static final String ARG_PARAM2 = "dashboardId";
    private static final String ARG_PARAM3 = "taskId";
    private static final String ARG_PARAM4 = "previousTaskName";
    private static final String ARG_PARAM5 = "parentId";

    YellTask currentYellTask;
    List<YellTask> subTasks;
    FragmentTaskBinding binding;
    YellTaskViewModel viewModel;
    TaskAdapter yellTaskAdapter;
    LoadingDialog loadingDialog;
    DashboardCard dashboard;

    // TODO: Rename and change types of parameters
    private String previousTaskName;

    public TaskFragment() {
        // Required empty public constructor
    }
    public TaskFragment(YellTask yellTask, String parentName) {
        this.currentYellTask = yellTask;
        previousTaskName = parentName;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param taskId Parameter 1.
     * @param dashBoardId Parameter 2.
     * @return A new instance of fragment TaskFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TaskFragment newInstance(String taskName, String dashBoardId, String taskId,
                                           String previousTaskName, DashboardViewModel viewModel) {
        TaskFragment fragment = new TaskFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, taskName);
        args.putString(ARG_PARAM2, dashBoardId);
        args.putString(ARG_PARAM3, taskId);
        args.putString(ARG_PARAM4, previousTaskName);
        fragment.setArguments(args);
        return fragment;
    }
    public static TaskFragment newInstance(String taskName, String dashBoardId, String taskId,
                                           String previousTaskName, String parentId) {
        TaskFragment fragment = new TaskFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, taskName);
        args.putString(ARG_PARAM2, dashBoardId);
        args.putString(ARG_PARAM3, taskId);
        args.putString(ARG_PARAM4, previousTaskName);
        args.putString(ARG_PARAM5, parentId);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        subTasks = new ArrayList<>();
        loadingDialog = new LoadingDialog(getActivity());
        viewModel = new ViewModelProvider(this).get(YellTaskViewModel.class);
        viewModel.init();
        viewModel.getYellTaskLiveData().observe(this, new Observer<YellTask>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onChanged(YellTask yellTask) {
                currentYellTask = yellTask;
                subTasks = new ArrayList<>();
                List<YellTask> temp = dashboard.getTasks();
                for (int i=0;i< temp.size();i++) {
                    if (temp.get(i).getParent_id() != null)
                        if (temp.get(i).getParent_id() == currentYellTask.getTask_id())
                            subTasks.add(temp.get(i));
                }
                subTasks = currentYellTask.getSubtasks();
                if (getActivity() != null) {
                    if (loadingDialog != null)
                        loadingDialog.dismissDialog();
                    if (yellTask != null) {
                        if (currentYellTask.getEnd_time() != null)
                            binding.deadlineTask.setText(serverTime2MobileTime(currentYellTask.getEnd_time()));

                        if (currentYellTask.getPriority() != null) {
                            if (currentYellTask.getPriority() == 1)
                                binding.priorityTextView.setText("Thấp");
                            else if (currentYellTask.getPriority() == 2)
                                binding.priorityTextView.setText("Thường");
                            else if (currentYellTask.getPriority() == 3) {
                                binding.priorityTextView.setText("Cao");
                            }
                        }

                        if (currentYellTask.getStatus() != null) {
                            if (currentYellTask.getStatus() == 2)
                                binding.statusTextView.setText("Đã hoàn thành");
                            else if (currentYellTask.getStatus() == 1) {
                                binding.statusTextView.setText("Chưa hoàn thành");
                            }
                        }
                        binding.taskName.setText(yellTask.getName());
                        binding.contentEditText.setText(yellTask.getContent());
                        yellTaskAdapter.setYellTaskArrayList(subTasks);
                    }
                }
            }
        });
        viewModel.getTaskIdLiveData().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (currentYellTask.getTask_id() == null)
                    currentYellTask.setTask_id(s);
                else {
                    yellTaskAdapter.setYellTaskArrayList(subTasks);
                }
            }
        });
        viewModel.getDashboardCardLiveData().observe(this, new Observer<DashboardCard>() {
            @Override
            public void onChanged(DashboardCard dashboardCard) {
                dashboard = dashboardCard;
                try {
                    yellTaskAdapter.setRole(dashboardCard.getUsers());
                }
                catch (Exception e)
                {
                    Log.e("TaskFragment","Line 169 in Task Fragment");
                    viewModel.getDashboard(currentYellTask.getDashboard_id());
                }
            }
        });
        yellTaskAdapter = new TaskAdapter(getActivity());
        yellTaskAdapter.setParentName(currentYellTask.getName());
        viewModel.getDashboard(currentYellTask.getDashboard_id());
    }




    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentTaskBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        if (!viewModel.getTask(currentYellTask.getTask_id()))
            loadingDialog.startLoadingDialog();
        setToolbarTaskListener();
        setConfigTaskListener();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void setToolbarTaskListener() {
        AppCompatImageButton editNameTask = binding.editNameTask;
        AppCompatImageButton deleteTask = binding.deleteTask;
        AppCompatEditText taskName = binding.taskName;
        AppCompatImageButton taskIcon = binding.taskIcon;
        if (currentYellTask.getName() != null)
            taskName.setText(currentYellTask.getName());
        if (this.previousTaskName != null)
            binding.previousTask.setText(previousTaskName);
        taskIcon.setClickable(false);
        taskIcon.setTag("false");
        StringBuffer currentName = new StringBuffer();
        final Drawable[] currentDrawable = new Drawable[1];
        editNameTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkPermission()) {
                    Toast.makeText(getContext(), "Bạn không có quyền thực hiện chức năng này", Toast.LENGTH_LONG).show();
                    return;
                }
                if ((String)taskIcon.getTag() == "false") {
                    taskName.setEnabled(true);
                    taskName.requestFocusFromTouch();
                    InputMethodManager imm = (InputMethodManager)
                            getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(taskName, InputMethodManager.SHOW_IMPLICIT);
                    taskName.setSelection(taskName.getText().length());

                    taskIcon.setClickable(true);
                    taskIcon.setTag("true");
                    currentDrawable[0] = taskIcon.getDrawable();

                    editNameTask.setImageResource(R.drawable.ic_done);
                    editNameTask.setColorFilter(getResources().getColor(R.color.green));

                    deleteTask.setImageResource(R.drawable.ic_close);
                    deleteTask.setColorFilter(getResources().getColor(R.color.red));
                    currentName.append(taskName.getText());
                }
                else {
                    taskName.setEnabled(false);
                    currentName.delete(0,currentName.length());

                    taskIcon.setClickable(false);
                    taskIcon.setTag("false");

                    editNameTask.setImageResource(R.drawable.ic_edit_mode);
                    editNameTask.setColorFilter(null);

                    deleteTask.setImageResource(R.drawable.ic_delete);
                    deleteTask.setColorFilter(null);
                    currentYellTask.setName(taskName.getText().toString());
                    viewModel.editTask(currentYellTask);

                }
            }
        });
        taskName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length()>40) {
                    taskName.setError("Nhập quá 40 ký tự");
                    editNameTask.setClickable(false);
                    editNameTask.setColorFilter(getResources().getColor(R.color.dark_gray));
                }
                else if (s.toString().length()==00){
                    taskName.setError("Tên không được phép rỗng");
                    editNameTask.setClickable(false);
                    editNameTask.setColorFilter(getResources().getColor(R.color.dark_gray));
                }
                else {
                    taskName.setError(null);
                    yellTaskAdapter.setParentName(s.toString());
                    editNameTask.setClickable(true);
                    if (taskIcon.getTag() == "true")
                        editNameTask.setColorFilter(getResources().getColor(R.color.green));
                }

            }
        });
        deleteTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkPermission()) {
                    Toast.makeText(getContext(), "Bạn không có quyền thực hiện chức năng này", Toast.LENGTH_LONG).show();
                    return;
                }
                if ((String)taskIcon.getTag() == "true") {
                    taskName.setEnabled(false);
                    taskName.setText(currentName);
                    taskName.setError(null);

                    taskIcon.setClickable(false);
                    taskIcon.setTag("false");
                    taskIcon.setImageDrawable(currentDrawable[0]);

                    editNameTask.setImageResource(R.drawable.ic_edit_mode);
                    editNameTask.setColorFilter(null);

                    deleteTask.setImageResource(R.drawable.ic_delete);
                    deleteTask.setColorFilter(null);

                    currentName.delete(0,currentName.length());
                }
                else {
                    MaterialAlertDialogBuilder confirmDelete = new MaterialAlertDialogBuilder(getContext())
                            .setTitle("Xoá công việc")
                            .setMessage("Bạn có chắc chắn muốn xoá công việc này?")
                            .setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    viewModel.deleteTask(currentYellTask);
                                    getActivity().getSupportFragmentManager().popBackStack();
                                }
                            })
                            .setNegativeButton("Huỷ", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                    confirmDelete.show();
                }
            }
        });
        taskIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkPermission()) {
                    Toast.makeText(getContext(), "Bạn không có quyền thực hiện chức năng này", Toast.LENGTH_LONG).show();
                    return;
                }
                BottomSheetIconPicker bottomSheetIconPicker = new BottomSheetIconPicker();
                bottomSheetIconPicker.show(getActivity().getSupportFragmentManager(),"Icon Picker");
            }
        });

        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getActivity()!= null){
                    requireActivity().onBackPressed();
                }
            }
        });
        if (previousTaskName != null)
            binding.previousTask.setText(previousTaskName);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setConfigTaskListener() {
        AppCompatTextView deadlineTask = binding.deadlineTask;
        DeadlineTimeDialog deadlineTimeDialog = new DeadlineTimeDialog();
        deadlineTimeDialog.getDateTimeLiveData().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                deadlineTask.setText(s);
                currentYellTask.setEnd_time(mobileTime2ServerTime(s));
                viewModel.editTask(currentYellTask);
            }
        });
        deadlineTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkPermission()) {
                    Toast.makeText(getContext(), "Bạn không có quyền thực hiện chức năng này", Toast.LENGTH_LONG).show();
                    return;
                }
                deadlineTimeDialog.show(getActivity().getSupportFragmentManager(),
                        "DeadlineTimeDialog");
            }
        });
        AppCompatTextView priority = binding.priorityTextView;
        priority.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkPermission()) {
                    Toast.makeText(getContext(), "Bạn không có quyền thực hiện chức năng này", Toast.LENGTH_LONG).show();
                    return;
                }
                String [] priorities = new String[]{"Thấp","Thường","Cao"};
                MaterialAlertDialogBuilder priorityDialog = new MaterialAlertDialogBuilder(getContext())
                        .setTitle("Độ ưu tiên")
                        .setItems(priorities, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                priority.setText(priorities[which]);
                                currentYellTask.setPriority(which+1);
                                viewModel.editTask(currentYellTask);
                            }
                        });
                priorityDialog.show();
            }
        });
        AppCompatTextView status = binding.statusTextView;
        status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkPermission()) {
                    Toast.makeText(getContext(), "Bạn không có quyền thực hiện chức năng này", Toast.LENGTH_LONG).show();
                    return;
                }
                String [] statuses = new String[]{"Chưa hoàn thành","Đã hoàn thành"};
                MaterialAlertDialogBuilder statusDialog = new MaterialAlertDialogBuilder(getContext())
                        .setTitle("Trạng thái")
                        .setItems(statuses, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                status.setText(statuses[which]);
                                currentYellTask.setStatus(which+1);
                                viewModel.editTask(currentYellTask);
                            }
                        });
                statusDialog.show();
            }
        });
        AppCompatEditText content = binding.contentEditText;
        AppCompatImageButton editContent = binding.editContent;
        AppCompatImageButton editContentDiscard = binding.editContentDiscard;
        StringBuffer currentContent = new StringBuffer();
        editContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkPermission()) {
                    Toast.makeText(getContext(), "Bạn không có quyền thực hiện chức năng này", Toast.LENGTH_LONG).show();
                    return;
                }
                if (editContentDiscard.getVisibility() == View.GONE) {
                    content.setEnabled(true);
                    content.requestFocusFromTouch();
                    InputMethodManager imm = (InputMethodManager)
                            getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(content, InputMethodManager.SHOW_IMPLICIT);
                    content.setSelection(content.getText().length());

                    editContent.setImageResource(R.drawable.ic_done);
                    editContent.setColorFilter(getResources().getColor(R.color.green));

                    editContentDiscard.setVisibility(View.VISIBLE);
                    currentContent.append(content.getText());
                }
                else {
                    content.setEnabled(false);
                    currentContent.delete(0,currentContent.length());

                    editContent.setColorFilter(null);
                    editContent.setImageResource(R.drawable.ic_edit_mode);

                    editContentDiscard.setVisibility(View.GONE);

                    currentYellTask.setContent(content.getText().toString());
                    viewModel.editTask(currentYellTask);
                }
            }
        });
        editContentDiscard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                content.setEnabled(false);
                content.setText(currentContent.toString());
                content.setError(null);
                currentContent.delete(0,currentContent.length());


                editContent.setColorFilter(null);
                editContent.setImageResource(R.drawable.ic_edit_mode);

                editContentDiscard.setVisibility(View.GONE);
            }
        });

        AppCompatTextView chooseFileTextView = binding.chooseFileTextView;
        BottomSheetFilePicker filePicker = new BottomSheetFilePicker();
        chooseFileTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filePicker.show(getActivity().getSupportFragmentManager(),"File Picker");
            }
        });

        content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length()>200) {
                    editContent.setClickable(false);
                    editContent.setColorFilter(getResources().getColor(R.color.dark_gray));
                    content.setError("Nhập quá 200 ký tự");
                }
                else
                {
                    if (editContentDiscard.getVisibility() == View.VISIBLE) {
                        editContent.setClickable(true);
                        editContent.setColorFilter(getResources().getColor(R.color.green));
                    }
                    content.setError(null);
                }
            }
        });


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        binding.listTask.setAdapter(yellTaskAdapter);
        binding.listTask.setLayoutManager(linearLayoutManager);
        yellTaskAdapter.getSizeList().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                subTasks.remove(integer);
            }
        });
        binding.addSubTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkPermission()) {
                    Toast.makeText(getContext(), "Bạn không có quyền thực hiện chức năng này", Toast.LENGTH_LONG).show();
                    return;
                }
                while(currentYellTask.getTask_id() == null)
                    ;
                viewModel.addTask(new YellTask(currentYellTask.getDashboard_id(),"Untitled",currentYellTask.getTask_id()));
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private String serverTime2MobileTime(String time) {
        SimpleDateFormat currentFormat = new SimpleDateFormat("HH:mm  dd/MM/yyyy");
        currentFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date date = isoFormat.parse(time);
            return currentFormat.format(date);
        } catch (ParseException e) {
            Log.e("TimeParseError", "Time Parse Error");
            return null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private String mobileTime2ServerTime(String time) {
        SimpleDateFormat currentFormat = new SimpleDateFormat("HH:mm  dd/MM/yyyy");
        currentFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date date = currentFormat.parse(time);
            return isoFormat.format(date);
        } catch (ParseException e) {
            Log.e("TimeParseError", "Time Parse Error");
            return null;
        }
    }

    private boolean checkPermission() {
        String uid = getActivity().getSharedPreferences(getActivity().getResources().getString(R.string.yell_sp), MODE_PRIVATE)
                .getString("uid","n");
        try {
            List<DashboardPermission> users = dashboard.getUsers();
            for (int i = 0;i<users.size();i++) {
                if (uid.equals(users.get(i).getUid()))
                    if ((users.get(i).getRole().equals("admin"))||(users.get(i).getRole().equals("editor"))) {
                        return true;
                    }
            }
        }
        catch (Exception e) {
            viewModel.getDashboard(currentYellTask.getDashboard_id());
            return checkPermission();
        }
        return false;
    }

}