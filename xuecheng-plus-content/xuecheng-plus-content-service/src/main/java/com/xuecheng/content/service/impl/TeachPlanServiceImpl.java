package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachPlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class TeachPlanServiceImpl implements TeachPlanService {
    @Autowired
    private TeachplanMapper teachplanMapper;
    @Autowired
    private TeachplanMediaMapper teachplanMediaMapper;

    /**
     * @param courseId 课程id
     * @return 返回查询到的课程计划列表
     * @description 查询课程计划列表
     */
    @Override
    public List<TeachPlanDto> findTeachPlanTree(Long courseId) {
        return teachplanMapper.selectTreeNodes(courseId);
    }

    /**
     * @param bindTeachplanMediaDto dto
     * @desciption 课程计划绑定媒资视频
     */
    @Override
    public void associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto) {
        // 删除历史关联视频
        LambdaQueryWrapper<TeachplanMedia> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(TeachplanMedia::getTeachplanId, bindTeachplanMediaDto.getTeachplanId());
        int delete = teachplanMediaMapper.delete(queryWrapper);
        if (delete <= 0){
            log.error("删除历史关联视频失败");
        }
        // 新增历史关联视频
        TeachplanMedia teachplanMedia = new TeachplanMedia();
        Teachplan teachplan = teachplanMapper.selectById(bindTeachplanMediaDto.getTeachplanId());
        Long courseId = teachplan.getCourseId();
        teachplanMedia.setMediaId(bindTeachplanMediaDto.getMediaId());
        teachplanMedia.setMediaFilename(bindTeachplanMediaDto.getFileName());
        teachplanMedia.setTeachplanId(bindTeachplanMediaDto.getTeachplanId());
        teachplanMedia.setCourseId(courseId);
        teachplanMedia.setCreateDate(LocalDateTime.now());
        int insert = teachplanMediaMapper.insert(teachplanMedia);
        if (insert <= 0) {
            log.error("插入TeachplanMedia失败");
        }
    }

    /**
     * @param mediaId     媒资id
     * @param teachPlanId 课程计划id
     * @desciption 删除课程计划绑定的媒资视频
     */
    @Override
    public void deleteAssociationMedia(String mediaId, Long teachPlanId) {
        LambdaQueryWrapper<TeachplanMedia> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TeachplanMedia::getMediaId, mediaId).eq(TeachplanMedia::getTeachplanId, teachPlanId);
        teachplanMediaMapper.delete(queryWrapper);
    }
}
