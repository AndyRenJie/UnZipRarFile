//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.unzip.andy.library;

import com.unzip.andy.library.encode.ZipEncoding;
import com.unzip.andy.library.encode.ZipEncodingHelper;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.AESExtraDataRecord;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.DigitalSignature;
import net.lingala.zip4j.model.EndCentralDirRecord;
import net.lingala.zip4j.model.ExtraDataRecord;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.LocalFileHeader;
import net.lingala.zip4j.model.Zip64EndCentralDirLocator;
import net.lingala.zip4j.model.Zip64EndCentralDirRecord;
import net.lingala.zip4j.model.Zip64ExtendedInfo;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.util.Raw;
import net.lingala.zip4j.util.Zip4jUtil;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

/**
 * 自定义HeaderReader
 *
 * @author Andy.R
 */
public class HeaderReader {

    private RandomAccessFile zip4jRaf;
    private ZipModel zipModel;

    public HeaderReader(RandomAccessFile zip4jRaf) {
        this.zip4jRaf = zip4jRaf;
    }

    public ZipModel readAllHeaders() throws ZipException {
        return this.readAllHeaders(null);
    }

    public ZipModel readAllHeaders(String fileNameCharset) throws ZipException {
        this.zipModel = new ZipModel();
        this.zipModel.setFileNameCharset(fileNameCharset);
        this.zipModel.setEndCentralDirRecord(this.readEndOfCentralDirectoryRecord());
        this.zipModel.setZip64EndCentralDirLocator(this.readZip64EndCentralDirLocator());
        if (this.zipModel.isZip64Format()) {
            this.zipModel.setZip64EndCentralDirRecord(this.readZip64EndCentralDirRec());
            if (this.zipModel.getZip64EndCentralDirRecord() != null && this.zipModel.getZip64EndCentralDirRecord().getNoOfThisDisk() > 0) {
                this.zipModel.setSplitArchive(true);
            } else {
                this.zipModel.setSplitArchive(false);
            }
        }

        this.zipModel.setCentralDirectory(this.readCentralDirectory());
        return this.zipModel;
    }

    private EndCentralDirRecord readEndOfCentralDirectoryRecord() throws ZipException {
        if (this.zip4jRaf == null) {
            throw new ZipException("random access file was null", 3);
        } else {
            try {
                byte[] ebs = new byte[4];
                long pos = this.zip4jRaf.length() - 22L;
                EndCentralDirRecord endCentralDirRecord = new EndCentralDirRecord();
                int counter = 0;

                do {
                    this.zip4jRaf.seek(pos--);
                    ++counter;
                } while ((long) Raw.readLeInt(this.zip4jRaf, ebs) != 101010256L && counter <= 3000);

                if ((long) Raw.readIntLittleEndian(ebs, 0) != 101010256L) {
                    throw new ZipException("zip headers not found. probably not a zip file");
                } else {
                    byte[] intBuff = new byte[4];
                    byte[] shortBuff = new byte[2];
                    endCentralDirRecord.setSignature(101010256L);
                    this.readIntoBuff(this.zip4jRaf, shortBuff);
                    endCentralDirRecord.setNoOfThisDisk(Raw.readShortLittleEndian(shortBuff, 0));
                    this.readIntoBuff(this.zip4jRaf, shortBuff);
                    endCentralDirRecord.setNoOfThisDiskStartOfCentralDir(Raw.readShortLittleEndian(shortBuff, 0));
                    this.readIntoBuff(this.zip4jRaf, shortBuff);
                    endCentralDirRecord.setTotNoOfEntriesInCentralDirOnThisDisk(Raw.readShortLittleEndian(shortBuff, 0));
                    this.readIntoBuff(this.zip4jRaf, shortBuff);
                    endCentralDirRecord.setTotNoOfEntriesInCentralDir(Raw.readShortLittleEndian(shortBuff, 0));
                    this.readIntoBuff(this.zip4jRaf, intBuff);
                    endCentralDirRecord.setSizeOfCentralDir(Raw.readIntLittleEndian(intBuff, 0));
                    this.readIntoBuff(this.zip4jRaf, intBuff);
                    byte[] longBuff = this.getLongByteFromIntByte(intBuff);
                    endCentralDirRecord.setOffsetOfStartOfCentralDir(Raw.readLongLittleEndian(longBuff, 0));
                    this.readIntoBuff(this.zip4jRaf, shortBuff);
                    int commentLength = Raw.readShortLittleEndian(shortBuff, 0);
                    endCentralDirRecord.setCommentLength(commentLength);
                    if (commentLength > 0) {
                        byte[] commentBuf = new byte[commentLength];
                        this.readIntoBuff(this.zip4jRaf, commentBuf);
                        endCentralDirRecord.setComment(new String(commentBuf));
                        endCentralDirRecord.setCommentBytes(commentBuf);
                    } else {
                        endCentralDirRecord.setComment((String) null);
                    }

                    int diskNumber = endCentralDirRecord.getNoOfThisDisk();
                    if (diskNumber > 0) {
                        this.zipModel.setSplitArchive(true);
                    } else {
                        this.zipModel.setSplitArchive(false);
                    }

                    return endCentralDirRecord;
                }
            } catch (IOException var11) {
                throw new ZipException("Probably not a zip file or a corrupted zip file", var11, 4);
            }
        }
    }

    private CentralDirectory readCentralDirectory() throws ZipException {
        if (this.zip4jRaf == null) {
            throw new ZipException("random access file was null", 3);
        } else if (this.zipModel.getEndCentralDirRecord() == null) {
            throw new ZipException("EndCentralRecord was null, maybe a corrupt zip file");
        } else {
            try {
                CentralDirectory centralDirectory = new CentralDirectory();
                ArrayList fileHeaderList = new ArrayList();
                EndCentralDirRecord endCentralDirRecord = this.zipModel.getEndCentralDirRecord();
                long offSetStartCentralDir = endCentralDirRecord.getOffsetOfStartOfCentralDir();
                int centralDirEntryCount = endCentralDirRecord.getTotNoOfEntriesInCentralDir();
                if (this.zipModel.isZip64Format()) {
                    offSetStartCentralDir = this.zipModel.getZip64EndCentralDirRecord().getOffsetStartCenDirWRTStartDiskNo();
                    centralDirEntryCount = (int) this.zipModel.getZip64EndCentralDirRecord().getTotNoOfEntriesInCentralDir();
                }

                this.zip4jRaf.seek(offSetStartCentralDir);
                byte[] intBuff = new byte[4];
                byte[] shortBuff = new byte[2];
                byte[] longBuff = new byte[8];

                int sizeOfData;
                for (int i = 0; i < centralDirEntryCount; ++i) {
                    FileHeader fileHeader = new FileHeader();
                    this.readIntoBuff(this.zip4jRaf, intBuff);
                    sizeOfData = Raw.readIntLittleEndian(intBuff, 0);
                    if ((long) sizeOfData != 33639248L) {
                        throw new ZipException("Expected central directory entry not found (#" + (i + 1) + ")");
                    }

                    fileHeader.setSignature(sizeOfData);
                    this.readIntoBuff(this.zip4jRaf, shortBuff);
                    fileHeader.setVersionMadeBy(Raw.readShortLittleEndian(shortBuff, 0));
                    this.readIntoBuff(this.zip4jRaf, shortBuff);
                    fileHeader.setVersionNeededToExtract(Raw.readShortLittleEndian(shortBuff, 0));
                    this.readIntoBuff(this.zip4jRaf, shortBuff);
                    fileHeader.setFileNameUTF8Encoded((Raw.readShortLittleEndian(shortBuff, 0) & 2048) != 0);
                    int firstByte = shortBuff[0];
                    int result = firstByte & 1;
                    if (result != 0) {
                        fileHeader.setEncrypted(true);
                    }
                    fileHeader.setGeneralPurposeFlag(shortBuff.clone());
                    fileHeader.setDataDescriptorExists(firstByte >> 3 == 1);
                    this.readIntoBuff(this.zip4jRaf, shortBuff);
                    fileHeader.setCompressionMethod(Raw.readShortLittleEndian(shortBuff, 0));
                    this.readIntoBuff(this.zip4jRaf, intBuff);
                    fileHeader.setLastModFileTime(Raw.readIntLittleEndian(intBuff, 0));
                    this.readIntoBuff(this.zip4jRaf, intBuff);
                    fileHeader.setCrc32((long) Raw.readIntLittleEndian(intBuff, 0));
                    fileHeader.setCrcBuff(intBuff.clone());
                    this.readIntoBuff(this.zip4jRaf, intBuff);
                    longBuff = this.getLongByteFromIntByte(intBuff);
                    fileHeader.setCompressedSize(Raw.readLongLittleEndian(longBuff, 0));
                    this.readIntoBuff(this.zip4jRaf, intBuff);
                    longBuff = this.getLongByteFromIntByte(intBuff);
                    fileHeader.setUncompressedSize(Raw.readLongLittleEndian(longBuff, 0));
                    this.readIntoBuff(this.zip4jRaf, shortBuff);
                    int fileNameLength = Raw.readShortLittleEndian(shortBuff, 0);
                    fileHeader.setFileNameLength(fileNameLength);
                    this.readIntoBuff(this.zip4jRaf, shortBuff);
                    int extraFieldLength = Raw.readShortLittleEndian(shortBuff, 0);
                    fileHeader.setExtraFieldLength(extraFieldLength);
                    this.readIntoBuff(this.zip4jRaf, shortBuff);
                    int fileCommentLength = Raw.readShortLittleEndian(shortBuff, 0);
                    fileHeader.setFileComment(new String(shortBuff));
                    this.readIntoBuff(this.zip4jRaf, shortBuff);
                    fileHeader.setDiskNumberStart(Raw.readShortLittleEndian(shortBuff, 0));
                    this.readIntoBuff(this.zip4jRaf, shortBuff);
                    fileHeader.setInternalFileAttr(shortBuff.clone());
                    this.readIntoBuff(this.zip4jRaf, intBuff);
                    fileHeader.setExternalFileAttr(intBuff.clone());
                    this.readIntoBuff(this.zip4jRaf, intBuff);
                    longBuff = this.getLongByteFromIntByte(intBuff);
                    fileHeader.setOffsetLocalHeader(Raw.readLongLittleEndian(longBuff, 0) & 4294967295L);
                    byte[] fileCommentBuf;
                    if (fileNameLength > 0) {
                        fileCommentBuf = new byte[fileNameLength];
                        this.readIntoBuff(this.zip4jRaf, fileCommentBuf);
                        String fileName;
                        if (fileHeader.isFileNameUTF8Encoded()) {
                            fileName = new String(fileCommentBuf, "UTF8");
                        } else {
                            ZipEncoding entryEncoding = ZipEncodingHelper.getZipEncoding(this.zipModel.getFileNameCharset());
                            fileName = entryEncoding.decode(fileCommentBuf);
                        }
//                        if (Zip4jUtil.isStringNotNullAndNotEmpty(this.zipModel.getFileNameCharset())) {
//                            fileName = new String(fileCommentBuf, this.zipModel.getFileNameCharset());
//                        } else {
//                            fileName = Zip4jUtil.decodeFileName(fileCommentBuf, fileHeader.isFileNameUTF8Encoded());
//                        }
                        if (fileName == null) {
                            throw new ZipException("fileName is null when reading central directory");
                        }
                        if (fileName.indexOf(":" + System.getProperty("file.separator")) >= 0) {
                            fileName = fileName.substring(fileName.indexOf(":" + System.getProperty("file.separator")) + 2);
                        }

                        fileHeader.setFileName(fileName);
                        fileHeader.setDirectory(fileName.endsWith("/") || fileName.endsWith("\\"));
                    } else {
                        fileHeader.setFileName(null);
                    }

                    this.readAndSaveExtraDataRecord(fileHeader);
                    this.readAndSaveZip64ExtendedInfo(fileHeader);
                    this.readAndSaveAESExtraDataRecord(fileHeader);
                    if (fileCommentLength > 0) {
                        fileCommentBuf = new byte[fileCommentLength];
                        this.readIntoBuff(this.zip4jRaf, fileCommentBuf);
                        fileHeader.setFileComment(new String(fileCommentBuf));
                    }
                    fileHeaderList.add(fileHeader);
                }
                centralDirectory.setFileHeaders(fileHeaderList);
                DigitalSignature digitalSignature = new DigitalSignature();
                this.readIntoBuff(this.zip4jRaf, intBuff);
                int signature = Raw.readIntLittleEndian(intBuff, 0);
                if ((long) signature != 84233040L) {
                    return centralDirectory;
                } else {
                    digitalSignature.setHeaderSignature(signature);
                    this.readIntoBuff(this.zip4jRaf, shortBuff);
                    sizeOfData = Raw.readShortLittleEndian(shortBuff, 0);
                    digitalSignature.setSizeOfData(sizeOfData);
                    if (sizeOfData > 0) {
                        byte[] sigDataBuf = new byte[sizeOfData];
                        this.readIntoBuff(this.zip4jRaf, sigDataBuf);
                        digitalSignature.setSignatureData(new String(sigDataBuf));
                    }

                    return centralDirectory;
                }
            } catch (IOException var20) {
                throw new ZipException(var20);
            }
        }
    }

    private void readAndSaveExtraDataRecord(FileHeader fileHeader) throws ZipException {
        if (this.zip4jRaf == null) {
            throw new ZipException("invalid file handler when trying to read extra data record");
        } else if (fileHeader == null) {
            throw new ZipException("file header is null");
        } else {
            int extraFieldLength = fileHeader.getExtraFieldLength();
            if (extraFieldLength > 0) {
                fileHeader.setExtraDataRecords(this.readExtraDataRecords(extraFieldLength));
            }
        }
    }

    private void readAndSaveExtraDataRecord(LocalFileHeader localFileHeader) throws ZipException {
        if (this.zip4jRaf == null) {
            throw new ZipException("invalid file handler when trying to read extra data record");
        } else if (localFileHeader == null) {
            throw new ZipException("file header is null");
        } else {
            int extraFieldLength = localFileHeader.getExtraFieldLength();
            if (extraFieldLength > 0) {
                localFileHeader.setExtraDataRecords(this.readExtraDataRecords(extraFieldLength));
            }
        }
    }

    private ArrayList readExtraDataRecords(int extraFieldLength) throws ZipException {
        if (extraFieldLength <= 0) {
            return null;
        } else {
            try {
                byte[] extraFieldBuf = new byte[extraFieldLength];
                this.zip4jRaf.read(extraFieldBuf);
                int counter = 0;
                ArrayList extraDataList = new ArrayList();

                while (counter < extraFieldLength) {
                    ExtraDataRecord extraDataRecord = new ExtraDataRecord();
                    int header = Raw.readShortLittleEndian(extraFieldBuf, counter);
                    extraDataRecord.setHeader((long) header);
                    counter += 2;
                    int sizeOfRec = Raw.readShortLittleEndian(extraFieldBuf, counter);
                    if (2 + sizeOfRec > extraFieldLength) {
                        sizeOfRec = Raw.readShortBigEndian(extraFieldBuf, counter);
                        if (2 + sizeOfRec > extraFieldLength) {
                            break;
                        }
                    }

                    extraDataRecord.setSizeOfData(sizeOfRec);
                    counter += 2;
                    if (sizeOfRec > 0) {
                        byte[] data = new byte[sizeOfRec];
                        System.arraycopy(extraFieldBuf, counter, data, 0, sizeOfRec);
                        extraDataRecord.setData(data);
                    }

                    counter += sizeOfRec;
                    extraDataList.add(extraDataRecord);
                }

                return extraDataList.size() > 0 ? extraDataList : null;
            } catch (IOException var9) {
                throw new ZipException(var9);
            }
        }
    }

    private Zip64EndCentralDirLocator readZip64EndCentralDirLocator() throws ZipException {
        if (this.zip4jRaf == null) {
            throw new ZipException("invalid file handler when trying to read Zip64EndCentralDirLocator");
        } else {
            try {
                Zip64EndCentralDirLocator zip64EndCentralDirLocator = new Zip64EndCentralDirLocator();
                this.setFilePointerToReadZip64EndCentralDirLoc();
                byte[] intBuff = new byte[4];
                byte[] longBuff = new byte[8];
                this.readIntoBuff(this.zip4jRaf, intBuff);
                int signature = Raw.readIntLittleEndian(intBuff, 0);
                if ((long) signature == 117853008L) {
                    this.zipModel.setZip64Format(true);
                    zip64EndCentralDirLocator.setSignature((long) signature);
                    this.readIntoBuff(this.zip4jRaf, intBuff);
                    zip64EndCentralDirLocator.setNoOfDiskStartOfZip64EndOfCentralDirRec(Raw.readIntLittleEndian(intBuff, 0));
                    this.readIntoBuff(this.zip4jRaf, longBuff);
                    zip64EndCentralDirLocator.setOffsetZip64EndOfCentralDirRec(Raw.readLongLittleEndian(longBuff, 0));
                    this.readIntoBuff(this.zip4jRaf, intBuff);
                    zip64EndCentralDirLocator.setTotNumberOfDiscs(Raw.readIntLittleEndian(intBuff, 0));
                    return zip64EndCentralDirLocator;
                } else {
                    this.zipModel.setZip64Format(false);
                    return null;
                }
            } catch (Exception var5) {
                throw new ZipException(var5);
            }
        }
    }

    private Zip64EndCentralDirRecord readZip64EndCentralDirRec() throws ZipException {
        if (this.zipModel.getZip64EndCentralDirLocator() == null) {
            throw new ZipException("invalid zip64 end of central directory locator");
        } else {
            long offSetStartOfZip64CentralDir = this.zipModel.getZip64EndCentralDirLocator().getOffsetZip64EndOfCentralDirRec();
            if (offSetStartOfZip64CentralDir < 0L) {
                throw new ZipException("invalid offset for start of end of central directory record");
            } else {
                try {
                    this.zip4jRaf.seek(offSetStartOfZip64CentralDir);
                    Zip64EndCentralDirRecord zip64EndCentralDirRecord = new Zip64EndCentralDirRecord();
                    byte[] shortBuff = new byte[2];
                    byte[] intBuff = new byte[4];
                    byte[] longBuff = new byte[8];
                    this.readIntoBuff(this.zip4jRaf, intBuff);
                    int signature = Raw.readIntLittleEndian(intBuff, 0);
                    if ((long) signature != 101075792L) {
                        throw new ZipException("invalid signature for zip64 end of central directory record");
                    } else {
                        zip64EndCentralDirRecord.setSignature((long) signature);
                        this.readIntoBuff(this.zip4jRaf, longBuff);
                        zip64EndCentralDirRecord.setSizeOfZip64EndCentralDirRec(Raw.readLongLittleEndian(longBuff, 0));
                        this.readIntoBuff(this.zip4jRaf, shortBuff);
                        zip64EndCentralDirRecord.setVersionMadeBy(Raw.readShortLittleEndian(shortBuff, 0));
                        this.readIntoBuff(this.zip4jRaf, shortBuff);
                        zip64EndCentralDirRecord.setVersionNeededToExtract(Raw.readShortLittleEndian(shortBuff, 0));
                        this.readIntoBuff(this.zip4jRaf, intBuff);
                        zip64EndCentralDirRecord.setNoOfThisDisk(Raw.readIntLittleEndian(intBuff, 0));
                        this.readIntoBuff(this.zip4jRaf, intBuff);
                        zip64EndCentralDirRecord.setNoOfThisDiskStartOfCentralDir(Raw.readIntLittleEndian(intBuff, 0));
                        this.readIntoBuff(this.zip4jRaf, longBuff);
                        zip64EndCentralDirRecord.setTotNoOfEntriesInCentralDirOnThisDisk(Raw.readLongLittleEndian(longBuff, 0));
                        this.readIntoBuff(this.zip4jRaf, longBuff);
                        zip64EndCentralDirRecord.setTotNoOfEntriesInCentralDir(Raw.readLongLittleEndian(longBuff, 0));
                        this.readIntoBuff(this.zip4jRaf, longBuff);
                        zip64EndCentralDirRecord.setSizeOfCentralDir(Raw.readLongLittleEndian(longBuff, 0));
                        this.readIntoBuff(this.zip4jRaf, longBuff);
                        zip64EndCentralDirRecord.setOffsetStartCenDirWRTStartDiskNo(Raw.readLongLittleEndian(longBuff, 0));
                        long extDataSecSize = zip64EndCentralDirRecord.getSizeOfZip64EndCentralDirRec() - 44L;
                        if (extDataSecSize > 0L) {
                            byte[] extDataSecRecBuf = new byte[(int) extDataSecSize];
                            this.readIntoBuff(this.zip4jRaf, extDataSecRecBuf);
                            zip64EndCentralDirRecord.setExtensibleDataSector(extDataSecRecBuf);
                        }

                        return zip64EndCentralDirRecord;
                    }
                } catch (IOException var11) {
                    throw new ZipException(var11);
                }
            }
        }
    }

    private void readAndSaveZip64ExtendedInfo(FileHeader fileHeader) throws ZipException {
        if (fileHeader == null) {
            throw new ZipException("file header is null in reading Zip64 Extended Info");
        } else if (fileHeader.getExtraDataRecords() != null && fileHeader.getExtraDataRecords().size() > 0) {
            Zip64ExtendedInfo zip64ExtendedInfo = this.readZip64ExtendedInfo(fileHeader.getExtraDataRecords(), fileHeader.getUncompressedSize(), fileHeader.getCompressedSize(), fileHeader.getOffsetLocalHeader(), fileHeader.getDiskNumberStart());
            if (zip64ExtendedInfo != null) {
                fileHeader.setZip64ExtendedInfo(zip64ExtendedInfo);
                if (zip64ExtendedInfo.getUnCompressedSize() != -1L) {
                    fileHeader.setUncompressedSize(zip64ExtendedInfo.getUnCompressedSize());
                }

                if (zip64ExtendedInfo.getCompressedSize() != -1L) {
                    fileHeader.setCompressedSize(zip64ExtendedInfo.getCompressedSize());
                }

                if (zip64ExtendedInfo.getOffsetLocalHeader() != -1L) {
                    fileHeader.setOffsetLocalHeader(zip64ExtendedInfo.getOffsetLocalHeader());
                }

                if (zip64ExtendedInfo.getDiskNumberStart() != -1) {
                    fileHeader.setDiskNumberStart(zip64ExtendedInfo.getDiskNumberStart());
                }
            }

        }
    }

    private void readAndSaveZip64ExtendedInfo(LocalFileHeader localFileHeader) throws ZipException {
        if (localFileHeader == null) {
            throw new ZipException("file header is null in reading Zip64 Extended Info");
        } else if (localFileHeader.getExtraDataRecords() != null && localFileHeader.getExtraDataRecords().size() > 0) {
            Zip64ExtendedInfo zip64ExtendedInfo = this.readZip64ExtendedInfo(localFileHeader.getExtraDataRecords(), localFileHeader.getUncompressedSize(), localFileHeader.getCompressedSize(), -1L, -1);
            if (zip64ExtendedInfo != null) {
                localFileHeader.setZip64ExtendedInfo(zip64ExtendedInfo);
                if (zip64ExtendedInfo.getUnCompressedSize() != -1L) {
                    localFileHeader.setUncompressedSize(zip64ExtendedInfo.getUnCompressedSize());
                }

                if (zip64ExtendedInfo.getCompressedSize() != -1L) {
                    localFileHeader.setCompressedSize(zip64ExtendedInfo.getCompressedSize());
                }
            }

        }
    }

    private Zip64ExtendedInfo readZip64ExtendedInfo(ArrayList extraDataRecords, long unCompressedSize, long compressedSize, long offsetLocalHeader, int diskNumberStart) throws ZipException {
        for (int i = 0; i < extraDataRecords.size(); ++i) {
            ExtraDataRecord extraDataRecord = (ExtraDataRecord) extraDataRecords.get(i);
            if (extraDataRecord != null && extraDataRecord.getHeader() == 1L) {
                Zip64ExtendedInfo zip64ExtendedInfo = new Zip64ExtendedInfo();
                byte[] byteBuff = extraDataRecord.getData();
                if (extraDataRecord.getSizeOfData() > 0) {
                    byte[] longByteBuff = new byte[8];
                    byte[] intByteBuff = new byte[4];
                    int counter = 0;
                    boolean valueAdded = false;
                    long val;
                    if ((unCompressedSize & 65535L) == 65535L && counter < extraDataRecord.getSizeOfData()) {
                        System.arraycopy(byteBuff, counter, longByteBuff, 0, 8);
                        val = Raw.readLongLittleEndian(longByteBuff, 0);
                        zip64ExtendedInfo.setUnCompressedSize(val);
                        counter += 8;
                        valueAdded = true;
                    }

                    if ((compressedSize & 65535L) == 65535L && counter < extraDataRecord.getSizeOfData()) {
                        System.arraycopy(byteBuff, counter, longByteBuff, 0, 8);
                        val = Raw.readLongLittleEndian(longByteBuff, 0);
                        zip64ExtendedInfo.setCompressedSize(val);
                        counter += 8;
                        valueAdded = true;
                    }

                    if ((offsetLocalHeader & 65535L) == 65535L && counter < extraDataRecord.getSizeOfData()) {
                        System.arraycopy(byteBuff, counter, longByteBuff, 0, 8);
                        val = Raw.readLongLittleEndian(longByteBuff, 0);
                        zip64ExtendedInfo.setOffsetLocalHeader(val);
                        counter += 8;
                        valueAdded = true;
                    }

                    if ((diskNumberStart & '\uffff') == 65535 && counter < extraDataRecord.getSizeOfData()) {
                        System.arraycopy(byteBuff, counter, intByteBuff, 0, 4);
                        int val2 = Raw.readIntLittleEndian(intByteBuff, 0);
                        zip64ExtendedInfo.setDiskNumberStart(val2);
                        counter += 8;
                        valueAdded = true;
                    }

                    if (valueAdded) {
                        return zip64ExtendedInfo;
                    }
                }
                break;
            }
        }

        return null;
    }

    private void setFilePointerToReadZip64EndCentralDirLoc() throws ZipException {
        try {
            byte[] ebs = new byte[4];
            long pos = this.zip4jRaf.length() - 22L;

            do {
                this.zip4jRaf.seek(pos--);
            } while ((long) Raw.readLeInt(this.zip4jRaf, ebs) != 101010256L);

            this.zip4jRaf.seek(this.zip4jRaf.getFilePointer() - 4L - 4L - 8L - 4L - 4L);
        } catch (IOException var4) {
            throw new ZipException(var4);
        }
    }

    public LocalFileHeader readLocalFileHeader(FileHeader fileHeader) throws ZipException {
        if (fileHeader != null && this.zip4jRaf != null) {
            long locHdrOffset = fileHeader.getOffsetLocalHeader();
            if (fileHeader.getZip64ExtendedInfo() != null) {
                Zip64ExtendedInfo zip64ExtendedInfo = fileHeader.getZip64ExtendedInfo();
                if (zip64ExtendedInfo.getOffsetLocalHeader() > 0L) {
                    locHdrOffset = fileHeader.getOffsetLocalHeader();
                }
            }

            if (locHdrOffset < 0L) {
                throw new ZipException("invalid local header offset");
            } else {
                try {
                    this.zip4jRaf.seek(locHdrOffset);
                    int length = 0;
                    LocalFileHeader localFileHeader = new LocalFileHeader();
                    byte[] shortBuff = new byte[2];
                    byte[] intBuff = new byte[4];
                    byte[] longBuff = new byte[8];
                    this.readIntoBuff(this.zip4jRaf, intBuff);
                    int sig = Raw.readIntLittleEndian(intBuff, 0);
                    if ((long) sig != 67324752L) {
                        throw new ZipException("invalid local header signature for file: " + fileHeader.getFileName());
                    } else {
                        localFileHeader.setSignature(sig);
                        length = length + 4;
                        this.readIntoBuff(this.zip4jRaf, shortBuff);
                        localFileHeader.setVersionNeededToExtract(Raw.readShortLittleEndian(shortBuff, 0));
                        length += 2;
                        this.readIntoBuff(this.zip4jRaf, shortBuff);
                        localFileHeader.setFileNameUTF8Encoded((Raw.readShortLittleEndian(shortBuff, 0) & 2048) != 0);
                        int firstByte = shortBuff[0];
                        int result = firstByte & 1;
                        if (result != 0) {
                            localFileHeader.setEncrypted(true);
                        }

                        localFileHeader.setGeneralPurposeFlag(shortBuff);
                        length += 2;
                        String binary = Integer.toBinaryString(firstByte);
                        if (binary.length() >= 4) {
                            localFileHeader.setDataDescriptorExists(binary.charAt(3) == '1');
                        }

                        this.readIntoBuff(this.zip4jRaf, shortBuff);
                        localFileHeader.setCompressionMethod(Raw.readShortLittleEndian(shortBuff, 0));
                        length += 2;
                        this.readIntoBuff(this.zip4jRaf, intBuff);
                        localFileHeader.setLastModFileTime(Raw.readIntLittleEndian(intBuff, 0));
                        length += 4;
                        this.readIntoBuff(this.zip4jRaf, intBuff);
                        localFileHeader.setCrc32((long) Raw.readIntLittleEndian(intBuff, 0));
                        localFileHeader.setCrcBuff((byte[]) intBuff.clone());
                        length += 4;
                        this.readIntoBuff(this.zip4jRaf, intBuff);
                        longBuff = this.getLongByteFromIntByte(intBuff);
                        localFileHeader.setCompressedSize(Raw.readLongLittleEndian(longBuff, 0));
                        length += 4;
                        this.readIntoBuff(this.zip4jRaf, intBuff);
                        longBuff = this.getLongByteFromIntByte(intBuff);
                        localFileHeader.setUncompressedSize(Raw.readLongLittleEndian(longBuff, 0));
                        length += 4;
                        this.readIntoBuff(this.zip4jRaf, shortBuff);
                        int fileNameLength = Raw.readShortLittleEndian(shortBuff, 0);
                        localFileHeader.setFileNameLength(fileNameLength);
                        length += 2;
                        this.readIntoBuff(this.zip4jRaf, shortBuff);
                        int extraFieldLength = Raw.readShortLittleEndian(shortBuff, 0);
                        localFileHeader.setExtraFieldLength(extraFieldLength);
                        length += 2;
                        if (fileNameLength > 0) {
                            byte[] fileNameBuf = new byte[fileNameLength];
                            this.readIntoBuff(this.zip4jRaf, fileNameBuf);
                            String fileName = Zip4jUtil.decodeFileName(fileNameBuf, localFileHeader.isFileNameUTF8Encoded());
                            if (fileName == null) {
                                throw new ZipException("file name is null, cannot assign file name to local file header");
                            }

                            if (fileName.indexOf(":" + System.getProperty("file.separator")) >= 0) {
                                fileName = fileName.substring(fileName.indexOf(":" + System.getProperty("file.separator")) + 2);
                            }

                            localFileHeader.setFileName(fileName);
                            length += fileNameLength;
                        } else {
                            localFileHeader.setFileName((String) null);
                        }

                        this.readAndSaveExtraDataRecord(localFileHeader);
                        length += extraFieldLength;
                        localFileHeader.setOffsetStartOfData(locHdrOffset + (long) length);
                        localFileHeader.setPassword(fileHeader.getPassword());
                        this.readAndSaveZip64ExtendedInfo(localFileHeader);
                        this.readAndSaveAESExtraDataRecord(localFileHeader);
                        if (localFileHeader.isEncrypted() && localFileHeader.getEncryptionMethod() != 99) {
                            if ((firstByte & 64) == 64) {
                                localFileHeader.setEncryptionMethod(1);
                            } else {
                                localFileHeader.setEncryptionMethod(0);
                            }
                        }

                        if (localFileHeader.getCrc32() <= 0L) {
                            localFileHeader.setCrc32(fileHeader.getCrc32());
                            localFileHeader.setCrcBuff(fileHeader.getCrcBuff());
                        }

                        if (localFileHeader.getCompressedSize() <= 0L) {
                            localFileHeader.setCompressedSize(fileHeader.getCompressedSize());
                        }

                        if (localFileHeader.getUncompressedSize() <= 0L) {
                            localFileHeader.setUncompressedSize(fileHeader.getUncompressedSize());
                        }

                        return localFileHeader;
                    }
                } catch (IOException var17) {
                    throw new ZipException(var17);
                }
            }
        } else {
            throw new ZipException("invalid read parameters for local header");
        }
    }

    private void readAndSaveAESExtraDataRecord(FileHeader fileHeader) throws ZipException {
        if (fileHeader == null) {
            throw new ZipException("file header is null in reading Zip64 Extended Info");
        } else if (fileHeader.getExtraDataRecords() != null && fileHeader.getExtraDataRecords().size() > 0) {
            AESExtraDataRecord aesExtraDataRecord = this.readAESExtraDataRecord(fileHeader.getExtraDataRecords());
            if (aesExtraDataRecord != null) {
                fileHeader.setAesExtraDataRecord(aesExtraDataRecord);
                fileHeader.setEncryptionMethod(99);
            }

        }
    }

    private void readAndSaveAESExtraDataRecord(LocalFileHeader localFileHeader) throws ZipException {
        if (localFileHeader == null) {
            throw new ZipException("file header is null in reading Zip64 Extended Info");
        } else if (localFileHeader.getExtraDataRecords() != null && localFileHeader.getExtraDataRecords().size() > 0) {
            AESExtraDataRecord aesExtraDataRecord = this.readAESExtraDataRecord(localFileHeader.getExtraDataRecords());
            if (aesExtraDataRecord != null) {
                localFileHeader.setAesExtraDataRecord(aesExtraDataRecord);
                localFileHeader.setEncryptionMethod(99);
            }

        }
    }

    private AESExtraDataRecord readAESExtraDataRecord(ArrayList extraDataRecords) throws ZipException {
        if (extraDataRecords == null) {
            return null;
        } else {
            for (int i = 0; i < extraDataRecords.size(); ++i) {
                ExtraDataRecord extraDataRecord = (ExtraDataRecord) extraDataRecords.get(i);
                if (extraDataRecord != null && extraDataRecord.getHeader() == 39169L) {
                    if (extraDataRecord.getData() == null) {
                        throw new ZipException("corrput AES extra data records");
                    }

                    AESExtraDataRecord aesExtraDataRecord = new AESExtraDataRecord();
                    aesExtraDataRecord.setSignature(39169L);
                    aesExtraDataRecord.setDataSize(extraDataRecord.getSizeOfData());
                    byte[] aesData = extraDataRecord.getData();
                    aesExtraDataRecord.setVersionNumber(Raw.readShortLittleEndian(aesData, 0));
                    byte[] vendorIDBytes = new byte[2];
                    System.arraycopy(aesData, 2, vendorIDBytes, 0, 2);
                    aesExtraDataRecord.setVendorID(new String(vendorIDBytes));
                    aesExtraDataRecord.setAesStrength(aesData[4] & 255);
                    aesExtraDataRecord.setCompressionMethod(Raw.readShortLittleEndian(aesData, 5));
                    return aesExtraDataRecord;
                }
            }

            return null;
        }
    }

    private byte[] readIntoBuff(RandomAccessFile zip4jRaf, byte[] buf) throws ZipException {
        try {
            if (zip4jRaf.read(buf, 0, buf.length) != -1) {
                return buf;
            } else {
                throw new ZipException("unexpected end of file when reading short buff");
            }
        } catch (IOException var4) {
            throw new ZipException("IOException when reading short buff", var4);
        }
    }

    private byte[] getLongByteFromIntByte(byte[] intByte) throws ZipException {
        if (intByte == null) {
            throw new ZipException("input parameter is null, cannot expand to 8 bytes");
        } else if (intByte.length != 4) {
            throw new ZipException("invalid byte length, cannot expand to 8 bytes");
        } else {
            byte[] longBuff = new byte[]{intByte[0], intByte[1], intByte[2], intByte[3], 0, 0, 0, 0};
            return longBuff;
        }
    }
}
