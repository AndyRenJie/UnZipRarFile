package com.gstarcad.andy.unrarsample.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.gstarcad.andy.unrarsample.R;
import com.gstarcad.andy.unrarsample.utils.DateUtils;
import com.gstarcad.andy.unrarsample.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * 路径选择界面
 *
 * @author Andy.R
 */
public class PathSelectedActivity extends AppCompatActivity {

    private ImageView ivBack;
    private RecyclerView rvFilePath;
    private Button btnAdd;
    private RecyclerView rvFileList;
    private Button btnCancel, btnOk;

    private List<File> dirFileList = new ArrayList<>();
    private List<String> dirFilePath = new ArrayList<>();

    private BaseQuickAdapter fileListAdapter;
    private BaseQuickAdapter filePathAdapter;

    private String currentPath;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path_selected);
        initView();
        setAdapter();
        getFileList(FileUtils.getSDCardFilesPath());
        initListener();
    }

    private void setAdapter() {
        fileListAdapter = new BaseQuickAdapter<File, BaseViewHolder>(R.layout.file_list_item, dirFileList) {
            @Override
            protected void convert(BaseViewHolder helper, File itemFile) {
                helper.setText(R.id.tv_file_name, itemFile.getName());
                helper.setText(R.id.tv_file_date, DateUtils.getDateToString(new Date(itemFile.lastModified())));
                helper.setText(R.id.tv_file_size, FileUtils.formatFileSize(itemFile.length()));
                helper.setImageResource(R.id.iv_file_icon, FileUtils.getFileIcon(
                        itemFile.isDirectory(), itemFile.getAbsolutePath()));
                if (itemFile.isDirectory()) {
                    helper.setVisible(R.id.tv_file_size, false);
                } else {
                    helper.setVisible(R.id.tv_file_size, true);
                }
            }
        };
        rvFileList.setAdapter(fileListAdapter);

        filePathAdapter = new BaseQuickAdapter<String, BaseViewHolder>(R.layout.file_path_item, dirFilePath) {
            @Override
            protected void convert(BaseViewHolder helper, String itemPath) {
                helper.setText(R.id.tv_value, itemPath);
            }
        };
        rvFilePath.setAdapter(filePathAdapter);
    }

    private void initListener() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(PathSelectedActivity.this, getResources().getString(R.string.add_file_no_open),
                        Toast.LENGTH_SHORT).show();
            }
        });
        fileListAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                getFileList(((File) adapter.getItem(position)).getAbsolutePath());
            }
        });
        filePathAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (dirFilePath != null && dirFilePath.size() > 0) {
                    if (position == dirFilePath.size() - 1) {
                        return;
                    }
                    String sdFilePath;
                    if (position > 2) {
                        StringBuilder strBuilderPath = new StringBuilder();
                        for (int i = 0; i < position + 1; i++) {
                            strBuilderPath.append(dirFilePath.get(i)).append("/");
                        }
                        sdFilePath = strBuilderPath.substring(0, strBuilderPath.length() - 1);
                    } else {
                        sdFilePath = FileUtils.getSDCardFilesPath();
                    }
                    getFileList(sdFilePath);
                }
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("selectedPath", currentPath);
                setResult(RESULT_OK, intent);
                onBackPressed();
            }
        });
    }

    private void getFileList(String path) {
        currentPath = path;
        //处理Path
        dirFilePath.clear();
        String[] splitPath = path.split("/");
        if (splitPath != null && splitPath.length > 0) {
            for (String filePath : splitPath) {
                if (!TextUtils.isEmpty(filePath)) {
                    dirFilePath.add(filePath);
                }
            }
            filePathAdapter.replaceData(dirFilePath);
            filePathAdapter.notifyDataSetChanged();
        }
        //处理List
        dirFileList.clear();
        File dirFile = new File(path);
        if (dirFile != null && dirFile.isDirectory()) {
            File[] files = dirFile.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    if (file.isDirectory() && !file.getName().startsWith(".")) {
                        dirFileList.add(file);
                    }
                }
                fileListAdapter.replaceData(dirFileList);
                fileListAdapter.notifyDataSetChanged();
            }
        }
    }

    private void initView() {
        ivBack = findViewById(R.id.iv_back);
        btnAdd = findViewById(R.id.btn_add);
        rvFilePath = findViewById(R.id.rv_file_path);
        rvFilePath.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,
                false));
        rvFileList = findViewById(R.id.rv_file_list);
        rvFileList.setLayoutManager(new LinearLayoutManager(this));

        btnCancel = findViewById(R.id.btn_cancel);
        btnOk = findViewById(R.id.btn_ok);
    }
}
