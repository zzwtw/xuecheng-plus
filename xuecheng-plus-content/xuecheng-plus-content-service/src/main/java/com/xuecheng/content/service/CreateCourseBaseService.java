package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;

public interface CreateCourseBaseService {
    CourseBaseInfoDto createCourseBase(Long companyId,AddCourseDto addCourseDto);
    CourseBaseInfoDto getCourseBaseInfo(Long courseId);
}
