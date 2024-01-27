package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CourseCategoryController {
    @Autowired
    private CourseCategoryService courseCategoryService;

    @GetMapping("/course-category/tree-nodes")
    @ApiOperation("查询课程分类列表")
    public List<CourseCategoryTreeDto> courseCategoryController(){
        List<CourseCategoryTreeDto> courseCategoryTreeDtosResult = courseCategoryService.queryTreeNodes("1");
        return courseCategoryTreeDtosResult;
    }
}
