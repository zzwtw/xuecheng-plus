package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.config.MinioConfig;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.UploadObjectArgs;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/10 8:58
 */
@Slf4j
@Service
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket.files}")
    private String mediaFilesBucket;

    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }

    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath) {
        // 获取文件
        File file = new File(localFilePath);
        // 获取文件名
        String fileOriginalName = uploadFileParamsDto.getFilename();
        // 获取文件拓展名
        String extension = fileOriginalName.substring(fileOriginalName.lastIndexOf("."));
        // 获取文件的mediaType
        String mimeType = getMimeType(extension);
        // 获取目录
        String objectFolder = getObjectFolder(uploadFileParamsDto);
        // 获取文件的MD5值
        String fileMd5 = getFileMd5(file);
        // 整合文件子目录与文件名与后缀
        String objectName = objectFolder + fileMd5 + extension;
        // 上传图片到minio
        upLoadFile2MinIo(localFilePath, mimeType, objectName);
        // 保存图片数据到数据库
        MediaFiles mediaFiles = insertFileData2DataBase(companyId, uploadFileParamsDto, fileMd5, objectName);
        // 创建返回对象
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);

        return uploadFileResultDto;
    }


    private MediaFiles insertFileData2DataBase(Long companyId, UploadFileParamsDto uploadFileParamsDto, String fileMd5, String objectName) {
        MediaFiles mediaFiles = new MediaFiles();
        BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
        mediaFiles.setCompanyId(companyId);
        // 插入文件id，也就是文件的md5
        mediaFiles.setFileId(fileMd5);
        // 插入文件的存储目录，也就是文件所在的bucket
        mediaFiles.setBucket(mediaFilesBucket);
        // 插入文件的存储路径，也就是文件的子目录 + md5 + 后缀
        mediaFiles.setFilePath(objectName);
        // 插入id
        mediaFiles.setId(fileMd5);
        // 插入url,url = bucket + 子目录 + 文件的md5值 + 后缀
        String url = "/" + mediaFilesBucket + "/" + objectName;
        mediaFiles.setUrl(url);
        int insert = mediaFilesMapper.insert(mediaFiles);
        if (insert <= 0) {
            log.error("文件数据插入数据库失败，url{}", "/" + url);
        }
        return mediaFiles;
    }

    private void upLoadFile2MinIo(String localFilePath, String mimeType, String objectName) {
        try {
            minioClient.uploadObject(UploadObjectArgs.builder()
                    .bucket(mediaFilesBucket) // 桶名称
                    .filename(localFilePath) // 图片路径
                    .object(objectName) // 上传之后的对象名
                    .contentType(mimeType)
                    .build());
        } catch (Exception e) {
            log.error("图片上传至MinIo失败，bucket{},filename{},objectName{},contentType{}", mediaFilesBucket, localFilePath, objectName, mimeType);
            XueChengPlusException.cast("图片上传失败，请查看日志");
        }

    }

    // 获取文件的mediaType
    private String getMimeType(String extension) {
        if (extension == null)
            extension = "";
        //根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        //通用mimeType，字节流
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if (extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;
    }

    // 获取文件的MD5值
    private String getFileMd5(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            String fileMd5 = DigestUtils.md5Hex(fileInputStream);
            return fileMd5;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 获取文件子目录
    private String getObjectFolder(UploadFileParamsDto uploadFileParamsDto) {
        // 指定时间格式
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String folder = dateFormat.format(new Date()).replace("-", "/") + "/";
        return folder;
    }
}
