package com.xuecheng.media.service.jobhandler;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.impl.MediaFileServiceImpl;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * XxlJob开发示例（Bean模式）
 *
 * 开发步骤：
 *      1、任务开发：在Spring Bean实例中，开发Job方法；
 *      2、注解配置：为Job方法添加注解 "@XxlJob(value="自定义jobhandler名称", init = "JobHandler初始化方法", destroy = "JobHandler销毁方法")"，注解value值对应的是调度中心新建任务的JobHandler属性的值。
 *      3、执行日志：需要通过 "XxlJobHelper.log" 打印执行日志；
 *      4、任务结果：默认任务结果为 "成功" 状态，不需要主动设置；如有诉求，比如设置任务结果为失败，可以通过 "XxlJobHelper.handleFail/handleSuccess" 自主设置任务结果；
 *
 * @author xuxueli 2019-12-11 21:52:51
 */
@Component
public class SampleXxlJob {
    private static Logger logger = LoggerFactory.getLogger(SampleXxlJob.class);


    /**
     * 1、简单任务示例（Bean模式）
     */
    @XxlJob("demoJobHandler")
    public void demoJobHandler() throws Exception {
//        XxlJobHelper.log("XXL-JOB, Hello World.");
//
//        for (int i = 0; i < 5; i++) {
//            XxlJobHelper.log("beat at:" + i);
//            TimeUnit.SECONDS.sleep(2);
//        }

        logger.error("执行器开始执行调度任务");
        // default success
    }
    @Autowired
    private MediaFileServiceImpl mediaFileService;

    @Value("${minio.bucket.videofiles}")
    private String videoFilesBucket;

    @XxlJob("videoJobHandler")
    public void videoJobHandler() throws Exception {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        logger.error("shardIndex{},shardTotal{}", shardIndex, shardTotal);
        // 获取cpu最大核心数，也就是线程的最大并行数
        int size = Runtime.getRuntime().availableProcessors();
        // 获取任务列表
        List<MediaProcess> videoTaskList = mediaFileService.getVideoTaskList(shardIndex, shardTotal, size);
        // 构建线程池
        ExecutorService threadPool = Executors.newFixedThreadPool(size);
        // 计数器
        CountDownLatch countDownLatch = new CountDownLatch(videoTaskList.size());

        // 执行任务列表
        videoTaskList.forEach(item -> {
            threadPool.execute(() -> {
                try {
                    // 根据任务id抢占任务
                    Long id = item.getId();
                    boolean b = mediaFileService.getMediaProcessDistributedLockByDataBase(id);
                    // 没抢到锁
                    if (!b) {
                        return;
                    }
                    // 启动任务
                    //1.获取视频，从minio上下载视频
                    // 获取fileMd5
                    String fileMd5 = item.getFileId();
                    // 获取文件拓展名
                    String extension = item.getFilename().substring(item.getFilename().lastIndexOf("."));
                    if (!extension.equals(".avi")) {
                        updateVideoInfo("文件拓展名不是avi!", item, "3");
                        return;
                    }
                    File file = null;
                    try {
                        file = mediaFileService.downloadFileFromMinIo(fileMd5, extension);
                    } catch (Exception e) {
                        updateVideoInfo("视频任务处理时，视频文件从minio下载失败", item, "3");
                        return;
                    }
                    //2.如果是avi视频，则对视频进行转码处理
                    //ffmpeg的路径
                    String ffmpeg_path = "C:\\Users\\1\\ffmpeg\\ffmpeg.exe";//ffmpeg的安装位置
                    //源avi视频的路径
                    String video_path = file.getAbsolutePath();
                    // 创建临时文件来装转码后的视频
                    File tempMp4File = null;
                    try {
                        tempMp4File = File.createTempFile("mp4", ".mp4");
                    } catch (IOException e) {
                        updateVideoInfo("创建临时文件失败", item, "3");
                        return;
                    }
                    //创建工具类对象
                    Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpeg_path, video_path, tempMp4File.getName(), tempMp4File.getAbsolutePath());
                    //开始视频转换，成功将返回success
                    String s = videoUtil.generateMp4();
                    if (!s.equals("success")) {
                        updateVideoInfo("视频转换失败", item, "3");
                        return;
                    }
                    //3.处理完成之后，上传视频，更新数据库
                    // 文件绝对路径
                    String localFilePath = tempMp4File.getAbsolutePath();
                    // 后缀名mimeType
                    String mimeType = mediaFileService.getMimeType(".mp4");
                    // objectName
                    String objectName = mediaFileService.getMergeFileObjectName(fileMd5, ".mp4");
                    mediaFileService.upLoadFile2MinIo(localFilePath, mimeType, objectName, videoFilesBucket);
                    updateVideoInfo(null, item, "2");
                }catch (Exception e){
                    logger.error("VideoHandler出现问题{}",e.getMessage());
                }
                finally {
                    countDownLatch.countDown();
                }

            });
        });
        countDownLatch.await();

    }

    private void updateVideoInfo(String errmsg, MediaProcess item, String status) {
        logger.error(errmsg);
        mediaFileService.updateVideoInfoAfterDealTask(item.getId(), status, errmsg);
    }

    /**
     * 2、分片广播任务
     */
    @XxlJob("shardingJobHandler")
    public void shardingJobHandler() throws Exception {

        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        logger.error("shardIndex{},shardTotal{}",shardIndex,shardTotal);
        XxlJobHelper.log("分片参数：当前分片序号 = {}, 总分片数 = {}", shardIndex, shardTotal);

        // 业务逻辑
        for (int i = 0; i < shardTotal; i++) {
            if (i == shardIndex) {
                XxlJobHelper.log("第 {} 片, 命中分片开始处理", i);
            } else {
                XxlJobHelper.log("第 {} 片, 忽略", i);
            }
        }

    }


    /**
     * 3、命令行任务
     */
    @XxlJob("commandJobHandler")
    public void commandJobHandler() throws Exception {
        String command = XxlJobHelper.getJobParam();
        int exitValue = -1;

        BufferedReader bufferedReader = null;
        try {
            // command process
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command(command);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();
            //Process process = Runtime.getRuntime().exec(command);

            BufferedInputStream bufferedInputStream = new BufferedInputStream(process.getInputStream());
            bufferedReader = new BufferedReader(new InputStreamReader(bufferedInputStream));

            // command log
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                XxlJobHelper.log(line);
            }

            // command exit
            process.waitFor();
            exitValue = process.exitValue();
        } catch (Exception e) {
            XxlJobHelper.log(e);
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }

        if (exitValue == 0) {
            // default success
        } else {
            XxlJobHelper.handleFail("command exit value("+exitValue+") is failed");
        }

    }


    /**
     * 4、跨平台Http任务
     *  参数示例：
     *      "url: http://www.baidu.com\n" +
     *      "method: get\n" +
     *      "data: content\n";
     */
    @XxlJob("httpJobHandler")
    public void httpJobHandler() throws Exception {

        // param parse
        String param = XxlJobHelper.getJobParam();
        if (param==null || param.trim().length()==0) {
            XxlJobHelper.log("param["+ param +"] invalid.");

            XxlJobHelper.handleFail();
            return;
        }

        String[] httpParams = param.split("\n");
        String url = null;
        String method = null;
        String data = null;
        for (String httpParam: httpParams) {
            if (httpParam.startsWith("url:")) {
                url = httpParam.substring(httpParam.indexOf("url:") + 4).trim();
            }
            if (httpParam.startsWith("method:")) {
                method = httpParam.substring(httpParam.indexOf("method:") + 7).trim().toUpperCase();
            }
            if (httpParam.startsWith("data:")) {
                data = httpParam.substring(httpParam.indexOf("data:") + 5).trim();
            }
        }

        // param valid
        if (url==null || url.trim().length()==0) {
            XxlJobHelper.log("url["+ url +"] invalid.");

            XxlJobHelper.handleFail();
            return;
        }
        if (method==null || !Arrays.asList("GET", "POST").contains(method)) {
            XxlJobHelper.log("method["+ method +"] invalid.");

            XxlJobHelper.handleFail();
            return;
        }
        boolean isPostMethod = method.equals("POST");

        // request
        HttpURLConnection connection = null;
        BufferedReader bufferedReader = null;
        try {
            // connection
            URL realUrl = new URL(url);
            connection = (HttpURLConnection) realUrl.openConnection();

            // connection setting
            connection.setRequestMethod(method);
            connection.setDoOutput(isPostMethod);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setReadTimeout(5 * 1000);
            connection.setConnectTimeout(3 * 1000);
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.setRequestProperty("Accept-Charset", "application/json;charset=UTF-8");

            // do connection
            connection.connect();

            // data
            if (isPostMethod && data!=null && data.trim().length()>0) {
                DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
                dataOutputStream.write(data.getBytes("UTF-8"));
                dataOutputStream.flush();
                dataOutputStream.close();
            }

            // valid StatusCode
            int statusCode = connection.getResponseCode();
            if (statusCode != 200) {
                throw new RuntimeException("Http Request StatusCode(" + statusCode + ") Invalid.");
            }

            // result
            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
            }
            String responseMsg = result.toString();

            XxlJobHelper.log(responseMsg);

            return;
        } catch (Exception e) {
            XxlJobHelper.log(e);

            XxlJobHelper.handleFail();
            return;
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (Exception e2) {
                XxlJobHelper.log(e2);
            }
        }

    }

    /**
     * 5、生命周期任务示例：任务初始化与销毁时，支持自定义相关逻辑；
     */
    @XxlJob(value = "demoJobHandler2", init = "init", destroy = "destroy")
    public void demoJobHandler2() throws Exception {
        XxlJobHelper.log("XXL-JOB, Hello World.");
    }
    public void init(){
        logger.info("init");
    }
    public void destroy(){
        logger.info("destroy");
    }


}
