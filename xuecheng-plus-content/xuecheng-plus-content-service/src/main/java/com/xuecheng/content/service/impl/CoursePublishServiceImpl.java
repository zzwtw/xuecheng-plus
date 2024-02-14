package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.FeignClient.MediaServiceClient;
import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.mapper.CoursePublishPreMapper;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.model.po.CoursePublishPre;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.CreateCourseBaseService;
import com.xuecheng.content.service.TeachPlanService;
import com.xuecheng.messagesdk.mapper.MqMessageMapper;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.aspectj.lang.annotation.AfterThrowing;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class CoursePublishServiceImpl implements CoursePublishService {
    @Autowired
    private CourseBaseMapper courseBaseMapper;
    @Autowired
    private CourseMarketMapper courseMarketMapper;
    @Autowired
    private TeachPlanService teachPlanService;
    @Autowired
    private CreateCourseBaseService createCourseBaseService;

    @Autowired
    private CoursePublishMapper coursePublishMapper;
    @Autowired
    private CoursePublishPreMapper coursePublishPreMapper;

    @Autowired
    private MqMessageService mqMessageService;

    @Autowired
    private MediaServiceClient mediaServiceClient;

    /**
     * @param courseId 课程id
     * @return CoursePreviewDto 课程预览DTO
     * @description 查询课程的基本信息+营销信息+课程计划，返回给前端视频预览模板页面
     */
    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        // 查询课程基本信息与课程营销信息
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        // 查询课程计划
        List<TeachPlanDto> teachPlanDtoList = teachPlanService.findTeachPlanTree(courseId);


        coursePreviewDto.setCourseBase(courseBaseInfoDto);
        coursePreviewDto.setTeachplans(teachPlanDtoList);

        return coursePreviewDto;
    }

    /**
     * @description 提交课程审核
     * @param companyId 机构id
     * @param courseId 课程id
     */
    @Transactional // 事务控制
    @Override
    public void commitAudit(Long companyId, Long courseId) {
        // 提交课程审核
        // 查询课程基本信息
        CourseBaseInfoDto courseBaseInfo = createCourseBaseService.getCourseBaseInfo(courseId);
        if (courseBaseInfo == null) {
            XueChengPlusException.cast("该课程不存在");
        }
        // 检查是否是本机构
        if (!companyId.equals(courseBaseInfo.getCompanyId())) {
            XueChengPlusException.cast("非本机构不允许提交");
        }
        // 检查图片是否填写
        String pic = courseBaseInfo.getPic();
        System.out.println(pic);
        if (Objects.equals(pic, "")) {
            XueChengPlusException.cast("提交失败，请上传图片");
        }
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        BeanUtils.copyProperties(courseBaseInfo, coursePublishPre);
        // 设置id
        coursePublishPre.setId(courseId);
        // 查询课程营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        String courseMarketJson = JSON.toJSONString(courseMarket);
        coursePublishPre.setMarket(courseMarketJson);
        // 查询课程计划
        List<TeachPlanDto> teachPlanTree = teachPlanService.findTeachPlanTree(courseId);
        // 检查是否有课程计划
        if (teachPlanTree.size() == 0) {
            XueChengPlusException.cast("该课程的课程计划为空，请设置课程计划");
        }
        String teachPlanTreeJson = JSON.toJSONString(teachPlanTree);
        coursePublishPre.setTeachplan(teachPlanTreeJson);
        // 提交时间
        coursePublishPre.setCreateDate(LocalDateTime.now());
        // 提交状态
        coursePublishPre.setStatus("202003");
        // 插入课程预发布表
        CoursePublishPre coursePublishPre1 = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre1 != null) {
            coursePublishPreMapper.updateById(coursePublishPre);
        } else {
            coursePublishPreMapper.insert(coursePublishPre);
        }
        // 同步更新课程基本信息表的提交状态
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setAuditStatus("202003"); // 已提交
        courseBaseMapper.updateById(courseBase);
    }

    /**
     * @param companyId 机构id
     * @param courseId  课程id
     * @description 发布课程，发布课程之后，任务调度方法扫描mq_message表，获取任务列表，执行静态化，插入es，缓存redis
     */
    @Transactional
    @Override
    public void publish(Long companyId, Long courseId) {
        // 插入course_publish表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre == null) {
            XueChengPlusException.cast("课程预发布信息为空");
        }
        // 校验是否是本机构
        if (!Objects.equals(companyId, coursePublishPre.getCompanyId())) {
            XueChengPlusException.cast("非本机构不能操作");
        }
        // 状态不为审核通过无法进行发布
        if (!coursePublishPre.getStatus().equals("202004")) {
            XueChengPlusException.cast("审核通过后才能进行发布");
        }
        // 插入课程发布表
        saveCoursePublish(courseId, coursePublishPre);
        // 向mq_message消息表插入一条消息,消息类型为course_publish
        saveCoursePublishMessage(courseId);
        // 删除课程预发布表
        coursePublishPreMapper.deleteById(courseId);
    }

    /**
     * @param courseId         课程id
     * @param coursePublishPre 课程预发布表信息
     * @description 保存课程发布信息
     */
    private void saveCoursePublish(Long courseId, CoursePublishPre coursePublishPre) {
        CoursePublish coursePublishNew = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre, coursePublishNew);
        // 更新发布状态为已发布
        coursePublishNew.setStatus("203002");
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setStatus("203002");
        courseBaseMapper.updateById(courseBase);
        // 如果course_publish表中已经存在，则更新
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        if (coursePublish == null) {
            int insert = coursePublishMapper.insert(coursePublishNew);
            if (insert <= 0) {
                XueChengPlusException.cast("插入course_publish失败");
            }
        } else {
            int update = coursePublishMapper.updateById(coursePublishNew);
            if (update <= 0) {
                XueChengPlusException.cast("更新course_publish失败");
            }
        }
    }

    /**
     * @param courseId 课程id
     * @description 保存消息表记录
     */
    private void saveCoursePublishMessage(Long courseId) {
        MqMessage mqMessage = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if (mqMessage == null){
            XueChengPlusException.cast(CommonError.UNKOWN_ERROR);
        }


    }

    public void test() {
        File file = new File("C:\\Users\\1\\Desktop\\video\\1.html");
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        String objectName = "course/1.html";
        mediaServiceClient.uploadFile(multipartFile,objectName);
    }

}
