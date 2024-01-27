package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.service.CourseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {
    @Autowired
    private CourseCategoryMapper courseCategoryMapper;
    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes(id);
        // 构建返回结果
        List<CourseCategoryTreeDto> courseCategoryTreeResultList = new ArrayList<>();
        // 构建Map
        Map<String, CourseCategoryTreeDto> treeDtoMap = courseCategoryTreeDtos.stream().filter(item -> !id.equals(item.getId())).collect(Collectors.toMap(key -> key.getId(), value -> value, (key1, key2) -> key2));
        // 循环构建
        courseCategoryTreeDtos.stream().filter(item -> !id.equals(item.getId())).forEach(item ->{
            // 找到根节点的子节点
            if (item.getParentid().equals(id)){
                courseCategoryTreeResultList.add(item);
            }
            // 找到父节点
            CourseCategoryTreeDto courseCategoryTreeDtoParent = treeDtoMap.get(item.getParentid());
            // 如果父节点不为空
            if (courseCategoryTreeDtoParent != null){
                // 如果父节点的list为空，则new
                if(courseCategoryTreeDtoParent.getChildrenTreeNodes() == null){
                    courseCategoryTreeDtoParent.setChildrenTreeNodes(new ArrayList<CourseCategoryTreeDto>());
                }
                courseCategoryTreeDtoParent.getChildrenTreeNodes().add(item);
            }
        });
        return courseCategoryTreeResultList;
    }
}
