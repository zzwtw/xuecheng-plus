package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.CourseTeacherSaveParams;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.dto.CourseTeacherDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.SaveCourseTeacherInfoService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SaveCourseTeacherServiceImpl implements SaveCourseTeacherInfoService {
    @Autowired
    private CourseTeacherMapper courseTeacherMapper;

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    /**
     * @description
     * @param companyId 机构id
     * @param courseTeacherSaveParams 前端传来的需要保存的课程教师信息，包含了课程id
     * @return 返回课程添加的课程教师信息
     */
    @Override
    public CourseTeacherDto saveCourseTeacherInfo(Long companyId,CourseTeacherSaveParams courseTeacherSaveParams) {
        // 判断是否是本机构
        Long courseId = courseTeacherSaveParams.getCourseId();
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseBase::getId,courseId);
        CourseBase courseBase = courseBaseMapper.selectOne(queryWrapper);
        if (companyId.equals(courseBase.getCompanyId())){
            // 如果是本机构进行修改
            // 插入教师信息
            CourseTeacher courseTeacherNew = new CourseTeacher();
            BeanUtils.copyProperties(courseTeacherSaveParams,courseTeacherNew);
            courseTeacherNew.setCreateDate(LocalDateTime.now());
            int insert = courseTeacherMapper.insert(courseTeacherNew);
            if (insert<=0){
                XueChengPlusException.cast("课程教师信息插入失败");
            }
            // 插入成功后获取教师的id，进行查询
            Long teacherNewId = courseTeacherNew.getId();
            CourseTeacher courseTeacher = courseTeacherMapper.selectById(teacherNewId);
            CourseTeacherDto courseTeacherDtoResult = new CourseTeacherDto();
            BeanUtils.copyProperties(courseTeacher,courseTeacherDtoResult);
            return courseTeacherDtoResult;
        }
        XueChengPlusException.cast("非本课程机构进行添加！");
        return null;
    }
}
