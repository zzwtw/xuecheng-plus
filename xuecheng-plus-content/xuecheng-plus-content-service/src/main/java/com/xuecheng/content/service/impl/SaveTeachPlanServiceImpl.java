package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.SaveTeachPlanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.service.SaveTeachPlanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SaveTeachPlanServiceImpl implements SaveTeachPlanService {
    @Autowired
    private TeachplanMapper teachplanMapper;

    /**
     * @description 新增课程计划信息
     * @param saveTeachPlanDto 需要新增的课程计划信息
     */
    @Override
    public void saveTeachPlan(SaveTeachPlanDto saveTeachPlanDto) {
        // 判断是新增还是修改
        Long id = saveTeachPlanDto.getId();
        // 如果课程计划id不存在那就新增
        if (id == null){
            // 计算出orderby字段
            Integer count = getCount(saveTeachPlanDto);
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(saveTeachPlanDto,teachplan);
            teachplan.setOrderby(count + 1);
            int insert = teachplanMapper.insert(teachplan);
            if (insert <=0){
                XueChengPlusException.cast("课程计划新增失败");
            }
            // 如果是大章节，需要给他新增一个小章节，不然查不出来
            Long parentId = saveTeachPlanDto.getParentid();
            if (parentId == 0){
                // 给小章节的parentId
                Long parentIdNew = teachplan.getId();
                // 新建一个小章节
                Teachplan teachPlanNew = new Teachplan();
                teachPlanNew.setParentid(parentIdNew);
                teachPlanNew.setCourseId(saveTeachPlanDto.getCourseId());
                teachPlanNew.setGrade(2);
                teachPlanNew.setPname("小节名称 [点击修改]");
                teachPlanNew.setOrderby(1);
                // 插入小章节
                int insertNew = teachplanMapper.insert(teachPlanNew);
                if (insertNew <=0){
                    XueChengPlusException.cast("课程计划大章节的小章节新增失败");
                }
            }
        }else {
            // 更新
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(saveTeachPlanDto,teachplan);
            int update = teachplanMapper.updateById(teachplan);
            if (update <=0){
                XueChengPlusException.cast("课程计划更新失败");
            }
        }
    }

    private Integer getCount(SaveTeachPlanDto saveTeachPlanDto) {
        Long parentId = saveTeachPlanDto.getParentid();
        Long courseId = saveTeachPlanDto.getCourseId();
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId,courseId).eq(Teachplan::getParentid,parentId);
        return teachplanMapper.selectCount(queryWrapper);
    }
}
