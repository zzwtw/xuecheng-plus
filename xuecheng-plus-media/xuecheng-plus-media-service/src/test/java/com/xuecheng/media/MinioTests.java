package com.xuecheng.media;

import com.alibaba.nacos.common.utils.IoUtils;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;
import io.minio.errors.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class MinioTests {

    static MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://127.0.0.1:9000")
                    .credentials("minioadmin", "minioadmin")
                    .build();

    @Test
    public void testUploadFile2Minio() throws Exception {
        //根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(".sql");
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//通用mimeType，字节流
        if (extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }
        minioClient.uploadObject(UploadObjectArgs.builder()
                .bucket("testbucket")
                .filename("C:\\Users\\1\\Desktop\\学成在线\\第一部分\\xcplus_media.sql")
//              .object("media.sql")
                .object("001/media1.sql")
                .contentType(mimeType)// 添加子目录
                .build());
    }

    @Test
    public void testDeleteFileFromMinio() throws Exception {
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket("testbucket")
                .object("001/media1.sql")
                .build());
    }

    @Test
    public void testGetFile() throws Exception {
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket("testbucket")
                .object("log4j2-dev.xml")
                .build();

        InputStream inputStream = minioClient.getObject(getObjectArgs);
        FileOutputStream fileOutputStream = new FileOutputStream(new File("C:\\Users\\1\\Desktop\\学成在线\\第一部分\\minio\\1.xml"));
        IoUtils.copy(inputStream,fileOutputStream);

    }
}
