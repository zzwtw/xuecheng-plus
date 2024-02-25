package com.xuecheng.learning.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.mapper.XcChooseCourseMapper;
import com.xuecheng.learning.mapper.XcCourseTablesMapper;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcChooseCourse;
import com.xuecheng.learning.model.po.XcCourseTables;
import com.xuecheng.learning.service.ChooseCourseService;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChooseCourseServiceImpl implements ChooseCourseService {
    @Autowired
    private ContentServiceClient contentServiceClient;
    @Autowired
    private XcChooseCourseMapper xcChooseCourseMapper;
    @Autowired
    private XcCourseTablesMapper courseTablesMapper;
    @Autowired
    private XcCourseTablesMapper xcCourseTablesMapper;
    /**
     * @description 如果是免费课程，添加到选课记录和课程表
     * @param courseId 课程id
     */
    @Override
    public XcChooseCourseDto addChooseCourse(String userId, Long courseId) {
        // 查询是收费还是免费课程
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        String charge = coursepublish.getCharge();
        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        // 免费课程
        if (charge.equals("201000")){
            // 插入选课记录表
            xcChooseCourse = addFreeCourse(userId, coursepublish);
            // 插入课程表
            addCourseTables(userId, courseId, coursepublish, xcChooseCourse);
        }else {
            // 添加收费课程到选课记录表
            xcChooseCourse = addChargeCourse(userId, coursepublish);
            // 支付之后再添加到课程表
        }
        XcCourseTablesDto learningStatus = getLearningStatus(userId, courseId);
        XcChooseCourseDto xcChooseCourseDto = new XcChooseCourseDto();
        BeanUtils.copyProperties(xcChooseCourse,xcChooseCourseDto);
        xcChooseCourseDto.setLearnStatus(learningStatus.getLearnStatus());
        return xcChooseCourseDto;
    }
    /**
     * @description 判断学习资格
     * @param userId 用户id
     * @param courseId 课程id
     * @return XcCourseTablesDto 学习资格状态 [{"code":"702001","desc":"正常学习"},{"code":"702002","desc":"没有选课或选课后没有支付"},{"code":"702003","desc":"已过期需要申请续期或重新支付"}]
     */
    @Override
    public XcCourseTablesDto getLearningStatus(String userId, Long courseId) {
        XcCourseTables xcCourseTables = getXcCourseTables(userId, courseId);
        // 如果没有查到这门课，说明是收费的课程并且未支付
        if (xcCourseTables == null){
            XcCourseTablesDto xcCourseTablesDto = new XcCourseTablesDto();
            xcCourseTablesDto.setLearnStatus("702002");
            return xcCourseTablesDto;
        }else {
            // 是否过期
            boolean before = xcCourseTables.getValidtimeEnd().isBefore(LocalDateTime.now());
            XcCourseTablesDto xcCourseTablesDto = new XcCourseTablesDto();
            if (!before){
                xcCourseTablesDto.setLearnStatus("702001");
            }else {
                xcCourseTablesDto.setLearnStatus("702003");
            }
            return xcCourseTablesDto;
        }
    }

    @Nullable
    private XcChooseCourse addChargeCourse(String userId, CoursePublish coursepublish) {
        // 判断有没有已经存在的收费课程
        // 如果存在待支付交易记录直接返回
        LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper = queryWrapper.eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getCourseId, coursepublish.getId())
                .eq(XcChooseCourse::getOrderType, "700002")//收费订单
                .eq(XcChooseCourse::getStatus, "701002");//待支付
        List<XcChooseCourse> xcChooseCourses = xcChooseCourseMapper.selectList(queryWrapper);
        if (xcChooseCourses != null && xcChooseCourses.size()>0) {
            return xcChooseCourses.get(0);
        }
        // 插入
        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(coursepublish.getId());
        xcChooseCourse.setCourseName(coursepublish.getName());
        xcChooseCourse.setCoursePrice(coursepublish.getPrice());
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursepublish.getCompanyId());
        xcChooseCourse.setOrderType("700002");//收费课程
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setStatus("701002");//待支付
        xcChooseCourse.setValidDays(coursepublish.getValidDays());
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(coursepublish.getValidDays()));
        xcChooseCourseMapper.insert(xcChooseCourse);
        return xcChooseCourse;
    }

    private XcCourseTables addCourseTables(String userId, Long courseId, CoursePublish coursepublish, XcChooseCourse xcChooseCourse) {
        //选课记录完成且未过期可以添加课程到课程表
        String status = xcChooseCourse.getStatus();
        if (!"701001".equals(status)){
            XueChengPlusException.cast("选课未成功，无法添加到课程表");
        }
        //查询我的课程表
        XcCourseTables xcCourseTablesOld = getXcCourseTables(xcChooseCourse.getUserId(), xcChooseCourse.getCourseId());
        if(xcCourseTablesOld !=null){
            return xcCourseTablesOld;
        }
        XcCourseTables xcCourseTables = new XcCourseTables();
        xcCourseTables.setChooseCourseId(xcChooseCourse.getId());
        xcCourseTables.setUserId(userId);
        xcCourseTables.setCourseId(courseId);
        xcCourseTables.setCompanyId(coursepublish.getCompanyId());
        xcCourseTables.setCourseName(coursepublish.getName());
        xcCourseTables.setCourseType(xcChooseCourse.getOrderType());
        xcCourseTables.setCreateDate(LocalDateTime.now());
        xcCourseTables.setValidtimeStart(LocalDateTime.now());
        xcCourseTables.setValidtimeEnd(LocalDateTime.now().plusDays(365));
        xcCourseTablesMapper.insert(xcCourseTables);
        return xcCourseTables;
    }

    private XcCourseTables getXcCourseTables(String userId, Long courseId) {
        LambdaQueryWrapper<XcCourseTables> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(XcCourseTables::getCourseId,courseId).eq(XcCourseTables::getUserId,userId);
        XcCourseTables xcCourseTables = courseTablesMapper.selectOne(queryWrapper);
        return xcCourseTables;
    }

    private XcChooseCourse addFreeCourse(String userId, CoursePublish coursepublish) {
        //查询选课记录表是否存在免费的且选课成功的订单
        LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper = queryWrapper.eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getCourseId, coursepublish.getId())
                .eq(XcChooseCourse::getOrderType, "700001")//免费课程
                .eq(XcChooseCourse::getStatus, "701001");//选课成功
        List<XcChooseCourse> xcChooseCourses = xcChooseCourseMapper.selectList(queryWrapper);
        if (xcChooseCourses != null && xcChooseCourses.size()>0) {
            return xcChooseCourses.get(0);
        }
        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(coursepublish.getId());
        xcChooseCourse.setCourseName(coursepublish.getName());
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursepublish.getCompanyId());
        xcChooseCourse.setOrderType("700001");
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setValidDays(365);
        xcChooseCourse.setStatus("701001"); // 选课成功
        int price = 0;
        xcChooseCourse.setCoursePrice((float) price);
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));
        xcChooseCourseMapper.insert(xcChooseCourse);
        return xcChooseCourse;
    }

}
