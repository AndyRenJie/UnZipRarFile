//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.unzip.andy.library;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.UnzipParameters;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.progress.ProgressMonitor;
import net.lingala.zip4j.unzip.Unzip;
import net.lingala.zip4j.util.ArchiveMaintainer;
import net.lingala.zip4j.util.InternalZipConstants;
import net.lingala.zip4j.util.Zip4jUtil;
import net.lingala.zip4j.zip.ZipEngine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * 自定义ZipFile，用于语言编码
 *
 * @author Andy.R
 */
public class MyZipFile {

    private String zipFilePath;
    private int mode;
    private ZipModel zipModel;
    private boolean isEncrypted;
    private ProgressMonitor progressMonitor;
    private boolean runInThread;
    private String fileNameCharset;

    public MyZipFile(String zipFile) throws ZipException {
        this(new File(zipFile));
    }

    public MyZipFile(File zipFile) throws ZipException {
        if (zipFile == null) {
            throw new ZipException("Input zip file parameter is not null", 1);
        } else {
            this.zipFilePath = zipFile.getPath();
            this.mode = 2;
            this.progressMonitor = new ProgressMonitor();
            this.runInThread = false;
        }
    }

    public void createZipFile(File sourceFile, ZipParameters parameters) throws ZipException {
        ArrayList sourceFileList = new ArrayList();
        sourceFileList.add(sourceFile);
        this.createZipFile(sourceFileList, parameters, false, -1L);
    }

    public void createZipFile(File sourceFile, ZipParameters parameters, boolean splitArchive, long splitLength) throws ZipException {
        ArrayList sourceFileList = new ArrayList();
        sourceFileList.add(sourceFile);
        this.createZipFile(sourceFileList, parameters, splitArchive, splitLength);
    }

    public void createZipFile(ArrayList sourceFileList, ZipParameters parameters) throws ZipException {
        this.createZipFile(sourceFileList, parameters, false, -1L);
    }

    public void createZipFile(ArrayList sourceFileList, ZipParameters parameters, boolean splitArchive, long splitLength) throws ZipException {
        if (!Zip4jUtil.isStringNotNullAndNotEmpty(zipFilePath)) {
            throw new ZipException("zip file path is empty");
        } else if (Zip4jUtil.checkFileExists(zipFilePath)) {
            throw new ZipException("zip file: " + zipFilePath + " already exists. To add files to existing zip file use addFile method");
        } else if (sourceFileList == null) {
            throw new ZipException("input file ArrayList is null, cannot create zip file");
        } else if (!Zip4jUtil.checkArrayListTypes(sourceFileList, 1)) {
            throw new ZipException("One or more elements in the input ArrayList is not of type File");
        } else {
            this.createNewZipModel();
            this.zipModel.setSplitArchive(splitArchive);
            this.zipModel.setSplitLength(splitLength);
            this.addFiles(sourceFileList, parameters);
        }
    }

    public void createZipFileFromFolder(String folderToAdd, ZipParameters parameters, boolean splitArchive, long splitLength) throws ZipException {
        if (!Zip4jUtil.isStringNotNullAndNotEmpty(folderToAdd)) {
            throw new ZipException("folderToAdd is empty or null, cannot create Zip File from folder");
        } else {
            this.createZipFileFromFolder(new File(folderToAdd), parameters, splitArchive, splitLength);
        }
    }

    public void createZipFileFromFolder(File folderToAdd, ZipParameters parameters, boolean splitArchive, long splitLength) throws ZipException {
        if (folderToAdd == null) {
            throw new ZipException("folderToAdd is null, cannot create zip file from folder");
        } else if (parameters == null) {
            throw new ZipException("input parameters are null, cannot create zip file from folder");
        } else if (Zip4jUtil.checkFileExists(zipFilePath)) {
            throw new ZipException("zip file: " + zipFilePath + " already exists. To add files to existing zip file use addFolder method");
        } else {
            this.createNewZipModel();
            this.zipModel.setSplitArchive(splitArchive);
            if (splitArchive) {
                this.zipModel.setSplitLength(splitLength);
            }

            this.addFolder(folderToAdd, parameters, false);
        }
    }

    public void addFile(File sourceFile, ZipParameters parameters) throws ZipException {
        ArrayList sourceFileList = new ArrayList();
        sourceFileList.add(sourceFile);
        this.addFiles(sourceFileList, parameters);
    }

    public void addFiles(ArrayList sourceFileList, ZipParameters parameters) throws ZipException {
        this.checkZipModel();
        if (this.zipModel == null) {
            throw new ZipException("internal error: zip model is null");
        } else if (sourceFileList == null) {
            throw new ZipException("input file ArrayList is null, cannot add files");
        } else if (!Zip4jUtil.checkArrayListTypes(sourceFileList, 1)) {
            throw new ZipException("One or more elements in the input ArrayList is not of type File");
        } else if (parameters == null) {
            throw new ZipException("input parameters are null, cannot add files to zip");
        } else if (this.progressMonitor.getState() == 1) {
            throw new ZipException("invalid operation - Zip4j is in busy state");
        } else if (Zip4jUtil.checkFileExists(zipFilePath) && this.zipModel.isSplitArchive()) {
            throw new ZipException("Zip file already exists. Zip file format does not allow updating split/spanned files");
        } else {
            ZipEngine zipEngine = new ZipEngine(this.zipModel);
            zipEngine.addFiles(sourceFileList, parameters, this.progressMonitor, this.runInThread);
        }
    }

    public void addFolder(String path, ZipParameters parameters) throws ZipException {
        if (!Zip4jUtil.isStringNotNullAndNotEmpty(path)) {
            throw new ZipException("input path is null or empty, cannot add folder to zip file");
        } else {
            this.addFolder(new File(path), parameters);
        }
    }

    public void addFolder(File path, ZipParameters parameters) throws ZipException {
        if (path == null) {
            throw new ZipException("input path is null, cannot add folder to zip file");
        } else if (parameters == null) {
            throw new ZipException("input parameters are null, cannot add folder to zip file");
        } else {
            this.addFolder(path, parameters, true);
        }
    }

    private void addFolder(File path, ZipParameters parameters, boolean checkSplitArchive) throws ZipException {
        this.checkZipModel();
        if (this.zipModel == null) {
            throw new ZipException("internal error: zip model is null");
        } else if (checkSplitArchive && this.zipModel.isSplitArchive()) {
            throw new ZipException("This is a split archive. Zip file format does not allow updating split/spanned files");
        } else {
            ZipEngine zipEngine = new ZipEngine(this.zipModel);
            zipEngine.addFolderToZip(path, parameters, this.progressMonitor, this.runInThread);
        }
    }

    public void addStream(InputStream inputStream, ZipParameters parameters) throws ZipException {
        if (inputStream == null) {
            throw new ZipException("inputstream is null, cannot add file to zip");
        } else if (parameters == null) {
            throw new ZipException("zip parameters are null");
        } else {
            this.setRunInThread(false);
            this.checkZipModel();
            if (this.zipModel == null) {
                throw new ZipException("internal error: zip model is null");
            } else if (Zip4jUtil.checkFileExists(zipFilePath) && this.zipModel.isSplitArchive()) {
                throw new ZipException("Zip file already exists. Zip file format does not allow updating split/spanned files");
            } else {
                ZipEngine zipEngine = new ZipEngine(this.zipModel);
                zipEngine.addStreamToZip(inputStream, parameters);
            }
        }
    }

    private void readZipInfo() throws ZipException {
        if (!Zip4jUtil.checkFileExists(zipFilePath)) {
            throw new ZipException("zip file does not exist");
        } else if (!Zip4jUtil.checkFileReadAccess(zipFilePath)) {
            throw new ZipException("no read access for the input zip file");
        } else if (this.mode != 2) {
            throw new ZipException("Invalid mode");
        } else {
            RandomAccessFile raf = null;

            try {
                raf = new RandomAccessFile(new File(zipFilePath), "r");
                if (this.zipModel == null) {
                    HeaderReader headerReader = new HeaderReader(raf);
                    this.zipModel = headerReader.readAllHeaders(this.fileNameCharset);
                    if (this.zipModel != null) {
                        this.zipModel.setZipFile(zipFilePath);
                    }
                }
            } catch (FileNotFoundException var10) {
                throw new ZipException(var10);
            } finally {
                if (raf != null) {
                    try {
                        raf.close();
                    } catch (IOException var9) {
                        ;
                    }
                }

            }

        }
    }

    public void extractAll(String destPath) throws ZipException {
        this.extractAll(destPath, null);
    }

    public void extractAll(String destPath, UnzipParameters unzipParameters) throws ZipException {
        if (!Zip4jUtil.isStringNotNullAndNotEmpty(destPath)) {
            throw new ZipException("output path is null or invalid");
        } else if (!Zip4jUtil.checkOutputFolder(destPath)) {
            throw new ZipException("invalid output path");
        } else {
            if (this.zipModel == null) {
                this.readZipInfo();
            }

            if (this.zipModel == null) {
                throw new ZipException("Internal error occurred when extracting zip file");
            } else if (this.progressMonitor.getState() == 1) {
                throw new ZipException("invalid operation - Zip4j is in busy state");
            } else {
                Unzip unzip = new Unzip(this.zipModel);
                unzip.extractAll(unzipParameters, destPath, this.progressMonitor, this.runInThread);
            }
        }
    }

    public void extractFile(FileHeader fileHeader, String destPath) throws ZipException {
        this.extractFile(fileHeader, destPath, null);
    }

    public void extractFile(FileHeader fileHeader, String destPath, UnzipParameters unzipParameters) throws ZipException {
        this.extractFile(fileHeader, destPath, unzipParameters, null);
    }

    public void extractFile(FileHeader fileHeader, String destPath, UnzipParameters unzipParameters, String newFileName) throws ZipException {
        if (fileHeader == null) {
            throw new ZipException("input file header is null, cannot extract file");
        } else if (!Zip4jUtil.isStringNotNullAndNotEmpty(destPath)) {
            throw new ZipException("destination path is empty or null, cannot extract file");
        } else {
            this.readZipInfo();
            if (this.progressMonitor.getState() == 1) {
                throw new ZipException("invalid operation - Zip4j is in busy state");
            } else {
                fileHeader.extractFile(this.zipModel, destPath, unzipParameters, newFileName, this.progressMonitor, this.runInThread);
            }
        }
    }

    public void extractFile(String fileName, String destPath) throws ZipException {
        this.extractFile(fileName, destPath, null);
    }

    public void extractFile(String fileName, String destPath, UnzipParameters unzipParameters) throws ZipException {
        this.extractFile(fileName, destPath, unzipParameters, null);
    }

    public void extractFile(String fileName, String destPath, UnzipParameters unzipParameters, String newFileName) throws ZipException {
        if (!Zip4jUtil.isStringNotNullAndNotEmpty(fileName)) {
            throw new ZipException("file to extract is null or empty, cannot extract file");
        } else if (!Zip4jUtil.isStringNotNullAndNotEmpty(destPath)) {
            throw new ZipException("destination string path is empty or null, cannot extract file");
        } else {
            this.readZipInfo();
            FileHeader fileHeader = Zip4jUtil.getFileHeader(this.zipModel, fileName);
            if (fileHeader == null) {
                throw new ZipException("file header not found for given file name, cannot extract file");
            } else if (this.progressMonitor.getState() == 1) {
                throw new ZipException("invalid operation - Zip4j is in busy state");
            } else {
                fileHeader.extractFile(this.zipModel, destPath, unzipParameters, newFileName, this.progressMonitor, this.runInThread);
            }
        }
    }

    public void setPassword(String password) throws ZipException {
        if (!Zip4jUtil.isStringNotNullAndNotEmpty(password)) {
            throw new NullPointerException();
        } else {
            this.setPassword(password.toCharArray());
        }
    }

    public void setPassword(char[] password) throws ZipException {
        if (this.zipModel == null) {
            this.readZipInfo();
            if (this.zipModel == null) {
                throw new ZipException("Zip Model is null");
            }
        }

        if (this.zipModel.getCentralDirectory() != null && this.zipModel.getCentralDirectory().getFileHeaders() != null) {
            for (int i = 0; i < this.zipModel.getCentralDirectory().getFileHeaders().size(); ++i) {
                if (this.zipModel.getCentralDirectory().getFileHeaders().get(i) != null && ((FileHeader) this.zipModel.getCentralDirectory().getFileHeaders().get(i)).isEncrypted()) {
                    ((FileHeader) this.zipModel.getCentralDirectory().getFileHeaders().get(i)).setPassword(password);
                }
            }

        } else {
            throw new ZipException("invalid zip file");
        }
    }

    public List getFileHeaders() throws ZipException {
        this.readZipInfo();
        return this.zipModel != null && this.zipModel.getCentralDirectory() != null ? this.zipModel.getCentralDirectory().getFileHeaders() : null;
    }

    public FileHeader getFileHeader(String fileName) throws ZipException {
        if (!Zip4jUtil.isStringNotNullAndNotEmpty(fileName)) {
            throw new ZipException("input file name is emtpy or null, cannot get FileHeader");
        } else {
            this.readZipInfo();
            return this.zipModel != null && this.zipModel.getCentralDirectory() != null ? Zip4jUtil.getFileHeader(this.zipModel, fileName) : null;
        }
    }

    public boolean isEncrypted() throws ZipException {
        if (this.zipModel == null) {
            this.readZipInfo();
            if (this.zipModel == null) {
                throw new ZipException("Zip Model is null");
            }
        }

        if (this.zipModel.getCentralDirectory() != null && this.zipModel.getCentralDirectory().getFileHeaders() != null) {
            ArrayList fileHeaderList = this.zipModel.getCentralDirectory().getFileHeaders();

            for (int i = 0; i < fileHeaderList.size(); ++i) {
                FileHeader fileHeader = (FileHeader) fileHeaderList.get(i);
                if (fileHeader != null && fileHeader.isEncrypted()) {
                    this.isEncrypted = true;
                    break;
                }
            }

            return this.isEncrypted;
        } else {
            throw new ZipException("invalid zip file");
        }
    }

    public boolean isSplitArchive() throws ZipException {
        if (this.zipModel == null) {
            this.readZipInfo();
            if (this.zipModel == null) {
                throw new ZipException("Zip Model is null");
            }
        }

        return this.zipModel.isSplitArchive();
    }

    public void removeFile(String fileName) throws ZipException {
        if (!Zip4jUtil.isStringNotNullAndNotEmpty(fileName)) {
            throw new ZipException("file name is empty or null, cannot remove file");
        } else {
            if (this.zipModel == null && Zip4jUtil.checkFileExists(zipFilePath)) {
                this.readZipInfo();
            }

            if (this.zipModel.isSplitArchive()) {
                throw new ZipException("Zip file format does not allow updating split/spanned files");
            } else {
                FileHeader fileHeader = Zip4jUtil.getFileHeader(this.zipModel, fileName);
                if (fileHeader == null) {
                    throw new ZipException("could not find file header for file: " + fileName);
                } else {
                    this.removeFile(fileHeader);
                }
            }
        }
    }

    public void removeFile(FileHeader fileHeader) throws ZipException {
        if (fileHeader == null) {
            throw new ZipException("file header is null, cannot remove file");
        } else {
            if (this.zipModel == null && Zip4jUtil.checkFileExists(zipFilePath)) {
                this.readZipInfo();
            }

            if (this.zipModel.isSplitArchive()) {
                throw new ZipException("Zip file format does not allow updating split/spanned files");
            } else {
                ArchiveMaintainer archiveMaintainer = new ArchiveMaintainer();
                archiveMaintainer.initProgressMonitorForRemoveOp(this.zipModel, fileHeader, this.progressMonitor);
                archiveMaintainer.removeZipFile(this.zipModel, fileHeader, this.progressMonitor, this.runInThread);
            }
        }
    }

    public void mergeSplitFiles(File outputZipFile) throws ZipException {
        if (outputZipFile == null) {
            throw new ZipException("outputZipFile is null, cannot merge split files");
        } else if (outputZipFile.exists()) {
            throw new ZipException("output Zip File already exists");
        } else {
            this.checkZipModel();
            if (this.zipModel == null) {
                throw new ZipException("zip model is null, corrupt zip file?");
            } else {
                ArchiveMaintainer archiveMaintainer = new ArchiveMaintainer();
                archiveMaintainer.initProgressMonitorForMergeOp(this.zipModel, this.progressMonitor);
                archiveMaintainer.mergeSplitZipFiles(this.zipModel, outputZipFile, this.progressMonitor, this.runInThread);
            }
        }
    }

    public void setComment(String comment) throws ZipException {
        if (comment == null) {
            throw new ZipException("input comment is null, cannot update zip file");
        } else if (!Zip4jUtil.checkFileExists(zipFilePath)) {
            throw new ZipException("zip file does not exist, cannot set comment for zip file");
        } else {
            this.readZipInfo();
            if (this.zipModel == null) {
                throw new ZipException("zipModel is null, cannot update zip file");
            } else if (this.zipModel.getEndCentralDirRecord() == null) {
                throw new ZipException("end of central directory is null, cannot set comment");
            } else {
                ArchiveMaintainer archiveMaintainer = new ArchiveMaintainer();
                archiveMaintainer.setComment(this.zipModel, comment);
            }
        }
    }

    public String getComment() throws ZipException {
        return this.getComment(null);
    }

    public String getComment(String encoding) throws ZipException {
        if (encoding == null) {
            if (Zip4jUtil.isSupportedCharset("windows-1254")) {
                encoding = "windows-1254";
            } else {
                encoding = InternalZipConstants.CHARSET_DEFAULT;
            }
        }

        if (Zip4jUtil.checkFileExists(zipFilePath)) {
            this.checkZipModel();
            if (this.zipModel == null) {
                throw new ZipException("zip model is null, cannot read comment");
            } else if (this.zipModel.getEndCentralDirRecord() == null) {
                throw new ZipException("end of central directory record is null, cannot read comment");
            } else if (this.zipModel.getEndCentralDirRecord().getCommentBytes() != null && this.zipModel.getEndCentralDirRecord().getCommentBytes().length > 0) {
                try {
                    return new String(this.zipModel.getEndCentralDirRecord().getCommentBytes(), encoding);
                } catch (UnsupportedEncodingException var3) {
                    throw new ZipException(var3);
                }
            } else {
                return null;
            }
        } else {
            throw new ZipException("zip file does not exist, cannot read comment");
        }
    }

    private void checkZipModel() throws ZipException {
        if (this.zipModel == null) {
            if (Zip4jUtil.checkFileExists(zipFilePath)) {
                this.readZipInfo();
            } else {
                this.createNewZipModel();
            }
        }

    }

    private void createNewZipModel() {
        this.zipModel = new ZipModel();
        this.zipModel.setZipFile(zipFilePath);
        this.zipModel.setFileNameCharset(this.fileNameCharset);
    }

    public void setFileNameCharset(String charsetName) throws ZipException {
        if (!Zip4jUtil.isStringNotNullAndNotEmpty(charsetName)) {
            throw new ZipException("null or empty charset name");
        } else if (!Zip4jUtil.isSupportedCharset(charsetName)) {
            throw new ZipException("unsupported charset: " + charsetName);
        } else {
            this.fileNameCharset = charsetName;
        }
    }

    public ZipInputStream getInputStream(FileHeader fileHeader) throws ZipException {
        if (fileHeader == null) {
            throw new ZipException("FileHeader is null, cannot get InputStream");
        } else {
            this.checkZipModel();
            if (this.zipModel == null) {
                throw new ZipException("zip model is null, cannot get inputstream");
            } else {
                Unzip unzip = new Unzip(this.zipModel);
                return unzip.getInputStream(fileHeader);
            }
        }
    }

    public boolean isValidZipFile() {
        try {
            this.readZipInfo();
            return true;
        } catch (Exception var2) {
            return false;
        }
    }

    public ArrayList getSplitZipFiles() throws ZipException {
        this.checkZipModel();
        return Zip4jUtil.getSplitZipFiles(this.zipModel);
    }

    public ProgressMonitor getProgressMonitor() {
        return this.progressMonitor;
    }

    public boolean isRunInThread() {
        return this.runInThread;
    }

    public void setRunInThread(boolean runInThread) {
        this.runInThread = runInThread;
    }

    public File getFile() {
        return new File(zipFilePath);
    }
}
