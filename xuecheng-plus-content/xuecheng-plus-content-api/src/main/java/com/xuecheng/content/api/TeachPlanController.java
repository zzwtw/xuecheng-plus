package com.xuecheng.content.api;

import com.xuecheng.base.model.TeachPlanResult;
import com.xuecheng.content.model.dto.SaveTeachPlanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.service.*;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TeachPlanController {
    @Autowired
    private TeachPlanService teachPlanService;
    @Autowired
    private SaveTeachPlanService saveTeachPlanService;
    @Autowired
    private DeleteTeachPlanService deleteTeachPlanService;

    @Autowired
    private MoveDownTeachPlanService moveDownTeachPlanService;

    @Autowired
    private MoveUpTeachPlanService moveUpTeachPlanService;

    @GetMapping("teachplan/{courseId}/tree-nodes")
    @ApiOperation("获取课程计划树结构")
    public List<TeachPlanDto> getTeachPlanTreeNodes(@PathVariable Long courseId) {
        return teachPlanService.findTeachPlanTree(courseId);
    }

    @PostMapping("/teachplan")
    @ApiOperation("新增/修改课程章节（大和小）")
    public void saveTeachPlan(@RequestBody SaveTeachPlanDto saveTeachPlanDto) {
        saveTeachPlanService.saveTeachPlan(saveTeachPlanDto);
    }

    @DeleteMapping("/teachplan/{id}")
    @ApiOperation("根据id删除课程计划章节")
    public TeachPlanResult deleteTeachPlan(@PathVariable Long id) {
        return deleteTeachPlanService.deleteTeachPlanById(id);
    }

    @PostMapping("/teachplan/movedown/{id}")
    @ApiOperation("下移动课程计划")
    public void moveDownTeachPlan(@PathVariable Long id) {
        moveDownTeachPlanService.moveDownTeachPlanById(id);
    }

    @PostMapping("/teachplan/moveup/{id}")
    @ApiOperation("下移动课程计划")
    public void moveUpTeachPlan(@PathVariable Long id) {
        moveUpTeachPlanService.moveUpTeachPlanById(id);
    }
}
