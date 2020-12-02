package com.brc.acctrl.bean;

import com.brc.acctrl.utils.FileUtils;

import java.io.File;

public class BaseFileInfo {

    public File file;

    public BaseFileInfo(String fileStr){
        File distFile = new File(fileStr);
        file=distFile;
    }
}
