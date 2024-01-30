package com.xuecheng.content.api;

import com.xuecheng.base.model.CourseTeacherSaveParams;
import com.xuecheng.content.model.dto.CourseTeacherDto;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.DeleteCourseTeacherInfoService;
import com.xuecheng.content.service.GetCourseTeacherService;
import com.xuecheng.content.service.SaveCourseTeacherInfoService;
import com.xuecheng.content.service.UpdateCourseTeacherInfoService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class CourseTeacherController {
    @Autowired
    private GetCourseTeacherService getCourseTeacherService;

    @Autowired
    private SaveCourseTeacherInfoService saveCourseTeacherInfoService;

    @Autowired
    private UpdateCourseTeacherInfoService updateCourseTeacherInfoService;

    @Autowired
    private DeleteCourseTeacherInfoService deleteCourseTeacherInfoService;
    @GetMapping("/courseTeacher/list/{id}")
    @ApiOperation("查询课程老师信息")
    public List<CourseTeacherDto> getCourseTeacherList(@PathVariable Long id){
        return getCourseTeacherService.getCourseTeacherById(id);
    }

    @PostMapping("/courseTeacher")
    @ApiOperation("添加课程老师信息")
    public CourseTeacherDto saveCourseTeacherInfo(@RequestBody CourseTeacherSaveParams courseTeacherSaveParams){
        Long companyId = 1232141425L;
        return saveCourseTeacherInfoService.saveCourseTeacherInfo(companyId,courseTeacherSaveParams);
    }

    @PutMapping("/courseTeacher")
    @ApiOperation("修改课程老师信息")
    public CourseTeacherDto updateCourseTeacherInfo(@RequestBody CourseTeacherDto courseTeacherDto){
        Long companyId = 1232141425L;
        return updateCourseTeacherInfoService.updateCourseTeacherInfo(companyId,courseTeacherDto);
    }

    @DeleteMapping ("/courseTeacher/course/{courseId}/{id}")
    @ApiOperation("删除课程老师信息")
    public void deleteCourseTeacherInfo(@PathVariable Long courseId, @PathVariable Long id){
        Long companyId = 1232141425L;
        deleteCourseTeacherInfoService.deleteCourseTeacherInfo(companyId,courseId,id);
    }
}
