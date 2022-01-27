package com.triplet.yellapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileUtils;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.triplet.yellapp.adapters.FileNameAdapter;
import com.triplet.yellapp.databinding.DialogAttachBinding;
import com.triplet.yellapp.databinding.FragmentTaskBinding;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class BottomSheetFilePicker extends BottomSheetDialogFragment {

    private static final int MY_REQUEST_CODE_PERMISSION = 1000;
    private static final int MY_RESULT_CODE_FILECHOOSER = 2000;
    private static final String LOG_TAG = "AndroidExample";

    DialogAttachBinding binding;
    RecyclerView recyclerView;
    FileNameAdapter fileNameAdapter;

    public BottomSheetFilePicker () {
        fileNameAdapter = new FileNameAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogAttachBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        recyclerView = binding.fileNameList;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(fileNameAdapter);
        fileNameAdapter.getSizeFileNameList().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                AppCompatTextView fileCount = ((AppCompatTextView)getActivity().findViewById(R.id.chooseFileTextView));
                fileCount.setText(String.valueOf(integer)+" tệp");
            }
        });
        AppCompatButton chooseFileButton = binding.chooseFileButton;
        AppCompatButton cancelFileButton = binding.cancelFileButton;
        chooseFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO : Lấy uri từ activity result, từ uri trích xuất file gửi lên cho server
                if (cancelFileButton.getVisibility() == View.GONE) {
                    askPermissionAndBrowseFile();
                }
                else {
                    fileNameAdapter.addFileName(binding.fileNameShow.getText().toString());
                    cancelFileButton.setVisibility(View.GONE);
                    chooseFileButton.setText("Chọn tệp");
                    binding.fileNameShow.setText("Empty");

                }
            }
        });
        cancelFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelFileButton.setVisibility(View.GONE);
                chooseFileButton.setText("Chọn tệp");
                binding.fileNameShow.setText("Empty");
            }
        });
        return view;
    }
    private void askPermissionAndBrowseFile()  {
        // With Android Level >= 23, you have to ask the user
        // for permission to access External Storage.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) { // Level 23

            // Check if we have Call permission
            int permisson = ActivityCompat.checkSelfPermission(this.getContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE);

            if (permisson != PackageManager.PERMISSION_GRANTED) {
                // If don't have permission so prompt the user.
                this.requestPermissions(
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_REQUEST_CODE_PERMISSION
                );
                return;
            }
        }
        this.doBrowseFile();
    }
    private void doBrowseFile()  {
        Intent chooseFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFileIntent.setType("*/*");
        // Only return URIs that can be opened with ContentResolver
        chooseFileIntent.addCategory(Intent.CATEGORY_OPENABLE);

        chooseFileIntent = Intent.createChooser(chooseFileIntent, "Choose a file");
        startActivityForResult(chooseFileIntent, MY_RESULT_CODE_FILECHOOSER);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //
        switch (requestCode) {
            case MY_REQUEST_CODE_PERMISSION: {

                // Note: If request is cancelled, the result arrays are empty.
                // Permissions granted (CALL_PHONE).
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.i( LOG_TAG,"Permission granted!");
                    Toast.makeText(this.getContext(), "Permission granted!", Toast.LENGTH_SHORT).show();

                    this.doBrowseFile();
                }
                // Cancelled or denied.
                else {
                    Log.i(LOG_TAG,"Permission denied!");
                    Toast.makeText(this.getContext(), "Permission denied!", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // If the user doesn't pick a file just return
        if (requestCode != MY_RESULT_CODE_FILECHOOSER) {
            return;
        }
        Uri uri = data.getData();
        Cursor cursor = getActivity().getContentResolver()
                .query(uri, null, null, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                @SuppressLint("Range") String displayName = cursor.getString(
                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
               if (displayName != null) {
                   binding.fileNameShow.setText(displayName);
                   binding.chooseFileButton.setText("Lưu");
                   binding.cancelFileButton.setVisibility(View.VISIBLE);
               }

            }
        }
        finally {
            cursor.close();
        }
    }

}
