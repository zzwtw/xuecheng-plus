package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CourseBaseInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    @Autowired
    private CourseBaseMapper courseBaseMapper;
    @Autowired
    private CourseMarketMapper courseMarketMapper;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {
        // 测试查询接口
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();

        //拼接查询条件
        queryWrapper.like(
                StringUtils.isNotBlank(queryCourseParamsDto.getCourseName()),
                CourseBase::getName,
                queryCourseParamsDto.getCourseName()
        );

        queryWrapper.eq(
                StringUtils.isNotBlank(queryCourseParamsDto.getAuditStatus()),
                CourseBase::getAuditStatus,
                queryCourseParamsDto.getAuditStatus()
        );

        queryWrapper.eq(
                StringUtils.isNotBlank(queryCourseParamsDto.getPublishStatus()),
                CourseBase::getStatus,
                queryCourseParamsDto.getPublishStatus()
        );

        // 分页参数
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());

        // 分页查询
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);

        // 数据
        List<CourseBase> items = pageResult.getRecords();

        // 总记录数
        long total = pageResult.getTotal();

        // 准备返回数据
        PageResult<CourseBase> courseBasePageResult = new PageResult<>(items, total, pageParams.getPageNo(), pageParams.getPageSize());

        return  courseBasePageResult;

    }

    @Override
    public CourseBaseInfoDto updateCourseBaseInfo(Long companyId, EditCourseDto editCourseDto) {
        // 校验
        CourseBase courseBase = courseBaseMapper.selectById(editCourseDto.getId());
        if (courseBase == null){
            XueChengPlusException.cast("该课程不存在");
        }

        if(!companyId.equals(courseBase.getCompanyId())){
            XueChengPlusException.cast("不能修改非本机构的课程信息");
        }

        // 将新信息copy到旧信息中
        BeanUtils.copyProperties(editCourseDto,courseBase);
        // 更新修改时间
        courseBase.setChangeDate(LocalDateTime.now());
        // 更新课程基本信息
        courseBaseMapper.updateById(courseBase);
        // 查询课程营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseBase.getId());
        // 将新营销信息copy到旧营销信息中
        BeanUtils.copyProperties(editCourseDto,courseMarket);
        // 更新营销信息
        courseMarketMapper.updateById(courseMarket);
        // 根据id查询课程基本信息与营销信息
        CourseBase courseBaseNew = courseBaseMapper.selectById(courseBase.getId());
        CourseMarket courseMarketNew = courseMarketMapper.selectById(courseMarket.getId());
        // 组合信息
        CourseBaseInfoDto courseBaseInfoDtoResult = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBaseNew,courseBaseInfoDtoResult);
        BeanUtils.copyProperties(courseMarketNew,courseBaseInfoDtoResult);
        return courseBaseInfoDtoResult;
    }
}
