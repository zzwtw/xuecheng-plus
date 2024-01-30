package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.dto.CourseTeacherDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.UpdateCourseTeacherInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UpdateCourseTeacherInfoServiceImpl implements UpdateCourseTeacherInfoService {
    @Autowired
    private CourseTeacherMapper courseTeacherMapper;

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    /**
     * @description 更新课程教师信息
     * @param companyId 机构id
     * @param courseTeacherDto 需要更新的课程教师信息
     * @return 返回更新好的课程教师信息
     */
    @Override
    public CourseTeacherDto updateCourseTeacherInfo(Long companyId, CourseTeacherDto courseTeacherDto) {
        // 判断是否是本机构
        Long courseId = courseTeacherDto.getCourseId();
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseBase::getId,courseId);
        CourseBase courseBase = courseBaseMapper.selectOne(queryWrapper);
        if (companyId.equals(courseBase.getCompanyId())){
            // 如果是本机构进行修改
            // 修改教师信息
            CourseTeacher courseTeacher = courseTeacherMapper.selectById(courseTeacherDto.getId());
            BeanUtils.copyProperties(courseTeacherDto,courseTeacher);
            int update = courseTeacherMapper.updateById(courseTeacher);
            if (update<=0){
                XueChengPlusException.cast("课程教师信息更新失败");
            }
            // 插入成功后获取教师的id，进行查询
            Long teacherNewId = courseTeacher.getId();
            CourseTeacher courseTeacherResult = courseTeacherMapper.selectById(teacherNewId);
            CourseTeacherDto courseTeacherDtoResult = new CourseTeacherDto();
            BeanUtils.copyProperties(courseTeacherResult,courseTeacherDtoResult);
            return courseTeacherDtoResult;
        }
        XueChengPlusException.cast("非本课程机构进行修改！");
        return null;
    }
}
