package com.gstarcad.andy.unrarsample.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.gstarcad.andy.unrarsample.R;
import com.gstarcad.andy.unrarsample.utils.AssetsUtils;
import com.gstarcad.andy.unrarsample.utils.DateUtils;
import com.gstarcad.andy.unrarsample.utils.FileUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Andy.R
 */
public class MainActivity extends AppCompatActivity {


    private RecyclerView rvZipRarList;
    private BaseQuickAdapter baseQuickAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        copyAssetsToSD();
        initListener();
    }

    private void initListener() {
        if (baseQuickAdapter != null) {
            baseQuickAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    Intent intent = new Intent(MainActivity.this, ZipRarFileActivity.class);
                    intent.putExtra("zipRarFilePath", ((File) adapter.getItem(position)).getAbsolutePath());
                    startActivity(intent);
                }
            });
        }
    }

    private void initView() {
        rvZipRarList = findViewById(R.id.rv_ziprar_list);
        rvZipRarList.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * 复制到SD卡
     */
    private void copyAssetsToSD() {
        File file = AssetsUtils.copyAssetsToSD(this, "20181119.rar");
        if (file != null && file.exists()) {
            File parentFile = file.getParentFile();
            File[] files = parentFile.listFiles();
            List<File> filesList = Arrays.asList(files);
            baseQuickAdapter = new BaseQuickAdapter<File, BaseViewHolder>(R.layout.file_list_item, filesList) {
                @Override
                protected void convert(BaseViewHolder helper, File itemFile) {
                    if (itemFile != null) {
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
                }
            };
            rvZipRarList.setAdapter(baseQuickAdapter);
        } else {
            Toast.makeText(this, getResources().getString(R.string.file_copy_error),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
