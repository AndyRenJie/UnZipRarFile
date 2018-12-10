package com.gstarcad.andy.unrarsample.bean;

import java.io.Serializable;

/**
 * @author Andy.R
 */
public class FileModel implements Serializable {

    private String fileId;
    private String fileName;
    private String fileIcon;
    private String filePath;
    private long fileDate;
    private String fileDateShow;
    private long fileSize;
    private String fileSizeShow;
    private String fileType;
    private String fileFrom;
    private boolean isFavorite = false;
    private boolean isFile = false;
    private boolean isDir = false;
    private boolean fileSelected = false;

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileIcon() {
        return fileIcon;
    }

    public void setFileIcon(String fileIcon) {
        this.fileIcon = fileIcon;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getFileDate() {
        return fileDate;
    }

    public void setFileDate(long fileDate) {
        this.fileDate = fileDate;
    }

    public String getFileDateShow() {
        return fileDateShow;
    }

    public void setFileDateShow(String fileDateShow) {
        this.fileDateShow = fileDateShow;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileSizeShow() {
        return fileSizeShow;
    }

    public void setFileSizeShow(String fileSizeShow) {
        this.fileSizeShow = fileSizeShow;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileFrom() {
        return fileFrom;
    }

    public void setFileFrom(String fileFrom) {
        this.fileFrom = fileFrom;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public boolean isFile() {
        return isFile;
    }

    public void setFile(boolean file) {
        isFile = file;
    }

    public boolean isDir() {
        return isDir;
    }

    public void setDir(boolean dir) {
        isDir = dir;
    }

    public boolean isFileSelected() {
        return fileSelected;
    }

    public void setFileSelected(boolean fileSelected) {
        this.fileSelected = fileSelected;
    }

    @Override
    public String toString() {
        return "FileModel{" +
                "fileId='" + fileId + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileIcon='" + fileIcon + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileDate=" + fileDate +
                ", fileDateShow='" + fileDateShow + '\'' +
                ", fileSize=" + fileSize +
                ", fileSizeShow='" + fileSizeShow + '\'' +
                ", fileType='" + fileType + '\'' +
                ", fileFrom='" + fileFrom + '\'' +
                ", isFavorite=" + isFavorite +
                ", isFile=" + isFile +
                ", isDir=" + isDir +
                ", fileSelected=" + fileSelected +
                '}';
    }
}
