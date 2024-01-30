package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.service.MoveUpTeachPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MoveUpTeachPlanServiceImpl implements MoveUpTeachPlanService {
    @Autowired
    private TeachplanMapper teachplanMapper;

    /**
     * @description 课程计划上移（大章节小章节上移）
     * @param id 课程计划id
     */
    @Override
    public void moveUpTeachPlanById(Long id) {
        // 上移课程计划，如果已经是第一个则不进行操作
        Teachplan teachplan = teachplanMapper.selectById(id);
        Long parentId = teachplan.getParentid();
        // 如果是小章节
        if (parentId != 0) {
            Integer orderBy = teachplan.getOrderby();
            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Teachplan::getParentid, parentId);
            if (orderBy > 1) {
                // 获取交换的对象
                Integer orderByChange = orderBy - 1;
                LambdaQueryWrapper<Teachplan> queryWrapperNew = new LambdaQueryWrapper<>();
                queryWrapperNew.eq(Teachplan::getParentid, parentId).eq(Teachplan::getOrderby, orderByChange);
                ExchangeTeachPlan(teachplan, orderBy, orderByChange, queryWrapperNew);
            }
        }else {
            // 如果是 大章节
            Integer orderBy = teachplan.getOrderby();
            Long courseId = teachplan.getCourseId();
            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Teachplan::getParentid, parentId).eq(Teachplan::getCourseId,courseId);
            if (orderBy > 1) {
                // 获取交换的对象
                Integer orderByChange = orderBy - 1;
                LambdaQueryWrapper<Teachplan> queryWrapperNew = new LambdaQueryWrapper<>();
                queryWrapperNew.eq(Teachplan::getParentid, parentId).eq(Teachplan::getCourseId,courseId).eq(Teachplan::getOrderby, orderByChange);
                ExchangeTeachPlan(teachplan, orderBy, orderByChange, queryWrapperNew);
            }
        }


    }

    private void ExchangeTeachPlan(Teachplan teachplan, Integer orderBy, Integer orderByChange, LambdaQueryWrapper<Teachplan> queryWrapperNew) {
        List<Teachplan> teachPlanList = teachplanMapper.selectList(queryWrapperNew);
        Teachplan teachPlanChange = teachPlanList.get(0);
        // 交换orderBy
        teachPlanChange.setOrderby(orderBy);
        teachplan.setOrderby(orderByChange);
        // 更新
        teachplanMapper.updateById(teachPlanChange);
        teachplanMapper.updateById(teachplan);
    }
}


