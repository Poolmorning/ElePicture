package com.example.elepicture.utils;

import java.io.File;
import java.util.List;


//剪贴板管理类，用于存储复制/剪切的文件
public class ClipboardManager {
    public enum OperationType {
        COPY, CUT//定义复制剪切两种操作
    }

    private List<File> clipboardFiles;//存储剪贴板中的文件列表
    private OperationType operationType; //记录当前操作类型(COPY或CUT)

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
    //清空剪贴板内容
    public void clear() {
        clipboardFiles = null;
        operationType = null;
    }
    //检查剪贴板中是否有数据
    public boolean hasData() {
        return clipboardFiles != null && !clipboardFiles.isEmpty();
    }
}
