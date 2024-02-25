package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcMenuMapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcMenu;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @description 实现了该接口后，
 * 用户提交账号和密码由DaoAuthenticationProvider调用UserDetailsService的loadUserByUsername()方法获取UserDetails用户信息。
 * 调用的loadUserByUsername()方法就会是我们自己写的，也就是从我们的数据库中获取用户信息。
 */
@Service
@Slf4j
public class UserServiceImpl implements UserDetailsService {
    @Autowired
    XcUserMapper xcUserMapper;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    XcMenuMapper xcMenuMapper;
    /**
     * @param s
     * @return
     * @throws UsernameNotFoundException
     * @description 该方法实现了UserDetailsService中的方法，用于使用已知的用户名获取数据库用户信息，
     * 将用户名，密码，权限封装成UserDetails对象，返回给DaoAuthenticationProvider，
     * 之后SpringSecurity会自动验证密码是否正确
     */
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        AuthParamsDto authParamsDto = null;
        // 将json转对象
        authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        // 开始认证
        String authType = authParamsDto.getAuthType();
        AuthService authService = applicationContext.getBean(authType + "_authservice", AuthService.class);
        XcUserExt xcUser = authService.execute(authParamsDto);
        UserDetails userDetails = getUserDetails(xcUser);
        return userDetails;
    }

    private  UserDetails getUserDetails(XcUserExt xcUser) {
        // 用户权限,如果不加报Cannot pass a null GrantedAuthority collection
        String[] authorities = {"test"};
        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(xcUser.getId());
        List<String> authoritiesList = new ArrayList<>();
        xcMenus.forEach(item ->{
            String code = item.getCode();
            authoritiesList.add(code);
        });
        authorities = authoritiesList.toArray(new String[0]);
        String password = xcUser.getPassword();
        xcUser.setPassword(null);
        String xcUserJson = JSON.toJSONString(xcUser);
        // 创建UserDetails对象，权限信息待实现
        UserDetails userDetails = User.withUsername(xcUserJson).password(password).authorities(authorities).build();
        return userDetails;
    }
}
