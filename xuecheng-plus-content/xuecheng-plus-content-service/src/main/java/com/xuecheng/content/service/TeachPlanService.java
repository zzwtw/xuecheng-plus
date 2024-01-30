package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.TeachPlanDto;

import java.util.List;

public interface TeachPlanService {
    public List<TeachPlanDto> findTeachPlanTree(Long courseId);
}
