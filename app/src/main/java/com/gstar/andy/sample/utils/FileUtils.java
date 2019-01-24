package com.gstar.andy.sample.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.gstar.andy.sample.MyApplication;
import com.gstar.andy.sample.R;
import com.gstar.andy.sample.bean.FileModel;
import com.zhy.base.fileprovider.FileProvider7;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 文件工具
 *
 * @author Andy.R
 */
public class FileUtils {

    /**
     * QQ文件夹
     **/
    public final static String QQ_FILES = "/Tencent/QQfile_recv/";
    /**
     * 微信文件夹
     **/
    public final static String WEIXIN_FILES = "/Tencent/MicroMsg/Download/";
    /**
     * 临时文件夹(隐藏目录，存储在安装包名下)
     **/
    public final static String DEFAULT_TEMP = "/.Temp/";

    /**
     * 转换文件大小单位(KB/MB/GB)
     *
     * @param fileSize 转换文件大小
     * @return
     */
    public static String formatFileSize(long fileSize) {
        DecimalFormat df = new DecimalFormat("0.00");
        String fileSizeString;
        if (fileSize <= 0) {
            fileSizeString = "0KB";
        } else if (fileSize < (1024 * 1024)) {
            fileSizeString = df.format((double) fileSize / 1024) + "KB";
        } else if (fileSize < (1024 * 1024 * 1024)) {
            fileSizeString = df.format((double) fileSize / (1024 * 1024)) + "MB";
        } else {
            fileSizeString = df.format((double) fileSize / (1024 * 1024 * 1024)) + "GB";
        }
        return fileSizeString;
    }

    /**
     * 获取文件扩展名(不包含前面那个点 ‘.’)
     *
     * @param path
     * @return
     */
    public static String getFileExtensionNoPoint(String path) {
        if (TextUtils.isEmpty(path)) {
            return "";
        }
        return getFileExtensionNoPoint(new File(path));
    }

    /**
     * 获取文件扩展名(不包含前面那个点 ‘.’)
     *
     * @param file
     * @return
     */
    private static String getFileExtensionNoPoint(File file) {
        if (file == null || file.isDirectory()) {
            return "";
        }
        String fileName = file.getName();
        if (fileName != null && fileName.length() > 0) {
            int lastIndex = fileName.lastIndexOf('.');
            if ((lastIndex > -1) && (lastIndex < (fileName.length() - 1))) {
                return fileName.substring(lastIndex + 1);
            }
        }
        return "";
    }

    /**
     * 本地文件数组排序功能
     *
     * @param list    排序数据
     * @param sortKey 排序字段或者说是种类名称（如：日期，文件名，文件大小，文件类型）
     * @param order   排序方式 true 升序 false 降序
     */
    public static void sortFileModelList(List<FileModel> list, final String sortKey, final boolean order) {
        if (list == null || list.size() < 1 || TextUtils.isEmpty(sortKey)) {
            return;
        }
        try {
            Collections.sort(list, new Comparator<FileModel>() {
                @Override
                public int compare(FileModel o1, FileModel o2) {
                    if (TextUtils.isEmpty(sortKey)) {
                        return 0;
                    }
                    if (sortKey.equalsIgnoreCase("fileName")) {
                        String str1 = o1.getFileName();
                        String str2 = o2.getFileName();
                        if (order) {
                            return str1.compareToIgnoreCase(str2);
                        } else {
                            return str2.compareToIgnoreCase(str1);
                        }
                    } else if (sortKey.equalsIgnoreCase("fileType")) {
                        String str1 = o1.getFileType();
                        String str2 = o2.getFileType();
                        if (order) {
                            return str1.compareToIgnoreCase(str2);
                        } else {
                            return str2.compareToIgnoreCase(str1);
                        }
                    } else if (sortKey.equalsIgnoreCase("fileDate")) {
                        Long lng1 = o1.getFileDate();
                        Long lng2 = o2.getFileDate();
                        if (order) {
                            return lng1.compareTo(lng2);
                        } else {
                            return lng2.compareTo(lng1);
                        }
                    } else if (sortKey.equalsIgnoreCase("fileSize")) {
                        Long lng1 = o1.getFileSize();
                        Long lng2 = o2.getFileSize();
                        if (order) {
                            return lng1.compareTo(lng2);
                        } else {
                            return lng2.compareTo(lng1);
                        }
                    }
                    return 0;
                }
            });
            if (!sortKey.equalsIgnoreCase("fileType")) {
                sortFileMode_FolderToTop(list);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 按照文件夹在上，文件在下的顺序排列
     *
     * @param list 排序数据
     */
    private static void sortFileMode_FolderToTop(List<FileModel> list) {
        if (list == null || list.size() < 1) {
            return;
        }
        try {
            Collections.sort(list, new Comparator<FileModel>() {
                @Override
                public int compare(FileModel o1, FileModel o2) {
                    boolean bool1 = o1.isDir();
                    boolean bool2 = o2.isDir();
                    if (bool1 != bool2) {
                        if (bool1) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                    return 0;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取文件icon
     *
     * @param filePath
     * @return
     */
    public static int getFileIcon(boolean isDir, String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            return getFileIcon(isDir, new File(filePath));
        } else {
            return 0;
        }
    }


    /**
     * 比较两个文件是否相同
     *
     * @param path1
     * @param path2
     * @return true 相同,false 不同
     */
    private static boolean isCompareFiles(String path1, String path2) {
        if (TextUtils.isEmpty(path1) || TextUtils.isEmpty(path2)) {
            return false;
        }
        if (path1.equalsIgnoreCase(path2)) {
            return true;
        } else {
            return isCompareFiles(new File(path1), new File(path2));
        }
    }

    /**
     * 比较两个文件是否相同
     *
     * @param file1
     * @param file2
     * @return true 相同,false 不同
     */
    private static boolean isCompareFiles(File file1, File file2) {
        if (file1 == null || file2 == null) {
            return false;
        }
        if (file1.getPath().equalsIgnoreCase(file2.getPath())) {
            return true;
        }
        return false;
    }

    /**
     * 获取文件icon
     *
     * @param file
     * @return
     */
    private static int getFileIcon(boolean isDir, File file) {
        int resId = 0;
        if (isDir) {
            if (FileUtils.isCompareFiles(file.getPath(), FileUtils.getSDCardFilesPath_QQ())) {
                resId = R.mipmap.file_icon_folder_qq;
            } else if (FileUtils.isCompareFiles(file.getPath(), FileUtils.getSDCardFilesPath_WeiXin())) {
                resId = R.mipmap.file_icon_folder_weixin;
            } else if (FileUtils.isCompareFiles(file.getPath(), FileUtils.getSDCardDownloadPath())) {
                resId = R.mipmap.file_icon_folder_download;
            } else {
                resId = R.mipmap.file_icon_folder;
            }
            return resId;
        }
        if (file != null) {
            String fileType = getFileExtensionNoPoint(file);
            if (TextUtils.isEmpty(fileType) || file.isDirectory()) {
                if (FileUtils.isCompareFiles(file.getPath(), FileUtils.getSDCardFilesPath_QQ())) {
                    resId = R.mipmap.file_icon_folder_qq;
                } else if (FileUtils.isCompareFiles(file.getPath(), FileUtils.getSDCardFilesPath_WeiXin())) {
                    resId = R.mipmap.file_icon_folder_weixin;
                } else if (FileUtils.isCompareFiles(file.getPath(), FileUtils.getSDCardDownloadPath())) {
                    resId = R.mipmap.file_icon_folder_download;
                } else {
                    resId = R.mipmap.file_icon_folder;
                }
            } else if (fileType.equalsIgnoreCase("dwg")) {
                resId = R.mipmap.file_icon_dwg;
            } else if (fileType.equalsIgnoreCase("dws")) {
                resId = R.mipmap.file_icon_dws;
            } else if (fileType.equalsIgnoreCase("dwt")) {
                resId = R.mipmap.file_icon_dwt;
            } else if (fileType.equalsIgnoreCase("dxf")) {
                resId = R.mipmap.file_icon_dxf;
            } else if (fileType.equalsIgnoreCase("ocf")) {
                resId = R.mipmap.file_icon_ocf;
            } else if (fileType.equalsIgnoreCase("ttf")) {
                resId = R.mipmap.file_icon_ttf;
            } else if (fileType.equalsIgnoreCase("ttc")) {
                resId = R.mipmap.file_icon_ttc;
            } else if (fileType.equalsIgnoreCase("shx")) {
                resId = R.mipmap.file_icon_shx;
            } else if (fileType.equalsIgnoreCase("sht")) {
                resId = R.mipmap.file_icon_sht;
            } else if (fileType.equalsIgnoreCase("shp")) {
                resId = R.mipmap.file_icon_shp;
            } else if (fileType.equalsIgnoreCase("fon")) {
                resId = R.mipmap.file_icon_fon;
            } else if (fileType.equalsIgnoreCase("pdf")) {
                resId = R.mipmap.file_icon_pdf;
            } else if (fileType.equalsIgnoreCase("gif")) {
                resId = R.mipmap.file_icon_gif;
            } else if (fileType.equalsIgnoreCase("png")) {
                resId = R.mipmap.file_icon_png;
            } else if (fileType.equalsIgnoreCase("jpg")) {
                resId = R.mipmap.file_icon_jpg;
            } else if (fileType.equalsIgnoreCase("jpeg")) {
                resId = R.mipmap.file_icon_jpg;
            } else if (fileType.equalsIgnoreCase("bmp")) {
                resId = R.mipmap.file_icon_bmp;
            } else if (fileType.equalsIgnoreCase("doc")) {
                resId = R.mipmap.file_icon_doc;
            } else if (fileType.equalsIgnoreCase("docx")) {
                resId = R.mipmap.file_icon_docx;
            } else if (fileType.equalsIgnoreCase("xls")) {
                resId = R.mipmap.file_icon_xls;
            } else if (fileType.equalsIgnoreCase("xlsx")) {
                resId = R.mipmap.file_icon_xlsx;
            } else if (fileType.equalsIgnoreCase("ppt")) {
                resId = R.mipmap.file_icon_ppt;
            } else if (fileType.equalsIgnoreCase("pptx")) {
                resId = R.mipmap.file_icon_pptx;
            } else if (fileType.equalsIgnoreCase("txt")) {
                resId = R.mipmap.file_icon_txt;
            } else if (fileType.equalsIgnoreCase("tif")) {
                resId = R.mipmap.file_icon_tif;
            } else if (fileType.equalsIgnoreCase("rtf")) {
                resId = R.mipmap.file_icon_rtf;
            } else if (fileType.equalsIgnoreCase("jww")) {
                resId = R.mipmap.file_icon_jww;
            } else if (fileType.equalsIgnoreCase("rar")) {
                resId = R.mipmap.file_icon_rar;
            } else if (fileType.equalsIgnoreCase("zip")) {
                resId = R.mipmap.file_icon_zip;
            } else {
                resId = R.mipmap.file_icon_other;
            }
        }
        return resId;
    }

    /**
     * 获取SD卡根目下的QQ接收文件路径
     */
    private static String getSDCardFilesPath_QQ() {
        if (!isSDExist()) {
            return "";
        }
        String qqFilePath = getSDCardFilesPath() + QQ_FILES;
        if (checkFileExists(qqFilePath)) {
            return qqFilePath;
        } else {
            return "";
        }
    }

    /**
     * 获取SD卡根目下的微信接收文件路径
     */
    private static String getSDCardFilesPath_WeiXin() {
        if (!isSDExist()) {
            return "";
        }
        String qqFilePath = getSDCardFilesPath() + WEIXIN_FILES;
        if (checkFileExists(qqFilePath)) {
            return qqFilePath;
        } else {
            return "";
        }
    }

    /**
     * 检测SD卡是否存在
     */
    private static boolean isSDExist() {
        return Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取SD卡根目录路径
     *
     * @return e.g. /storage/sdcard0/
     */
    public static String getSDCardFilesPath() {
        if (!isSDExist()) {
            return "";
        }
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
    }

    /**
     * 获取SD卡根目下的下载目录路径
     *
     * @return e.g. /storage/sdcard0/Download/
     */
    private static String getSDCardDownloadPath() {
        if (!isSDExist()) {
            return "";
        }
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/";
    }


    /**
     * 判断SD卡中给定位置的文件是否存在
     *
     * @param strURL
     * @return true 存在 false 不存在
     */
    private static Boolean checkFileExists(String strURL) {
        if (strURL == null || "".equals(strURL)) {
            return false;
        }
        File file = new File(strURL);
        boolean result = true;
        if (FileUtils.isSDExist()) {
            // 判断文件是否存在
            result = file.exists();
        }
        return result;
    }

    /**
     * 获取不带扩展名的文件名
     *
     * @param filePath
     * @return
     */
    public static String getFileNameNoExtension(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return "";
        }
        return getFileNameNoExtension(new File(filePath));
    }

    /**
     * 获取不带扩展名的文件名
     *
     * @param file
     * @return
     */
    public static String getFileNameNoExtension(File file) {
        if (file == null) {
            return "";
        }
        String filename = file.getName();
        if (!TextUtils.isEmpty(filename)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

    /**
     * 检测文件是否存在
     *
     * @param path
     * @return
     */
    private static boolean isFileExist(String path) {
        try {
            if (!TextUtils.isEmpty(path)) {
                File file = new File(path);
                return file.exists();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 调用当前app打开文件
     *
     * @param context
     * @param filePath 文件路径
     */
    public static void openFileByApp(Context context, String filePath) {
        try {
            if (!TextUtils.isEmpty(filePath) && isFileExist(filePath)) {
                openFileBySystemApp(context, new File(filePath));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据文件mimeType调用系统相关程序打开
     *
     * @param context
     * @param file
     */
    private static void openFileBySystemApp(Context context, File file) {
        try {
            if (file != null && file.exists()) {
                String mimeType = getFileMimeTypeFromExtension(getFileExtensionNoPoint(file));
                Intent intent = new Intent();
                // 设置intent的Action属性
                intent.setAction(Intent.ACTION_VIEW);
                // 设置intent的data和Type属性。
                FileProvider7.setIntentDataAndType(context, intent, mimeType, file, true);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取文件的mimeType
     *
     * @param fileType
     * @return
     */
    private static String getFileMimeTypeFromExtension(String fileType) {
        try {
            if (TextUtils.isEmpty(fileType)) {
                return "*/*";
            }
            fileType = fileType.replace(".", "");
            if (fileType.equalsIgnoreCase("docx") || fileType.equalsIgnoreCase("wps")) {
                fileType = "doc";
            } else if (fileType.equalsIgnoreCase("xlsx")) {
                fileType = "xls";
            } else if (fileType.equalsIgnoreCase("pptx")) {
                fileType = "ppt";
            }
            MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
            if (mimeTypeMap.hasExtension(fileType)) {
                // 获得文件类型的MimeType
                return mimeTypeMap.getMimeTypeFromExtension(fileType);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "*/*";
    }

    /**
     * 临时文件夹(主要存放图纸缩略图文件)
     */
    public static String getAppTempPath() {
        return getAvailableFilesPathAndroidData(true) + DEFAULT_TEMP;
    }

    /**
     * 获取缓存路径
     *
     * @param boolToCache
     * @return
     */
    private static String getAvailableFilesPathAndroidData(boolean boolToCache) {
        if (!isSDExist()) {
            if (boolToCache) {
                return MyApplication.getContext().getCacheDir().getAbsolutePath() + "/";
            }
            return MyApplication.getContext().getFilesDir().getAbsolutePath() + "/";
        } else {
            if (boolToCache) {
                return MyApplication.getContext().getExternalCacheDir().getAbsolutePath() + "/";
            }
            return MyApplication.getContext().getExternalFilesDir("").getAbsolutePath() + "/";
        }
    }
}
