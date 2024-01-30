package com.xuecheng.content.service;

import com.xuecheng.base.model.CourseTeacherSaveParams;
import com.xuecheng.content.model.dto.CourseTeacherDto;

public interface SaveCourseTeacherInfoService {
    CourseTeacherDto saveCourseTeacherInfo(Long companyId, CourseTeacherSaveParams courseTeacherSaveParams);
}
