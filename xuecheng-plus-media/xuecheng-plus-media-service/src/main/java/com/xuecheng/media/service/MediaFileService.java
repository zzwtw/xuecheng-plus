package com.xuecheng.media.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description 媒资文件管理业务类
 * @date 2022/9/10 8:55
 */
public interface MediaFileService {

    /**
     * @param pageParams          分页参数
     * @param queryMediaParamsDto 查询条件
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
     * @description 媒资文件查询方法
     * @author Mr.M
     * @date 2022/9/10 8:57
     */
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath);

    public RestResponse<Boolean> checkFile(String fileMd5);

    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex);

    public RestResponse uploadChunk(MultipartFile file, String fileMd5, int chunkIndex) throws IOException;

    public RestResponse mergeChunks(Long companyId, UploadFileParamsDto uploadFileParamsDto, String fileMd5, int chunkTotal);

    /**
     * @description 添加视频处理任务信息
     */
    public void saveVideoTaskDetail(MediaFiles mediaFiles);

    /**
     * @descrition 视频任务处理完成之后，更新任务处理状态
     */
    public void updateVideoInfoAfterDealTask(Long taskId, String status, String errormsg);

    /**
     * @description 获取该处理器的待处理任务表
     */
    public List<MediaProcess> getVideoTaskList(int shardIndex, int shardTotal, int count);

    /**
     * @description 用于分布式锁，如果更新成功，则获取到这个任务，若没有更新成功则没有拿到任务
     */
    public boolean getMediaProcessDistributedLockByDataBase(Long taskId);

    public File downloadFileFromMinIo(String fileMd5, String extension) throws Exception;

    public String getMimeType(String extension);

    public void upLoadFile2MinIo(String localFilePath, String mimeType, String objectName, String bucketName);
    public String getMergeFileObjectName(String fileMd5, String extension);
}

