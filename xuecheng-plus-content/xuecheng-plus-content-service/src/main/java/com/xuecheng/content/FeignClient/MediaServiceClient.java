package com.xuecheng.content.FeignClient;

import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
@Component
@FeignClient(value = "media-api", configuration = {MultipartSupportConfig.class},fallbackFactory = MediaServiceClientFactory.class)
public interface MediaServiceClient {
    /**
     * @description 上传静态化页面接口
     * @param uploadFile 上传的文件
     * @param objectName 子目录
     * @return
     */
    @RequestMapping(value = "/media/upload/coursefile",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadFileResultDto uploadFile(@RequestPart("filedata") MultipartFile uploadFile, @RequestParam(value = "objectName", required = false) String objectName);
}
