package com.xuecheng.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.media.model.po.MediaProcess;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface MediaProcessMapper extends BaseMapper<MediaProcess> {
    @Select("SELECT * FROM media_process t WHERE t.`id` % #{shardTotal} = #{shardIndex} AND t.`status` = '1' LIMIT #{count}")
    public List<MediaProcess> getMediaProcessList(@Param("shardIndex") int shardIndex, @Param("shardTotal")int shardTotal, @Param("count")int count);
}
