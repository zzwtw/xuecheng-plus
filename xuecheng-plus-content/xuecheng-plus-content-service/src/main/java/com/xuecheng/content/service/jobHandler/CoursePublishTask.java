package com.xuecheng.content.service.jobHandler;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.FeignClient.MediaServiceClient;
import com.xuecheng.content.FeignClient.SearchServiceClient;
import com.xuecheng.content.FeignClient.po.CourseIndex;
import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class CoursePublishTask extends MessageProcessAbstract {
    @Autowired
    private CoursePublishService coursePublishService;
    @Autowired
    private MediaServiceClient mediaServiceClient;
    @Autowired
    private SearchServiceClient searchServiceClient;
    @Autowired
    private CoursePublishMapper coursePublishMapper;

    /**
     * @throws Exception
     * @description 分布式事务任务调度入口
     */
    //任务调度入口
    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() throws Exception {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.debug("shardIndex=" + shardIndex + ",shardTotal=" + shardTotal);
        //参数:分片序号、分片总数、消息类型、一次最多取到的任务数量、一次任务调度执行的超时时间
        process(shardIndex, shardTotal, "course_publish", 30, 60);
    }

    /**
     * @param mqMessage 执行任务内容
     * @return
     * @description 重写的任务执行方法
     */
    @Override
    public boolean execute(MqMessage mqMessage) {
        // 获取消息相关的业务信息
        String businessKey1 = mqMessage.getBusinessKey1();
        Long courseId = Long.parseLong(businessKey1);
        // 课程静态化
        generateCourseHtml(mqMessage, courseId);
        // 插入es
        saveCourseIndex(mqMessage, courseId);
        // 插入redis，缓存
        saveCourseCache(mqMessage, courseId);
        return false;
    }

    /**
     * @param mqMessage mq信息表
     * @param courseId  课程id
     * @description 课程页面静态化并且上传至minio
     */
    private void generateCourseHtml(MqMessage mqMessage, Long courseId) {
        Long id = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        int stageOne = mqMessageService.getStageOne(id);
        if (stageOne > 0) {
            log.debug("stageOne=1，课程静态化已经处理");
            return;
        }
        // 静态化处理
        // 静态化一个页面
        File tempFile = null;
        try {
            Configuration configuration = new Configuration(Configuration.getVersion());
            // 加载模板
            String classPath = this.getClass().getResource("/").getPath();
            configuration.setDirectoryForTemplateLoading(new File(classPath + "/templates/"));
            // 设置字符编码
            configuration.setDefaultEncoding("utf-8");
            // 指定模板文件名称
            Template template = configuration.getTemplate("course_template.ftl");
            // 准备数据
            CoursePreviewDto coursePreviewDto = coursePublishService.getCoursePreviewInfo(courseId);
            Map<Object, Object> map = new HashMap<>();
            map.put("model", coursePreviewDto);
            // 静态化
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
            // 将静态化内容输出到文件中
            InputStream inputStream = IOUtils.toInputStream(content);
            // 输出流
            tempFile = File.createTempFile("static", ".html");
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            IOUtils.copy(inputStream, outputStream);
        } catch (Exception e) {
            log.debug("页面静态化异常{}", e.getMessage());
            XueChengPlusException.cast("页面静态化异常");
        }
        // 静态化之后，上传至minio
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(tempFile);
        String objectName = "course/" + courseId + ".html";
        mediaServiceClient.uploadFile(multipartFile, objectName);
        // 完成静态化，设置StageOne为1
        mqMessageService.completedStageOne(id);
    }

    /**
     * @param mqMessage
     * @param courseId
     * @description 将课程信息缓存至redis
     */
    //
    public void saveCourseCache(MqMessage mqMessage, long courseId) {
        log.debug("将课程信息缓存至redis,课程id:{}", courseId);

    }

    /**
     * @param mqMessage
     * @param courseId  课程id
     * @description 保存课程索引信息
     */
    public void saveCourseIndex(MqMessage mqMessage, long courseId) {
        // 幂等性处理
        MqMessageService mqMessageService = this.getMqMessageService();
        Long id = mqMessage.getId();
        if (mqMessageService.getStageTwo(id) > 0) {
            log.debug("stageTwo=1，课程插入es已经处理");
            return;
        }
        log.debug("保存课程索引信息,课程id:{}", courseId);
        // 保存到es
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        CourseIndex courseIndex = new CourseIndex();
        BeanUtils.copyProperties(coursePublish, courseIndex);
        searchServiceClient.add(courseIndex);
        // 更改mqMessage StageState2
        mqMessageService.completedStageTwo(id);
    }
}
