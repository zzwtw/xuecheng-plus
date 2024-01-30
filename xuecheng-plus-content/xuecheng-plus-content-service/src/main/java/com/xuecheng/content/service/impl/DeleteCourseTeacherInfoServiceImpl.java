package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.dto.CourseTeacherDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.DeleteCourseTeacherInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DeleteCourseTeacherInfoServiceImpl implements DeleteCourseTeacherInfoService {
    @Autowired
    private CourseBaseMapper courseBaseMapper;
    @Autowired
    private CourseTeacherMapper courseTeacherMapper;

    /**
     * @description 删除一个课程的教师信息
     * @param companyId 机构id
     * @param courseId 课程id
     * @param id 教师id
     */
    @Override
    public void deleteCourseTeacherInfo(Long companyId, Long courseId, Long id) {
        // 判断是否是本机构
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseBase::getId, courseId);
        CourseBase courseBase = courseBaseMapper.selectOne(queryWrapper);
        if (companyId.equals(courseBase.getCompanyId())) {
            // 如果是本机构进行删除
            courseTeacherMapper.deleteById(id);
        } else {
            XueChengPlusException.cast("非本课程机构进行删除！");
        }

    }
}

