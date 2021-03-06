package com.triplet.yellapp.adapters;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.triplet.yellapp.LoadingDialog;
import com.triplet.yellapp.R;
import com.triplet.yellapp.TaskFragment;
import com.triplet.yellapp.models.DashboardCard;
import com.triplet.yellapp.models.DashboardPermission;
import com.triplet.yellapp.models.InfoMessage;
import com.triplet.yellapp.models.YellTask;
import com.triplet.yellapp.repository.YellTaskRepository;
import com.triplet.yellapp.utils.ApiService;
import com.triplet.yellapp.utils.Client;
import com.triplet.yellapp.utils.SessionManager;
import com.triplet.yellapp.viewmodels.DashboardViewModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    List<YellTask> yellTaskArrayList;
    MutableLiveData<Integer> sizeList;
    YellTaskRepository repository;
    FragmentActivity activity;
    String parentName;
    String role;

    public TaskAdapter(FragmentActivity activity) {
        this.activity = activity;
        yellTaskArrayList = new ArrayList<>();
        repository = new YellTaskRepository(activity.getApplication());
        role = "n";
        sizeList = new MutableLiveData<>();
    }

    public void setRole(List<DashboardPermission> users) {
        String uid = activity.getSharedPreferences(activity.getResources().getString(R.string.yell_sp), MODE_PRIVATE)
                .getString("uid","n");
        for (int i = 0;i<users.size();i++) {
            if (uid.equals(users.get(i).getUid()))
                this.role = users.get(i).getRole();
        }
    }

    public MutableLiveData<Integer> getSizeList (){
        return sizeList;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public void setYellTaskArrayList(List<YellTask> yells) {
        this.yellTaskArrayList = yells;
        notifyDataSetChanged();
    }

    public void addYellTask(YellTask yellTask) {
        yellTaskArrayList.add(yellTask);
        notifyItemInserted(yellTaskArrayList.size()-1);
    }

    public void removeYellTask(int position) {
        yellTaskArrayList.remove(position);
        sizeList.postValue(position);
        notifyItemRemoved(position);
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task,parent,false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        YellTask yellTask = yellTaskArrayList.get(position);
        String taskName = yellTask.getName();
        Integer status = yellTask.getStatus();
        if (yellTask == null)
            return;
        holder.taskName.setText(taskName);
        if (status == null)
        {

        }
        else if (status == 2)
        {
            holder.taskLabel.setText("???? ho??n th??nh");
            holder.taskLabel.setBackgroundResource(R.drawable.frame_cover_item_task_green);
            holder.makeCompleteBtn.setImageResource(R.drawable.ic_check_circle_filled);
            holder.makeCompleteBtn.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.orange)));
        }
        else if (status == 1) {
            holder.taskLabel.setText("Ch??a ho??n th??nh");
            holder.taskLabel.setBackgroundResource(R.drawable.frame_cover_item_task_yellow);
            holder.makeCompleteBtn.setImageResource(R.drawable.ic_check_circle_line);
            TypedValue typedValue = new TypedValue();
            activity.getTheme().resolveAttribute(R.attr.iconTint, typedValue, true);
            holder.makeCompleteBtn.setImageTintList(ColorStateList.valueOf(typedValue.data));
        }
        holder.deleteTaskItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (role.equals("admin")||(role.equals("editor"))) {
                    MaterialAlertDialogBuilder confirmDelete = new MaterialAlertDialogBuilder(activity)
                            .setTitle("Xo?? c??ng vi???c")
                            .setMessage("B???n c?? ch???c ch???n mu???n xo?? c??ng vi???c n??y?")
                            .setPositiveButton("?????ng ??", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    repository.deleteYellTask(yellTask);
                                    removeYellTask(holder.getLayoutPosition());
                                }
                            })
                            .setNegativeButton("Hu???", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                    confirmDelete.show();
                }
                else {
                    Toast.makeText(activity, "B???n kh??ng c?? quy???n th???c hi???n ch???c n??ng n??y", Toast.LENGTH_LONG).show();
                }
            }
        });
        holder.taskName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TaskFragment fragment = new TaskFragment(yellTask,parentName);
                activity.getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right,
                                R.anim.slide_in_right, R.anim.slide_out_left)
                        .replace(R.id.fragmentContainer, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
        holder.makeCompleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((yellTask.status == null)||(yellTask.status == 2)) {
                    yellTask.status = 1;
                    ((ImageButton) view).setImageResource(R.drawable.ic_check_circle_line);
                    holder.taskLabel.setText("Ch??a ho??n th??nh");
                    holder.taskLabel.setBackgroundResource(R.drawable.frame_cover_item_task_yellow);
                    holder.makeCompleteBtn.setImageResource(R.drawable.ic_check_circle_line);
                    TypedValue typedValue = new TypedValue();
                    activity.getTheme().resolveAttribute(R.attr.iconTint, typedValue, true);
                    holder.makeCompleteBtn.setImageTintList(ColorStateList.valueOf(typedValue.data));
                }
                else {
                    yellTask.status = 2;
                    ((ImageButton) view).setImageResource(R.drawable.ic_check_circle_filled);
                    holder.taskLabel.setText("???? ho??n th??nh");
                    holder.taskLabel.setBackgroundResource(R.drawable.frame_cover_item_task_green);
                    holder.makeCompleteBtn.setImageResource(R.drawable.ic_check_circle_filled);
                    holder.makeCompleteBtn.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.orange)));
                }
                repository.patchTask(yellTask);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (yellTaskArrayList != null)
            return yellTaskArrayList.size();
        return 0;
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder {
        AppCompatTextView taskName;
        AppCompatTextView taskLabel;
        CardView deleteTaskItem;
        ImageButton makeCompleteBtn;
        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskName = itemView.findViewById(R.id.taskNameItem);
            taskLabel = itemView.findViewById(R.id.taskLabelItem);
            deleteTaskItem = itemView.findViewById(R.id.deleteTaskItem);
            makeCompleteBtn = itemView.findViewById(R.id.makeCompleteBtn);
        }
    }
}
