package com.xuecheng.media.service.impl;

import com.alibaba.nacos.common.utils.IoUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.config.MinioConfig;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    @Value("${minio.bucket.videofiles}")
    private String videoFilesBucket;

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


    /**
     * @description 上传文件
     * @param companyId 机构id
     * @param uploadFileParamsDto 上传文件插入数据库的dto信息
     * @param localFilePath 文件在本地的路径
     * @return
     */
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
        String objectFolder = getObjectFolder();
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

    /**
     * @description 检查文件是否已经上传
     * @param fileMd5 文件的md5值
     * @return
     */
    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        // 检查数据库中是否有文件数据
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles != null) {
            // 获取objectName
            String filePath = mediaFiles.getFilePath();
            // 获取bucket
            String bucket = mediaFiles.getBucket();
            // 检查minio中是否有文件信息
            try {
                GetObjectResponse objectResponse = minioClient.getObject(GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(filePath)
                        .build());
                if (objectResponse != null) {
                    return RestResponse.success(true);
                }
            } catch (Exception e) {
                return RestResponse.validfail(false, "文件不存在并且查询minio时出现异常");
            }
        }

        return RestResponse.success(false);
    }

    /**
     * @description 检查该分块文件是否上传过，如果上传过则不重复上传
     * @param fileMd5 文件的md5值
     * @param chunkIndex 分块文件的块号
     * @return
     */
    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        // 拼接ObjectName
        String objectName = getChunkFileObjectName(fileMd5, chunkIndex);
        // 检查分块号为chunkIndex的分块文件是否已经上传过了
        try {
            InputStream fileInputStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(videoFilesBucket)
                    .object(objectName)
                    .build());
            if (fileInputStream != null) {
                return RestResponse.success(true);
            }
        } catch (Exception e) {
//            log.error("在minio中查询该分块文件时出现异常{}", e.getMessage());
//            return RestResponse.validfail(false, "在minio中查询该分块文件时出现异常");
        }
        return RestResponse.success(false);
    }

    /**
     * @description 上传分块文件
     * @param file 文件本身
     * @param fileMd5 文件的md5值
     * @param chunkIndex 分块文件的块号
     * @return
     * @throws IOException
     */
    @Override
    public RestResponse uploadChunk(MultipartFile file, String fileMd5, int chunkIndex) throws IOException {
        // 获取ObjectName
        String objectName = getChunkFileObjectName(fileMd5, chunkIndex);
        // 获取本地文件路径
        File tempFile = File.createTempFile("minio", ".temp");
        file.transferTo(tempFile);
        String absolutePath = tempFile.getAbsolutePath();
        // 获取mimeType
        String mimeType = getMimeType(null);
        // 上传分块号为chunkIndex的分块文件
        try {
            minioClient.uploadObject(UploadObjectArgs.builder()
                    .bucket(videoFilesBucket)
                    .object(objectName)
                    .filename(absolutePath)
                    .contentType(mimeType)
                    .build());
        } catch (Exception e) {
        }
        return RestResponse.success(true);
    }

    /**
     * @description 合并上传之后的分块文件
     * @param companyId 机构id
     * @param uploadFileParamsDto 上传文件插入数据库的dto信息
     * @param fileMd5 文件的md5值
     * @param chunkTotal 文件的分块总数
     * @return
     */
    @Override
    public RestResponse mergeChunks(Long companyId, UploadFileParamsDto uploadFileParamsDto, String fileMd5, int chunkTotal) {
        // 获取extension
        String extension = uploadFileParamsDto.getFilename().substring(uploadFileParamsDto.getFilename().lastIndexOf("."));
        // 获取objectName
        String mergeFileObjectName = getMergeFileObjectName(fileMd5, extension);
        // sources
        List<ComposeSource> composeSourceList = new ArrayList<>();
        for (int i = 0; i < chunkTotal; i++) {
            ComposeSource composeSource = ComposeSource.builder().bucket(videoFilesBucket).object(getChunkFileObjectName(fileMd5, i)).build();
            composeSourceList.add(composeSource);
        }
        // 合并视频
        try {
            minioClient.composeObject(ComposeObjectArgs.builder()
                    .bucket(videoFilesBucket)
                    .sources(composeSourceList)
                    .object(mergeFileObjectName)
                    .build());
        } catch (Exception e) {
        }
        // 下载视频到本地，校验视频
        try {
            InputStream fileInputStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(videoFilesBucket)
                    .object(getMergeFileObjectName(fileMd5, extension))
                    .build());
            // 获取下载的文件
            File downloadFile = downloadFileFromMinIo(fileInputStream);
            FileInputStream fileInputStreamNew = new FileInputStream(downloadFile);
            // 验证
            String downloadFileMd5 = DigestUtils.md5Hex(fileInputStreamNew);
            if(downloadFileMd5.equals(fileMd5)){
                // 插入数据库
                uploadFileParamsDto.setFileSize(downloadFile.length());
                MediaFiles mediaFiles = insertFileData2DataBase(companyId, uploadFileParamsDto, fileMd5, mergeFileObjectName);
            }else {
                return RestResponse.validfail(false, "文件合并校验失败，最终上传失败。");
            }
        } catch (Exception e) {
            log.debug("校验文件失败,fileMd5:{},异常:{}",fileMd5,e.getMessage(),e);
            return RestResponse.validfail(false, "文件合并校验失败，最终上传失败。");
        }
        // 清除分块文件
        clearChunkFiles(fileMd5,chunkTotal);
        return RestResponse.success(true);
    }

    /**
     * @description 清楚分块文件
     * @param fileMd5 文件的md5值
     * @param chunkTotal 文件的分块总数
     */
    private void clearChunkFiles(String fileMd5,int chunkTotal){
        List<DeleteObject> objects = new ArrayList<>();
        for (int i = 0; i < chunkTotal; i++) {
            DeleteObject deleteObject = new DeleteObject(getChunkFileObjectName(fileMd5, i));
            objects.add(deleteObject);
        }
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(RemoveObjectsArgs.builder()
                .bucket(videoFilesBucket)
                .objects(objects)
                .build());
        results.forEach(r->{
            DeleteError deleteError = null;
            try {
                deleteError = r.get();
            }catch (Exception e){
                e.printStackTrace();
                log.error("清除分块文件失败,objectname:{}",deleteError.objectName(),e);
            }
        });
    }

    /**
     * @description 从MinIo中下载文件
     * @param fileInputStream 下载文件的输入流
     * @return
     * @throws IOException
     */
    private static File downloadFileFromMinIo(InputStream fileInputStream) throws IOException {
        File minioTempFile = File.createTempFile("tempFile",".merge");
        FileOutputStream fileOutputStream = new FileOutputStream(minioTempFile);
        IOUtils.copy(fileInputStream,fileOutputStream);
        return minioTempFile;
    }

    /**
     * @description 获取分块文件的Minio的路径
     * @param fileMd5 文件的md5值
     * @param chunkIndex 分块文件的块号
     * @return
     */
    private String getChunkFileObjectName(String fileMd5, int chunkIndex) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + "chunk" + "/" + chunkIndex;
    }

    /**
     * @description  获取合并文件的Minio的路径
     * @param fileMd5 文件的md5值
     * @param extension 文件的后缀名
     * @return
     */
    private String getMergeFileObjectName(String fileMd5, String extension) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + fileMd5 + extension;
    }


    /**
     * @description 插入文件信息到数据库中
     * @param companyId 机构id
     * @param uploadFileParamsDto 上传文件的dto信息
     * @param fileMd5 文件的md5值
     * @param objectName minio中的文件路径
     * @return
     */
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
        // 时间
        mediaFiles.setCreateDate(LocalDateTime.now());

        int insert = mediaFilesMapper.insert(mediaFiles);
        if (insert <= 0) {
            log.error("文件数据插入数据库失败，url{}", "/" + url);
        }
        return mediaFiles;
    }

    /**
     * @description 上传文件到minio
     * @param localFilePath 文件的本地路径
     * @param mimeType 文件后缀类型
     * @param objectName minio中的文件路径
    */
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

    /**
     * @description 获取文件的mediaType
     * @param extension 文件后缀
     * @return
     */
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

    /**
     * @description 获取文件的MD5值
     * @param file 文件
     * @return
     */
    private String getFileMd5(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            String fileMd5 = DigestUtils.md5Hex(fileInputStream);
            return fileMd5;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @description 获取文件子目录
     * @return
     */
    private String getObjectFolder() {
        // 指定时间格式
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String folder = dateFormat.format(new Date()).replace("-", "/") + "/";
        return folder;
    }
}
