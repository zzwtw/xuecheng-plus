package com.xuecheng.media;

import com.baomidou.mybatisplus.extension.api.R;
import com.mysql.cj.result.Field;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BigFileTest {
    //测试文件分块方法
    @Test
    public void testChunk() throws IOException {
        // 拿到文件
        File sourceFile = new File("C:\\Users\\1\\Desktop\\素材\\Counter-strike 2 2024.01.04 - 18.44.44.01.mp4");
        // 分块到这个目录下
        String chunkPath = "C:\\Users\\1\\Desktop\\素材\\chunk\\";
        File chunkFolder = new File(chunkPath);
        if (!chunkFolder.exists()) {
            chunkFolder.mkdirs();
        }
        //分块大小 1024 = 2的10次方，2的20次方等于1MB
        long chunkSize = 1024 * 1024 * 5;
        //分块数量
        long chunkNum = (long) Math.ceil(sourceFile.length() * 1.0 / chunkSize);
        System.out.println("分块总数：" + chunkNum);
        //缓冲区大小
        byte[] b = new byte[1024];
        //使用RandomAccessFile访问文件
        RandomAccessFile raf_read = new RandomAccessFile(sourceFile, "r");
        //分块
        for (int i = 0; i < chunkNum; i++) {
            //创建分块文件
            File file = new File(chunkPath + i);
            if (file.exists()) {
                file.delete();
            }
            boolean newFile = file.createNewFile();
            if (newFile) {
                //向分块文件中写数据
                RandomAccessFile raf_write = new RandomAccessFile(file, "rw");
                int len = -1;
                while ((len = raf_read.read(b)) != -1) {
                    raf_write.write(b, 0, len);
                    if (file.length() >= chunkSize) {
                        break;
                    }
                }
                raf_write.close();
                System.out.println("完成分块" + i);
            }

        }
        raf_read.close();

    }

    @Test
    public void testMerge() throws IOException {
        // 分块目录
        String chunkPath = "C:\\Users\\1\\Desktop\\素材\\chunk\\";
        // 合并文件
        File mergeFile = new File("C:\\Users\\1\\Desktop\\素材\\merge.mp4");
        RandomAccessFile randomAccessFile1 = new RandomAccessFile(mergeFile, "rw");
        File chunkFile = new File(chunkPath);
        File[] listFiles = chunkFile.listFiles();
        List<File> fileList = Arrays.asList(listFiles);
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return Integer.parseInt(o1.getName()) - Integer.parseInt(o2.getName());
            }
        });
        byte[] bytes = new byte[1024];
        for (File file : fileList) {
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            int len = -1;
            while ((len = randomAccessFile.read(bytes)) != -1) {
                randomAccessFile1.write(bytes, 0, len);
            }
            randomAccessFile.close();
        }
        randomAccessFile1.close();

    }

    @Test
    public void testStream1() throws IOException {
        // 创建对象时，参数是路径或者File对象都可以
        // 如果文件不存在，会创建一个新的文件，但是要保证父级路径存在
        // 如果文件存在，则会清空文件
        FileOutputStream fileOutputStream = new FileOutputStream("C:\\Users\\1\\Desktop\\代码\\xuecheng-plus\\xuecheng-plus-media\\xuecheng-plus-media-service\\src\\test\\java\\com\\xuecheng\\media\\1.txt");
        fileOutputStream.write(97); // 写出数据
        fileOutputStream.close(); // 释放资源，解除资源的占用
    }

    @Test
    public void testStream2() throws IOException {
        // 创建对象时，参数是路径或者File对象都可以
        // 如果文件不存在，会创建一个新的文件，但是要保证父级路径存在
        // 如果文件存在，则会清空文件
        FileOutputStream fileOutputStream = new FileOutputStream("C:\\Users\\1\\Desktop\\代码\\xuecheng-plus\\xuecheng-plus-media\\xuecheng-plus-media-service\\src\\test\\java\\com\\xuecheng\\media\\1.txt");
        byte[] bytes = {97, 98, 99, 100, 101, 102};
//        fileOutputStream.write(bytes); // 一次写入多个
        fileOutputStream.write(bytes, 1, 2); // 从索引为1开始，写2个字节
        fileOutputStream.close(); // 释放资源，解除资源的占用
    }

    @Test
    public void testStream3() throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream("C:\\Users\\1\\Desktop\\代码\\xuecheng-plus\\xuecheng-plus-media\\xuecheng-plus-media-service\\src\\test\\java\\com\\xuecheng\\media\\1.txt",
                true);// append: 续写开关，true为开启续写
        String str = "zwt";
        byte[] bytes = str.getBytes(); // 写入多个字符，转字节数组之后写入
        fileOutputStream.write(bytes);
        String wrap = "\r\n";
        byte[] bytes1 = wrap.getBytes();
        fileOutputStream.write(bytes1);
        String str1 = "666";
        byte[] bytes2 = str1.getBytes();
        fileOutputStream.write(bytes2);
        fileOutputStream.close(); // 释放资源，解除资源的占用
    }

    @Test
    public void testStream4() throws IOException {
        // 文件不存在，则报错
        FileInputStream fileInputStream = new FileInputStream("C:\\Users\\1\\Desktop\\代码\\xuecheng-plus\\xuecheng-plus-media\\xuecheng-plus-media-service\\src\\test\\java\\com\\xuecheng\\media\\1.txt");
        byte[] bytes = new byte[]{};
        int read1 = fileInputStream.read(); // 返回对应的ASCII
        System.out.println((char) read1); // 读到文件末尾了，read方法返回-1
        fileInputStream.close();
    }

    @Test
    public void testStream5() throws IOException {
        // 文件不存在，则报错
        FileInputStream fileInputStream = new FileInputStream("C:\\Users\\1\\Desktop\\代码\\xuecheng-plus\\xuecheng-plus-media\\xuecheng-plus-media-service\\src\\test\\java\\com\\xuecheng\\media\\1.txt");
        // 循环读取
        int b;
        while ((b = fileInputStream.read()) != -1) {
            System.out.println((char) b);
        }
        fileInputStream.close();
    }

    @Test
    public void testStream6() throws IOException {
        // 文件不存在，则报错
        FileInputStream fileInputStream = new FileInputStream("C:\\Users\\1\\Desktop\\素材\\Counter-strike 2 2024.01.04 - 18.44.44.01.mp4");
        FileOutputStream fileOutputStream = new FileOutputStream("C:\\Users\\1\\Desktop\\代码\\xuecheng-plus\\xuecheng-plus-media\\xuecheng-plus-media-service\\src\\test\\java\\com\\xuecheng\\media\\copy.mp4");
        // 循环读取
        int b;
        while ((b = fileInputStream.read()) != -1) {
            fileOutputStream.write(b);
        }
        // 先打开的后关闭
        fileOutputStream.close();
        fileInputStream.close();

    }

    @Test
    public void testStream7() throws IOException {
        // 文件不存在，则报错
        FileInputStream fileInputStream = new FileInputStream("C:\\Users\\1\\Desktop\\素材\\Counter-strike 2 2024.01.04 - 18.44.44.01.mp4");
        FileOutputStream fileOutputStream = new FileOutputStream("C:\\Users\\1\\Desktop\\代码\\xuecheng-plus\\xuecheng-plus-media\\xuecheng-plus-media-service\\src\\test\\java\\com\\xuecheng\\media\\copy.mp4");
        // 循环读取
        int len;
        byte[] bytes = new byte[1024];
        // len：读取了多少个字节数据，输入流读取的数据放入bytes数组中
        while ((len = fileInputStream.read(bytes)) != -1) {
            fileOutputStream.write(bytes, 0, len);// 写入bytes数组中的0到len的字节数据到文件中
        }
        // 先打开的后关闭
        fileOutputStream.close();
        fileInputStream.close();

    }


}