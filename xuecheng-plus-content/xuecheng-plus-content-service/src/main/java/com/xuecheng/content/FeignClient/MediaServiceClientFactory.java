package com.xuecheng.content.FeignClient;

import com.xuecheng.media.model.dto.UploadFileResultDto;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
@Slf4j
@Component
public class MediaServiceClientFactory implements FallbackFactory<MediaServiceClient> {

    /**
     * @description 熔断之后的降级方法，本地调用方法，并且给出熔断的异常
     * @param throwable
     * @return
     */
    @Override
    public MediaServiceClient create(Throwable throwable) {
        return new MediaServiceClient() {
            @Override
            public UploadFileResultDto uploadFile(MultipartFile uploadFile, String objectName) {
                //降级方法
                log.debug("调用媒资管理服务上传文件时发生熔断，异常信息:{}",throwable.toString(),throwable);
                return null;
            }
        };
    }
}
