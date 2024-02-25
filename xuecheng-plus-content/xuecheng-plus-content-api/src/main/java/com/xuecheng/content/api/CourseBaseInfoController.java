package com.xuecheng.content.api;

import com.xuecheng.base.exception.ValidationGroups;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CreateCourseBaseService;
import com.xuecheng.content.utils.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 课程信息编辑接口
 */
@Api(value = "课程信息编辑接口",tags = "课程信息编辑接口")
@RestController
public class CourseBaseInfoController {
    @Autowired
    private CourseBaseInfoService courseBaseInfoService;
    @Autowired
    private CreateCourseBaseService createCourseBaseService;
    @ApiOperation("课程查询接口")
    @PreAuthorize("hasAuthority('xc_teachmanager_course_list')")
    @RequestMapping("/course/list")
    public PageResult<CourseBase> list(
            PageParams pageParams,
            @RequestBody(required = false) QueryCourseParamsDto queryCourseParamsDto
    ){
        PageResult<CourseBase> courseBasePageResult = courseBaseInfoService.queryCourseBaseList(pageParams, queryCourseParamsDto);
        return courseBasePageResult;
    }

    @ApiOperation("新增课程基础信息")
    @PostMapping("/course")
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated(ValidationGroups.Insert.class) AddCourseDto addCourseDto){
        Long companyId = 1232141425L;
        return createCourseBaseService.createCourseBase(companyId, addCourseDto);
    }

    @ApiOperation("根据id查询课程信息")
    @GetMapping("/course/{courseId}")
    public CourseBaseInfoDto getCourseBaseById(@PathVariable Long courseId){
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        return createCourseBaseService.getCourseBaseInfo(courseId);
    }

    @ApiOperation("更新课程信息")
    @PutMapping("/course")
    public CourseBaseInfoDto updateCourseBaseInfo(@RequestBody EditCourseDto editCourseDto){
        Long companyId = 1232141425L;
        CourseBaseInfoDto courseBaseInfoDto = courseBaseInfoService.updateCourseBaseInfo(companyId, editCourseDto);
        return courseBaseInfoDto;
    }

    @ApiOperation("删除课程信息")
    @DeleteMapping("/course/{id}")
    public void deleteCourseBaseInfo(@PathVariable Long id){
        Long companyId = 1232141425L;
        courseBaseInfoService.deleteCourseBaseInfo(companyId,id);
    }
}
