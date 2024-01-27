package com.xuecheng.base.exception;

/**
 * 系统自定义异常类型
 */
public class XueChengPlusException extends RuntimeException {
    private String errMessage;

    public XueChengPlusException() {
        super();
    }

    public XueChengPlusException(String errMessage) {
        super(errMessage);
        this.errMessage = errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public static void cast(CommonError commonError){
        throw new XueChengPlusException(commonError.getErrMessage());
    }

    public static void cast(String errMessage){
        throw new XueChengPlusException(errMessage);
    }
}
