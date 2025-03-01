package org.mule.pdf.extension.api;

public class PDFAttributes {
    private int pageCount;
    private int fileSize;
    private String fileName;

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String newFileName) {
        this.fileName = newFileName;
    }
}
