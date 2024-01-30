package com.xuecheng.base.model;

import lombok.Data;
import lombok.ToString;

/**
 * 删除课程计划返回的参数
 */
@Data
@ToString
public class TeachPlanResult {
    private String errCode;
    private String errMessage;

    public TeachPlanResult(String errCode) {
        this.errCode = errCode;
    }

    public TeachPlanResult(String errCode, String errMessage) {
        this.errCode = errCode;
        this.errMessage = errMessage;
    }

    public TeachPlanResult() {
    }
}
