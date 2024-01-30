package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.service.TeachPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeachPlanServiceImpl implements TeachPlanService {
    @Autowired
    private TeachplanMapper teachplanMapper;

    /**
     * @description 查询课程计划列表
     * @param courseId 课程id
     * @return 返回查询到的课程计划列表
     */
    @Override
    public List<TeachPlanDto> findTeachPlanTree(Long courseId) {
        return teachplanMapper.selectTreeNodes(courseId);
    }
}
