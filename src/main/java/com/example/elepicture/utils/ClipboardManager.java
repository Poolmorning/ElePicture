package com.example.elepicture.utils;

import java.io.File;
import java.util.List;


//剪贴板管理类，用于存储复制/剪切的文件
public class ClipboardManager {
    public enum OperationType {
        COPY, CUT
    }

    private List<File> clipboardFiles;
    private OperationType operationType;

    public List<File> getClipboardFiles() {
        return clipboardFiles;
    }

    public void setClipboardFiles(List<File> clipboardFiles) {
        this.clipboardFiles = clipboardFiles;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }

    public void clear() {
        clipboardFiles = null;
        operationType = null;
    }

    public boolean hasData() {
        return clipboardFiles != null && !clipboardFiles.isEmpty();
    }
}
