package com.triplet.yellapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.MutableLiveData;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.triplet.yellapp.databinding.DialogDeadlineTimeBinding;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DeadlineTimeDialog extends DialogFragment {
    public String date;
    public String time;
    DialogDeadlineTimeBinding binding;
    MutableLiveData<String> dateTime;

    public DeadlineTimeDialog() {
        dateTime = new MutableLiveData<>();
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Activity activity = getActivity();
        View view = configView();
        return new MaterialAlertDialogBuilder(activity)
                .setTitle("Thời gian hết hạn")
                .setPositiveButton("Hoàn thành", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TextView deadlineTask = getActivity().findViewById(R.id.deadlineTask);
                        deadlineTask.setText(time+"  "+date);
                        dateTime.postValue(time+"  "+date);
                    }
                })
                .setNegativeButton("Huỷ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setView(view)
                .create();
    }

    public MutableLiveData<String> getDateTimeLiveData (){
        return dateTime;
    }

    private View configView() {
        binding = DialogDeadlineTimeBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        String [] modes = new String[]{"Không","Trước 1 giờ","Trước 1 ngày"};
        ArrayAdapter<String> modeList = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1,modes);
        modeList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.reminderMode.setAdapter(modeList);
        binding.dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialDatePicker datePicker = MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Chọn ngày")
                        .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
                        .build();
                datePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
                    @Override
                    public void onPositiveButtonClick(Object selection) {
                        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                        date = formatter.format(selection);
                        binding.dateEditText.setText(date);
                        binding.dateEditText.setError(null);
                    }
                });
                datePicker.show(getActivity().getSupportFragmentManager(),"Date Picker");
            }
        });

        binding.dateEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isValidDate(binding.dateEditText.getText().toString()))
                    binding.dateEditText.setError("Ngày không hợp lệ");
            }

            @Override
            public void afterTextChanged(Editable s) {
                date = binding.dateEditText.getText().toString();
            }
        });

        binding.timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                        .setTitleText("Chọn giờ")
                        .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                        .setTimeFormat(TimeFormat.CLOCK_24H)
                        .build();
                timePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String hour = String.format("%02d",timePicker.getHour());
                        String minute = String.format("%02d",timePicker.getMinute());
                        time = hour+":"+minute;
                        binding.timeEditText.setText(time);
                        binding.timeEditText.setError(null);
                    }
                });
                timePicker.show(getActivity().getSupportFragmentManager(),"Time Picker");
            }
        });

        binding.timeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isValidTime(binding.timeEditText.getText().toString()))
                    binding.timeEditText.setError("Thời gian không hợp lệ");
            }

            @Override
            public void afterTextChanged(Editable s) {
                time = binding.timeEditText.getText().toString();
            }
        });
        return view;
    }

    public boolean isValidDate(String dateStr) {
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        formatter.setLenient(false);
        try {
            formatter.parse(dateStr);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }
    public boolean isValidTime(String timeStr) {
        if (timeStr.matches("^(2[0-3]|[01]?[0-9]):([0-5]?[0-9])$"))
            return true;
        return false;
    }

}
