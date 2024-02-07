package com.xuecheng.media.service.jobhandler;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.impl.MediaFileServiceImpl;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Slf4j
@Component
public class VideoXxIJob {
//    @Autowired
//    private MediaFileServiceImpl mediaFileService;
//
//    @Value("${minio.bucket.videofiles}")
//    private String videoFilesBucket;
//
//    @XxlJob("videoJobHandler")
//    public void videoJobHandler() throws Exception {
//        System.out.println("==============================================================================");
//        // 分片参数
//        int shardIndex = XxlJobHelper.getShardIndex();
//        int shardTotal = XxlJobHelper.getShardTotal();
//        log.error("shardIndex{},shardTotal{}", shardIndex, shardTotal);
//        // 获取cpu最大核心数，也就是线程的最大并行数
//        int size = Runtime.getRuntime().availableProcessors();
//        // 获取任务列表
//        List<MediaProcess> videoTaskList = mediaFileService.getVideoTaskList(shardIndex, shardTotal, size);
//        // 构建线程池
//        ExecutorService threadPool = Executors.newFixedThreadPool(size);
//        // 计数器
//        CountDownLatch countDownLatch = new CountDownLatch(size);
//
//        // 执行任务列表
//        videoTaskList.forEach(item -> {
//            threadPool.execute(() -> {
//                try {
//                    // 根据任务id抢占任务
//                    Long id = item.getId();
//                    boolean b = mediaFileService.getMediaProcessDistributedLockByDataBase(id);
//                    // 没抢到锁
//                    if (!b) {
//                        return;
//                    }
//                    // 启动任务
//                    //1.获取视频，从minio上下载视频
//                    // 获取fileMd5
//                    String fileMd5 = item.getFileId();
//                    // 获取文件拓展名
//                    String extension = item.getFilename().substring(item.getFilename().lastIndexOf("."));
//                    if (!extension.equals(".avi")) {
//                        updateVideoInfo("文件拓展名不是avi!", item, "3");
//                        return;
//                    }
//                    File file = null;
//                    try {
//                        file = mediaFileService.downloadFileFromMinIo(fileMd5, extension);
//                    } catch (Exception e) {
//                        updateVideoInfo("视频任务处理时，视频文件从minio下载失败", item, "3");
//                        return;
//                    }
//                    //2.如果是avi视频，则对视频进行转码处理
//                    //ffmpeg的路径
//                    String ffmpeg_path = "C:\\Users\\1\\ffmpeg\\ffmpeg.exe";//ffmpeg的安装位置
//                    //源avi视频的路径
//                    String video_path = file.getAbsolutePath();
//                    // 创建临时文件来装转码后的视频
//                    File tempMp4File = null;
//                    try {
//                        tempMp4File = File.createTempFile("mp4", ".mp4");
//                    } catch (IOException e) {
//                        updateVideoInfo("创建临时文件失败", item, "3");
//                        return;
//                    }
//                    //创建工具类对象
//                    Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpeg_path, video_path, tempMp4File.getName(), tempMp4File.getAbsolutePath());
//                    //开始视频转换，成功将返回success
//                    String s = videoUtil.generateMp4();
//                    if (!s.equals("success")) {
//                        updateVideoInfo("视频转换失败", item, "3");
//                        return;
//                    }
//                    //3.处理完成之后，上传视频，更新数据库
//                    // 文件绝对路径
//                    String localFilePath = tempMp4File.getAbsolutePath();
//                    // 后缀名mimeType
//                    String mimeType = mediaFileService.getMimeType(".mp4");
//                    // objectName
//                    String objectName = mediaFileService.getMergeFileObjectName(fileMd5, ".mp4");
//                    mediaFileService.upLoadFile2MinIo(localFilePath, mimeType, objectName, videoFilesBucket);
//                    updateVideoInfo(null, item, "2");
//                }catch (Exception e){
//                    log.error("VideoHandler出现问题{}",e.getMessage());
//                }
//                finally {
//                    countDownLatch.countDown();
//                }
//
//            });
//        });
//        countDownLatch.await();
//
//    }
//
//    private void updateVideoInfo(String errmsg, MediaProcess item, String status) {
//        log.error(errmsg);
//        mediaFileService.updateVideoInfoAfterDealTask(item.getId(), status, errmsg);
//    }

}
