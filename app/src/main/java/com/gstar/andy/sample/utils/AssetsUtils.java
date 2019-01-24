package com.gstar.andy.sample.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Assets工具类
 *
 * @author Andy.R
 */
public class AssetsUtils {

    public static final String SD_FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();

    /**
     * 将assets文件复制到SD卡
     *
     * @param context
     * @param assetsFileName
     * @return
     */
    public static File copyAssetsToSD(Context context, String assetsFileName) {
        AssetManager assetManager = context.getResources().getAssets();
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        byte[] buffer = new byte[1024 * 2];
        int length;
        File file;
        try {
            inputStream = assetManager.open(assetsFileName);
            file = new File(SD_FILE_PATH + "/UnRar/" + assetsFileName);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            fileOutputStream = new FileOutputStream(file);
            while ((length = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, length);
            }
            fileOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
            file = null;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return file;
    }
}
