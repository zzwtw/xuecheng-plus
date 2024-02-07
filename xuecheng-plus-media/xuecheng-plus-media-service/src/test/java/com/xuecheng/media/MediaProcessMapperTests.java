package com.xuecheng.media;

import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaProcess;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class MediaProcessMapperTests {
    @Autowired
    MediaProcessMapper mediaProcessMapper;
    @Test
    public void testGetMediaProcessList(){
        List<MediaProcess> mediaProcessList = mediaProcessMapper.getMediaProcessList(0, 2, 2);
        System.out.println(mediaProcessList);
    }
}
