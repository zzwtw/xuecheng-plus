package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CourseTeacherDto;

import java.util.List;

public interface GetCourseTeacherService {
    List<CourseTeacherDto> getCourseTeacherById(Long id);
}
