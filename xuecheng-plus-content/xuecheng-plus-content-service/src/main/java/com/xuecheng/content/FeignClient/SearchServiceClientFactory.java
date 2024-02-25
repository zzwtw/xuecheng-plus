package com.xuecheng.content.FeignClient;

import com.xuecheng.content.FeignClient.po.CourseIndex;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Slf4j
@Component
public class SearchServiceClientFactory implements FallbackFactory<SearchServiceClient> {
    @Override
    public SearchServiceClient create(Throwable throwable) {
        return new SearchServiceClient() {

            @Override
            public Boolean add(CourseIndex courseIndex) {
                log.error("SearchServiceClient熔断降级");
                return null;
            }
        };
    }
}
