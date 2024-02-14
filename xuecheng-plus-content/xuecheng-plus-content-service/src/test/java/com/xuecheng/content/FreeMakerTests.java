package com.xuecheng.content;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.service.CoursePublishService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.netty.handler.codec.http2.Http2HeadersDecoder;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class FreeMakerTests {
    @Autowired
    private CoursePublishService coursePublishService;

    @Test
    public void testFreeMaker() throws IOException, TemplateException {
        // 静态化一个页面
        Configuration configuration = new Configuration(Configuration.getVersion());
        // 加载模板
        String classPath = this.getClass().getResource("/").getPath();
        configuration.setDirectoryForTemplateLoading(new File(classPath + "/templates/"));
        // 设置字符编码
        configuration.setDefaultEncoding("utf-8");
        // 指定模板文件名称
        Template template = configuration.getTemplate("course_template.ftl");
        // 准备数据
        CoursePreviewDto coursePreviewDto = coursePublishService.getCoursePreviewInfo(1L);
        Map<Object, Object> map = new HashMap<>();
        map.put("model", coursePreviewDto);
        // 静态化
        String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
        // 将静态化内容输出到文件中
        InputStream inputStream = IOUtils.toInputStream(content);
        // 输出流
        FileOutputStream outputStream = new FileOutputStream("C:\\Users\\1\\Desktop\\素材\\1.html");
        IOUtils.copy(inputStream,outputStream);
    }
}
