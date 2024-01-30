package com.xuecheng.base.model;

import lombok.Data;
import lombok.ToString;

/**
 * 新增课程教师信息前端传来的参数
 */
@Data
@ToString
public class CourseTeacherSaveParams {
    private Long courseId;
    private String teacherName;
    private String position;
    private String introduction;
}
