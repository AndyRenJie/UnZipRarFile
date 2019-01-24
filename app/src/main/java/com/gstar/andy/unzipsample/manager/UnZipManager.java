package com.gstar.andy.unzipsample.manager;

import android.os.Environment;
import android.text.TextUtils;

import com.gstar.andy.unzipsample.bean.FileModel;
import com.gstar.andy.unzipsample.utils.DateUtils;
import com.gstar.andy.unzipsample.utils.FileUtils;
import com.unzip.andy.library.MyZipFile;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import net.lingala.zip4j.util.Zip4jUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Andy.R
 */
public class UnZipManager {

    private static final UnZipManager ourInstance = new UnZipManager();

    public static UnZipManager getInstance() {
        return ourInstance;
    }

    private UnZipManager() {
    }

    /**
     * 压缩文件到压缩包带有密码
     *
     * @param srcFile  源文件地址
     * @param destFile 压缩后存放地址
     * @param password 压缩密码
     */
    public boolean zipFile(String srcFile, String destFile, String password) {
        boolean flag;
        try {
            ZipParameters parameters = new ZipParameters();
            // 压缩方式
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            // 压缩级别
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
            if (TextUtils.isEmpty(password)) {
                // Set password
                parameters.setEncryptFiles(true);
                parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);
                parameters.setPassword(password);
            }
            ZipFile zipFile = new ZipFile(destFile);
            zipFile.addFile(new File(srcFile), parameters);
            flag = true;
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    /**
     * 解压缩文件到指定目录
     *
     * @param scrFile
     * @param destFile
     * @param password
     * @return
     * @Author StoneJxn
     * @Date 2016年3月21日 下午5:51:27
     */
    public String unZipAllFile(String scrFile, String destFile, String password) {
        if (TextUtils.isEmpty(destFile)) {
            destFile = Environment.getDownloadCacheDirectory().getAbsolutePath();
        }
        try {
            MyZipFile zipFile = new MyZipFile(scrFile);
            zipFile.setFileNameCharset("GBK");
            if (zipFile.isEncrypted()) {
                // 在这里输入密码，如果写错了，会报异常
                zipFile.setPassword(password);
            }
            File destDir = new File(destFile);
            if (!destDir.exists()) {
                destDir.mkdir();
            }
            zipFile.extractAll(destFile);
        } catch (Exception e) {
            e.printStackTrace();
            destFile = "";
        }
        return destFile;
    }

    /**
     * 得到zip中包含的文件列表（目录和相对文件路径）
     *
     * @param srcFile
     * @return
     */
    public List<FileModel> getZipFileAllList(File srcFile) {
        // 文件目录的容器
        List<FileModel> listFiles = new ArrayList<>();
        try {
            MyZipFile zipFile = new MyZipFile(srcFile);
            zipFile.setFileNameCharset("GBK");
            List<FileHeader> list = zipFile.getFileHeaders();
            FileModel fileModel;
            for (FileHeader fileHeader : list) {
                fileModel = new FileModel();
                if (!fileHeader.getFileName().startsWith(".")) {
                    int fileDate = fileHeader.getLastModFileTime();
                    Date date = new Date(Zip4jUtil.dosToJavaTme(fileDate));
                    // Date date = getDateDos(fileDate);
                    String formatDate = DateUtils.getDateToString(date);
                    long fileSize = fileHeader.getUncompressedSize();
                    String formatSize = FileUtils.formatFileSize(fileSize);
                    String fileName;
                    String filePath;

                    if (fileHeader.isDirectory()) {
                        fileName = fileHeader.getFileName().substring(0, fileHeader.getFileName().length() - 1).replace("/", "\\");
                        filePath = fileHeader.getFileName().substring(0, fileHeader.getFileName().length() - 1).replace("/", "\\");
                    } else {
                        fileName = fileHeader.getFileName().replace("/", "\\");
                        filePath = fileHeader.getFileName().replace("/", "\\");
                    }
                    String fileType = FileUtils.getFileExtensionNoPoint(fileName);
                    fileModel.setFileName(fileName);
                    fileModel.setFilePath(filePath);
                    fileModel.setFileDate(fileDate);
                    fileModel.setFileDateShow(formatDate);
                    fileModel.setFileSize(fileSize);
                    fileModel.setFileSizeShow(formatSize);
                    fileModel.setFileType(fileType);
                    fileModel.setDir(fileHeader.isDirectory());
                    fileModel.setFile(!fileHeader.isDirectory());
                    fileModel.setFileSelected(false);
                    listFiles.add(fileModel);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listFiles;
    }

    /**
     * 处理时间格式
     *
     * @param time
     * @return
     */
    private Date getDateDos(int time) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, (time >>> 25) + 1980);
        cal.set(Calendar.MONTH, ((time >>> 21) & 0x0f) - 1);
        cal.set(Calendar.DAY_OF_MONTH, (time >>> 16) & 0x1f);
        cal.set(Calendar.HOUR_OF_DAY, (time >>> 11) & 0x1f);
        cal.set(Calendar.MINUTE, (time >>> 5) & 0x3f);
        cal.set(Calendar.SECOND, (time & 0x1f) * 2);
        return cal.getTime();
    }

    /**
     * 解压单个文件
     *
     * @param scrFile
     * @param descFolder
     * @param fileName
     * @return
     */
    public String unZipFileSingle(String scrFile, String descFolder, String fileName, String password) {
        if (TextUtils.isEmpty(descFolder)) {
            descFolder = Environment.getDownloadCacheDirectory().getAbsolutePath();
        }
        String outPath = "";
        try {
            MyZipFile zipFile = new MyZipFile(scrFile);
            zipFile.setFileNameCharset("GBK");
            if (zipFile.isEncrypted()) {
                // 在这里输入密码，如果写错了，会报异常
                zipFile.setPassword(password);
            }
            //划分路径
            fileName = fileName.replace("\\", "/");
            zipFile.extractFile(fileName, descFolder);
            outPath = descFolder + fileName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outPath;
    }

    /**
     * 检查zip文件是否有密码
     *
     * @param zipFilePath
     * @return
     */
    public boolean checkZipFileHasPassword(String zipFilePath) {
        boolean isHasPasword = false;
        if (!TextUtils.isEmpty(zipFilePath)) {
            try {
                MyZipFile zipFile = new MyZipFile(zipFilePath);
                zipFile.setFileNameCharset("GBK");
                isHasPasword = zipFile.isEncrypted();
            } catch (Exception e) {
                e.printStackTrace();
                isHasPasword = false;
            }
        }
        return isHasPasword;
    }
}
