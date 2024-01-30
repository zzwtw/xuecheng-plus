package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * 课程计划dto
 */
@Data
@ToString
public class TeachPlanDto extends Teachplan {
    private TeachplanMedia teachplanMedia;
    private List<TeachPlanDto> teachPlanTreeNodes;
}
