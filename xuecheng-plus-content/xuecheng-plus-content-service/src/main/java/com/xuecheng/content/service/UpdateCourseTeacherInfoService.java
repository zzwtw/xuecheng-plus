package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CourseTeacherDto;

public interface UpdateCourseTeacherInfoService {
    CourseTeacherDto updateCourseTeacherInfo(Long companyId, CourseTeacherDto courseTeacherDto);
}
