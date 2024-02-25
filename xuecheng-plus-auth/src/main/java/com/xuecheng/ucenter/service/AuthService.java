package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
/**
 * @description 认证方法
 * @return com.xuecheng.ucenter.model.po.XcUser 用户信息
 */
public interface AuthService {
    XcUserExt execute(AuthParamsDto authParamsDto);
}
