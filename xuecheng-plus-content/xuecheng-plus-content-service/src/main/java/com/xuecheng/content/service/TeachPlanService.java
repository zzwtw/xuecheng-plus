package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.TeachPlanDto;

import java.util.List;

public interface TeachPlanService {
    public List<TeachPlanDto> findTeachPlanTree(Long courseId);

    /**
     * @desciption 课程计划绑定媒资视频
     * @param bindTeachplanMediaDto dto
     */
    public void associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto);

    /**
     * @desciption 删除课程计划绑定的媒资视频
     * @param mediaId 媒资id
     * @param teachPlanId 课程计划id
     */
    public void deleteAssociationMedia(String mediaId,Long teachPlanId);
}
