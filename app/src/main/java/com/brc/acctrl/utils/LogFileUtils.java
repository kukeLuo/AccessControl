package com.brc.acctrl.utils;

import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author zhengdan
 * @date 2019-07-31
 * @Description:
 */
public class LogFileUtils {
    public static void writeFloatToData(float[] verts, String gcodeFile, int count) {
        try {
            RandomAccessFile aFile = new RandomAccessFile(gcodeFile, "rw");
            FileChannel outChannel = aFile.getChannel();
            //one float 4 bytes
            ByteBuffer buf = ByteBuffer.allocate(4 * count);
            buf.clear();
            buf.asFloatBuffer().put(verts);
            //while(buf.hasRemaining())
            {
                outChannel.write(buf);
            }
            //outChannel.close();
            buf.rewind();
            outChannel.close();
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }

    /**
     * A方法追加文件：使用RandomAccessFile
     */
    public static void appendMethodA(String fileName, String content) {
        try {
            // 打开一个随机访问文件流，按读写方式
            RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw");
            // 文件长度，字节数
            long fileLength = randomFile.length();
            //将写文件指针移到文件尾。
            randomFile.seek(fileLength);
            randomFile.writeBytes(content + "\n");
            randomFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void appendMethodA(String content) {
        File logFile = new File(Environment.getExternalStorageDirectory(), "log.txt");

        try {
            // 打开一个随机访问文件流，按读写方式
            RandomAccessFile randomFile = new RandomAccessFile(logFile.getAbsolutePath(), "rw");
            // 文件长度，字节数
            long fileLength = randomFile.length();
            //将写文件指针移到文件尾。
            randomFile.seek(fileLength);
            randomFile.writeBytes(content + "\n");
            randomFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
