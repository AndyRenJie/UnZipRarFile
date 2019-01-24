package com.gstar.andy.unzipsample.manager;

import android.text.TextUtils;

import com.gstar.andy.unzipsample.bean.FileModel;
import com.gstar.andy.unzipsample.utils.DateUtils;
import com.gstar.andy.unzipsample.utils.FileUtils;
import com.unrar.andy.library.de.innosystec.unrar.Archive;
import com.unrar.andy.library.de.innosystec.unrar.rarfile.FileHeader;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Andy.R
 */
public class UnRarManager {

    private static final UnRarManager ourInstance = new UnRarManager();

    public static UnRarManager getInstance() {
        return ourInstance;
    }

    private UnRarManager() {
    }

    /**
     * 解压文件到指定目录
     * 解压速度比P7Zip慢
     *
     * @param rarFilePath   源文件地址
     * @param outPathString 解压后存放地址
     * @version 1.0
     */
    public String unRarAllFile(String rarFilePath, String outPathString, String password) {
        File rarFile = new File(rarFilePath);
        if (TextUtils.isEmpty(outPathString)) {
            outPathString = rarFile.getParentFile().getPath();
        }
        FileOutputStream fileOutputStream = null;
        Archive archive = null;
        try {
            archive = new Archive(rarFile, password);
            List<FileHeader> fileHeaders = archive.getFileHeaders();
            for (FileHeader fileHeader : fileHeaders) {
                String fileName;
                //解決中文乱码
                if (fileHeader.isUnicode()) {
                    fileName = fileHeader.getFileNameW().trim();
                } else {
                    fileName = fileHeader.getFileNameString().trim();
                }
                fileName = fileName.replace("\\", "/");
                File file = new File(outPathString + "/" + fileName);
                if (fileHeader.isDirectory()) {
                    file.mkdirs();
                } else {
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                    file.createNewFile();
                    fileOutputStream = new FileOutputStream(file);
                    archive.extractFile(fileHeader, fileOutputStream);
                    fileOutputStream.flush();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            outPathString = "";
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                if (archive != null) {
                    archive.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                outPathString = "";
            }
        }
        return outPathString;
    }

    /**
     * 预览rar文件内容列表
     *
     * @param file
     * @return
     */
    public List<FileModel> getRarFileAllList(File file) {
        // 文件目录的容器
        List<FileModel> listFiles = new ArrayList<>();
        Archive rarfile = null;
        try {
            rarfile = new Archive(file, "");
            List<FileHeader> fileHeaders = rarfile.getFileHeaders();
            for (FileHeader fileHeader : fileHeaders) {
                FileModel item = new FileModel();
                String fileName;
                //解決中文乱码
                if (fileHeader.isUnicode()) {
                    fileName = fileHeader.getFileNameW().trim();
                } else {
                    fileName = fileHeader.getFileNameString().trim();
                }
                long fileDate = fileHeader.getMTime().getTime();
                Date date = new Date(fileDate);
                String formatDate = DateUtils.getDateToString(date);
                long fileSize = fileHeader.getFullUnpackSize();
                String formatSize = FileUtils.formatFileSize(fileSize);
                String fileType = FileUtils.getFileExtensionNoPoint(fileName);

                item.setFileName(fileName);
                item.setFilePath(fileName);
                item.setFileDate(fileDate);
                item.setFileDateShow(formatDate);
                item.setFileSize(fileSize);
                item.setFileSizeShow(formatSize);
                item.setFileType(fileType);
                item.setDir(fileHeader.isDirectory());
                item.setFile(!fileHeader.isDirectory());
                item.setFileSelected(false);
                listFiles.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rarfile != null) {
                    rarfile.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return listFiles;
    }

    /**
     * 解压单个文件
     *
     * @param rarFile
     * @param outPathString
     * @param rarFileName
     * @return
     */
    public String unRarFileSingle(File rarFile, String outPathString, String rarFileName, String password) {
        String outPath = "";
        if (TextUtils.isEmpty(outPathString)) {
            outPathString = rarFile.getParentFile().getPath();
        }
        FileOutputStream fileOutputStream = null;
        Archive archive = null;
        String fileHeaderName;
        try {
            archive = new Archive(rarFile, password);
            List<FileHeader> fileHeaders = archive.getFileHeaders();
            for (FileHeader fileHeader : fileHeaders) {
                //解決中文乱码
                if (fileHeader.isUnicode()) {
                    fileHeaderName = fileHeader.getFileNameW().trim();
                } else {
                    fileHeaderName = fileHeader.getFileNameString().trim();
                }
                if (fileHeaderName.endsWith(rarFileName)) {
                    File file = new File(outPathString, rarFileName);
                    if (file.exists()) {
                        file.delete();
                    }
                    file.createNewFile();
                    fileOutputStream = new FileOutputStream(file);
                    archive.extractFile(fileHeader, fileOutputStream);
                    fileOutputStream.flush();
                }
            }
            outPath = outPathString + rarFileName;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (archive != null) {
                    archive.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return outPath;
    }

    /**
     * 检查rar文件是否有密码
     *
     * @param rarFile
     * @return
     */
    public boolean checkRarFileHasPassword(File rarFile) {
        boolean isHasPasword;
        Archive archive = null;
        try {
            archive = new Archive(rarFile, "");
            isHasPasword = archive.isPasswordProtected();
        } catch (Exception e) {
            e.printStackTrace();
            isHasPasword = false;
        } finally {
            try {
                if (archive != null) {
                    archive.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return isHasPasword;
    }
}
