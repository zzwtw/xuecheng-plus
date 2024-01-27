package com.xuecheng.base.exception;

public enum CommonError {
    UNKOWN_ERROR("执行过程异常，请重试。");

    public String getErrMessage() {
        return errMessage;
    }

    private String errMessage;

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }

    CommonError(String errMessage) {
        this.errMessage = errMessage;
    }
}
