package com.xuecheng.base.exception;

import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * 响应异常统一类型
 */

public class RestErrorResponse implements Serializable {
    private String errMessage;

    public RestErrorResponse(String errMessage) {
        this.errMessage = errMessage;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }


}
