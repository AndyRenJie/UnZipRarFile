package com.gstar.andy.unzipsample.ui.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.gstar.andy.unzipsample.R;
import com.gstar.andy.unzipsample.manager.UnRarManager;
import com.gstar.andy.unzipsample.manager.UnZipManager;
import com.gstar.andy.unzipsample.ui.activity.ZipRarFileActivity;
import com.gstar.andy.unzipsample.utils.FileUtils;

import java.io.File;

/**
 * @author Andy.R
 */
public class PasswordDialog extends Dialog {

    private String outFolerPath;
    private String fileModelPath;
    private String zipRarFilePath;

    private Context context;
    private EditText etInputPassword;

    public PasswordDialog(@NonNull Context context, int themeResId, String zipRarFilePath,
                          String outFolerPath, String fileModelPath) {
        super(context, themeResId);
        this.context = context;
        this.zipRarFilePath = zipRarFilePath;
        this.outFolerPath = outFolerPath;
        this.fileModelPath = fileModelPath;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_edit_input);
        initView();
        initListener();
    }

    private void initListener() {
        findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = etInputPassword.getText().toString().trim();
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(context, context.getResources().getString(R.string.password_is_empty),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                //用fileModelPath来判断是解压全部还是单个预览
                if (TextUtils.isEmpty(fileModelPath)) {
                    ((ZipRarFileActivity) context).unZipRarAllFile(outFolerPath, password);
                } else {
                    String zipRarOutFilePath;
                    if (zipRarFilePath.endsWith(".zip")) {
                        zipRarOutFilePath = UnZipManager.getInstance().unZipFileSingle(zipRarFilePath,
                                outFolerPath, fileModelPath, password);
                    } else {
                        zipRarOutFilePath = UnRarManager.getInstance().unRarFileSingle(new File(zipRarFilePath),
                                outFolerPath, fileModelPath, password);
                    }
                    //输出路径为空代表密码错误解压失败
                    if (TextUtils.isEmpty(zipRarOutFilePath)) {
                        Toast.makeText(context, context.getResources().getString(R.string.passowrd_error),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        FileUtils.openFileByApp(context, zipRarOutFilePath);
                    }
                }
                dismiss();
            }
        });
    }

    private void initView() {
        etInputPassword = findViewById(R.id.et_input_password);
    }
}
