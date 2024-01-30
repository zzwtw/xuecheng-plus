package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.Query;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.TeachPlanResult;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.DeleteTeachPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeleteTeachPlanServiceImpl implements DeleteTeachPlanService {
    @Autowired
    private TeachplanMapper teachplanMapper;
    @Autowired
    private TeachplanMediaMapper teachplanMediaMapper;

    /**
     * @description 根据课程计划id删除课程计划以及对应的媒资信息
     * @param id 课程计划id（大章节小章节id）
     * @return 删除课程计划返回的参数
     */
    @Override
    public TeachPlanResult deleteTeachPlanById(Long id) {
        TeachPlanResult teachPlanResult = new TeachPlanResult();
        Teachplan teachplan = teachplanMapper.selectById(id);
        Long parentId = teachplan.getParentid();
        // 如果是小章节
        if (parentId != 0) {
            // 删除小章节以及媒资信息
            teachplanMapper.deleteById(id);
            teachplanMediaMapper.deleteById(id);
            // 删除小章节之后，要将小章节下面的小章节orderBy字段-1
            TeachPlanOrderBySub(teachplan, parentId);
            //如果删除的是这一章下面的最后一小章，则将大章节也删除掉
            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Teachplan::getParentid,parentId);
            Integer count = teachplanMapper.selectCount(queryWrapper);
            if (count == 0){
                // 找到大章节
                Teachplan teachPlanParent = teachplanMapper.selectById(parentId);
                // 删除大章节之前，要将大章节下面的大章节orderBy字段-1
                TeachPlanParentOrderBySub(teachPlanParent);
                // 删除大章节
                teachplanMapper.deleteById(parentId);
            }
        } else {
            // 如果是大章节
            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Teachplan::getParentid, id);
            Integer count = teachplanMapper.selectCount(queryWrapper);
            if (count > 0) {
                teachPlanResult.setErrCode("120409");
                teachPlanResult.setErrMessage("课程计划信息还有子级信息" +
                        "，无法操作");
                return teachPlanResult;
            }
        }
        teachPlanResult.setErrCode("200");
        return teachPlanResult;
    }

    private void TeachPlanParentOrderBySub(Teachplan teachPlanParent) {
        Long courseId = teachPlanParent.getCourseId();
        Long parentIdNew = teachPlanParent.getParentid();
        Integer orderByNew = teachPlanParent.getOrderby();
        LambdaQueryWrapper<Teachplan> queryWrapperNew = new LambdaQueryWrapper<>();
        queryWrapperNew.eq(Teachplan::getCourseId,courseId).eq(Teachplan::getParentid,parentIdNew).gt(Teachplan::getOrderby,orderByNew);
        List<Teachplan> teachPlanList = teachplanMapper.selectList(queryWrapperNew);
        for (Teachplan teachPlanObj : teachPlanList) {
            Integer orderBy = teachPlanObj.getOrderby();
            teachPlanObj.setOrderby(orderBy - 1);
            teachplanMapper.updateById(teachPlanObj);
        }
    }

    private void TeachPlanOrderBySub(Teachplan teachplan, Long parentId) {
        Integer orderBy = teachplan.getOrderby();
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getParentid, parentId).gt(Teachplan::getOrderby, orderBy);
        List<Teachplan> teachPlanList = teachplanMapper.selectList(queryWrapper);
        for (Teachplan teachPlanObj : teachPlanList) {
            Integer orderby = teachPlanObj.getOrderby();
            teachPlanObj.setOrderby(orderby - 1);
            teachplanMapper.updateById(teachPlanObj);
        }
    }


}
