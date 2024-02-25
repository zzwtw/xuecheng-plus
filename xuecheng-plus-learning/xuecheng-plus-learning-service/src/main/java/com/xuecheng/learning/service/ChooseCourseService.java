package com.xuecheng.learning.service;

import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;

public interface ChooseCourseService {
    public XcChooseCourseDto addChooseCourse(String userId,Long courseId);
    public XcCourseTablesDto getLearningStatus(String userId, Long courseId);
}
