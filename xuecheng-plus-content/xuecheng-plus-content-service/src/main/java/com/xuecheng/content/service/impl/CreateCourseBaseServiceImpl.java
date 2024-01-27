package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CreateCourseBaseService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CreateCourseBaseServiceImpl implements CreateCourseBaseService {
    @Autowired
    private CourseBaseMapper courseBaseMapper;
    @Autowired
    private CourseMarketMapper courseMarketMapper;

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    /**
     * 插入新增的课程
     * @param companyId 机构id
     * @param dto 接收前端传来的数据的对象
     * @return
     */

    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto) {
        // 合法性校验
        if (StringUtils.isBlank(dto.getName())) {
            throw new XueChengPlusException("课程名称不能为空");
        }

        if (StringUtils.isBlank(dto.getMt())) {
            throw new RuntimeException("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getSt())) {
            throw new RuntimeException("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getGrade())) {
            throw new RuntimeException("课程等级为空");
        }

        if (StringUtils.isBlank(dto.getTeachmode())) {
            throw new RuntimeException("教育模式为空");
        }

        if (StringUtils.isBlank(dto.getUsers())) {
            throw new RuntimeException("适应人群为空");
        }

        if (StringUtils.isBlank(dto.getCharge())) {
            throw new RuntimeException("收费规则为空");
        }
        // 插入CourseBase
        CourseBase courseBaseNew = new CourseBase();
        BeanUtils.copyProperties(dto, courseBaseNew);
        // 设置审核状态
        courseBaseNew.setAuditStatus("202002");
        // 设置发布状态
        courseBaseNew.setStatus("203001");
        // 机构id
        courseBaseNew.setCompanyId(companyId);
        // 添加时间
        courseBaseNew.setCreateDate(LocalDateTime.now());
        // 插入课程基本信息表
        int insert = courseBaseMapper.insert(courseBaseNew);
        if (insert <= 0) {
            throw new RuntimeException("新增课程基本信息失败");
        }
        // 向课程营销表保存课程营销信息
        CourseMarket courseMarketNew = new CourseMarket();
        long courseId = courseBaseNew.getId();
        System.out.println(courseId);
        BeanUtils.copyProperties(dto, courseMarketNew);
        courseMarketNew.setId(courseId);
        int i = saveCourseMarket(courseMarketNew);
        if (i <= 0) {
            throw new RuntimeException("课程营销信息插入失败");
        }
        return getCourseBaseInfo(courseId);
    }

    private int saveCourseMarket(CourseMarket courseMarket) {
        String marketCharge = courseMarket.getCharge();
        if (StringUtils.isBlank(marketCharge)) {
            throw new RuntimeException("收费规则没有选择");
        }
        if (marketCharge.equals("201001")) {
            if (courseMarket.getPrice() == null || courseMarket.getPrice() <= 0) {
                throw new XueChengPlusException("课程价格不能为空或不能小于等于0");
            }
        }
        // 判断是插入还是更新
        CourseMarket courseMarketObj = courseMarketMapper.selectById(courseMarket.getId());
        if (courseMarketObj == null) {
            return courseMarketMapper.insert(courseMarket);
        } else {
            BeanUtils.copyProperties(courseMarket, courseMarketObj);
            courseMarketObj.setId(courseMarket.getId());
            return courseMarketMapper.updateById(courseMarketObj);

        }
    }

    public CourseBaseInfoDto getCourseBaseInfo(Long courseId) {
        //课程基本表中查询
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null) {
            return null;
        }

        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        if (courseMarket == null) {
            return null;
        }

        // 整合到一个返回对象中
        CourseBaseInfoDto courseBaseInfoDtoResult = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, courseBaseInfoDtoResult); // 只要属性名相同就可以copy
        BeanUtils.copyProperties(courseMarket, courseBaseInfoDtoResult);

        // 查询 mtName,stName
        CourseCategory courseCategoryByMt = courseCategoryMapper.selectById(courseBase.getMt());
        String mtName = courseCategoryByMt.getName();
        courseBaseInfoDtoResult.setMtName(mtName);
        CourseCategory courseCategoryBySt = courseCategoryMapper.selectById(courseBase.getSt());
        String stName = courseCategoryBySt.getName();
        courseBaseInfoDtoResult.setStName(stName);
        return courseBaseInfoDtoResult;

    }
}
