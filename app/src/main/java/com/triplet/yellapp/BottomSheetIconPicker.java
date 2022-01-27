package com.triplet.yellapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.triplet.yellapp.adapters.IconAdapter;
import com.triplet.yellapp.databinding.DialogIconPickerBinding;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class BottomSheetIconPicker extends BottomSheetDialogFragment {
    public BottomSheetIconPicker () {

    };
    private DialogIconPickerBinding binding;
    private RecyclerView recyclerView;
    private IconAdapter iconAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogIconPickerBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        ArrayList<Integer> resourceList = new ArrayList<>();
        Field[] ID_Fields = R.drawable.class.getFields();
        resourceList.add(R.drawable.ic_close);
        resourceList.add(R.drawable.ic_date);
        resourceList.add(R.drawable.ic_delete);
        resourceList.add(R.drawable.ic_done);
        resourceList.add(R.drawable.ic_add);
        resourceList.add(R.drawable.ic_google);
        resourceList.add(R.drawable.ic_home);
        resourceList.add(R.drawable.ic_eye_hide);
        resourceList.add(R.drawable.ic_eye_show);
        resourceList.add(R.drawable.ic_search_black_24dp);
        resourceList.add(R.drawable.ic_edit_mode);
        resourceList.add(R.drawable.ic_account);
        recyclerView = binding.iconGallery;
        iconAdapter = new IconAdapter(getContext());
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),4);
        recyclerView.setLayoutManager(gridLayoutManager);
        iconAdapter.setData(resourceList);
        recyclerView.setAdapter(iconAdapter);
        return view;
    }
}
