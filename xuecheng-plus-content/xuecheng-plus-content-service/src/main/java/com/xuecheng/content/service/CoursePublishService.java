package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.po.CoursePublish;

public interface CoursePublishService {
    public CoursePreviewDto getCoursePreviewInfo(Long courseId);
    public void commitAudit(Long companyId, Long courseId);
    public void publish(Long companyId,Long courseId);
    public void test();
    public CoursePublish getCoursepublish(Long courseId);
}
