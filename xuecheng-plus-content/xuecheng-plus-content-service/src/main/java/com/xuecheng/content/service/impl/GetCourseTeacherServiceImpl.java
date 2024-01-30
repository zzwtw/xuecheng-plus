package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.dto.CourseTeacherDto;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.GetCourseTeacherService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GetCourseTeacherServiceImpl implements GetCourseTeacherService {
    @Autowired
    private CourseTeacherMapper courseTeacherMapper;

    /**
     * @description 通过课程id获取课程教师信息
     * @param id 课程id
     * @return 以列表的形式返回查询到的对应的课程的教师信息
     */
    @Override
    public List<CourseTeacherDto> getCourseTeacherById(Long id) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId,id);
        List<CourseTeacher> courseTeachers = courseTeacherMapper.selectList(queryWrapper);
        // 如果已经有了教师信息
        if (courseTeachers != null){
            List<CourseTeacherDto> courseTeacherDtoList = new ArrayList<>();
            for (CourseTeacher courseTeacher : courseTeachers){
                CourseTeacherDto courseTeacherDto = new CourseTeacherDto();
                BeanUtils.copyProperties(courseTeacher,courseTeacherDto);
                courseTeacherDtoList.add(courseTeacherDto);
            }
            return courseTeacherDtoList;
        }
        // 如果还没有教师信息
        return null;
    }
}
