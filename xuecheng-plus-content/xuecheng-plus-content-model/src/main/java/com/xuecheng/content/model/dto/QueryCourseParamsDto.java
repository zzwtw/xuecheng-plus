package com.xuecheng.content.model.dto;

import lombok.Data;
import lombok.ToString;

/*
* 课程查询参数类，RequestBody所接受的参数需要转换的对象
* */
@Data
@ToString
public class QueryCourseParamsDto {
    // 审核状态
    private String auditStatus;
    // 课程名称
    private String courseName;
    // 发布状态
    private String publishStatus;
}
